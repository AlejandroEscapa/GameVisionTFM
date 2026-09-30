#!/usr/bin/env node
/*
 * Auditoría de cierre — carril máquina (bloques 1-5 del protocolo).
 *
 * Ejecuta las comprobaciones verificables y PRE-RELLENA el informe de auditoría
 * (docs/metodologia/auditoria-de-cierre.md §3), para que la validación humana
 * se centre en el bloque 6 (experiencia). Complementa la CI: aquí es local y
 * consolida la evidencia en un solo informe.
 *
 * Uso:
 *   node scripts/auditoria/auditoria.mjs                 # unitarios + lint + check-docs
 *   node scripts/auditoria/auditoria.mjs --release       # + assembleDebug/assembleRelease
 *   node scripts/auditoria/auditoria.mjs --rules         # + suite de reglas (Emulator Suite)
 *   node scripts/auditoria/auditoria.mjs --out informe.md  # guardar el informe a fichero
 *
 * Salida 0 si todo lo ejecutado pasa; 1 si algo falla (el informe se imprime igual).
 */

import { execFileSync, spawnSync } from 'node:child_process';
import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const REPO = join(import.meta.dirname, '..', '..');
const JBR = 'C:/Program Files/Android/Android Studio/jbr';
const args = process.argv.slice(2);
const want = f => args.includes(f);
const outFile = args[args.indexOf('--out') + 1] ?? null;

const run = (cmd, cmdArgs, opts = {}) => {
  const label = [cmd, ...cmdArgs].join(' ');
  console.log(`→ ${label}`);
  const r = spawnSync(cmd, cmdArgs, {
    cwd: REPO,
    stdio: ['ignore', 'pipe', 'pipe'],
    encoding: 'utf8',
    maxBuffer: 64 * 1024 * 1024,
    env: {
      ...process.env,
      JAVA_HOME: process.env.JAVA_HOME || (existsSync(JBR) ? JBR : process.env.JAVA_HOME),
      ...(opts.env ?? {}),
    },
    shell: process.platform === 'win32',
  });
  return { ok: r.status === 0, out: (r.stdout || '') + (r.stderr || ''), label };
};

const gradlew = (...t) =>
  run('gradlew.bat', t, { env: { CI: undefined } });

const res = {};
res.unit = gradlew('testDebugUnitTest', '--console=plain', '-q');
res.lint = gradlew('lintDebug', '--console=plain', '-q');
if (want('--release')) {
  res.debug = gradlew('assembleDebug', '--console=plain', '-q');
  res.release = gradlew('assembleRelease', '--console=plain', '-q');
}
if (want('--rules')) res.rules = run('npm', ['test'], { env: {} });

// check-docs: en local el workspace está al lado; si no existe el mapa, modo CI.
const mapaExiste = existsSync(join(REPO, '..', 'mapa-gamevision.md'));
res.docs = run(
  process.execPath,
  [join('scripts', 'check-docs', 'check-docs.js')],
  { env: mapaExiste ? {} : { CHECK_DOCS_SKIP_WORKSPACE: '1' } },
);

// Recuento de unitarios desde los XML de JUnit (fuente real, no memoria).
let tests = 0, fallos = 0, skipped = 0;
const dir = join(REPO, 'app', 'build', 'test-results', 'testDebugUnitTest');
if (existsSync(dir)) {
  for (const f of readdirSync(dir).filter(f => f.endsWith('.xml'))) {
    const xml = readFileSync(join(dir, f), 'utf8');
    const n = (re, acc) => { const m = xml.match(re); return acc + (m ? +m[1] : 0); };
    tests = n(/tests="(\d+)"/, tests);
    fallos = n(/failures="(\d+)"/, fallos);
    skipped = n(/skipped="(\d+)"/, skipped);
  }
}

const marca = ok => (ok === undefined ? '—' : ok ? '✅' : '⛔');
const lineas = [];
const add = s => lineas.push(s);

add('```');
add(`Auditoría (carril máquina)          Fecha: ${new Date().toISOString().slice(0, 10)}`);
add(`Bloque 1 Build:        debug ${marca(res.debug?.ok)} · release ${marca(res.release?.ok)}${res.debug === undefined ? '  (no ejecutado: usar --release)' : ''}`);
add(`Bloque 2 Tests:        unitarios ${marca(res.unit.ok)} — ${tests} tests, ${fallos} fallos, ${skipped} omitidos (instrumentados: correrlos aparte, necesitan emulador)`);
add(`Bloque 3 Lint:         ${marca(res.lint.ok)}`);
add(`Bloque 4/5 Arq. y docs: check-docs ${marca(res.docs.ok)} — el SSOT y los saltos de capa se revisan en el diff`);
add(`Bloque 6 Experiencia:  ⬜ HUMANO — recorrido de pantallas en emulador (vacío/carga/error/offline)`);
add(`Reglas Firestore:      ${marca(res.rules?.ok)}${res.rules === undefined ? '  (no ejecutado: usar --rules)' : ''}`);
add('```');
if (fallos > 0 || Object.entries(res).some(([, r]) => !r.ok)) {
  add('');
  add('⚠️ Revisar: ' + Object.entries(res).filter(([, r]) => !r.ok).map(([k]) => k).join(', '));
}

const informe = lineas.join('\n');
console.log('\n' + informe);
if (outFile) { writeFileSync(outFile, informe + '\n'); console.log(`\n→ Informe guardado en ${outFile}`); }
process.exit(Object.values(res).every(r => r.ok) ? 0 : 1);
