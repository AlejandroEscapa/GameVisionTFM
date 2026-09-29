# Entorno de pruebas — usuario QA de GameVision

> Cuenta de pruebas fija para verificar login, perfil, biblioteca, amigos y feed
> **sin crear usuarios nuevos en cada sesión**.
> Las **credenciales** NO viven en el repo: están en `.secrets/qa-user.md`
> (fuera del control de versiones).

## Usuario QA

| Campo | Valor |
|---|---|
| Email | `gamevision@gmail.com` |
| Nombre completo | QA GameVision |
| Nombre de usuario | QA GameVision |
| UID (Firebase Auth) | `gKfQYvAdiBOezcGnrWo3rH0nUmY2` |
| Proyecto | `gamevision-tfm-b1b4d` |
| Documento Firestore | `users/gamevision@gmail.com` |
| Alta | 2026-09-29 (registrada desde la app) |

> **Contraseña:** en `.secrets/qa-user.md` (local). Es simple a propósito (entorno de pruebas).

## Para qué sirve
- **CA0.2** (registro offline → biblioteca): iniciar sesión con esta cuenta en el emulador.
- **CA0.3** (foto entre dispositivos): subir foto desde un dispositivo y abrir en otro.
- Probar en F1/F2 sin ensuciar el proyecto con cuentas nuevas.

## Cómo entrar
1. Emulador `Pixel_9` con la app instalada.
2. Login → email y contraseña del usuario QA.
3. Al entrar, el perfil `users/gamevision@gmail.com` ya existe en Firestore.

## Pendiente
- Verificar la **foto de perfil**: bloqueada por el coste de Firebase Storage (ver
  [investigación de alternativas](../investigacion-2026/almacenamiento-imagenes-2026.md)).
