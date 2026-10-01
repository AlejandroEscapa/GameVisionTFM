# Plan de Deuda Técnica — GameVision (2026)

> **Creado:** 28/09/2026 · **Estado:** en ejecución (se actualiza al cerrar cada bloque)
> **Método:** inventario completo → priorización (`impacto × riesgo ÷ esfuerzo`) → bloques acotados
> con criterio de cierre y **evidencia verificable**.
> **Contexto:** nace al cerrar el bloque B2 de F0-B (biblioteca nueva). Recoge la deuda encontrada
> durante la sesión y la ya conocida, ordenada para pagarla **antes de arrancar F1**, con las
> piezas que bloquean la publicación marcadas para F5.

---

## 1. Principios de ejecución (criterio senior)

1. **La deuda se paga en ventanas acotadas**, no "en ratos sueltos": cada bloque es una sesión
   (o dos), con criterio de cierre y evidencia (build + tests + verificación en emulador/captura).
2. **Orden por riesgo:** primero lo que protege **datos y accesos** (seguridad), después lo que
   protege la **velocidad de trabajo** (CI e higiene), después el **futuro** (toolchain, release).
3. **Nada de refactors sin disparador:** lo que puede esperar se queda con una **condición
   explícita** de revisión; no hay "algún día".
4. **Todo pasa por el GDF:** Definition of Done, commits convencionales y — si es una decisión
   cara de revertir — ADR.
5. **Cero deuda nueva por prisa:** antes de añadir features (F1), se cierra esta lista; así F1 se
   construye sobre base limpia.

---

## 2. Inventario de deuda

### 2.1 Seguridad — resuelto hoy; residual documentado

| ID | Deuda | Estado |
|---|---|---|
| **D-S1** | Reglas de Firestore desplegadas demasiado abiertas (perfil público sin login; cualquier usuario autenticado podía acceder por comodín a los datos de cualquier otro) | ✅ **Resuelto (28/09)**: reglas nuevas publicadas por el propietario y **verificadas desde la app** (login, lectura y escritura de biblioteca). Los flujos sociales quedaron revisados: el borrado del muro ya estaba guardado para mensajes propios |
| **D-S2** | Verificación **negativa** de las reglas | ✅ **Resuelto (28/09)**: [`firebase-tests/`](../../firebase-tests/README.md) con Emulator Suite — **17/17 OK**, incluidos todos los negativos (acceso cruzado denegado, perfiles protegidos, listas antiguas cerradas) |
| **D-S3** | `docs/firebase-setup.md` desactualizado | ✅ Refrescado (28/09): «Estado actual» arriba e historial marcado |

### 2.2 Cierre de F0-B (paso B3)

| ID | Deuda | Notas |
|---|---|---|
| **D-B3-1** | Datos de prueba antiguos: listas viejas (`playedlist`/`wishlist`/`favorites`/`history`), basura (`aa`, `ee`), cuentas de prueba | ✅ **Ejecutado (29/09)**: backup JSON + borrado (12 subcolecciones + `aa`/`ee`) con verificación ✅ — [`scripts/b3-limpieza/`](../../scripts/b3-limpieza/README.md) |
| **D-B3-2** | Métodos muertos de las listas antiguas en `UserRepository` (sin uso tras B2) | ✅ Eliminados (28/09) |
| **D-B3-3** | Reunificación de ramas: `ui-redesign-2026` vs `master` | ✅ **Resuelto (29/09)**: `ui-redesign-2026` fusionada a `master` por fast-forward (decisión del propietario) |

### 2.3 Código (higiene)

| ID | Deuda | Notas |
|---|---|---|
| **D-C1** | API adaptativa deprecada en `BottomBarNavigation` | ✅ Migrada a la API V2 (`currentWindowAdaptiveInfoV2` + `isWidthAtLeastBreakpoint`) el 28/09 |
| **D-C2** | `DDBBViewModel` es una fachada legacy (nombre "DDBB"; mezcla biblioteca y social) | ✅ **Resuelto (30/09)**: partida en `LibraryViewModel` + `SocialViewModel`; `DDBBViewModel` eliminado; 12 consumidores re-apuntados sin cambios de comportamiento ([CIERRE-PRE-F3](CIERRE-PRE-F3-2026.md), bloque 2) |
| **D-C3** | Botón "Me gusta" del timeline es decorativo (no hace nada) | ✅ **Resuelto por F2**: reacciones reales (`setLike`/`hasLiked`/`likedByMe` + contador), con instrumentados |
| **D-C4** | Avisos de compilación menores | ✅ A cero (28/09); incluidos los tests instrumentados migrados a las APIs v2 de Compose Test |
| **D-C5** | Strings hardcodeados en español (i18n) | **Diferido**: disparador = plantear multiidioma |

