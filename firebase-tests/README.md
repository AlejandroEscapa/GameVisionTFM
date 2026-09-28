# Tests de reglas de Firestore — GameVision

Tests automatizados de las reglas de seguridad usando el **Emulator Suite**
de Firebase (no toca producción, no necesita login).

## Qué cubre

- Biblioteca (`users/{uid}/library|logs|sessions|stats`): solo su dueño.
- Perfil raíz: lectura con sesión; escritura solo del dueño (uid o email).
- Amigos: solo el dueño. Mensajes: lectura autenticada; escritura del dueño del muro.
- Listas antiguas y resto de rutas: cerradas.

## Cómo se ejecutan

```bash
cd firebase-tests
npm install        # una vez
npm test
```

`npm test` arranca el emulador de Firestore (descarga el JAR la primera vez; requiere **JDK 21+**
en el PATH — firebase-tools ya no soporta versiones anteriores), ejecuta `rules.test.mjs` contra
`firebase/firestore.rules` (el fichero real del repo) y lo apaga todo.

En CI se ejecutan solos (job `firestore-rules` de `.github/workflows/ci.yml`).
