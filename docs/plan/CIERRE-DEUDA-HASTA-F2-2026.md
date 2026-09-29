# Plan — Cierre de deuda hasta F2 (verificación primero)

> Aprobado por el propietario el **29/09/2026**. Alcance: dejar F2 abrible limpio.
> Origen: [informe de calidad de 29/09](../metodologia/informe-calidad-2026-09-29.md).

## Contexto

F0 y F1 están hechas y verificadas E2E; el Bloque 5 (migración `uid`, ADR-0008) se ejecutó contra
datos reales con 9 usuarios migrados y 0 fallos. La auditoría del 29/09 encontró **7 defectos
abiertos**, y el patrón de fondo es claro: **el 70/70 verde no toca Firestore**. Los dos bugs que se
cazaron durante la verificación manual (perfil vacío por `currentEmail`, y el script fallando con
`usernames/QA Game Vision`) **ningún test los habría detectado**.

Prioridad: **verificación de datos primero**, después estructura, después la capa de agente del
workspace.

## Fase V1 — Verificación de datos (prioridad máxima)

- **V1.1 Suite de reglas de Firestore** 🔴 `firebase-tests/rules.test.mjs` tiene **0 menciones** de
  `email_index` y `usernames`, y dos checks legacy (`users/alice@test.dev`, `friends/bob@test.dev`).
  Añadir: lectura autenticada de `email_index`; escritura solo si `request.resource.data.uid ==
  request.auth.uid`; **borrado prohibido**. Ídem para `usernames/{username}`, incluyendo que el dueño
  **sí** pueda borrarlo (rename). Y el negativo clave: **un usuario NO puede escribir en
  `users/{otroEmail}`** — es el cambio que hizo `isOwner` y hoy nadie lo prueba. Actualizar los dos
  checks legacy a la forma por uid.
  *Evidencia:* `npm test` verde, y los nuevos checks **fallan** si se reintroduce el fallback por email.

- **V1.2 Tests de `UserRepository` con fake de Firestore** 🔴 177 líneas cambiadas, 0 cobertura.
  Cubrir el camino de escritura: `createProfile` escribe perfil + `email_index` + `usernames` con el
  email normalizado; `updateProfile` con `username` no deja índice fantasma; `findUidByEmail` devuelve
  `null` en blanco y sin índice.

- **V1.3 Tests de `SessionRepository`** 🟠 Registro con `createProfile` fallido → la cuenta se
  revierte **y no queda documento huérfano**; el SSOT expone `currentUid` como única identidad.

- **V1.4 Tests de `LibraryRepository`** 🟠 Contadores en lote (`gamesTotal`, `ratingSum`,
  `ratingCount`, `minutesTotal`, `sessionsTotal`).

- **V1.5 Verificación de reglas con el emulador** 🟡 Reutilizando `@firebase/rules-unit-testing`
  (ya instalado) para comprobar el modelo completo: perfil + amigos + mensajes + índices.

*Criterio de aceptación:* la suite cubre las tres colecciones de identidad; `UserRepository` y
`SessionRepository` tienen tests; `testDebugUnitTest` + `npm test` verdes; y un negativo falla
deliberadamente cuando se rompe la regla.

## Fase V2 — Estructura y código

- **V2.1 `migrate-uid.js`: invertir el seguro** 🔴 `const DRY = argv.includes('--dry-run')` hace que
  olvidar el flag **ejecute la migración y borre datos**. El precedente del repo (`b3-limpieza`) es
  dry-run por defecto, `--commit` para escribir. Cambiar a ese modelo.

- **V2.2 `updateProfile`: atomicidad + índice** 🟠 Sin `WriteBatch`: el perfil se escribe antes que
  el índice, así que un username ocupado deja los campos cambiados y la UI dice que no. No se borra
  `usernames/{viejo}` al renombrar → índice fantasma. Unificar el indexado en un solo sitio.