### 2.4 Calidad e infraestructura

| ID | Deuda | Notas |
|---|---|---|
| **D-Q1** | Sin CI | ✅ Workflow creado (28/09): 2 jobs — build+tests unitarios y tests de reglas |
| **D-Q2** | Reglas de Firestore sin tests automatizados | ✅ **17/17 OK** en local (28/09); corre también en CI |
| **D-Q3** | Tests instrumentados nunca ejecutados | ✅ Ejecutados (28/09): **7/7 verdes** en el emulador (incl. smoke nuevo) |
| **D-Q4** | Changelog manual | Opcional: autogenerar desde commits convencionales |
| **D-Q5** | Backups de Firestore sin configurar | Se cierra junto a F5 (con alertas de presupuesto y App Check) |

### 2.5 Toolchain

| ID | Deuda | Notas |
|---|---|---|
| **D-T1** | Kotlin 2.2.10 vs 2.4.x; Coil/googleid congelados | ✅ **Cerrado (29/09)**: Kotlin 2.4.20 (KGP externo) + Coil 3.6.3 + googleid 1.2.1 vía palancas de transición; verificado (30+7+release). Retorno a built-in cuando AGP ≥9.5/10 — checklist en [ADR-0004](../metodologia/adr/0004-upgrade-toolchain.md) |
| **D-T2** | Hilt 2.59.2 vs 2.60.1 | ✅ Subido a 2.60.1 (+ KSP 2.3.12) el 28/09, verificado con build y tests |
| **D-T3** | Nav2 vs Nav3 | **Diferido con disparador**: cuando F4 necesite lista-detalle/tablet |
| **D-T4** | Sin Baseline Profiles | **Diferido**: F4, con runtime estable |

### 2.6 Release

| ID | Deuda | Notas |
|---|---|---|
| **D-R1** | Release sin firmar | ✅ **Hecho (29/09)**: keystore RSA-4096 + firma; `assembleRelease` verificado con apksigner e instalado/arrancado en emulador. Ver [`release-signing.md`](../release-signing.md) |

---

### 2.7 UI / Compose (higiene)

| ID | Deuda | Notas |
|---|---|---|
| **D-U1** | `GameCover` usa `SubcomposeAsyncImage` (dentro de listas: GameCard, Search, GameList, Diary, Details) | ✅ **Resuelto (30/09)**: `AsyncImage` con `placeholder`/`error` monocromo (iniciales con `TextMeasurer`) |
| **D-U2** | `ui-tooling` en `implementation` (además de `debugImplementation`) | ✅ **Resuelto (30/09)**: solo `debugImplementation`; merged manifest de release sin `PreviewActivity` (verificado con aapt) |
| **D-U3** | 40 `collectAsState` y 0 `collectAsStateWithLifecycle`; falta `lifecycle-runtime-compose` en el catálogo | ✅ **Resuelto (30/09)**: 52 usos migrados (0 restantes) + dependencia en el catálogo |
| **D-U4** | `AGENTS.md` desfasado (6.900 líneas / 36 archivos / 12 rutas → 10.513 / 79 / 14) y cita `ui/theme/Theme.kt`, que no existe | ✅ **Resuelto (30/09)**: cifras reales y ruta `ui/designsystem/GVTheme.kt`; `03-stack-android-2026.md` marcado DOCUMENTO HISTÓRICO |

### 2.8 UI / adopción del design system (auditoría del 02/10/2026)

> Origen: [auditoría de diseño 2026-10-02](auditoria-diseno-2026-10-02.md) (43 ficheros de `ui/`,
> ~11.000 líneas). **Diagnóstico:** los tokens están bien construidos; **19 ficheros maquetaban a mano
> al margen del sistema**. La primera pasada ya se aplicó (sombras, superficies, colores literales,
> copy, emoji, contraste y tamaño táctil), además de cuatro bugs reales.

