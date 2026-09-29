# UI 2026 — Auditoría de la reflexión externa y plan de adopción

> Qué: contraste de una reflexión externa (generada con Claude sobre el código empaquetado)
> con el estado real del repo, y decisión de qué se adopta, qué se aplaza y qué se descarta.
> Cuándo: **29/09/2026**, sobre `master` `0436882` (F1 en curso por un agente paralelo).
> Método: lectura estática por patrones (`Select-String` / `Get-Content`), **sin compilar ni ejecutar**.
> Documentos hermanos: [ui-redesign-2026-proposals.md](../ui-redesign-2026-proposals.md) (dirección visual
> decidida el 27/09) y [DESIGN.md](../../DESIGN.md) (fuente única de verdad visual).
> Este documento **no cambia la dirección visual**: solo ordena ingeniería y superficie de contenido.

---

## 1. Resumen ejecutivo

1. **La reflexión acierta en lo técnico y en el orden de urgencia.** De 9 hallazgos de ingeniería,
   9 se confirman (1 con matiz) contra el código actual. El orden propuesto (barato primero,
   estructural después) es el correcto.
2. **Pero diagnostica el problema al revés.** Afirma implícitamente que falta dirección visual. No es
   el caso: la dirección está decidida (27/09), hay `DESIGN.md` y existe `ui/designsystem/` con 14
   archivos. Lo que falta no es **identidad**, es **superficie de contenido** (rails, grids, paginación)
   y **modelado de estado** (`UiState`). La reflexión no menciona ninguno de los dos como hueco central.
3. **Parte del mapa está desactualizado** en dos puntos: `AGENTS.md` ya no tiene el stack viejo (§2 está
   en Kotlin 2.4.20 / Hilt 2.60.1 / Coil 3.6.3) y sí hay skills Android instaladas (26 en `.zcode/skills/`).
   Lo que sigue desfasado es el propio `AGENTS.md` en números y en un archivo fantasma.
4. **Balance de adopción:** 3 acciones P0 (bajo riesgo), 3 bloques P1 (arquitectura de UI, al abrir F4.5),
   1 acción P2, 5 descartes razonados. Nada de esto bloquea a F1/F4.
5. **Bug de documentación detectado y verificado** que sí afecta a agentes: `AGENTS.md` describe
   `ui/theme/Theme.kt`, que **no existe** (el tema vive en `ui/designsystem/GVTheme.kt`).

---

## 2. Verificación: hallazgo por hallazgo

Medido el 29/09/2026 sobre `app/src/main` (79 archivos `.kt`, 10.513 líneas) + build files + docs.

| # | Hallazgo de la reflexión | Medición real | Veredicto | Impacto en UI 2026 |
|---|---|---|---|---|
| 1 | `SubcomposeAsyncImage` en listas | `GameCover.kt:35` (2 coincidencias: import + uso). `AsyncImage` real: 1 (`NewsScreen.kt:167`) | **Confirmado** | **Alto** — `GameCover` es el componente que usan GameCard, Search, GameList y Diary; un solo archivo decide el coste de scroll de media app |
| 2 | Recolección sin lifecycle | `collectAsState(` = **40** en 16+ archivos · `collectAsStateWithLifecycle` = **0** · `lifecycle-runtime-compose` **no está** en el catálogo (solo `lifecycle-runtime-ktx 2.11.0`) | **Confirmado** | Medio — correctness y consumo en segundo plano |
| 3 | `ui-tooling` en release | `implementation(libs.compose.ui.tooling)` **+** `debugImplementation(libs.compose.ui.tooling)` | **Confirmado** | Medio — peso y superficie de release; verificar `merged manifest` |
| 4 | Rutas con strings, 0 tipadas | `composable("...")` = **14**; rutas `@Serializable` = **0**. (Los 17 `@Serializable` del repo son DTOs: `GameDAO`, `NewDAO`, `RecentGamesStore`) | **Confirmado** | Bajo/medio — ergonomía de navegación, no estética |
| 5 | ViewModels izados a mano | `hiltViewModel(` = **0** · `viewModel(` = **16** · `hilt-navigation-compose` ausente del catálogo | **Confirmado** | Bajo/medio — mantenibilidad del grafo |
| 6 | Restos de LiveData | `GoogleViewModel` (`LiveData`/`MutableLiveData` + `_signInState`) y `LoginScreen:48` (`observeAsState`) + `runtime-livedata` | **Confirmado** | Bajo — inconsistencia de modelo de estado |
| 7 | Sin tipos `UiState` | `UiState` = **0** · `mutableStateOf` = **50** | **Confirmado** | **Alto** — es lo que hace inconsistente loading/empty/error entre pantallas |
| 8 | Sin superficie de descubrimiento | `LazyRow` = **0** · `LazyVerticalGrid` = **0** · pager = **0** · `HomeScreen.kt` = 175 líneas **sin ninguna lista** | **Confirmado** | **Muy alto** — es el hueco real del producto |
| 9 | Sin paginación | `paging` **sin apariciones** en `app/build.gradle.kts` ni `libs.versions.toml` | **Confirmado** | Medio — escalado del catálogo |
| 10 | `AGENTS.md` desfasado | Números: dice "~6.900 líneas en 36 archivos" → real **10.513 / 79**; §5 dice "12 rutas" → **14**. Stack: §2**ya está actualizado** (Kotlin 2.4.20, Hilt 2.60.1, Coil 3.6.3). Archivo fantasma: §4:137 cita `ui/theme/Theme.kt`, que **no existe** (ver `Test-Path`) | **Parcial** | Alto para agentes, nulo para UI |
| 11 | "No vi ninguna skill instalada" | `.zcode/skills/` contiene **26** skills Android: `adaptive`, `styles`, `navigation-3`, `navigation-event`, `edge-to-edge`, `r8-analyzer`, `android-profiler`, `android-cli`, `testing-setup`, `agp-9-upgrade`… | **Falso hoy** | — |

