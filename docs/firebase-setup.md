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

## Diagnóstico verificado (27/09/2026)

Probado con la API REST de Firestore y con la app en el emulador:

| Operación | Resultado |
|---|---|
| GET `/users/{email}` (documento) | ✅ 200 |
| LIST `/users` | ✅ 200 |
| LIST `/users/{email}/playedlist` (subcolección) | ❌ 403 PERMISSION_DENIED |
| WRITE sin autenticar | ❌ 403 PERMISSION_DENIED |
| WRITE con sesión de Firebase Auth | ✅ 200 |

Es decir, las reglas actuales permiten **leer** documentos y listar `users`, pero
**deniegan listar las subcolecciones** (`history`, `playedlist`, `wishlist`,
`messages`, `friends`) y **deniegan escribir sin autenticar**.

### Consecuencias reales (todas reproducidas)

1. **El registro desde la app no puede funcionar.** La app no autentica con
   Firebase Auth en email/password, así que su escritura llega sin credenciales
   → 403. (Verificado en logcat: `Error registrando usuario: PERMISSION_DENIED`.)
2. **El login tampoco**, para los usuarios que ya existían, porque sus documentos
   **no tienen campo `password`** (verificado en `alex.escapax@gmail.com`: solo
   nameSurname, username, description, country, imageUri). `loginCheck()` compara
   `document.getString("password") == password` → siempre `false`.
3. **6 pantallas dependen de `FirebaseAuth.getInstance().currentUser`, que es
   `null`** para los usuarios de email/password (solo lo rellena Google Sign-In):
   `MainActivity`, `NewsScreen`, `ProfileScreen`, `GameListScreen`,
   `SocialScreen`, `FriendsComposables`. Verificado en logcat:
   `ProfileScreen: currentUser es null o no tiene email.` Por eso el perfil sale
   vacío y las pantallas no cargan datos.
4. **La lista de juegos no puede cargar** aunque el login funcione: leer la
   subcolección `playedlist` da 403 (verificado en logcat:
   `Error obteniendo juegos: PERMISSION_DENIED`).

### Usuario de prueba creado

`users/alex@gmail.com` con `password: 1234` (nameSurname, username, description,
country), creado con una sesión REST autenticada porque el registro de la app
no puede escribir. **Login verificado en el emulador**: entra y navega
(barra inferior con 5 items). El perfil aparece vacío por el punto 3.

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