| ID | Deuda | Notas |
|---|---|---|
| **D-U5** | **Margen de pantalla sin unificar**: Login/Pass/Register/Onboarding a 24, el perfil a 20, Social a 8 y Amigos a 10, con `GVSpacing.screenPadding = 16` | ⬜ Pendiente. Dentro de `EditProfileScreen` el título va a 16 y sus tarjetas a 24: **no comparten línea**, que es la primera regla de `GVScreenHeader`. Relleno de tarjeta también variable (12/16/20/24) |
| **D-U6** | **68 `RoundedCornerShape(n.dp)` a mano** (5/12/16/20/24/32) frente a los 3 tokens de `GVShapes` | ⬜ Pendiente. Las tarjetas de las pantallas de entrada (32 dp) son las más visibles |
| **D-U7** | **La Ficha enseña `e.message` al usuario en 7 sitios** (`GameDetails`), más Amigos, Diario y Social | ⬜ Pendiente. Es el hallazgo **H1** que ya se corrigió para el catálogo: la lección se aplicó en un ViewModel y **no** en las pantallas. Pide un `Throwable.aMensajeUsuario()` |
| **D-U8** | **Estados vacíos esquivados**: Social y Buscar pintan un `Text` suelto; Biblioteca y Diario usan `icon = Lucide.X` (una ✕ significa cerrar, no "aquí no hay nada") | ⬜ Pendiente. `EmptyState` con icono contextual y **acción** opcional |
| **D-U9** | **`RatingStars` son cinco nodos** ("1 estrellas", "2 estrellas"…) para expresar una nota de 4,5 | ⬜ Pendiente. Debe ser **un** nodo con `progressBarRangeInfo` |
| **D-U10** | **`Switch`/`RadioButton` de M3 sin estilizar** (Ajustes, Editar perfil, diálogos) contra un lenguaje tipo iOS | ⬜ Pendiente. Pide `GVSwitch`/`GVRadio` |
| **D-U11** | **Componentes duplicados**: 3 tarjetas de juego (una **muerta**: `designsystem/GameCard.kt`), 2 de noticia (`NewsCard.kt` sin usar), 5 cabeceras de sección | ⬜ Pendiente (refactor mayor). Anotado además en `DESIGN.md` §6 para que nadie lo "arregle" ni lo adopte sin decidir |
| **D-U12** | **`GVMotion` casi sin usar**: solo el dock y las transiciones del NavHost | ⬜ Pendiente — es materia de **F4.5** (bloque B), no de un barrido |
| **D-U13** | `NewsScreen` mete "Cargando noticias..." junto a los skeletons; barras de progreso hechas a mano con dos `Box` (Estadísticas) | ⬜ Pendiente (pulido) |
| **D-U14** | Parámetros sin uso: `EditProfileScreen(isDarkTheme)`, `GameListEmptyState(selectedList)` | ⬜ Pendiente (higiene) |

---

## 3. Plan por bloques

### Bloque 0 — Seguridad y cierre de F0-B ✅ (cerrado 29/09/2026)
**Incluye:** D-S1 ✅ (hecho), **B3** (D-B3-1: backup → borrado → usuario nuevo verificado),
decisión de ramas (D-B3-3).
**Criterio de cierre:** datos de prueba fuera; usuario nuevo con flujo completo verificado;
decisión de ramas escrita.
**Evidencia:** consulta de consola + E2E del usuario nuevo.

**Guion B3 (lo ejecuta el propietario — consola/Admin):**
1. **Backup**: Firestore → Backups (crear uno) o `gcloud firestore export`.
2. **Borrar listas antiguas** en cada `users/{email}` antiguo: subcolecciones `playedlist`,
   `wishlist`, `favorites`, `history`; y los documentos basura `aa` y `ee` (raíz de `users`).
3. **Cuentas de prueba**: revisar Authentication (las `qa.*` se pueden borrar).
4. **Usuario nuevo**: registro desde la app → añadir juego → favorito → comprobar pestañas.
5. **Verificación final**: revisar en consola que no quedan `playedlist/wishlist/favorites/history`.