**Nota sobre el punto 10:** el desfase que la reflexión atribuye al stack ya está corregido en
`AGENTS.md`. El documento que **sí** sigue con el stack viejo es
[03-stack-android-2026.md](../investigacion-2026/fuentes/03-stack-android-2026.md) (Kotlin 2.2.10,
Hilt 2.59.2, Coil 3.3.0), que ya es histórico: D-T1 y D-T2 se cerraron el 29/09.

---

## 3. Lo que ya está hecho (no volver a hacerlo)

Para evitar que un agente futuro "redescubra" trabajo entregado:

- **Dirección visual decidida** el 27/09 en [ui-redesign-2026-proposals.md](../ui-redesign-2026-proposals.md):
  *design-system-first cinematográfico con identidad editorial* — monocromo cálido + spot verde ácido
  `#C8F135`, Space Grotesk variable, física de muelles, portadas como protagonista.
- **`ui/designsystem/` operativo (14 archivos):** `GameVisionTheme`, `GVTypography`, `GVShapes`,
  `GVMotion`, `GVSharedTransition`, y los componentes `GameCard`, `GameCover`, `NewsCard`,
  `RatingBadge`, `FriendAvatar`, `GVSkeleton`, `EmptyState`, `GVButton`, `OfflineBanner`.
- **Base adaptativa y nativa:** `NavigationSuiteScaffold` con API V2 de ventana, edge-to-edge sin opt-out,
  Material 3 puro (Material 2 eliminado), R8 + lintVital, tests de reglas (17/17).
- **Skills Android instaladas** en `.zcode/skills/` (26).

**Consecuencia:** cualquier propuesta que empiece por "falta un design system" o "hay que definir la
identidad" es trabajo ya cerrado y debe rechazarse por defecto.

---

## 4. El hueco que la reflexión no vio (y es el que decide la UI 2026)

**Tesis: la app tiene los ingredientes, no el emplatado.**

- **Home no enseña el catálogo.** `HomeScreen.kt` son 175 líneas sin una sola lista: no hay rail de
  trending, ni grid de pósters, ni carrusel. Para un tracker + red social de videojuegos, la primera
  pantalla debería *hojear juegos*. Hoy no se puede.
- **Cero superficies de lista larga.** 0 `LazyRow`, 0 `LazyVerticalGrid`, 0 pager, 0 Paging 3: el
  catálogo no es navegable ni escalable, y no hay "raíles" que den ritmo visual.
- **Cero estado modelado.** Existen `GVSkeleton`, `EmptyState` y `OfflineBanner`… pero con 0 `UiState`,
  cada pantalla resuelve *cargando / vacío / error* a mano. Resultado: la pieza está, el uso es
  inconsistente en las 12 pantallas.

Es decir: **la identidad visual ya está; la experiencia de catálogo y la consistencia de estado no.**
Ese es el orden correcto de trabajo para "interfaz excelente y moderna 2026", y no coincide con el de
la reflexión, que prioriza limpieza de código por delante de superficie de producto.

Ventaja de contexto: al existir ya `GVSkeleton`, `EmptyState`, `GVSharedTransition` y `GVMotion`, las
acciones P1 de abajo **no requieren inventar nada nuevo**, solo conectar piezas existentes a pantallas
reales. Por eso son baratas en diseño y caras en impacto.

---

## 5. Plan de adopción priorizado

### P0 — Ahora (bajo riesgo, ~3 archivos, no depende de ninguna fase)

