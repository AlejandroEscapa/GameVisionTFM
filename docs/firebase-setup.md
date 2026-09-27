# Configuración de Firebase — GameVision

> Estado a 27/09/2026. Proyecto real: **`gamevision-tfm-b1b4d`** (número 241921328888).

## Lo que ya está configurado (verificado)

- `app/google-services.json` — config real del proyecto (gitignored, nunca se sube).
- `app/src/main/res/values/strings.xml` → `default_web_client_id` apunta al cliente
  web del proyecto real (`241921328888-j0qu3hb5n55la6bugnqj30lna18u4i9a…`).
  Antes apuntaba al proyecto antiguo del zip y rompía Google Sign-In.
- SHA-1 del keystore de debug (`6F:C5:54:BF:…:E3:BD`) **ya está registrado** en
  Firebase → Google Sign-In funcionará en debug.

## Estructura de la base de datos

```
users/{email}/                    ← documento por email del usuario
    nameSurname, username, description, country,
    password (solo en usuarios registrados desde la app),
    imageUri
    ├── history/{gameId}
    ├── messages/{msgId}
    ├── playedlist/{gameId}
    └── wishlist/{gameId}
```

## Investigación a fondo (27/09/2026) — matriz completa de reglas

Probado con la API REST de Firestore (sin autenticar y con sesión de Firebase
Auth) y con la app en el emulador:

| Operación | Sin auth | Con auth |
|---|---|---|
| GET `/users/{email}` | ✅ 200 | – |
| LIST `/users` | ✅ 200 | ✅ 200 |
| LIST `/users/{email}/playedlist` | ❌ 403 | ✅ 200 |
| WRITE `/users/{email}` | ❌ 403 | ✅ 200 |

**Conclusión decisiva: las reglas NO son el fallo.** Están bien diseñadas para un
app basada en Firebase Authentication: lectura pública de perfiles (para poder
buscar amigos por email), y escritura + subcolecciones solo para usuarios
autenticados. El problema es que **el login email/password nunca autentica**, así
que esos usuarios son tratados como anónimos: no pueden escribir ni leer sus
propias listas.

### La app tiene DOS sistemas de autenticación que no concuerdan

1. **Email/password — propio, sobre Firestore**: guarda el password en claro en
   el documento, `loginCheck()` compara en cliente y **no crea sesión de Firebase
   Auth**. Es el camino incompleto.
2. **Google Sign-In — Firebase Auth**: `signInWithCredential` crea sesión real →
   `currentUser` no nulo.

### Estado por funcionalidad

| Funcionalidad | Email/password | Google |
|---|---|---|
| Login | ✅ si el doc tiene `password` | ✅ |
| Registro | ❌ 403 al escribir (y la app **miente** diciendo que fue bien) | ✅ |
| Perfil | ❌ vacío (`currentUser` null, sin respaldo) | ✅ |
| News (fetch de usuario) | ❌ no se llama | ✅ |
| Auto-login al reabrir | ❌ no hay sesión persistida | ✅ |
| Lista de juegos / wishlist / historial | ❌ 403 al listar subcolecciones | ✅ |
| Amigos / mensajes | ❌ 403 | ✅ |
| Editar perfil / añadir juego / amigo / publicar | ❌ 403 | ✅ |
| Recuperar contraseña | ❌ **stub: no envía nada** | ✅ (vía Auth) |

Para un usuario de **Google la app funciona entera hoy** con las reglas actuales.

### Bugs de la app encontrados (independientes de las reglas)

1. **Fallo silencioso en el registro**: `addUser()` captura la excepción y solo
   la loguea; `registerUser()` devuelve `true` igualmente → la UI navega al login
   como si hubiera funcionado y luego el login falla sin explicación. Es
   exactamente el síntoma reportado.
2. **Recuperar contraseña es un stub**: `forgotPassword()` solo valida que el
   email no esté vacío y devuelve `true`; no envía ningún correo.
3. **`description`/`country` = null se muestran como el texto "null"**: el
   registro escribe `null` y la lectura hace `it.value.toString()`.
4. `removeFriend()` no es `suspend` y hace `.delete()` sin `await()`: errores
   tragados.

### Seguridad (crítico, verificado)

- **La colección `users` es legible SIN autenticar**: leí
  `alex.escapax@gmail.com` solo con la API key (que viaja dentro del APK).
- Por tanto **todos los passwords en claro y los datos personales están
  expuestos** a cualquiera que descompile el APK. `loginCheck` en cliente es
  trivialmente evitable.

## Cómo desbloquearlo (dos opciones)

### Opción 1 — Rápida (demo/TFM): permitir escritura en `/users`

Firebase Console → Firestore Database → **Reglas** → pegar el contenido de
[`firebase/firestore.rules`](../firebase/firestore.rules) → Publicar.

⚠️ Esa regla deja la base abierta (la app no usa Firebase Auth). Vale para el
TFM, no para producción.

Después de publicarla: **registrar el usuario desde la app** (Registro →
alex@gmail.com / 1234). El registro sí escribe el campo `password`, y el login
funcionará. Los usuarios antiguos sin `password` seguirán sin poder entrar
habría que añadirles el campo a mano en la consola.

### Opción 2 — Correcta (producción): migrar a Firebase Authentication

Sustituir el esquema de "password en Firestore" por Firebase Auth email/password:
1. Habilitar el proveedor Email/Password en Firebase Console → Authentication.
2. Crear/ migrar los usuarios a Auth.
3. `loginCheck` pasa a `signInWithEmailAndPassword`; dejar de guardar el password.
4. Reglas seguras basadas en `request.auth.uid`.

Es un cambio de arquitectura de autenticación (aprox. una sesión de trabajo).

## Comandos útiles

```bash
# Verificar lectura/escritura desde fuera de la app
KEY=AIzaSyCwN3dsDzSF7qg0zRKiuuQSNHC3zKBrJMo
curl "https://firestore.googleapis.com/v1/projects/gamevision-tfm-b1b4d/databases/(default)/documents/users/EMAIL?key=$KEY"
```