**Ramas (dato para decidir):** `ui-redesign-2026` está **21 commits por delante** de `master` y
**0 por detrás** → la fusión sería limpia (fast-forward). Recomendación: PR de cierre al terminar F0.

**Herramienta lista:** [`scripts/b3-limpieza/`](../../scripts/b3-limpieza/README.md) — dry-run +
`--commit` con backup JSON previo; solo falta que el propietario deje la clave de servicio.

**Resultado (29/09/2026):** B3 **ejecutado** (backup + 12 subcolecciones + `aa`/`ee`, verificación ✅);
ramas **fusionadas** (`master` actualizado por fast-forward, decisión del propietario); E2E con la
cuenta QA ya verificado (28/09). Push/CI pospuesto a petición del propietario (documentado).

### Bloque 1 — Higiene rápida ✅ (28/09/2026)
**Incluye:** D-C1 (API adaptativa V2), D-C4 (avisos), D-B3-2 (código muerto), D-S3 (docs Firebase).
**Resultado:** API adaptativa migrada a `currentWindowAdaptiveInfoV2()` + `isWidthAtLeastBreakpoint`
(window-core 1.5); **avisos de compilación a cero** (incluida la anotación de GoogleViewModel y los
tests instrumentados migrados a las APIs v2 de Compose Test); 4 métodos muertos eliminados de
`UserRepository`; `docs/firebase-setup.md` refrescado.
**Evidencia:** compilación sin warnings + **30 tests unitarios verdes**.

### Bloque 2 — Calidad y CI ✅ (28/09/2026)
**Incluye:** D-Q1 (GitHub Actions), D-Q2 (tests de reglas; cierra D-S2), D-Q3 (smoke instrumentado).
**Resultado:** workflow [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) con 2 jobs
(build+tests unitarios; tests de reglas con Emulator Suite) y dummy de `google-services.json` para CI;
[`firebase-tests/`](../../firebase-tests/README.md) con **17/17 tests de reglas OK** (incluidos los
negativos); tests instrumentados ejecutados por primera vez: **7/7 verdes** (incluido el smoke nuevo
`SmokeTest`). **Resuelto (29/09)**: el primer run real falló por dos causas (bit de ejecución de
  `gradlew`; JDK <21 en el emulador de reglas) **diagnosticadas con logs y corregidas** (`296241e`);
  el **segundo run quedó VERDE** (build+tests y reglas):
  https://github.com/AlejandroEscapa/GameVisionTFM/actions/runs/36491049211

### Bloque 3 — Toolchain Kotlin (+Hilt) ✅ (cerrado 29/09/2026)
**Resultado:** Kotlin **2.4.20** (KGP externo), **Coil 3.6.3** y **googleid 1.2.1** aplicados con
las palancas de transición `android.builtInKotlin=false` + `android.newDsl=false`
(ver «Actualización» de [ADR-0004](../metodologia/adr/0004-upgrade-toolchain.md)). Verificado:
30 unitarios + 7 instrumentados + `assembleRelease` firmado — todo verde. Micro-mejoras previas
(Hilt 2.60.1, KSP 2.3.12) incluidas.
**Deuda de retorno:** quitar las dos palancas + KGP cuando haya AGP estable con Kotlin integrado ≥2.4.

### Bloque 4 — Release y firma ✅ (29/09/2026)
**Resultado:** keystore RSA-4096 creado en `.secrets/` (fuera del repo); `assembleRelease` firmado,
**verificado con apksigner** (SHA-256 coincide con el keystore) e **instalado + arrancado en el
emulador**. Guía completa: [`docs/release-signing.md`](../release-signing.md).
Pendientes menores: copia del keystore/credenciales en gestor de secretos + añadir el SHA-1 de
release en Firebase (Google Sign-In en builds release).

