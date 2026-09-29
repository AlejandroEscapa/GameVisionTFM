/*
 * Chequeo anti-deriva de la documentación.
 *
 * Uso:  node scripts/check-docs/check-docs.js
 *
 * Por qué existe: el mismo dato repetido en dos sitios derivó varias veces en este
 * proyecto (F1 "en ejecución" cuando ya estaba cerrada, Kotlin 2.2.10 cuando ya era
 * 2.4.20, ramas borradas citadas como activas). Los avisos escritos no bastaron: el
 * `mapa-gamevision.md` llevaba encima un aviso de "esto es un índice" y derivó igual.
 *
 * Este script no confía en la disciplina. Comprueba que:
 *   1. `mapa-gamevision.md` NO declara estado (sin emojis de estado, ni "Pendiente",
 *      ni "En ejecución", ni "Estado exacto del árbol", ni inventarios de bloques).
 *   2. No cita ramas que ya no existen (solo `master`).
 *   3. Las versiones que cita coinciden con `gradle/libs.versions.toml`, la fuente real.
 *   4. El Plan Maestro tampoco declara estado en los títulos de fase.
 *   5. El roadmap no repite el detalle de bloques (eso vive en el fichero de fase).
 *
 * Sale con código 1 si hay deriva, 0 si está limpio.
 */
const fs = require('fs');
const path = require('path');

const REPO = path.resolve(__dirname, '..', '..');
const WORKSPACE = path.resolve(REPO, '..');
const MAPA = path.join(WORKSPACE, 'mapa-gamevision.md');
const TOML = path.join(REPO, 'gradle', 'libs.versions.toml');
const ROADMAP = path.join(REPO, 'docs', 'roadmap', 'README.md');
const PLAN = path.join(REPO, 'docs', 'plan', 'PLAN-MAESTRO-2026.md');

const problems = [];
const fail = (file, msg) => problems.push(path.relative(WORKSPACE, file) + '  →  ' + msg);

function read(p) {
  if (!fs.existsSync(p)) { fail(p, 'no existe'); return null; }
  return fs.readFileSync(p, 'utf8');
}

// --- 1. El mapa no declara estado ------------------------------------------
const mapa = read(MAPA);
if (mapa) {
  // Solo los símbolos de estado de fase (los del roadmap). 🔴/🟠/🟡 de *riesgo* son otra
  // cosa y se permiten.
  // OJO: la flag `u` es obligatoria. Sin ella la clase `[🟢⬜🔵⛔📄]` compara
  // surrogates UTF-16 sueltos y 🔴 (U+1F534) "casa" con 🔵 (U+1F535).
  const ESTADOS = [
    [/[🟢⬜🔵⛔📄]/gu, 'emoji de estado de fase'],
    [/\bEn ejecución\b/giu, 'declara "En ejecución"'],
    [/\bCompletada\b/giu, 'declara "Completada"'],
    [/\bEstado exacto del árbol\b/giu, 'declara "Estado exacto del árbol"'],
    [/\bPendiente en \w+/giu, 'inventario de pendientes'],
    [/\bBloque \d/giu, 'detalle de bloques (vive en el fichero de fase)'],
    [/Cambios sin commitear/giu, 'estado del árbol de trabajo'],
    [/\d+ tests? unitarios/giu, 'conteo de tests (se queda viejo; míralo en los XML)']
  ];
  for (const [re, why] of ESTADOS) {
    const m = mapa.match(re);
    if (m) fail(MAPA, 'declara estado: ' + why + '  [' + [...new Set(m)].join(', ') + ']');
  }

  // --- 2. Ramas ------------------------------------------------------------
  const RAMAS_BORRADAS = ['ui-redesign-2026', 'upgrade-2026'];
  for (const r of RAMAS_BORRADAS) {
    if (mapa.includes(r)) fail(MAPA, 'cita la rama borrada `' + r + '`');
  }

  // --- 3. Versiones vs libs.versions.toml ---------------------------------
  const toml = read(TOML);
  if (toml) {
    const ver = key => {
      const m = toml.match(new RegExp('^' + key + '\\s*=\\s*"([^"]+)"', 'm'));
      return m ? m[1] : null;
    };
    const VERSIONES = [
      ['kotlin', /Kotlin\s+(\d+\.\d+\.\d+)/g, 'Kotlin'],
      ['agp', /AGP\s+(\d+\.\d+\.\d+)/g, 'AGP'],
      ['coil', /Coil\s+(\d+\.\d+\.\d+)/gi, 'Coil'],
      ['room', /Room\s+(\d+\.\d+\.\d+)/gi, 'Room'],
      ['retrofit', /Retrofit\s+(\d+\.\d+\.\d+)/gi, 'Retrofit']
    ];
    for (const [key, re, etiqueta] of VERSIONES) {
      const real = ver(key);
      if (!real) continue;
      // TODAS las apariciones: la primera puede ser buena y la segunda vieja.
      for (const citada of mapa.matchAll(re)) {
        if (citada[1] !== real) {
          fail(MAPA, etiqueta + ' citado como ' + citada[1] + ' pero libs.versions.toml dice ' + real);
        }
      }
    }
  }
}

