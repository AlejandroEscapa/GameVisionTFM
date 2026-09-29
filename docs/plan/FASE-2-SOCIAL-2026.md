# Fase 2 — Social: plan de ejecución (30/09/2026)

**Estado:** 🟢 En ejecución (30/09) · Plan aprobado y decisiones cerradas con el propietario.

## Decisiones cerradas (30/09)

| # | Decisión | Resolución | Porqué |
|---|---|---|---|
| D2.1 | Modelo social | **Seguir asimétrico** | No exige permiso mutuo; crece solo; el patrón que funciona en el sector. Amigos actuales → aristas mutuas (T2.9). |
| D2.2 | Privacidad por defecto | **Público con interruptor privado** | El descubrimiento es el motor; el interruptor protege a quien quiera cerrarse. |
| D2.3 | Qué se publica en el feed | **Hitos + posts del usuario** (ampliación del propietario) | Los hitos (completado, reseña, lista pública) dan valor sin saturar; los posts dan voz. Las sesiones diarias NO van al feed. |
| D2.4 | Búsqueda de usuarios | **Índice `usernames` + reglas** (sin Blaze) | Las Cloud Functions exigen plan Blaze (mismo muro que Storage). El índice ya existe; las reglas limitan la exposición. Migración a Function documentada como mejora futura si hay plan de pago. |
| D2.5 | Moderación | **Reportar + bloquear** | Mínimo viable con criterio; cubre reseñas y posts. |
| D2.6 | Interacción | **Me gusta en F2; comentarios después** | Los comentarios implican moderación y notificaciones: deuda que no toca abrir ahora. |
| D2.7 | Chat existente | **Congelado** | Se oculta de la UI sin borrar código ni datos; se decide su destino en la auditoría de cierre de F2. |

## Vertientes

- **Producto:** encontrar gente por username, seguir, feed con vida, Top 4 como identidad.
- **Diseño:** reutilizar el design system; estados vacío/carga/error/offline en cada pantalla nueva.
- **Datos:** colecciones nuevas (`following`, `feed`, `blocks`, `reports`), visibilidad gobernada por `users/{uid}.isPrivate`, contadores por aggregate queries (sin escrituras cruzadas).
- **Comercial:** el feed y el descubrimiento son la retención diaria que sostiene la monetización futura de F5.
- **Marketing:** perfil público y Top 4 compartible fuera de la app (más adelante, F3 Rewind).
- **Proceso:** suite de reglas ampliada y desplegada solo en verde; migración con dry-run y backup; auditoría de cierre según protocolo.

## Modelo de datos

- **`following/{id}`** (raíz): `{followerUid, followedUid, createdAt}`; id `{followerUid}_{followedUid}` (determinista, idempotente). Create/delete solo si `followerUid == auth.uid`.
- **`feed/{id}`**: `{type: "milestone"|"post", authorUid, text?, gameId?, gameName?, gameCover?, milestoneType? ("completed"|"review"|"list_public"), rating?, likesCount, createdAt}`.
  - Hitos: id determinista `{uid}_{milestoneType}_{gameId}` → dedupe gratis al re-completar/re-reseñar.
  - Posts: id aleatorio, `text` 1–280 validado en reglas.
  - Create solo del autor; lectura autenticada; update restringido a `likesCount` por increment; delete solo el autor.
- **`feed/{id}/likes/{uid}`**: me gusta idempotente por uid; `likesCount` con `FieldValue.increment(±1)` en la misma vía de escritura... en dos pasos (set + update) aceptando el retraso visual de un tick.
- **`blocks/{blockerUid}/people/{blockedUid}`**: create/delete solo del blocker. **`reports/{id}`**: create-only con auth (nadie lee reports desde cliente).
- **Visibilidad:** `users/{uid}.isPrivate: Boolean` (default false). `library` y `stats` legibles si `!isPrivate || isOwner` mediante `get()` al perfil; `sessions`/`logs`/`messages` siguen siempre privados; `profile_images` sigue autenticado.
- Contadores de seguidores/seguidos: **aggregate queries count()** en cliente, nunca escrituras cruzadas de perfiles.

## Tareas (mapean T2.1–T2.9 del fichero de fase)

1. Reglas de Firestore nuevas + suite ampliada (35 → ~55 checks) y despliegue en verde.
2. `SocialRepository` (follow/unfollow, contadores, feed por chunks de 30, búsqueda por prefijo de username ≥3 chars, listas, likes, blocks, reports) + milestones escritos desde los puntos de acción existentes.
3. Lógica pura + tests: `FollowLogic`, `FeedQueryPlanner`, `VisibilityLogic`, `ListProgress`, `PostComposer` (~+25 unitarios → ~113).
4. UI perfil público `profile/{uid}` (cabecera, seguir, reportar/bloquear, Top 4, stats, listas, biblioteca) + Top 4 editable + listas con progreso.
5. Feed con composer de posts + tarjetas de hito/post + me gusta optimista + snapshots en vivo; búsqueda en `SocialScreen` (pestañas Feed/Buscar); chat oculto con flag.
6. Seguir/bloquear/reportar wired; script `scripts/migracion-social/` (dry-run por defecto, backup en `.secrets/`, `--commit`).
7. Segunda cuenta QA + CA2.1–CA2.10 verificadas; REST sin autenticar (CA2.6/CA2.4); `testDebugUnitTest lintDebug`; §4 del AGENTS.md del repo regenerado; auditoría de cierre F2.

## Criterios de aceptación

Del fichero de fase (CA2.1–CA2.7) más: **CA2.8** un post con texto se publica y recibe me gusta de la otra cuenta; **CA2.9** bloqueado no puede ver el perfil del bloqueador ni interactuar; **CA2.10** el reporte queda almacenado (verificado vía service key).

## Riesgos controlados

- **Sin Blaze:** feed por chunks `whereIn` (límite duro de la disyunción: 30), búsqueda por prefijo con range query. Documentado el camino de migración a Functions.
- **Migración T2.9:** backup completo en `.secrets/` y dry-run por defecto.
- **Moderación:** reportar/bloquear cubre reseñas y posts desde el día uno.
- **Coste de `get()` en reglas:** asumido a esta escala (9 usuarios); revisar si crece.