### Bloque 5 — Higiene UI/Compose ✅ (cerrado 30/09/2026)
**Alcance:** D-U1 (GameCover → `AsyncImage`), D-U2 (`ui-tooling` solo en debug), D-U3
(`collectAsStateWithLifecycle` + `lifecycle-runtime-compose`), D-U4 (sincronizar `AGENTS.md` y marcar
el stack histórico). Origen y evidencia: [auditoría UI 2026](ui-adopcion-hallazgos-2026.md).
**Resultado:** D-U1…D-U4 aplicados ([CIERRE-PRE-F3](CIERRE-PRE-F3-2026.md), bloque 3): `AsyncImage`
con placeholders monocromo, `ui-tooling` fuera de release (manifest verificado con aapt),
52 `collectAsState` migrados a `WithLifecycle`, `AGENTS.md` con cifras reales.
**Verificación:** build debug/release + tests (118/118) + lint en verde.

### Bloque 6 — Adopción del design system ⬜ (abierto 02/10/2026, parcialmente pagado)
**Alcance:** D-U5…D-U14, por orden de impacto × esfuerzo del
[informe de auditoría](auditoria-diseno-2026-10-02.md).
**Ya pagado en la primera pasada (02/10):** buscador único (`GVSearchField`) + búsqueda en vivo ·
**crash** de `substring` en Buscar · `isDarkTheme!!` en la portada · botón inerte del feed ·
«me gusta» sin estado visible · 14 sombras fuera · `surfaceVariant` → `surfaceContainer` ·
colores literales a roles · copy en español y emoji fuera · contraste del feed y del dock ·
tamaños táctiles bajo 48 dp · alcance del acento ([ADR-0012](../metodologia/adr/0012-alcance-del-acento.md)).
**Criterio de cierre:** cero `RoundedCornerShape` fuera de `GVShapes`, cero margen de pantalla fuera
de `screenPadding`, cero `e.message` al usuario, cero componente duplicado y `DESIGN.md` sin deuda
declarada en §6 y §7.
**Evidencia:** `testDebugUnitTest` + `lintDebug` + `assembleDebug` en verde y recorrido en emulador
con capturas.

### Diferidos con disparador (no se tocan ahora)
| Deuda | Disparador |
|---|---|
| Nav3 (D-T3) | F4: lista-detalle / tablet |
| Baseline Profiles (D-T4) | F4: runtime estable |
| App Check, alertas de presupuesto, backups (D-Q5) | Preparación de F5 |
| i18n (D-C5) | Decisión de multiidioma |
| Auditoría de costes (investigación F0-B §6) | Acercarse a ~1.000 usuarios activos |

---

## 4. Fuera de alcance (es roadmap, no deuda)

T0.12 (foto de perfil en Storage), T0.13 (FCM), T0.14 (persistencia offline), import/export y todo
F1+ son **trabajo planificado** en el [roadmap](../roadmap/README.md). No se mezclan aquí para no
inflar la mochila: deuda = riesgo acumulado; roadmap = producto pendiente.

---

## 5. Registro

- **02/10/2026 — auditoría de diseño y Bloque 6 abierto.** Se audita la adopción del design system
  ([informe](auditoria-diseno-2026-10-02.md)): **19 ficheros maquetaban a mano** al margen de los
  tokens. Primera pasada aplicada y **cuatro bugs reales cerrados** —uno de ellos un **crash** en la
  pantalla Buscar (`substring` sobre un campo que RAWG deja vacío, que dejaba la lista entera sin
  pintar) y un `!!` sobre el tema en la portada, que es el `startDestination` del NavHost. Se fija el
  **alcance del acento** en [ADR-0012](../metodologia/adr/0012-alcance-del-acento.md) y **DX.8 de
  F4.5 se cierra** (el re-anclaje se adelantó, ADR-0010). Entra la deuda **D-U5…D-U14** (Bloque 6).
- **30/09/2026 (madrugada del 01/10, cierre pre-F3 completado)** — **D-C2 cerrada** (split de
  `DDBBViewModel`), **Bloque 5 cerrado** (D-U1…D-U4), **D-C3 cerrada** (reacciones reales de F2),
  D0.5 anotada como cerrada en fase-0 (seguir asimétrico), backup de la migración `uid` copiado a
  `.secrets/migracion-uid-backup/` (10 ficheros), auditoría de F1 anotada como validada
  (deriva de fecha corregida) y `check-docs` integrado en CI. Plan de optimización del ciclo de
  verificación creado: [optimizacion-ciclo-verificacion-2026.md](optimizacion-ciclo-verificacion-2026.md)
  (D-OP1/2/3 cerradas con el propietario).
