// Semilla del flag `onboardingDone` en las cuentas anteriores a la iteración
// 02/10: sin esto, los usuarios ya registrados verían el onboarding al abrir.
// Uso: node seed-onboarding.mjs [--commit]   (dry-run por defecto)
import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { readFileSync } from 'node:fs';

const KEY = 'C:/Users/Usuario/Desktop/GameVision/.secrets/gamevision-admin.json';
const COMMIT = process.argv.includes('--commit');

initializeApp({ credential: cert(JSON.parse(readFileSync(KEY, 'utf8'))) });
const db = getFirestore();

const snap = await db.collection('users').get();
console.log(`Usuarios: ${snap.size}${COMMIT ? '' : ' (DRY-RUN: usa --commit)'}`);
let pendientes = 0;

for (const doc of snap.docs) {
  const data = doc.data();
  if (data.onboardingDone === true) continue;
  pendientes++;
  const etiqueta = data.username || data.email || doc.id;
  if (COMMIT) {
    await doc.ref.set({ onboardingDone: true }, { merge: true });
    console.log(`  ✓ ${etiqueta}`);
  } else {
    console.log(`  · marcaría ${etiqueta}`);
  }
}
console.log(`Total a marcar: ${pendientes}`);
process.exit(0);
