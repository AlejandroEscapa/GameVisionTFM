# Plan — Zanjado de F1 + F2 antes de abrir F3 (30/09/2026)

> **Aprobado por el propietario:** 30/09/2026. Alcance: dejar **cero flecos accionables** de F1 y
> F2 antes de abrir F3, incluidos los bloques de deuda que ya tenían disparador asignado.
> Origen: [informe de calidad](../metodologia/informe-calidad-2026-09-29.md), auditorías de F1/F2,
> [plan de deuda](DEUDA-TECNICA-2026.md) y la verificación E2E de F2 del 30/09.

## Diagnóstico (lo que queda)

**Verificado el 30/09 (no requiere acción):** los 7 defectos del informe de calidad están cerrados —
migración ejecutada (9 usuarios), suite de reglas con cobertura de `email_index`/`usernames`
(**94 checks**), `WriteBatch` en perfil/índice, `dry-run` por defecto y **backup antes de borrar** en
`migrate-uid.js`, literales «Amigo»/«Autor» fuera, `currentEmail` eliminado y `getFriends` sin N+1.

**Pendiente accionable:**

1. **Verificación de F2** (auditoría 🟡, bloque 6): CA2.4 con lista privada por UI; instrumentados de
   Social (like, bloqueo y **hito de reseña**); offline/red en Social; «Cargando perfil…» eterno en
   el perfil privado.
2. **Bloque 5 de la deuda** (higiene UI/Compose, abierto desde la auditoría de UI de F1): D-U1…D-U4.
3. **D-C2**: partir `DDBBViewModel` (191 líneas, 12 consumidores, sin `LibraryViewModel`).
4. **Flecos**: CA1.1 cronometrado; backup de la migración `uid` fuera de `.openclaw/tmp`; D0.5, D-C3,
   `AGENTS.md` y checklists al día; `check-docs` en CI.
5. **Consola (propietario)**: índice compuesto del feed, SHA-1 de release y copia del keystore.

## Bloques

### Bloque 0 — Plan por escrito

- [x] Crear y commitear este documento **antes de tocar código** (regla del workspace).

### Bloque 1 — Cerrar la verificación de F2

- [x] **1.1 CA2.4 por UI**: creadas por UI con c1 «QA privada F2» (privada) y «QA publica F2»
  (pública); admin confirma su ubicación en `gamelist_private` vs `gamelist`; desde c2 el perfil
  de c1 muestra «Listas (1)» solo con la pública. Se conservan ambas listas como evidencia QA.
- [x] **1.2 Aviso de perfil no disponible**: `_publicProfileError` en `SocialViewModel` (runCatching
  en la lectura) + `NotAvailableNotice` en `PublicProfileScreen`. **Bug crítico cazado:** abrir el
  perfil del bloqueador **crasheaba** la app (`PERMISSION_DENIED` no controlado); verificado crash con
  APK viejo y aviso sin crash con el nuevo. Bonus: `searchByUsername` ya no se tumba entera si un
  perfil individual está denegado. Cierra la deuda 3 de F2.
- [x] **1.3 Offline/red en Social**: banner «Sin conexión · mostrando datos guardados» en el feed
  (`rememberIsOnline()` en `ui/designsystem/components/Connectivity.kt`; `ACCESS_NETWORK_STATE`;
  exige red **validada** — onAvailable sin validar daba falso online). E2E con avión real:
  caché sin crash, like sincroniza al reconectar, post offline falla con mensaje, **conserva el
  texto** y no se encola. Cierra el punto 6 de la deuda de F2.
- [ ] **1.4 Instrumentados Compose de Social** (`app/src/androidTest`): like en un post, bloqueo filtra
  el feed y **hito de reseña** (ficha → reseña → tarjeta en el feed). Cuentas QA + limpieza posterior
  con admin. Cierra el punto 5 de la deuda de F2. *(después del split D-C2)*
- [ ] **1.5** Verificar el índice del feed cuando el propietario lo cree; conservar el fallback D2.8
  como resiliencia.

### Bloque 2 — Split D-C2 (base limpia antes de F3)

- [x] Partir `DDBBViewModel` → **`LibraryViewModel`** (biblioteca, diario, stats, HLTB, recientes,
  analítica) y mover **amigos/mensajes** al social (`SocialViewModel`; `profileExists`, sin
  consumidores, se retiró — vive en `UserRepository`). `DDBBViewModel` eliminado.