| ID | Acción | Archivos | Por qué primero |
|---|---|---|---|
| P0.1 | `GameCover` → `AsyncImage` con `placeholder` y `error`; retirar `SubcomposeAsyncImage` | `ui/designsystem/components/GameCover.kt` | Un archivo arregla el rendimiento de scroll de GameCard, Search, GameList, Diary y Details. Medir antes/después con la skill `android-profiler` |
| P0.2 | Quitar `implementation(libs.compose.ui.tooling)`; dejar **solo** `debugImplementation` | `app/build.gradle.kts` | Evita arrastrar `PreviewActivity` a release. Verificar en el `merged manifest` de release |
| P0.3 | Migrar los 40 `collectAsState` a `collectAsStateWithLifecycle`; añadir `lifecycle-runtime-compose` al catálogo | `gradle/libs.versions.toml` + ViewModels/pantallas | Práctica estándar; elimina recolección en segundo plano |
| P0.4 | Sincronizar docs (ver §6) | `AGENTS.md`, `03-stack-android-2026.md` | Un agente mal informado trabaja sobre un mapa falso |

### P1 — Al abrir F4.5 (arquitectura de UI; es donde se gana el "2026")

| ID | Acción | Detalle |
|---|---|---|
| P1.1 | **`UiState` por pantalla** (`sealed interface` con `Loading / Empty / Error / Content`) + `StateFlow<UiState>` en el VM | Piloto en `SearchScreen`/`SearchViewModel`; después extender a las 12. Consume `GVSkeleton`, `EmptyState`, `OfflineBanner` ya existentes |
| P1.2 | **Rutas tipadas** `@Serializable` (Nav Compose 2.8+, **sin** migrar a Nav3) + `hiltViewModel()` por destino | Reduce el `NavHost` de 10 parámetros; activa `navigation-event` (`NavigationEvent` de la skill) |
| P1.3 | **Superficie de descubrimiento en Home:** rail *trending* (`LazyRow` + `GameCard`) + grid de pósters (`LazyVerticalGrid`) | Mayor retorno visual por línea del proyecto. Reutiliza `GameCard`/`GameCover`; encaja con `GVSharedTransition` |
| P1.4 | Retirar `LiveData` de `GoogleViewModel`/`LoginScreen` | Cierra el último modelo de estado híbrido antes de estandarizar `StateFlow` |

### P2 — Con F1 cerrado

| ID | Acción | Disparador |
|---|---|---|
| P2.1 | **Paging 3** en Search/GameList | Catálogo con scroll infinito real (>200 resultados o peticiones paginadas a RAWG) |

### No adoptar (y por qué)

- **Modularización `:core/:data/:feature`** — 10,5 k líneas y un solo desarrollador: coste alto, retorno nulo.
  `D-C2` (partir `DDBBViewModel`) da más y es más barato.
- **KMP / Compose Multiplatform** — no hay iOS en el plan visible.
- **Nav3 ahora** — `D-T3` lo difiere al disparador correcto (F4 necesita lista-detalle). Las rutas tipadas
  de P1.2 se hacen **antes y sin** ese salto.
- **Baseline Profiles ahora** — `D-T4` los deja a F4 con runtime estable.
- **Revisión masiva de `contentDescription = null`** — no es un fallo automático; requiere caso por caso.

---

## 6. Deuda documental a sincronizar (afecta a agentes)

| Documento | Dice | Realidad | Acción |
|---|---|---|---|
| `AGENTS.md` §2 | "~6.900 líneas de Kotlin en 36 archivos (main)" | 10.513 líneas en 79 archivos | Actualizar el número |
| `AGENTS.md` §5 | "grafo de 12 rutas" | 14 rutas en `NavHost.kt` | Actualizar el número |
| `AGENTS.md` §4 (línea ~137) | `ui/theme/Theme.kt` — paletas M3 manuales | **No existe**; el tema está en `ui/designsystem/GVTheme.kt` | Corregir la ruta |
| `docs/investigacion-2026/fuentes/03-stack-android-2026.md` | Kotlin 2.2.10 · Hilt 2.59.2 · Coil 3.3.0 | Kotlin 2.4.20 · Hilt 2.60.1 · Coil 3.6.3 (D-T1/D-T2 cerradas 29/09) | Marcar como histórico con nota, o actualizar apuntando a D-T1/D-T2 |
| `.zcode/skills/` | — | 26 skills Android instaladas | Única sugerencia pendiente: añadir la skill de **Coil** (`coil-compose`), la única de las tres nombradas por la reflexión que no está |

---

## 7. Estado y siguiente paso

- **Nada de lo anterior bloquea** a F1 (en curso por el agente paralelo) ni a F4.
- **Propuesta de secuencia:** P0.1–P0.3 (~30–45 min, 3 archivos, sin riesgo de arquitectura) cuando
  el árbol quede libre; P0.4 (docs) en cualquier momento; P1 al abrir F4.5.
- **Criterio rector para la interfaz 2026:** antes de añadir estilo, **enseñar el catálogo** (rails y grid)
  y **modelar el estado** (`UiState`). La identidad visual ya existe; lo que falta es donde aplicarla.
