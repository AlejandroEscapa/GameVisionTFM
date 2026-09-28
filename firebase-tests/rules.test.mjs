/*
 * Tests de las reglas de Firestore de GameVision (F0-B).
 *
 * Verifican de forma SISTEMÁTICA (incluidos los casos negativos) que:
 *  - la biblioteca (users/{uid}/library|logs|sessions|stats) solo la toca su dueño
 *  - el perfil raíz se lee con sesión y se escribe solo por el dueño (uid o email)
 *  - amigos: solo el dueño; mensajes: lectura autenticada, escritura del dueño
 *  - las listas antiguas y el resto de rutas están cerradas
 *
 * Se ejecutan con el Emulator Suite (ver README.md de esta carpeta).
 */
import { readFileSync } from 'node:fs';
import {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc } from 'firebase/firestore';

const PROJECT_ID = 'demo-gamevision';

let passed = 0;
let failed = 0;

async function check(name, operation, expectAllowed) {
  try {
    if (expectAllowed) {
      await assertSucceeds(operation);
    } else {
      await assertFails(operation);
    }
    passed += 1;
    console.log(`  [OK] ${name}`);
  } catch (error) {
    failed += 1;
    console.log(`  [FALLO] ${name} -> ${error.message}`);
  }
}

const testEnv = await initializeTestEnvironment({
  projectId: PROJECT_ID,
  firestore: {
    rules: readFileSync(new URL('../firebase/firestore.rules', import.meta.url), 'utf8'),
  },
});

const alice = testEnv.authenticatedContext('aliceUid', { email: 'alice@test.dev' });
const bob = testEnv.authenticatedContext('bobUid', { email: 'bob@test.dev' });
const anon = testEnv.unauthenticatedContext();

const aliceDb = alice.firestore();
const bobDb = bob.firestore();
const anonDb = anon.firestore();

console.log('Reglas de Firestore — GameVision (F0-B):');

// 1) Biblioteca: el dueño puede escribir y leer
await check(
  'dueño escribe en su biblioteca',
  setDoc(doc(aliceDb, 'users/aliceUid/library/1'), { gameId: '1', status: 'jugando' }),
  true,
);
await check('dueño lee su biblioteca', getDoc(doc(aliceDb, 'users/aliceUid/library/1')), true);

// 2) Biblioteca: otro usuario NO puede leer ni escribir la ajena
await check('otro usuario NO lee biblioteca ajena', getDoc(doc(bobDb, 'users/aliceUid/library/1')), false);
await check(
  'otro usuario NO escribe en biblioteca ajena',
  setDoc(doc(bobDb, 'users/aliceUid/library/2'), { gameId: '2' }),
  false,
);

// 3) Biblioteca: sin sesión, nada
await check('sin sesión NO lee biblioteca', getDoc(doc(anonDb, 'users/aliceUid/library/1')), false);

// 4) Perfil raíz: lectura con sesión; sin sesión, no
await check('usuario autenticado lee perfil ajeno (amigos)', getDoc(doc(bobDb, 'users/aliceUid')), true);
await check('sin sesión NO lee perfil', getDoc(doc(anonDb, 'users/aliceUid')), false);

// 5) Perfil raíz: escribe solo el dueño (por uid o por email)
await check(
  'dueño escribe su perfil (uid)',
  setDoc(doc(aliceDb, 'users/aliceUid'), { nameSurname: 'Alice' }, { merge: true }),
  true,
);
await check(
  'dueño por email escribe su perfil',
  setDoc(doc(aliceDb, 'users/alice@test.dev'), { username: 'alice' }, { merge: true }),
  true,
);
await check(
  'otro NO escribe perfil ajeno',
  setDoc(doc(bobDb, 'users/aliceUid'), { nameSurname: 'hack' }, { merge: true }),
  false,
);

// 6) Amigos: dueño sí; otros no
await check(
  'dueño escribe sus amigos',
  setDoc(doc(aliceDb, 'users/aliceUid/friends/bob@test.dev'), {}),
  true,
);
await check('otro NO lee amigos ajenos', getDoc(doc(bobDb, 'users/aliceUid/friends/bob@test.dev')), false);

// 7) Mensajes: lectura autenticada; escritura solo el dueño del muro
await check('autenticado lee muro ajeno', getDoc(doc(bobDb, 'users/aliceUid/messages/m1')), true);
await check(
  'otro NO escribe en muro ajeno',
  setDoc(doc(bobDb, 'users/aliceUid/messages/m1'), { texto: 'hola' }),
  false,
);
await check(
  'dueño escribe en su muro',
  setDoc(doc(aliceDb, 'users/aliceUid/messages/m1'), { texto: 'hola' }),
  true,
);

// 8) Listas antiguas cerradas (se borran en B3)
await check('listas antiguas cerradas', getDoc(doc(aliceDb, 'users/alice@test.dev/playedlist/1')), false);

// 9) Resto de la base cerrado
await check('otras rutas cerradas', getDoc(doc(aliceDb, 'games/1')), false);

await testEnv.cleanup();

console.log(`\nResultado: ${passed} OK, ${failed} fallos`);
process.exit(failed === 0 ? 0 : 1);