- [x] Renombrar los 12 consumidores **sin cambios de comportamiento** (MainActivity, NavHost, News
  y PublicProfile pierden el parámetro muerto; FriendsComposables pasa al social; `SocialCard`,
  sin llamadas, re-apuntada al social).
  *Evidencia (30/09):* compileDebugKotlin, assembleDebug, testDebugUnitTest (118/118) y lintDebug
  en verde; smoke E2E en emulador — Social, Perfil, Estadísticas, Seguidos (amigos), Diario y
  GameList abren sin crash (logcat limpio).

### Bloque 3 — Higiene UI/Compose (Bloque 5 de la deuda)

- [ ] **D-U1**: `GameCover` de `SubcomposeAsyncImage` → `AsyncImage` con `placeholder`/`error`;
  comprobar scroll en GameCard, Search, GameList, Diary y Details.
- [ ] **D-U2**: `ui-tooling` solo en `debugImplementation`; merged manifest de release sin `PreviewActivity`.
- [ ] **D-U3**: `collectAsState` → `collectAsStateWithLifecycle` (+ `lifecycle-runtime-compose` en el catálogo).
- [ ] **D-U4**: `AGENTS.md` del repo con las cifras reales, la ruta `ui/designsystem/GVTheme.kt` y el
  stack histórico marcado como tal.
  *Evidencia:* debug + release (R8), unitarios, lint y manifest verificados.

### Bloque 4 — Flecos de F1 y documentación

- [ ] **CA1.1 cronometrado**: buscar → estado → guardar en < 60 s (3 repeticiones con tiempos);
  marcar CA1.1 y cerrar la reserva de la auditoría de F1.
- [ ] **Backup de la migración `uid`** → `.secrets/migracion-uid-backup/` (10 ficheros verificados).
- [ ] **Docs**: D0.5 ✅ (cerrada en F2/D2.1); reserva `storage.rules` obsoleta por ADR-0007; D-C3
  resuelto por F2; nota de «checklist histórico» en las auditorías de F0/F1.
- [ ] **`check-docs` en CI**: incluirlo en el workflow si pasa con los ficheros del repo; si depende
  del workspace, queda como script manual y documentado.
- [ ] **Registro** en [DEUDA-TECNICA-2026.md](DEUDA-TECNICA-2026.md): Bloque 5, D-C2 y D-C3 cerrados;
  tabla de fases al día.

### Bloque 5 — Cierre

- [ ] Suite completa: `testDebugUnitTest`, `connectedDebugAndroidTest`, reglas, lint, debug y release.
- [ ] Commits temáticos + push (`dpapi`).
- [ ] Validación del propietario de la auditoría de F2 (→ ✅).

## Tareas del propietario (consola Firebase)

- Crear el índice compuesto `feed(authorUid ASC, createdAt DESC)`; el agente deja el paso a paso y
  verifica después por REST.
- Añadir el **SHA-1 de release** en Firebase Auth → Google (el agente lo calcula del keystore).
- Copiar `gamevision-release.jks` + credenciales a un gestor de secretos.

## Fuera de alcance (con disparador)

Moderación de reportes (F4), Nav3 y Baseline Profiles (F4), i18n, mapping de Crashlytics (F5),
`DESIGN.md`/design system nuevo (F4.5/ADR-0009), App Check/backups/presupuesto (F5) y la reescritura
del runner de reglas + suite de integración Firestore (P2, cuando se toquen reglas).

## Riesgos

1. **D-C2** es el único refactor grande → commits granulares y suite verde como red de seguridad.
2. **Instrumentados** escriben en producción (hito de reseña) → cuentas QA y limpieza con admin.
3. **Offline** puede destapar un estado ambiguo → alcance mínimo (mensaje claro), sin rediseño.
4. **`check-docs` en CI** puede no pasar con rutas del workspace → no bloquea el cierre.

## Orden de ejecución

**0 → 1.1–1.3 → 2 (D-C2) → 3 (higiene UI) → 1.4 (instrumentados, con el árbol estable) → 4 (flecos y
docs) → 5 (cierre + validación).** F3 no arranca hasta cerrar esto y debatir D3.1–D3.6.
