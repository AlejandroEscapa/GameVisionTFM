/*
 * Tests de las reglas de Firestore de GameVision (F0-B).
 *
 * Verifican de forma SISTEMÁTICA (incluidos los casos negativos) que:
 *  - la biblioteca (users/{uid}/library|logs|sessions|stats) solo la toca su dueño
 *  - el perfil raíz se lee con sesión y se escribe solo por el dueño, por uid
 *  - amigos: solo el dueño; mensajes: lectura autenticada, escritura del dueño
 *  - los índices inversos (email_index, usernames) no se pueden secuestrar
 *  - las rutas heredadas y el resto de la base están cerradas
 *  - F2: visibilidad (isPrivate), seguimiento, feed + likes, bloqueos y reportes
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
import { deleteDoc, doc, getDoc, increment, setDoc, updateDoc } from 'firebase/firestore';

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
const carol = testEnv.authenticatedContext('carolUid', { email: 'carol@test.dev' });
const anon = testEnv.unauthenticatedContext();

const aliceDb = alice.firestore();
const bobDb = bob.firestore();
const carolDb = carol.firestore();
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

// ===================================================================
// F2 — Social (30/09): visibilidad, seguimiento, feed, bloqueos, reportes
// ===================================================================

console.log('\nF2 — Social:');

// 13) Visibilidad D2.2: biblioteca/stats públicas salvo cuenta privada o bloqueo
await check('F2: carol crea su perfil', setDoc(doc(carolDb, 'users/carolUid'), { username: 'carol' }, { merge: true }), true);
await check('F2: carol escribe su biblioteca (dueña)', setDoc(doc(carolDb, 'users/carolUid/library/9'), { gameId: '9' }), true);
await check('F2: biblioteca pública sin campo isPrivate se lee (default false)', getDoc(doc(bobDb, 'users/aliceUid/library/1')), true);
await check('F2: otro NO escribe biblioteca pública ajena', setDoc(doc(bobDb, 'users/aliceUid/library/77'), { gameId: '77' }), false);
await check('F2: sin sesión NO lee biblioteca pública', getDoc(doc(anonDb, 'users/aliceUid/library/1')), false);
await check('F2: la dueña activa su cuenta privada', setDoc(doc(aliceDb, 'users/aliceUid'), { isPrivate: true }, { merge: true }), true);
await check('F2: cuenta privada oculta la biblioteca al resto', getDoc(doc(bobDb, 'users/aliceUid/library/1')), false);
await check('F2: la dueña SI ve su biblioteca con cuenta privada', getDoc(doc(aliceDb, 'users/aliceUid/library/1')), true);
await check('F2: el perfil raíz sigue visible con cuenta privada', getDoc(doc(bobDb, 'users/aliceUid')), true);
await check('F2: cuenta privada oculta stats', getDoc(doc(bobDb, 'users/aliceUid/stats/summary')), false);
await check('F2: la dueña SI ve sus stats', getDoc(doc(aliceDb, 'users/aliceUid/stats/summary')), true);
await check('F2: alice reabre su cuenta', setDoc(doc(aliceDb, 'users/aliceUid'), { isPrivate: false }, { merge: true }), true);
await check('F2: biblioteca pública de nuevo', getDoc(doc(bobDb, 'users/aliceUid/library/1')), true);

// 14) Seguimiento D2.1: solo el follower crea y borra sus aristas
await check(
  'F2: alice sigue a bob',
  setDoc(doc(aliceDb, 'following/aliceUid_bobUid'), { followerUid: 'aliceUid', followedUid: 'bobUid', createdAt: Date.now() }),
  true,
);
await check('F2: nadie se sigue a sí mismo', setDoc(doc(bobDb, 'following/bobUid_bobUid'), { followerUid: 'bobUid', followedUid: 'bobUid' }), false);
await check('F2: NO puedes crear aristas fingiendo otro follower', setDoc(doc(bobDb, 'following/carolUid_bobUid'), { followerUid: 'carolUid', followedUid: 'bobUid' }), false);
await check('F2: la arista NO se puede actualizar', updateDoc(doc(aliceDb, 'following/aliceUid_bobUid'), { followedUid: 'carolUid' }), false);
await check('F2: otro NO borra tu arista', deleteDoc(doc(bobDb, 'following/aliceUid_bobUid')), false);
await check('F2: sin sesión NO lee following', getDoc(doc(anonDb, 'following/aliceUid_bobUid')), false);
await check('F2: get puntual de arista con bloqueo NO', (async () => {
  await setDoc(doc(bobDb, 'blocks/bobUid/people/aliceUid'), {});
  try { await getDoc(doc(aliceDb, 'following/aliceUid_bobUid')); return Promise.reject(new Error('sirvio')); }
  catch { return Promise.resolve(); }
  finally { await deleteDoc(doc(bobDb, 'blocks/bobUid/people/aliceUid')); }
})(), true);

// 15) Feed D2.3/D2.6: hitos y posts validados; me gusta idempotentes
await check(
  'F2: alice publica su hito de completado',
  setDoc(doc(aliceDb, 'feed/f1'), { type: 'milestone', authorUid: 'aliceUid', milestoneType: 'completed', gameId: '28589', gameName: 'Halo 3', likesCount: 0, createdAt: Date.now() }),
  true,
);
await check(
  'F2: bob publica un post',
  setDoc(doc(bobDb, 'feed/fB'), { type: 'post', authorUid: 'bobUid', text: 'Primer post de GameVision', likesCount: 0, createdAt: Date.now() }),
  true,
);
await check('F2: un post NO suplanta al autor', setDoc(doc(bobDb, 'feed/fX'), { type: 'post', authorUid: 'aliceUid', text: 'suplantacion' }), false);
await check('F2: post vacío NO', setDoc(doc(aliceDb, 'feed/fY'), { type: 'post', authorUid: 'aliceUid', text: '' }), false);
await check('F2: post de 281 caracteres NO', setDoc(doc(aliceDb, 'feed/fZ'), { type: 'post', authorUid: 'aliceUid', text: 'a'.repeat(281) }), false);
await check('F2: tipo desconocido NO', setDoc(doc(aliceDb, 'feed/fW'), { type: 'otro', authorUid: 'aliceUid', text: 'x' }), false);
await check('F2: me gusta de bob al hito', setDoc(doc(bobDb, 'feed/f1/likes/bobUid'), { at: Date.now() }), true);
await check('F2: el like es único por uid (id = uid)', setDoc(doc(bobDb, 'feed/f1/likes/bobUid'), { at: Date.now() }), false);
await check('F2: el contador sube con increment', updateDoc(doc(bobDb, 'feed/f1'), { likesCount: increment(1) }), true);
await check('F2: el update NO toca campos además de likesCount', updateDoc(doc(bobDb, 'feed/f1'), { likesCount: increment(1), text: 'hack' }), false);
await check('F2: el update NO cambia solo el texto', updateDoc(doc(bobDb, 'feed/f1'), { text: 'hack' }), false);
await check('F2: otro NO borra el feed ajeno', deleteDoc(doc(bobDb, 'feed/f1')), false);
await check('F2: sin sesión NO lee el feed', getDoc(doc(anonDb, 'feed/f1')), false);
await check('F2: el autor borra su propio post', deleteDoc(doc(bobDb, 'feed/fB')), true);

// 16) Bloqueos D2.5 (CA2.9): cortan la lectura en ambas direcciones
await check('F2: bob bloquea a alice', setDoc(doc(bobDb, 'blocks/bobUid/people/aliceUid'), { at: Date.now() }), true);
await check('F2: bob NO puede bloquearse a sí mismo', setDoc(doc(bobDb, 'blocks/bobUid/people/bobUid'), {}), false);
await check('F2: bob lee su lista de bloqueos', getDoc(doc(bobDb, 'blocks/bobUid/people/aliceUid')), true);
await check('F2: alice NO lee la lista de bloqueos de bob', getDoc(doc(aliceDb, 'blocks/bobUid/people/aliceUid')), false);
await check('F2: bloqueo corta el perfil raíz (alice→bob)', getDoc(doc(aliceDb, 'users/bobUid')), false);
await check('F2: bloqueo corta el perfil raíz (bob→alice)', getDoc(doc(bobDb, 'users/aliceUid')), false);
await check('F2: bloqueo oculta la biblioteca pública', getDoc(doc(bobDb, 'users/aliceUid/library/1')), false);
await check('F2: bob repone su post para el test de bloqueo', setDoc(doc(bobDb, 'feed/fB2'), { type: 'post', authorUid: 'bobUid', text: 'hola otra vez', likesCount: 0 }), true);
await check('F2: bloqueada, alice NO da me gusta al post de bob', setDoc(doc(aliceDb, 'feed/fB2/likes/aliceUid'), {}), false);
await check('F2: bob desbloquea a alice', deleteDoc(doc(bobDb, 'blocks/bobUid/people/aliceUid')), true);
await check('F2: tras desbloquear, alice ve el perfil de bob', getDoc(doc(aliceDb, 'users/bobUid')), true);
await check('F2: tras desbloquear, alice da su me gusta', setDoc(doc(aliceDb, 'feed/fB2/likes/aliceUid'), {}), true);
await check('F2: alice retira su me gusta', deleteDoc(doc(aliceDb, 'feed/fB2/likes/aliceUid')), true);
await check('F2: otro NO borra bloqueos ajenos', deleteDoc(doc(aliceDb, 'blocks/bobUid/people/aliceUid')), false);

// 17) Reportes D2.5: solo crear con tu uid; nadie lee desde cliente
await check('F2: bob reporta contenido', setDoc(doc(bobDb, 'reports/r1'), { reporterUid: 'bobUid', targetUid: 'aliceUid', reason: 'spam' }), true);
await check('F2: NO se reporta suplantando a otro', setDoc(doc(bobDb, 'reports/r2'), { reporterUid: 'aliceUid', reason: 'x' }), false);
await check('F2: los reportes NO se leen desde cliente', getDoc(doc(bobDb, 'reports/r1')), false);
await check('F2: los reportes NO se borran desde cliente', deleteDoc(doc(bobDb, 'reports/r1')), false);

// 18) Resto de la base cerrado
await check('otras rutas cerradas', getDoc(doc(aliceDb, 'games/1')), false);

await testEnv.cleanup();

console.log(`\nResultado: ${passed} OK, ${failed} fallos`);
process.exit(failed === 0 ? 0 : 1);