- **V2.3 `createProfile`: perfil huérfano** 🟠 La compensación borra la cuenta de Auth pero no el
  documento `users/{uid}`. Con `WriteBatch` desaparece de raíz.

- **V2.4 Regresión de UI** 🟠 `FriendsComposables.kt` → literal `"Amigo"`; `SocialScreen.kt` →
  literal `"Autor"` y avatar por `ownerUid.take(1)`. Debe mostrarse el `username`; `SocialScreen` ya
  tiene `currentFriends` (uid→username) sin usar.

- **V2.5 `currentEmail` muerto** 🟡 `UserViewModel.kt` lo expone sin consumidores tras la migración.
  Es la segunda fuente de identidad que el ADR-0008 vino a eliminar.

- **V2.6 `getFriends`: N+1** 🟡 Una lectura secuencial por amigo con el índice `usernames` sin usar.

*Criterio de aceptación:* `--commit` obligatorio para escribir; un rename no deja índice fantasma; la
UI distingue a dos amigos por nombre; `testDebugUnitTest`, lint 0 errores y `assembleDebug` +
`assembleRelease` OK.

## Fase V3 — La capa de agente del workspace

- **V3.1 Un solo dueño del estado** 🟡 El mismo dato en dos sitios ya ha mentido cinco veces. Regla
  nueva: **los resúmenes no declaran estado, enlazan al dueño**. El dueño del estado de una fase es
  su fichero.
- **V3.2 Poner al día lo desfasado** — `roadmap/README.md` y `PLAN-MAESTRO-2026.md` (F1
  "Bloque 1 hecho" → ✅ con Bloque 5); `mapa-gamevision.md` (rama `master`, KGP externo Kotlin
  2.4.20); `AGENTS.md` del repo §4 (árbol de `data/`: 11 paquetes, no 4).
- **V3.3 Mover los aprendizajes** de "Trampas del repo" del `AGENTS.md` del workspace al del repo.
- **V3.4 Backup de la migración fuera del repo** 🟠 `scripts/migracion-uid/migration-backup/` contiene
  datos reales de 9 usuarios. Mover a `.secrets/` + `.gitignore`.
- **V3.5 Commitear el informe de calidad** con la fecha de su revisión.

## Fase V4 — Cierre formal de F1

- **V4.1 Re-auditoría de F1 incluyendo ADR-0008** 🟠 La `auditoria-fase-1-2026.md` es anterior al
  Bloque 5, que cambió 13 ficheros y reescribió las reglas. Re-auditar según
  `docs/metodologia/auditoria-de-cierre.md`.
- **V4.2 Restaurar D-S2** — cuyo "17/17 definitivo" ya no prueba las reglas nuevas.
- **V4.3 Validación del propietario** (único paso no ejecutable por el agente).
- **V4.4 Push** — del propietario; GCM no autentica desde el agente.

## Fuera de alcance (a propósito)

- Reescribir `rules.test.mjs` como runner con descubrimiento automático → primero cobertura.
- `fallbackToDestructiveMigration` → correcto para una caché; documentarlo basta.
- Nav3, Baseline Profiles, App Check, i18n, `DESIGN.md` y F4.5 → todos con disparador asignado.

## Riesgos

1. **Colisión con el agente de AutoClaw**, que puede retomar. Mitigación: fases cerradas con commit
   propio; V3.1 es justamente lo que evita que dos agentes escriban el mismo estado.
2. **Las reglas ya están desplegadas** (release 200) contra datos reales: ningún cambio de
   `firestore.rules` se despliega sin la suite de V1.1 verde.
3. **El script de migración ya corrió en producción** con éxito; V2.1 es urgente por lo que queda
   reproducible, no por lo ya hecho.

## Orden de ejecución

**V1.1 → V1.2 → V2.1 → V2.2 → V2.3 → V2.4 → V1.3 → V1.4 → V1.5 → V2.5 → V2.6 → V3 → V4.**

La verificación urgente va primero, combinada con los tres defectos que requieren código, para no
tocar el mismo fichero dos veces.
