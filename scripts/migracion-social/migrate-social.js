/*
 * Migración social (F2/T2.9): amigos simétricos → aristas mutuas de seguimiento.
 *
 * Uso:  node migrate-social.js <ruta-service-account.json> [--commit | --dry-run] [--backup-dir <ruta>]
 *
 * SEGURIDAD: por defecto es DRY-RUN (no escribe ni borra nada). Igual que la
 * migración uid (ADR-0008) y b3-limpieza: olvidarse del flag nunca destruye datos.
 *
 * Qué hace (con BACKUP JSON por usuario, escrito ANTES de crear aristas):
 *
 *   1. Lista `users/{email}/friends` heredados (el modelo pre-uid con clave email).
 *      También detecta residuales `users/{uid}/friends` (clave uid) y los migra
 *      igual, resolviendo la clave al uid por Auth o por el índice usernames.
 *   2. Por cada amistad A↔B crea las DOS aristas de seguimiento:
 *      following/{A_B} (A sigue a B) y following/{B_A} (B sigue a A).
 *      Idempotente: si la arista ya existe, no la toca.
 *   3. (Solo --commit) borra `users/{x}/friends/{y}` tras crear sus aristas.
 *
 * La colección `messages` (chat pre-F2, D2.7 congelado) NO se toca.
 *
 * No depende del SDK: REST v1 + JWT de service account, igual que migrate-uid.js.
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const crypto = require('crypto');

const SA_PATH = process.argv[2];
// Dry-run POR DEFECTO. `--commit` escribe y borra.
const DRY = !process.argv.includes('--commit');
if (!DRY) console.log('⚠️  --commit: se van a CREAR aristas y BORRAR friends. Antes, BACKUP.\n');
else console.log('· DRY-RUN por defecto: no se escribe nada. Usa --commit para ejecutar.\n');

function argValue(flag) {
  const i = process.argv.indexOf(flag);
  return i >= 0 && process.argv[i + 1] ? process.argv[i + 1] : null;
}
// Backup FUERA del repo (.secrets/ del workspace); --backup-dir lo cambia.
const DEFAULT_BACKUP = path.resolve(__dirname, '..', '..', '..', '.secrets', 'migracion-social-backup');
const sa = JSON.parse(fs.readFileSync(SA_PATH, 'utf8'));
const PROJ = sa.project_id;
const BACKUP_DIR = argValue('--backup-dir') || DEFAULT_BACKUP;

function req(options, body) {
  return new Promise((resolve, reject) => {
    const r = https.request(options, res => {
      let d = '';
      res.on('data', c => (d += c));
      res.on('end', () => resolve({ status: res.statusCode, body: d }));
    });
    r.on('error', reject);
    if (body) r.write(body);
    r.end();
  });
}

async function getToken() {
  const now = Math.floor(Date.now() / 1000);
  const b64 = o => Buffer.from(JSON.stringify(o)).toString('base64url');
  const header = { alg: 'RS256', typ: 'JWT' };
  const claim = {
    iss: sa.client_email,
    scope: 'https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/identitytoolkit',
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600
  };
  const input = b64(header) + '.' + b64(claim);
  const sig = crypto.createSign('RSA-SHA256').update(input).sign(sa.private_key, 'base64url');
  const body = 'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=' + input + '.' + sig;
  const r = await req({
    hostname: 'oauth2.googleapis.com', path: '/token', method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(body) }
  }, body);
  return JSON.parse(r.body).access_token;
}

const FS = 'firestore.googleapis.com';
function enc(p) { return p.split('/').map(encodeURIComponent).join('/'); }
function fsPath(p) { return '/v1/projects/' + PROJ + '/databases/(default)/documents/' + enc(p); }

async function fsRequest(method, p, token, body) {
  const payload = body ? JSON.stringify(body) : null;
  return req({
    hostname: FS, path: fsPath(p), method,
    headers: Object.assign(
      { Authorization: 'Bearer ' + token },
      payload ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) } : {}
    )
  }, payload);
}

async function listCollection(p, token) {
  const out = [];
  let pageToken = '';
  do {
    const q = p + (pageToken ? '?pageToken=' + pageToken : '');
    const r = await fsRequest('GET', q, token);
    const j = JSON.parse(r.body);
    (j.documents || []).forEach(d => out.push(d));
    pageToken = j.nextPageToken || '';
  } while (pageToken);
  return out;
}

/** uid de un usuario por email vía Identity Toolkit (admin). */
async function getUserByEmail(email, token) {
  const r = await req({
    hostname: 'identitytoolkit.googleapis.com',
    path: '/v1/projects/' + PROJ + '/accounts:lookup',
    method: 'POST',
    headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(JSON.stringify({ email: [email] })) }
  }, JSON.stringify({ email: [email] }));
  const j = JSON.parse(r.body);
  if (j.error) throw new Error('auth lookup: ' + JSON.stringify(j.error));
  const u = (j.users || [])[0];
  return u ? u.localId : null;
}

