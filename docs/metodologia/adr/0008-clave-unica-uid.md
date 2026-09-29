# ADR-0008 — Clave única de identidad: `uid` (en lugar de email)

- **ADR:** 0008
- **Título:** Unificar la identidad en `uid`; el email pasa a ser un campo, no una clave
- **Estado:** **Propuesto** (pendiente de aprobación del propietario)
- **Fecha:** 2026-09-29
- **Decisores:** Alejandro Olivares Escapa (propietario)
- **Fase relacionada:** F0/F2 — debe cerrarse **antes de F2 (Social)**

## Contexto

Hoy conviven **dos claves distintas** para el mismo usuario:

| Dato | Dónde vive hoy | Clave |
|---|---|---|
| Perfil (`nameSurname`, `username`, `description`, `country`, `imageUri`) | `users/{email}` | **email** |
| Amigos | `users/{email}/friends/{friendEmail}` | **email** |
| Muro/mensajes | `users/{email}/messages/{id}` | **email** |
| Biblioteca | `users/{uid}/library/{gameId}` | **uid** |
| Diario (logs) | `users/{uid}/logs/{logId}` | **uid** |
| Sesiones | `users/{uid}/sessions/{id}` | **uid** |
| Estadísticas | `users/{uid}/stats/summary` | **uid** |
| Foto de perfil (ADR-0007) | `profile_images/{uid}` | **uid** |

Es la deuda que se anotó en la auditoría de F0. **Funciona hoy**, pero es un problema de diseño:

1. **Dos identidades para la misma persona** → cualquier consulta o regla tiene que considerar las dos.
2. **Las reglas necesitan un parche**: `isOwner(userId)` compara `uid` **o** `email`
   (`request.auth.uid == userId || request.auth.token.email == userId`). Es una concesión que
   esconde el problema.
3. **El email como id es inestable**: no se puede cambiar, y si el usuario cambia de correo en
   Firebase Auth, el id del documento queda desincronizado.
4. **Privacidad (F2)**: exponer emails en rutas y para localizar usuarios choca con la decisión
   D2.4 (buscar por `username`, no por email).
5. **Renombrar/duplicar**: migrar una colección entera si el email cambia es carísimo.

## Decisión propuesta

**Una sola clave de identidad: `uid` de Firebase Auth.** El email deja de ser clave y pasa a ser
**un campo más del documento de perfil**.

### Modelo objetivo

```
users/{uid}                          → perfil (campos: email, username, nameSurname,
                                        description, country, imageUri, …)
users/{uid}/library/{gameId}
users/{uid}/logs/{logId}
users/{uid}/sessions/{sessionId}
users/{uid}/stats/summary
users/{uid}/friends/{friendUid}
users/{uid}/messages/{messageId}
profile_images/{uid}                 → ya cumple (ADR-0007)

usernames/{username} → { uid }       → índice para BUSCAR por nombre de usuario
```

Beneficios directos:
- **Reglas simples y honestas**: `request.auth.uid == userId` (se elimina el `|| email`).
- **Sin email en las rutas** (privacidad por diseño, coherente con D2.4).
- **Un solo identificador** en todo el código y toda la documentación.
- **Cambiar de email** deja de ser un problema de datos.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| **Dejarlo como está** | Cero trabajo | Deuda que crece; reglas con parche; colisión con F2 | Se agrava justo cuando llega lo social |
| **`uid` como clave (elegida)** | Una identidad; reglas limpias; escalable | Requiere migración y tocar la UI | **Elegida**: es el modelo natural de Firebase Auth |
| **Email como clave en todo** | Mismo criterio en todas partes | Inestable, expone emails, choca con F2 | Va contra la dirección del producto |
| **Id sintético propio** | Desacopla de Auth | Duplicar la identidad de Auth sin necesidad | Sobre-ingeniería |

## Plan de migración (por fases, reversible)

> El proyecto usa **datos de prueba** (ya limpiados en B3), así que la migración es pequeña; el
> valor del plan es que quede **documentado y reproducible**.

1. **Compatibilidad de lectura/escritura (código)**
   - `UserRepository` pasa a leer/escribir el perfil en `users/{uid}` (email como campo).
   - Amigos y mensajes pasan a `users/{uid}/friends/{friendUid}` y `users/{uid}/messages/…`.
   - Búsqueda por username contra el índice `usernames/{username}`.
2. **Script de migración (Admin SDK, offline)**
   - Por cada `users/{email}`: crear `users/{uid}` con sus datos (email incluido).
   - Mapear `friends/{friendEmail}` → `friends/{friendUid}` (resolviendo email→uid).
   - Mapear `messages/*` a la ruta nueva.
   - **Backup JSON previo** (mismo patrón que `scripts/b3-limpieza/`).
3. **Reglas**
   - Simplificar a `isOwner(userId) = request.auth.uid == userId` (sin email).
   - Añadir reglas del índice `usernames` (lectura autenticada; escritura solo si el `uid` del
     documento es el propio).
4. **Verificación E2E**
   - Login, perfil, biblioteca, amigos y foto con dos cuentas; comprobar el corte limpio del email.
5. **Limpieza**
   - Borrar los `users/{email}` heredados tras verificar (con backup).
6. **Retirada del parche**
   - Eliminar el `|| request.auth.token.email == userId` de las reglas y del código.

### Orden y puerta de salida
Hacerlo **antes de F2**: F2 introduce el modelo social y la búsqueda de usuarios, y es el momento
en el que esta decisión tiene consecuencias. Puerta de salida: reglas sin parche + migración
verificada + `AGENTS.md`/roadmap actualizados.

## Consecuencias

**Positivas**
- Una sola identidad; reglas y consultas simples.
- Privacidad: los emails dejan de ir en las rutas.
- Preparado para F2 (seguir, buscar por username, feed).

**Negativas / coste asumido**
- Migración de datos y cambios en `UserRepository` + pantallas que usan el email como id.
- Ventana de compatibilidad mientras convivan ambos formatos.

**Riesgos y mitigación**
- Romper datos existentes → backup previo y verificación E2E con dos cuentas.
- Cambios a medias → se hace como bloque cerrado de F0/F2 con criterio de aceptación propio.

## Seguimiento

- Cerrar antes de F2 (ver [fase-2](../../roadmap/fase-2-social.md) y [D0.5/F0-B](../../plan/F0B-propuesta-modelo-datos.md)).
- Registrar el cierre en el roadmap y eliminar este estado "Propuesto" cuando se apruebe y ejecute.
