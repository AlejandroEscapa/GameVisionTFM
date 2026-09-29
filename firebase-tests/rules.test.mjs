/*
 * Tests de las reglas de Firestore de GameVision (F0-B).
 *
 * Verifican de forma SISTEMÁTICA (incluidos los casos negativos) que:
 *  - la biblioteca (users/{uid}/library|logs|sessions|stats) solo la toca su dueño
 *  - el perfil raíz se lee con sesión y se escribe solo por el dueño, por uid
 *  - amigos: solo el dueño; mensajes: lectura autenticada, escritura del dueño
 *  - los índices inversos (email_index, usernames) no se pueden secuestrar
 *  - las rutas heredadas y el resto de la base están cerradas
 *
 * OJO: este fichero NO descubre las reglas nuevas por sí solo. Cualquier cambio en
 * `firebase/firestore.rules` tiene que venir con su check aquí, o la suite seguirá
 * pasando "probando" una versión que ya no es la que corre.
 *
 * Se ejecutan con el Emulator Suite (ver README.md de esta carpeta).
 */
import { readFileSync } from 'node:fs';
import {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} from '@firebase/rules-unit-testing';
import { deleteDoc, doc, getDoc, setDoc } from 'firebase/firestore';

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
// El email ya NO es clave de documento (ADR-0008): `isOwner` exige uid.
await check(
  'el email ya NO sirve como clave: el dueño NO escribe users/{su email}',
  setDoc(doc(aliceDb, 'users/alice@test.dev'), { username: 'alice' }, { merge: true }),
  false,
);
await check(
  'el dueño NO escribe users/{email de otro}',
  setDoc(doc(aliceDb, 'users/bob@test.dev'), { username: 'hack' }, { merge: true }),
  false,
);
await check(
  'otro NO escribe perfil ajeno',
  setDoc(doc(bobDb, 'users/aliceUid'), { nameSurname: 'hack' }, { merge: true }),
  false,
);

// 6) Amigos: dueño sí; otros no
await check(
  'dueño escribe sus amigos',
  setDoc(doc(aliceDb, 'users/aliceUid/friends/bobUid'), {}),
  true,
);
await check('otro NO lee amigos ajenos', getDoc(doc(bobDb, 'users/aliceUid/friends/bobUid')), false);

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

// 8) Listas antiguas y rutas heredadas cerradas
await check(
  'listas antiguas cerradas (id heredado de email)',
  getDoc(doc(aliceDb, 'users/alice@test.dev/playedlist/1')),
  false,
);
await check(
  'listas antiguas cerradas (id de uid)',
  getDoc(doc(aliceDb, 'users/aliceUid/playedlist/1')),
  false,
);

// 9) Foto de perfil (ADR-0007): el dueño escribe; cualquiera con sesión la lee
await check(
  'dueño escribe su foto de perfil',
  setDoc(doc(aliceDb, 'profile_images/aliceUid'), { imageUri: 'data:image/png;base64,AA' }),
  true,
);
await check('autenticado lee fotos de perfil', getDoc(doc(bobDb, 'profile_images/aliceUid')), true);
await check(
  'otro NO escribe la foto de perfil ajena',
  setDoc(doc(bobDb, 'profile_images/aliceUid'), { imageUri: 'hack' }),
  false,
);
await check(
  'otro NO borra la foto de perfil ajena',
  deleteDoc(doc(bobDb, 'profile_images/aliceUid')),
  false,
);

// 10) email_index: solo se puede apuntar el propio email, y nadie lo roba
await check(
  'autenticado resuelve email → uid',
  getDoc(doc(bobDb, 'email_index/alice@test.dev')),
  true,
);
await check(
  'cada uno registra su propio email_index',
  setDoc(doc(aliceDb, 'email_index/alice@test.dev'), { uid: 'aliceUid' }),
  true,
);
await check(
  'NADIE secuestra el email_index de otro (el bug de las reglas)',
  setDoc(doc(bobDb, 'email_index/alice@test.dev'), { uid: 'bobUid' }),
  false,
);
await check(
  'el dueño tampoco puede apuntar su entrada a un uid ajeno',
  setDoc(doc(aliceDb, 'email_index/carol@test.dev'), { uid: 'bobUid' }),
  false,
);
await check(
  'nadie borra el email_index',
  deleteDoc(doc(aliceDb, 'email_index/alice@test.dev')),
  false,
);
await check(
  'sin sesión NO lee el email_index',
  getDoc(doc(anonDb, 'email_index/alice@test.dev')),
  false,
);

// 11) usernames: el rename retira el tuyo, pero nadie roba el ajeno
await check(
  'cada uno registra su propio username',
  setDoc(doc(aliceDb, 'usernames/alice'), { uid: 'aliceUid' }),
  true,
);
await check('autenticado resuelve username → uid', getDoc(doc(bobDb, 'usernames/alice')), true);
await check(
  'NADIE roba un username ajeno (mismo bug que email_index)',
  setDoc(doc(bobDb, 'usernames/alice'), { uid: 'bobUid' }),
  false,
);
await check(
  'el dueño libera su username al renombrar',
  deleteDoc(doc(aliceDb, 'usernames/alice')),
  true,
);
await check(
  'otro NO borra el username ajeno',
  deleteDoc(doc(bobDb, 'usernames/alice')),
  false,
);
await check(
  'sin sesión NO lee usernames',
  getDoc(doc(anonDb, 'usernames/alice')),
  false,
);

// 12) Resto de la base cerrado
await check('otras rutas cerradas', getDoc(doc(aliceDb, 'games/1')), false);

await testEnv.cleanup();

console.log(`\nResultado: ${passed} OK, ${failed} fallos`);
process.exit(failed === 0 ? 0 : 1);
