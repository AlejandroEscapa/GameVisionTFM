/*
 * Despliegue de índices compuestos de Firestore desde firebase/firestore.indexes.json.
 *
 * Uso:  node deploy-indexes.js <ruta-service-account.json> [--file <ruta-indexes.json>]
 *
 * Por qué existe: en esta máquina no hay CLI `firebase` ni `gcloud`, y la app
 * necesita sus índices en producción. El feed de F2 (D2.3) consulta
 * `feed` con whereIn(authorUid) + orderBy(createdAt DESC), lo que exige un
 * índice compuesto: sin él la escucha del feed falla con FAILED_PRECONDITION
 * y la pestaña Social muestra "No se pudo cargar el feed" (visto 30/09/2026).
 *
 * Qué hace: POST de creación sobre la API de Firestore Admin
 * (projects/{p}/databases/(default)/collectionGroups/{g}/indexes) por cada
 * índice declarado en el JSON. 409 ALREADY_EXISTS se trata como OK si ya hay
 * un índice idéntico (compara fields vía GET). Tras crear, comprueba el
 * estado (CREATING → READY) y lo imprime.
 *
 * Nota: la creación es POST; PATCH solo aplica a /indexes/{indexId} existente
 * (un PATCH sobre la colección responde 404 HTML de Google).
 *
 * No depende del SDK: REST v1 + JWT de service account, igual que las migraciones.
 */

const fs = require('fs');
const path = require('path');
const https = require('https');
const crypto = require('crypto');

const SA_PATH = process.argv[2];
if (!SA_PATH) {
  console.error('Uso: node deploy-indexes.js <ruta-service-account.json> [--file <ruta-indexes.json>]');
  process.exit(1);
}

function argValue(flag) {
  const i = process.argv.indexOf(flag);
  return i >= 0 && process.argv[i + 1] ? process.argv[i + 1] : null;
}

const INDEX_FILE = argValue('--file') || path.resolve(__dirname, '..', '..', 'firebase', 'firestore.indexes.json');
const def = JSON.parse(fs.readFileSync(INDEX_FILE, 'utf8'));
const sa = JSON.parse(fs.readFileSync(SA_PATH, 'utf8'));
const PROJ = sa.project_id;

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
    scope: 'https://www.googleapis.com/auth/cloud-platform',
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

const ADMIN = 'firestore.googleapis.com';
function indexUrl(group, indexId) {
  return '/v1/projects/' + PROJ + '/databases/(default)/collectionGroups/' + encodeURIComponent(group) +
    (indexId ? '/indexes/' + encodeURIComponent(indexId) : '/indexes');
}

function normFields(fields) {
  return JSON.stringify((fields || []).map(f => ({
    fieldPath: f.fieldPath,
    order: f.order || (f.arrayConfig ? undefined : 'ASCENDING'),
    arrayConfig: f.arrayConfig || undefined
  })));
}

async function listIndexes(group, token) {
  const r = await req({ hostname: ADMIN, path: indexUrl(group), method: 'GET', headers: { Authorization: 'Bearer ' + token } });
  if (r.status !== 200) return [];
  return JSON.parse(r.body).indexes || [];
}

/** Estado del índice tras la creación (READY tarda un poco en colecciones grandes). */
async function indexState(group, id, token) {
  const r = await req({ hostname: ADMIN, path: indexUrl(group, id), method: 'GET', headers: { Authorization: 'Bearer ' + token } });
  if (r.status !== 200) return 'unknown(' + r.status + ')';
  const j = JSON.parse(r.body);
  return j.state || 'STATE_UNSPECIFIED';
}

async function main() {
  const token = await getToken();
  const indexes = def.indexes || [];
  console.log('· ' + indexes.length + ' índice(s) declarados en ' + path.relative(process.cwd(), INDEX_FILE) + '\n');

  let ok = 0, fail = 0;
  for (const ix of indexes) {
    const group = ix.collectionGroup;
    const fields = (ix.fields || []).map(f => {
      const out = { fieldPath: f.fieldPath };
      if (f.order) out.order = f.order;
      if (f.arrayConfig) out.arrayConfig = f.arrayConfig;
      return out;
    });
    const payload = JSON.stringify({ queryScope: ix.queryScope || 'COLLECTION', fields });
    const label = group + '(' + fields.map(f => f.fieldPath + (f.order ? ' ' + f.order : f.arrayConfig ? ' ' + f.arrayConfig : '')).join(', ') + ')';

    const r = await req({
      hostname: ADMIN, path: indexUrl(group), method: 'POST',
      headers: {
        Authorization: 'Bearer ' + token,
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload)
      }
    }, payload);

    if (r.status === 200 || r.status === 201) {
      const j = JSON.parse(r.body);
      const id = (j.name || '').split('/').pop();
      const state = await indexState(group, id, token);
      console.log('  ✅ ' + label + ' → creado, estado ' + state);
      ok++;
    } else if (r.status === 409) {
      // ¿Es exactamente el índice que declaramos?
      const existing = await listIndexes(group, token);
      const twin = existing.find(ix2 => ix2.queryScope === (ix.queryScope || 'COLLECTION') && normFields(ix2.fields) === normFields(fields));
      if (twin) {
        const id = (twin.name || '').split('/').pop();
        const state = await indexState(group, id, token);
        console.log('  ✅ ' + label + ' → ya existía idéntico, estado ' + state);
        ok++;
      } else {
        console.log('  ⚠️  ' + label + ' → ya hay índices en ' + group + ' pero ninguno idéntico; revisar a mano');
        fail++;
      }
    } else {
      console.log('  ❌ ' + label + ' → HTTP ' + r.status + ': ' + r.body.slice(0, 300).replace(/\s+/g, ' '));
      fail++;
    }
  }
  console.log('\nResultado: ' + ok + ' ok, ' + fail + ' fallo(s)');
  process.exit(fail ? 1 : 0);
}

main().catch(e => { console.error(e); process.exit(1); });