- **30/09/2026 (noche, cierre pre-F3) — deudas 3 y 6 de F2 cerradas.** Aviso de perfil no
  disponible (`NotAvailableNotice`; de paso se corrigió un **crash** al abrir el perfil del
  bloqueador y una búsqueda que se tumbaba con perfiles denegados) y estados offline en Social
  (banner de red validada + verificación E2E en avión: caché, like que sincroniza, post con
  mensaje claro). CA2.4 verificado también por UI. **Instrumentados de Social** (like, bloqueo
  que filtra el feed e hito de reseña): 10/10 conectados. Detalle en
  [CIERRE-PRE-F3-2026.md](CIERRE-PRE-F3-2026.md).
- **30/09/2026 (noche) — F2 ejecutada y auditada.** Se retoma la fase tras la pausa y se
  completa la verificación E2E con dos cuentas (CA2.1–CA2.10), el bloqueo REST sin autenticar
  (403 en listados) y la suite completa (118 unitarios · lint limpio · 94 checks de reglas ·
  debug y release). Deuda abierta **con disparador** en
  [fase 2, «Deuda técnica»](../roadmap/fase-2-social.md#deuda-técnica-con-disparador):
  crear el índice compuesto `feed(authorUid, createdAt)` desde consola (la service account no
  puede, 403), aviso visual de perfil privado, instrumentados de Social y estados offline.
  Fase a la espera de **validación del propietario**.
- **28/09/2026** — Plan creado. D-S1 resuelta y verificada (positivos). Bloque 0 en curso.
  Pendiente inmediato: B3 (limpieza) y decisión de ramas.
- **28/09/2026 (noche)** — **Bloque 1 completado** (API adaptativa V2 · avisos a cero · código muerto
  fuera · docs Firebase al día) y **Bloque 2 completado** (CI con 2 jobs · **reglas 17/17** ·
  **instrumentados 7/7**). Bloque 0: guion B3 listo; ramas listas para fast-forward. Único pendiente
  del tramo: ejecutar B3 (propietario) y ver el primer run de CI al hacer push.
- **29/09/2026 (madrugada)** — **B3 ejecutado** (backup JSON + limpieza de 12 subcolecciones y
  `aa`/`ee`, verificación ✅). **`master` actualizado** por fast-forward (decisión del propietario).
  Push/CI pospuesto a petición del propietario. **Bloque 0 cerrado.**
- **29/09/2026 (madrugada, 2ª parte)** — **Bloque 4 cerrado** (release firmado: keystore + firma +
  verificación apksigner + instalado/arrancado en emulador). **Ramas limpiadas**: `ui-redesign-2026`
  y `upgrade-2026` borradas (local y remoto); queda solo `master`. **Push hecho** y **CI VERDE**
  (segundo run; el primero falló por dos causas ya corregidas).
- **29/09/2026 (madrugada, 3ª parte)** — **Bloque 3 APLICADO y cerrado**: Kotlin 2.4.20 (KGP externo)
  + Coil 3.6.3 + googleid 1.2.1 vía `android.builtInKotlin=false` + `android.newDsl=false`;
  verificación completa en verde (30 unitarios + 7 instrumentados + release firmado). Detalle en
  «Actualización» de ADR-0004. **Push hecho y CI VERDE** (run del 29/09, ambos jobs).
- **29/09/2026 (noche)** — **Auditoría UI 2026 registrada y planificación actualizada.** Se contrasta
  una reflexión externa con el repo real ([auditoría UI](ui-adopcion-hallazgos-2026.md)): 9 hallazgos
  confirmados y 2 matices; prioridad P0 (higiene Compose) / P1 (arquitectura de UI) / P2 (paginación).
  Se abre **Bloque 5 — Higiene UI/Compose** (D-U1…D-U4). Se actualizan **F4** (superficie de
  descubrimiento, rutas tipadas, Paging: T4.10–T4.12, D4.7, CA4.7) y **F4.5** (`UiState`, estados
  resueltos y re-anclaje). **Decisión del propietario:** GameVision usará un **design system nuevo**;
  `DESIGN.md` pasa a **provisional** → [ADR-0009](../metodologia/adr/0009-reanclaje-design-system.md),
  DX.8 y DX-T23…DX-T28.
