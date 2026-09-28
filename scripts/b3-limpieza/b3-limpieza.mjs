/*
 * B3 — Limpieza de datos de prueba (F0-B).
 *
 * Borra las listas antiguas (playedlist, wishlist, favorites, history) de TODOS los
 * usuarios y los documentos basura aa/ee, con backup previo en JSON.
 *
 * - Dry-run por defecto; borrar requiere --commit (y hace backup antes).
 * - Usa el Admin SDK (requiere clave de servicio; ver README.md).
 */
import { readFileSync, mkdirSync, writeFileSync } from 'node:fs';
import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';

const [, , keyPath, ...flags] = process.argv;
const commit = flags.includes('--commit');

if (!keyPath) {
  console.error('Uso: node b3-limpieza.mjs <ruta-clave-servicio.json> [--commit]');
  process.exit(1);
}

const TARGETS = ['playedlist', 'wishlist', 'favorites', 'history'];
const JUNK = ['aa', 'ee'];

initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, 'utf8'))) });
const db = getFirestore();

console.log(`Modo: ${commit ? 'EJECUCIÓN REAL (--commit)' : 'SIMULACIÓN (dry-run)'}`);

const usersSnap = await db.collection('users').get();
console.log(`Usuarios encontrados: ${usersSnap.size}\n`);

// 1) Plan
const plan = [];
for (const userDoc of usersSnap.docs) {
  const subs = await userDoc.ref.listCollections();
  const targets = subs.map((c) => c.id).filter((id) => TARGETS.includes(id));
  if (targets.length > 0) plan.push({ id: userDoc.id, targets });
}
const junkFound = usersSnap.docs.map((d) => d.id).filter((id) => JUNK.includes(id));

console.log('Plan:');
for (const p of plan) console.log(`  users/${p.id} → borrar subcolecciones: ${p.targets.join(', ')}`);
console.log(`  Documentos basura a borrar: ${junkFound.join(', ') || '(ninguno)'}\n`);

if (!commit) {
  console.log('Dry-run: nada modificado. Ejecuta con --commit para aplicar (hace backup antes).');
  process.exit(0);
}

// 2) Backup ANTES de borrar
const stamp = new Date().toISOString().replace(/[:.]/g, '-');
const dir = `backups/b3-${stamp}`;
mkdirSync(dir, { recursive: true });
const dump = {};
for (const userDoc of usersSnap.docs) {
  const entry = { profile: userDoc.data() ?? {}, collections: {} };
  for (const col of await userDoc.ref.listCollections()) {
    if (!TARGETS.includes(col.id)) continue;
    const docs = await col.get();
    entry.collections[col.id] = docs.docs.map((d) => ({ id: d.id, data: d.data() }));
  }
  dump[userDoc.id] = entry;
}
writeFileSync(`${dir}/backup-users.json`, JSON.stringify(dump, null, 2));
console.log(`Backup guardado en ${dir}/backup-users.json\n`);

// 3) Borrado (recursivo: subcolecciones incluidas)
for (const userDoc of usersSnap.docs) {
  for (const col of await userDoc.ref.listCollections()) {
    if (TARGETS.includes(col.id)) {
      await db.recursiveDelete(col);
      console.log(`  borrada users/${userDoc.id}/${col.id}`);
    }
  }
}
for (const id of junkFound) {
  await db.recursiveDelete(db.doc(`users/${id}`));
  console.log(`  borrado users/${id}`);
}

// 4) Verificación
console.log('\nVerificación:');
let pending = 0;
for (const userDoc of (await db.collection('users').get()).docs) {
  const subs = (await userDoc.ref.listCollections())
    .map((c) => c.id)
    .filter((id) => TARGETS.includes(id));
  if (subs.length > 0) {
    pending += 1;
    console.log(`  ⚠️ users/${userDoc.id} aún tiene: ${subs.join(', ')}`);
  }
}
console.log(pending === 0 ? '  ✅ Limpieza completa.' : `  ⚠️ Quedan ${pending} casos por revisar.`);