/** uid de un usuario por username vía el índice usernames/{username}. */
async function getUserByUsername(username, token) {
  const r = await fsRequest('GET', 'usernames/' + encodeURIComponent(username), token);
  if (r.status === 404) return null;
  const j = JSON.parse(r.body);
  return j.fields && j.fields.uid && j.fields.uid.stringValue ? j.fields.uid.stringValue : null;
}

/** Crea la arista following/{a}_{b} si no existe. Devuelve 'created'|'exists'. */
async function ensureEdge(a, b, token) {
  const id = a + '_' + b;
  const r = await fsRequest('GET', 'following/' + id, token);
  if (r.status === 200) return 'exists';
  await fsRequest('PATCH', 'following/' + id, token, {
    fields: {
      followerUid: { stringValue: a },
      followedUid: { stringValue: b },
      createdAt: { timestampValue: new Date().toISOString() }
    }
  });
  return 'created';
}

(async () => {
  if (!fs.existsSync(BACKUP_DIR)) fs.mkdirSync(BACKUP_DIR, { recursive: true });
  console.log('Backup en: ' + BACKUP_DIR);
  if (BACKUP_DIR.startsWith(__dirname)) {
    console.log('⚠️  el backup cae dentro del repo: muévelo a .secrets/ antes de commitear.\n');
  }
  const token = await getToken();

  const users = await listCollection('users', token);
  console.log('Usuarios en Firestore: ' + users.length + '\n');

  const backup = [];
  let friendships = 0, edges = 0, failures = 0, skippedUsers = 0;

  for (const doc of users) {
    const key = doc.name.split('/').pop();
    // Propietario de esta carpeta friends: resolver su uid (clave email → Auth).
    let ownerUid = key.includes('@') ? await getUserByEmail(key, token) : key;

    let friends = [];
    try {
      friends = await listCollection('users/' + key + '/friends', token);
    } catch (e) { friends = []; }

    if (friends.length === 0) { skippedUsers++; continue; }

    try {
      const friendKeys = friends.map(f => f.name.split('/').pop());
      const ownerBackup = { ownerKey: key, ownerUid, friends: friendKeys };
      backup.push(ownerBackup);
      fs.writeFileSync(
        path.join(BACKUP_DIR, 'friends-' + key.replace(/[=@]/g, '_') + '.json'),
        JSON.stringify(ownerBackup, null, 2)
      );

      for (const fk of friendKeys) {
        // Resolución de la clave del amigo: email | uid | username residual.
        let friendUid = null;
        if (fk.includes('@')) friendUid = await getUserByEmail(fk, token);
        if (!friendUid && /^[A-Za-z0-9]{28}$/.test(fk)) friendUid = fk; // uid de Firebase Auth
        if (!friendUid) friendUid = await getUserByUsername(fk, token);
        if (!friendUid || !ownerUid) {
          console.log('✗ ' + key + ' ↔ ' + fk + ': no se pudo resolver a uid');
          failures++;
          continue;
        }
        if (friendUid === ownerUid) { console.log('· ' + key + ' ↔ ' + fk + ': auto-seguimiento ignorado'); continue; }

        if (DRY) {
          console.log('· (dry) ' + key + ' ↔ ' + fk + ' → ' + ownerUid + '↔' + friendUid);
        } else {
          const e1 = await ensureEdge(ownerUid, friendUid, token);
          const e2 = await ensureEdge(friendUid, ownerUid, token);
          console.log('✓ ' + key + ' ↔ ' + fk + ' → ' + ownerUid.slice(0, 6) + '↔' + friendUid.slice(0, 6) + ' (' + e1 + '/' + e2 + ')');
          await fsRequest('DELETE', 'users/' + key + '/friends/' + fk, token);
        }
        friendships++;
        edges += 2;
      }
    } catch (e) {
      console.log('✗ ' + key + ': ' + e.message);
      failures++;
    }
    await new Promise(r => setTimeout(r, 150));
  }

  fs.writeFileSync(
    path.join(BACKUP_DIR, 'summary.json'),
    JSON.stringify({ date: new Date().toISOString(), dry: DRY, friendships, edges, failures, skippedUsers, backup }, null, 2)
  );
  console.log('\nRESUMEN: amistades=' + friendships + ' aristas=' + edges + ' fallos=' + failures + (DRY ? ' (DRY-RUN, nada escrito)' : ''));
  console.log('Backup en: ' + BACKUP_DIR);
})().catch(e => { console.error('ERR ' + e.message); process.exit(1); });
