/*
 * Migración ADR-0008: clave de identidad email → uid.
 *
 * Uso:  node migrate-uid.js <ruta-service-account.json> [--commit | --dry-run] [--backup-dir <ruta>]
 *
 * SEGURIDAD: por defecto es DRY-RUN (no escribe ni borra nada). Para escribir hay que
 * decirlo explícitamente con --commit, igual que `b3-limpieza`. Olvidarse de un flag
 * nunca destruye datos.
 *
 * Qué hace (con BACKUP JSON por usuario, escrito ANTES de borrar, en `.secrets/`):
 *  1. Lista los documentos raíz de `users` (los antiguos tienen id = email).
 *  2. Resuelve email → uid con Identity Toolkit (admin).
 *  3. Copia el perfil a `users/{uid}` añadiendo el campo `email`.
 *  4. Migra subcolecciones heredadas (friends/messages) resolviendo emails a uid.
 *  5. Crea los índices `email_index/{email}` y `usernames/{username}`.
 *  6. (Solo con --commit) borra el documento antiguo `users/{email}`.
 *
 * Idempotente: si ya existe `users/{uid}`, no lo pisa con datos vacíos.
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const crypto = require('crypto');

const SA_PATH = process.argv[2];
// Dry-run POR DEFECTO (igual que scripts/b3-limpieza). `--dry-run` se acepta por compatibilidad.
const DRY = !process.argv.includes('--commit');
if (!DRY) console.log('⚠️  --commit: se va a ESCRIBIR y BORRAR en Firestore.\n');
else console.log('· DRY-RUN por defecto: no se escribe nada. Usa --commit para ejecutar.\n');

// Backup FUERA del repo: son datos reales de usuarios, no deben pisar un commit.
// Por defecto va a `.secrets/` (raíz del workspace); `--backup-dir` lo cambia.
function argValue(flag) {
  const i = process.argv.indexOf(flag);
  return i >= 0 && process.argv[i + 1] ? process.argv[i + 1] : null;
}
const DEFAULT_BACKUP = path.resolve(__dirname, '..', '..', '..', '.secrets', 'migracion-uid-backup');
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
    const path = p + (pageToken ? '?pageToken=' + pageToken : '');
    const r = await fsRequest('GET', path, token);
    const j = JSON.parse(r.body);
    (j.documents || []).forEach(d => out.push(d));
    pageToken = j.nextPageToken || '';
  } while (pageToken);
  return out;
}

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

(async () => {
  if (!fs.existsSync(BACKUP_DIR)) fs.mkdirSync(BACKUP_DIR, { recursive: true });
  console.log('Backup en: ' + BACKUP_DIR);
  if (BACKUP_DIR.startsWith(__dirname)) {
    console.log('⚠️  el backup cae dentro del repo: muévelo a .secrets/ antes de commitear.\n');
  }
  const token = await getToken();

  const users = await listCollection('users', token);
  console.log('Documentos raíz en users: ' + users.length);

  const backup = [];
  let migrated = 0, skipped = 0, failures = 0;

  for (const doc of users) {
    const id = doc.name.split('/').pop();
    const isEmailKey = id.includes('@');
    if (!isEmailKey) { console.log('· ' + id + ' ya usa uid → se ignora'); skipped++; continue; }

    try {
      const uid = await getUserByEmail(id, token);
      if (!uid) { console.log('· ' + id + ' no existe en Auth → se ignora'); failures++; continue; }

      const profile = doc.fields || {};
      const username = profile.username && profile.username.stringValue ? profile.username.stringValue : '';

      // Subcolecciones heredadas
      const friends = await listCollection('users/' + id + '/friends', token);
      const messages = await listCollection('users/' + id + '/messages', token);

      backup.push({ oldId: id, uid, profile, friends, messages });
      fs.writeFileSync(path.join(BACKUP_DIR, 'users-' + id + '.json'), JSON.stringify({ oldId: id, uid, profile, friends, messages }, null, 2));

      if (DRY) { console.log('· (dry) ' + id + ' → ' + uid + ' | friends=' + friends.length + ' messages=' + messages.length); migrated++; continue; }

      // 1) Perfil en users/{uid} con el email como campo.
      const newFields = Object.assign({}, profile, { email: { stringValue: id } });
      await fsRequest('PATCH', 'users/' + uid, token, { fields: newFields });

      // 2) Índices
      await fsRequest('PATCH', 'email_index/' + id, token, { fields: { uid: { stringValue: uid } } });
      if (username) await fsRequest('PATCH', 'usernames/' + username, token, { fields: { uid: { stringValue: uid } } });

      // 3) Amigos: resolver email → uid
      for (const f of friends) {
        const friendEmail = f.name.split('/').pop();
        const friendUid = await getUserByEmail(friendEmail, token);
        if (friendUid) {
          await fsRequest('PATCH', 'users/' + uid + '/friends/' + friendUid, token, { fields: {} });
        }
      }

      // 4) Mensajes: copiar tal cual al nuevo muro
      for (const m of messages) {
        const mid = m.name.split('/').pop();
        await fsRequest('PATCH', 'users/' + uid + '/messages/' + mid, token, { fields: m.fields || {} });
      }

      // 5) Borrar el documento antiguo (y sus subcolecciones de primer nivel)
      for (const f of friends) await fsRequest('DELETE', f.name.replace('/v1/projects/' + PROJ + '/databases/(default)/documents/', ''), token);
      for (const m of messages) await fsRequest('DELETE', m.name.replace('/v1/projects/' + PROJ + '/databases/(default)/documents/', ''), token);
      await fsRequest('DELETE', 'users/' + id, token);

      console.log('✓ ' + id + ' → ' + uid + ' (friends=' + friends.length + ', messages=' + messages.length + ')');
      migrated++;
    } catch (e) {
      console.log('✗ ' + id + ': ' + e.message);
      failures++;
    }
    await new Promise(r => setTimeout(r, 200));
  }

  fs.writeFileSync(path.join(BACKUP_DIR, 'summary.json'), JSON.stringify({ date: new Date().toISOString(), migrated, skipped, failures, backup }, null, 2));
  console.log('\nRESUMEN: migrados=' + migrated + ' ignorados=' + skipped + ' fallos=' + failures + (DRY ? ' (DRY-RUN)' : ''));
  console.log('Backup en: ' + BACKUP_DIR);
})().catch(e => { console.error('ERR ' + e.message); process.exit(1); });
