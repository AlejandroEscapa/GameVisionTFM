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

## Diagnóstico del bloqueo actual (27/09/2026)

Probado con la API REST de Firestore y con la app en el emulador:

| Operación | Resultado |
|---|---|
| Lectura sin autenticar | ✅ Permitida |
| Escritura sin autenticar | ❌ `403 Missing or insufficient permissions` |

**Consecuencia:** el registro falla (no puede escribir el documento) y el login
de los usuarios existentes tampoco puede funcionar porque **sus documentos no
tienen campo `password`** (verificado: `alex.escapax@gmail.com` solo tiene
nameSurname, username, description, country, imageUri). `loginCheck()` compara
`document.getString("password") == password` → siempre devuelve `false`.

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