// --- 4. El Plan Maestro no declara estado ---------------------------------
// Los títulos de fase ni llevan emoji de estado ni palabras de presente. Y el cuerpo
// tampoco: el §2 es una foto histórica (28/09) marcada como tal, así que solo se
// tolera ahí lo que quede dentro de una línea de nota `>`.
const plan = read(PLAN);
if (plan) {
  for (const linea of plan.split('\n')) {
    if (!/^###\s+F\d/.test(linea)) continue;
    if (linea.trim().startsWith('>')) continue;
    if (/[🟢⬜🟡🔵⛔📄]/u.test(linea) || /\b(Pendiente|Completada|En ejecución)\b/.test(linea)) {
      fail(PLAN, 'el título de fase declara estado: "' + linea.trim() + '"');
    }
  }
  const PROHIBIDOS_PLAN = [
    [/\bEn ejecución\b/giu, 'declara "En ejecución" (el estado vive en docs/roadmap/)'],
    [/\bsin commitear\b/giu, 'declara estado del árbol de trabajo'],
    [/Kotlin\s+\d+\.\d+/g, 'cita una versión de Kotlin (viven en gradle/libs.versions.toml; deuda histórica → nota `>`)'],
    [/\bui-redesign-2026\b|\bupgrade-2026\b/g, 'cita una rama borrada']
  ];
  for (const linea of plan.split('\n')) {
    if (linea.trim().startsWith('>')) continue; // las notas históricas pueden citar el pasado
    for (const [re, why] of PROHIBIDOS_PLAN) {
      const m = linea.match(re);
      if (m) fail(PLAN, why + ': "' + linea.trim().slice(0, 70) + '"');
    }
  }
}

// --- 5. El roadmap no repite el detalle de bloques -------------------------
const roadmap = read(ROADMAP);
if (roadmap) {
  for (const linea of roadmap.split('\n')) {
    // Las notas explicativas (>) pueden citar el problema sin cometerlo.
    if (linea.trim().startsWith('>')) continue;
    if (/\bBloque \d/.test(linea)) {
      fail(ROADMAP, 'repite el detalle de bloques (vive en el fichero de fase): "' + linea.trim().slice(0, 70) + '"');
    }
  }
  // Y toda fase enlazada tiene que existir.
  for (const m of roadmap.matchAll(/\]\((fase-[^)]+\.md)\)/g)) {
    const destino = path.join(REPO, 'docs', 'roadmap', m[1]);
    if (!fs.existsSync(destino)) fail(ROADMAP, 'enlaza a un fichero de fase inexistente: ' + m[1]);
  }
}

// --- Salida ---------------------------------------------------------------
if (problems.length === 0) {
  console.log('✓ Documentación sin deriva. Comprobados: mapa, roadmap, plan maestro.');
  process.exit(0);
}
console.log('✗ Deriva detectada (' + problems.length + '):\n');
problems.forEach(p => console.log('  · ' + p));
console.log('\nRegla: el estado de una fase vive SOLO en su fichero (docs/roadmap/fase-N-*.md).');
process.exit(1);
