/*
 * Backfill T2.10: alias de búsqueda `usernames/~username` para usuarios pre-F2.
 *
 * Uso:  node backfill-alias-username.js <ruta-service-account.json> [--commit] [--dry-run]
 *
 * SEGURIDAD: por defecto es DRY-RUN. Solo CREA docs que faltan en `usernames/`
 * (alias en minúsculas con prefijo `~`, ver usernameAliasId()); nunca borra ni
 * toca docs existentes. Idempotente: re-ejecutar no hace nada.
 *
 * Por qué existe: la búsqueda por prefijo (D2.4) consulta el espacio de alias
 * `~…`. Los usuarios registrados antes de F2 solo tienen `usernames/{Original}`,
 * sin alias, y por eso no aparecen al buscar (detectado en E2E 30/09/2026:
 * buscar "qa g" no encontraba a QA GameVision). La app ya crea el alias al
 * registrarse/editar perfil; este script completa a los existentes.
 *
 * No depende del SDK: REST v1 + JWT de service account, igual que las migraciones.
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const crypto = require('crypto');

const SA_PATH = process.argv[2];
if (!SA_PATH) {
  console.error('Uso: node backfill-alias-username.js <ruta-service-account.json> [--commit]');
  process.exit(1);
}
const DRY = !process.argv.includes('--commit');
console.log(DRY
  ? '· DRY-RUN por defecto: no se escribe nada. Usa --commit para crear los alias.\n'
  : '⚠️  --commit: se CREARÁN los alias que falten (nada se borra).\n');

const sa = JSON.parse(fs.readFileSync(SA_PATH, 'utf8'));
const PROJ = sa.project_id;
const DEFAULT_BACKUP = path.resolve(__dirname, '..', '..', '..', '.secrets', 'backfill-alias-backup');

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
  const input = b64({ alg: 'RS256', typ: 'JWT' }) + '.' + b64({
    iss: sa.client_email,
    scope: 'https://www.googleapis.com/auth/cloud-platform',
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600
  });
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
    const q = p + (pageToken ? '?pageToken=' + encodeURIComponent(pageToken) : '');
    const r = await fsRequest('GET', q, token);
    const j = JSON.parse(r.body);
    (j.documents || []).forEach(d => out.push(d));
    pageToken = j.nextPageToken || '';
  } while (pageToken);
  return out;
}

/** usernameAliasId(): espejo exacto de UserIndexPlan.kt. */
function usernameAliasId(username) { return '~' + username.trim().toLowerCase(); }

async function main() {
  const token = await getToken();

  // 1. Usuarios con username definido.
  const users = await listCollection('users', token);
  const conUsername = users
    .map(d => ({
      uid: d.name.split('/').pop(),
      username: d.fields && d.fields.username && d.fields.username.stringValue || ''
    }))
    .filter(u => u.username.trim() !== '');

  console.log(`· ${users.length} usuarios, ${conUsername.length} con username.\n`);

  // 2. ¿Existe ya el alias de cada uno?
  const pendientes = [];
  for (const u of conUsername) {
    const aliasId = usernameAliasId(u.username);
    const r = await fsRequest('GET', 'usernames/' + aliasId, token);
    if (r.status === 200) {
      console.log(`  = ${u.username} → alias '~${u.username.trim().toLowerCase()}' ya existe`);
    } else if (r.status === 404) {
      pendientes.push({ ...u, aliasId });
      console.log(`  + ${u.username} → alias FALTA (se creará)`);
    } else {
      console.log(`  ? ${u.username} → HTTP ${r.status} al comprobar; se salta`);
    }
  }

  if (pendientes.length === 0) {
    console.log('\nNada que hacer: todos los alias existen.');
    return;
  }

  // 3. Backup del plan (qué se va a crear) fuera del repo.
  fs.mkdirSync(DEFAULT_BACKUP, { recursive: true });
  const backupFile = path.join(DEFAULT_BACKUP, `plan-${new Date().toISOString().replace(/[:.]/g, '-')}.json`);
  fs.writeFileSync(backupFile, JSON.stringify(pendientes, null, 2));
  console.log(`\n· Plan de creación guardado en ${backupFile}`);

  if (DRY) {
    console.log(`\nDRY-RUN: se crearían ${pendientes.length} alias. Relanza con --commit.`);
    return;
  }

  // 4. Crear alias: usernames/{~alias} = { uid } (mismo shape que la app).
  let ok = 0, fail = 0;
  for (const p of pendientes) {
    const r = await fsRequest('PATCH', 'usernames/' + p.aliasId, token, {
      fields: { uid: { stringValue: p.uid } }
    });
    if (r.status === 200) {
      console.log(`  ✅ usernames/${p.aliasId} → ${p.uid}`);
      ok++;
    } else {
      console.log(`  ❌ usernames/${p.aliasId} → HTTP ${r.status}: ${r.body.slice(0, 160)}`);
      fail++;
    }
  }
  console.log(`\nResultado: ${ok} alias creados, ${fail} fallo(s)`);
  process.exit(fail ? 1 : 0);
}

main().catch(e => { console.error(e); process.exit(1); });
