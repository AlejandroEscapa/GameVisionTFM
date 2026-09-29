/*
 * Despliegue de las reglas de Firestore vía la Firebase Rules API.
 *
 * Uso:  node deploy-rules.js <ruta-service-account.json> [--commit | --dry-run]
 *
 * Por qué existe este script: `firebase deploy` exige `firebase login` (interactivo) o
 * permisos de `serviceusage` que la service key no tiene, y falla en su pre-flight antes
 * de tocar las reglas. Este script usa la Rules API directamente, con la service key,
 * que es el mismo mecanismo que `scripts/migracion-uid/`.
 *
 * SEGURIDAD: por defecto es DRY-RUN. Para desplegar hay que decirlo con `--commit`,
 * igual que `b3-limpieza` y `migracion-uid`. Olvidarse de un flag no toca producción.
 *
 * Qué hace:
 *   1. Lee `firebase/firestore.rules`.
 *   2. Crea un ruleset nuevo (los rulesets son inmutables).
 *   3. Hace PATCH del release `cloud.firestore` para apuntar a ese ruleset.
 *
 * DETALLE IMPORTANTE: el release de Firestore se llama **`cloud.firestore`** y es el que
 * Firestore usa de verdad. Crear un release con otro nombre *parece* un despliegue y no
 * lo es: las reglas no cambian. Hay que actualizar el release existente.
 *
 * Los rulesets anteriores no se borran: el histórico queda reversible.
 */
const fs = require('fs');
const path = require('path');
const https = require('https');
const crypto = require('crypto');

const SA_PATH = process.argv[2];
if (!SA_PATH) {
  console.error('Uso: node deploy-rules.js <ruta-service-account.json> [--commit | --dry-run]');
  process.exit(1);
}
const DRY = !process.argv.includes('--commit');

const sa = JSON.parse(fs.readFileSync(SA_PATH, 'utf8'));
const PROJ = sa.project_id;
const RULES_PATH = path.resolve(__dirname, '..', '..', 'firebase', 'firestore.rules');
const RULES = fs.readFileSync(RULES_PATH, 'utf8');

if (DRY) {
  console.log('· DRY-RUN por defecto: no se despliega nada. Usa --commit para ejecutar.\n');
} else {
  console.log('⚠️  --commit: se van a desplegar las reglas a PRODUCCIÓN.\n');
}

console.log('Proyecto:  ' + PROJ);
console.log('Reglas:    ' + RULES_PATH + ' (' + RULES.length + ' bytes)');

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
  const input =
    b64({ alg: 'RS256', typ: 'JWT' }) + '.' +
    b64({
      iss: sa.client_email,
      scope: 'https://www.googleapis.com/auth/cloud-platform',
      aud: 'https://oauth2.googleapis.com/token',
      iat: now,
      exp: now + 3600
    });
  const sig = crypto.createSign('RSA-SHA256').update(input).sign(sa.private_key, 'base64url');
  const body =
    'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=' + input + '.' + sig;
  const r = await req({
    hostname: 'oauth2.googleapis.com',
    path: '/token',
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Content-Length': Buffer.byteLength(body)
    }
  }, body);
  return JSON.parse(r.body).access_token;
}

async function rulesApi(method, p, token, payload) {
  const body = payload ? JSON.stringify(payload) : null;
  const r = await req({
    hostname: 'firebaserules.googleapis.com',
    path: '/v1/projects/' + PROJ + p,
    method,
    headers: Object.assign(
      { Authorization: 'Bearer ' + token },
      body
        ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
        : {}
    )
  }, body);
  let json = null;
  try { json = JSON.parse(r.body); } catch (_) { /* puede no ser JSON */ }
  return { status: r.status, json, raw: r.body };
}

(async () => {
  const token = await getToken();

  // Estado actual, para mostrar qué se está cambiando.
  const releases = await rulesApi('GET', '/releases/cloud.firestore', token);
  const rulesetAnterior = (releases.json && releases.json.rulesetName) || '(desconocido)';
  console.log('\nRelease activo: ' + ((releases.json && releases.json.name) || '(ninguno)'));
  console.log('Ruleset actual: ' + rulesetAnterior);
  console.log('Última actualización: ' + ((releases.json && releases.json.updateTime) || '—'));

  if (DRY) {
    console.log('\n(DRY-RUN) Se crearía un ruleset con el contenido de firestore.rules');
    console.log('          y un release nuevo que lo activaría. Nada escrito.');
    return;
  }

  // 1) Ruleset nuevo (inmutable).
  const created = await rulesApi('POST', '/rulesets', token, {
    source: { files: [{ name: 'firestore.rules', content: RULES }] }
  });
  if (created.status !== 200) {
    console.error('\n✗ No se pudo crear el ruleset: HTTP ' + created.status);
    console.error(created.raw.slice(0, 500));
    process.exit(1);
  }
  const rulesetName = created.json.name;
  console.log('\n✓ Ruleset creado: ' + rulesetName);

  // 2) PATCH del release `cloud.firestore` → es el que Firestore usa de verdad.
  //
  // FORMA DEL CUERPO (es lo que hace firebase-tools y no es evidente): el Release va
  // ENVUELTO en `{ release: {...} }` y NO se pasa updateMask. Mandar el Release a secas
  // devuelve 400 "Unknown name rulesetName: Cannot find field".
  const RELEASE = '/releases/cloud.firestore';
  const body = {
    release: { name: 'projects/' + PROJ + '/releases/cloud.firestore', rulesetName }
  };
  const released = await rulesApi('PATCH', RELEASE, token, body);
  if (released.status !== 200) {
    console.error('\n✗ No se pudo actualizar el release cloud.firestore: HTTP ' + released.status);
    console.error(released.raw.slice(0, 500));
    console.error('\nSi el release no existe, créalo con POST /releases {name, rulesetName}.');
    process.exit(1);
  }
  console.log('✓ Release activo:  ' + released.json.name);
  console.log('   apunta a        ' + released.json.rulesetName);
  console.log('\nReglas DESPLEGADAS en ' + PROJ + '.');
  console.log('Para volver atrás: PATCH de ' + RELEASE + ' al ruleset anterior (' + rulesetAnterior + ').');
})();
