# Auditoría reinterpretada — 02/10/2026

> Reinterpretación de [auditoria-diseno-2026-10-02.md](auditoria-diseno-2026-10-02.md) y de
> [brainstorm-wow-2026.md](brainstorm-wow-2026.md) a la luz de cuatro skills usadas como base de
> conocimiento: `ui-ux-pro-max`, `design-taste-frontend`, `frontend-design` y `threejs-fundamentals`.

## 0. Veredicto en una línea

La auditoría anterior **midió bien pero enfocó mal**: midió la ejecución contra un skill
(`design-taste-frontend`) que **declara la app nativa fuera de su alcance**, ignoró la base de
conocimiento específica de Compose que el propietario ya tenía (`ui-ux-pro-max/stacks/jetpack-compose.csv`),
ignoró **26 skills locales del propio repo** y no se hizo la única pregunta que `frontend-design`
considera obligatoria.

---

## 1. Los cuatro skills, y qué es cada uno de verdad

| Skill | Alcance declarado | Aplica a GameVision (app Android) | Aplica a otra cosa |
|---|---|---|---|
| `design-taste-frontend` | Landing, portfolio y rediseño web. **Sección 13: "Native mobile (use Apple HIG / Material directly)" está FUERA DE ALCANCE** | **No como ley.** Solo como corrector de sesgos y en superficies de marca (onboarding, estados vacíos) | Web de producto, landing |
| `frontend-design` | Frontend web (HTML/CSS/JS, React, Vue). Pide comprometerse con una dirección estética audaz | **No como ley.** Sí como **test de dirección** | Web de producto, landing |
| `ui-ux-pro-max` | UI/UX agnóstico con datos adjuntos, incluida una pestaña de stack **`jetpack-compose`** | **Sí, es el único de los cuatro con alcance real sobre la app** | Todo |
| `threejs-fundamentals` | WebGL/JavaScript | **No.** Cero relación con Compose | Web, visor 3D, demo del TFM |

**Consecuencia directa:** el brainstorm anterior citó `design-taste-frontend` como fuente principal
para una app Android. Su propio texto dice que para eso se use Material directamente. Las reglas de
ese skill que sobreviven lo hacen **por traducción de intención**, no por autoridad. Eso hay que
decirlo, no maquillarlo.

## 2. Lo que la base de conocimiento específica de Compose revela

`ui-ux-pro-max/data/stacks/jetpack-compose.csv` son **52 guías** con severidad, para este stack
exacto. Medido contra el código (100 ficheros `.kt`):

### Cumple, y bien

| Guía (severidad) | Medición | Veredicto |
|---|---|---|
| #15 Lifecycle-aware collect (**High**) | `collectAsStateWithLifecycle` **92 usos / 20 ficheros**; `collectAsState(` **0** | Cumplimiento total |
| #34 Sin estilos de texto hardcodeados (Medium) | `TextStyle(` **solo** en `ui/designsystem` (12); `fontSize =` fuera del sistema **2** | Cumplimiento casi total |
| #38 Inyectar VM con Hilt (**High**) | `hiltViewModel` 18 / 10 ficheros | Cumple |
| #7 `rememberSaveable` (**High**) | 15 usos / 3 ficheros | Presente |
| #19 `key` en listas Lazy (**High**) | `key =` 27 / 19 ficheros | Sin evidencia de problema |

Esto confirma el diagnóstico anterior con más precisión: **la arquitectura de estado y tema está bien
hecha.** No es una app descuidada.

### No cumple, y bloquea el "wow"

| Guía (severidad) | Medición | Por qué importa para el efecto wow |
|---|---|---|
| #8 `derivedStateOf` para optimizar recomposición (Medium) | **0 usos** | **Toda** animación ligada al scroll (cabecera que colapsa, parallax de carátula, reveals por visibilidad) se construye sobre `derivedStateOf`. Sin esto, se anima a base de recomponer por frame |
| #28 Preferir `LazyColumn` a `Column` + `verticalScroll` (**High**) | `verticalScroll(` **12 usos / 12 ficheros** | Esos 12 pantallas **no tienen `LazyListState`**, así que no pueden hacer scroll-driven animation sin refactor |
| #29 Evitar contenedores de scroll anidados (**High**) | idem | Una cabecera colapsable dentro de un `verticalScroll` crea scroll anidado: exactamente lo que la guía prohíbe |
| #36 `testTag` para selectores estables (Medium) | **0 usos** | Sin ganchos de test, verificar animaciones queda en "a ojo en el emulador" |
| #41 API de `semantics` (Medium) | **0 usos** (con `contentDescription` en 90 sitios) | La accesibilidad es solo textual: no hay semántica de estado ni de acción |
| #30 Evitar `fillMaxSize` por defecto (Medium) | **63 usos / 21 ficheros** | El de mayor volumen; muchos serán legítimos, pero es la guía más incumplida del stack |
| #31 `IntrinsicSize` solo si es necesario (**High**) | 2 usos | Bajo, pero es severidad High |

**Esto reordena el plan.** El brainstorm anterior ponía "fundación de movimiento" como W1. La
fundación real es antes: **`derivedStateOf` y Lazy donde toque son prerequisitos**, no pulido. Un
efecto wow montado sobre `verticalScroll` + recomposición por frame es jank con esteroides.

## 3. La pregunta que faltaba: ¿esto es inolvidable?

`frontend-design` no pide ejecución, pide dirección:

- "Commit to a BOLD aesthetic direction", "pick an extreme".
- Anti-patrones: fuentes sobreusadas (**Inter**, **Roboto**, Arial, fuentes de sistema), esquemas
  trillados, layouts predecibles.
- "NEVER converge on common AI choices (Space Grotesk, for example)".
- La pregunta clave: **"What's the one thing someone will remember?"**

Contrastado con el estado real del repo:

- La dirección vigente es **"galería limpia estilo Apple"**, con **Inter** como sustituta de SF Pro
  (verificado en `ui/designsystem/GVTypography.kt`, `GoogleFont("Inter")` en la línea 28).
- `frontend-design` marca **Inter y las fuentes de sistema como anti-patrón explícito**.
  `design-taste-frontend` la **desaconseja como opción por defecto** (con una vía de escape que aquí
  no se cumple: el brief no es neutro ni público, es **videojuegos**).
- Y el propio `frontend-design` avisa de no converger en elecciones comunes de IA: el nombre que
  cita como ejemplo, **Space Grotesk**, es precisamente la fuente de la dirección anterior del
  proyecto (`docs/ui-redesign-2026-proposals.md`, conservada en el snapshot).

**La reinterpretación incómoda:** la dirección "galería limpia estilo Apple" es la opción **más
segura y más genérica** posible, y no tiene ninguna relación semántica con videojuegos. Es defendible
como sistema de lectura, pero no responde a la pregunta de `frontend-design`. La auditoría anterior
midió que el sistema se aplica; no preguntó si el sistema **dice algo**. Eso es un hueco de la
auditoría, no del código.

## 4. La segunda pregunta: ¿es el producto correcto?

El repo ya tenía la respuesta escrita y la auditoría no la abrió. Está en
`docs/investigacion-2026/` (`analisis-competitivo-2026.md`, `sintesis-2026.md`,
`fuentes-competencia/04-ui-ux-referencias.md`). Lo esencial:

**El nicho está fragmentado y sin dueño en móvil.** Los líderes son **web**: Backloggd
(~650 K usuarios, "best UI of any game tracker", sin app nativa), Grouvee, Completionator,
HowLongToBeat, GameFAQs. Los móviles son pequeños: **Stash es el rival directo**, GameTrack solo
iOS. La grieta declarada es **Android nativo con hábito diario**, no el catálogo: RAWG, IGDB y HLTB
son infraestructura que todos comparten. El foso es comunidad y hábito.

**Cinco sistemas que hoy no integra junto ningún tracker de juegos**, y que son la diferenciación
real: estados ricos (S3) + Wrapped (S9) + import/export universal (S12) + duelos ELO (S15) +
mood tags (S10). Ni la auditoría del 02/10 ni mi brainstorm los mencionan.

### La consecuencia incómoda para el "efecto wow"

El motor viral de coste cero ya está decidido y **está a medias**: el **Rewind/Wrapped**
(F3 tiene T3.1–T3.3 hechas; **todo el bloque B, T3.6–T3.11, pendiente**, incluida la pantalla, la
imagen compartible y el compartir nativo). Este corpus añade dos restricciones que el brainstorm no
tenía: el Rewind **nace** con el look final (ADR-0010) y **no se monetiza** (el error de Strava a
80 $/año).

Es decir: **"efecto wow 2026" puede no ser una capa de movimiento.** Una pantalla de Rewind
compartible, con una imagen que la gente quiera publicar, es un efecto wow de producto: visual,
viral, ya decidido, a medio construir, y **sin diseño asignado**. Comparado con eso, el grano global
y la física de muelles son acabado, no magia.

Eso reordena el "wow" en tres niveles, y conviene no confundirlos:

| Nivel | Qué es | Dónde está | Estado |
|---|---|---|---|
| **Producto** | Rewind compartible, estantería con lomos, import/export, mood tags | Investigación + mecánicas sin fase | Decidido, sin ejecutar, **sin diseño** |
| **Dirección** | Qué hace inolvidable a GameVision (la pregunta de `frontend-design`) | Sin responder | Abierto |
| **Ejecución** | Capa de motion, prerequisitos Compose, barridos de acento y radios | `DESIGN.md` + F4.5 | Motion construido y sin usar |

Mi brainstorm anterior trabajó solo en el nivel de **ejecución** y lo llamó "el wow". Los tres
niveles son reales, pero el impacto decreciente es el inverso al orden en que los puse.

---

## 5. Foco: hay que ampliarlo en tres direcciones

1. **Dentro del repo.** 26 skills locales instaladas en `.zcode/skills/` sin usar. **Corrección
   importante:** no son la guía de estilo del proyecto. Son **skills de proveedor** (Google LLC,
   fechadas en septiembre de 2026) sobre APIs que el repo todavía no usa. `styles` cubre la **API
   `Style` de Compose** y `adaptive` cubre layouts flexibles y multi-pane. Ninguna menciona
   GameVision ni sus tokens, y `styles/SKILL.md:23-24` **no soporta los componentes Material**, que
   es justo lo que usa el design system. Ver el §8.
   Junto a ellas, **`display-glasses-with-jetpack-compose-glimmer`** (32 ficheros, 372.430 bytes),
   que **no es sobre animación**: es *Jetpack Compose Glimmer*, el toolkit de UI de **Android XR para
   gafas con pantalla**. Se trata en el §7.
2. **Dentro de la investigación ya hecha.** El repo tiene investigación de UX y benchmarking
   competitivo (`docs/investigacion-2026/fuentes/04-diseno-ux-2026.md`,
   `fuentes-competencia/04-ui-ux-referencias.md`, `analisis-competitivo-2026.md`). La auditoría
   anterior no los abrió.
3. **Fuera de la app.** `threejs-fundamentals` y `frontend-design` **no aplican a la app**, y eso no
   los hace inservibles: el producto tiene `steam-worker` (Cloudflare Worker) y un
   `PLAN-MAESTRO-2026.md` con estrategia. Una web de producto es la única superficie donde el
   "efecto wow 2026" no está limitado por Material 3, por el tamaño de pantalla ni por el pulgar.
   Ahí sí mandan los tres skills web al 100%.

---

## 6. Correcciones al plan anterior

| Antes (brainstorm) | Ahora |
|---|---|
| W1 = fundación de movimiento | **W0.5 = prerequisitos**: `derivedStateOf`, Lazy donde toque, `testTag`/`semantics`. Sin esto, la capa de motion no se puede verificar ni medir |
| Motion como la deuda principal | Motion sigue siendo la deuda principal **de ejecución**, pero la deuda **de dirección** (qué hace inolvidable a GameVision) es anterior a la de ejecución |
| `design-taste-frontend` como fuente principal | Fuente principal para la app: `ui-ux-pro-max` + `jetpack-compose.csv` + skills locales del repo. `design-taste-frontend` manda en web/marca |
| Auditoría cerrada sobre el código de la app | Auditoría cerrada sobre app + skills locales + investigación previa + superficie web |
| "Efecto wow" = capa de movimiento (grano, muelles, contadores) | **"Efecto wow" = producto, dirección y ejecución, en ese orden de impacto.** El Rewind compartible es el wow de producto y no tiene diseño asignado |
| Micro-interacciones de recompensa (C1–C6) propuestas sin marco | El marco ético ya está investigado y con literatura: celebrar sin castigar, rachas opcionales sin penalización pública, **no monetizar el recap**, no gamificar volumen de horas |
| Accesibilidad tratada como incidentes ("cajas bajo 48 dp") | Criterio normativo ya documentado: WCAG 2.2 SC 2.5.8 (24×24), 2.5.5 (44×44) y 48 dp Material, más clases de ventana para tablet y plegable |

### Decisiones abiertas que este trabajo añadió

> **Estado (02/10): las cuatro primeras están CERRADAS por
> [ADR-0013](../metodologia/adr/0013-el-contrato-visual-manda.md)** con el criterio "el contrato
> manda": sombras (nada de elevación, la profundidad es la escalera de superficies), familias
> tipográficas (una sola; cifras tabulares con `fontFeatureSettings = "tnum"`), breakpoints
> (Compact/Medium/Expanded, ya escritos en `DESIGN.md` §7) y mis cinco ideas que incumplían el
> contrato (marcadas en [brainstorm-wow-2026.md](brainstorm-wow-2026.md)). Se dejan aquí como
> registro del debate. Lo que **sigue abierto** es la dirección (la tipografía con carácter) y las
> mecánicas de producto sin fase asignada.

- Estantería con lomos: la referencia la quiere en **acento**; ADR-0012 dice que el acento es solo
  para lo que se **toca**. Un lomo es contenido. **Choca.**
- Material You / color dinámico: el modo A de la investigación lo proponía; `DESIGN.md` lo prohíbe
  por la regla del acento único. Ganó `DESIGN.md`, y conviene dejar de citarlo como si siguiera vivo.
- **Onboarding TTFV < 60 s** ("elegir 3 juegos ya jugados") está decidido en la investigación y **no
  aparece como tarea de ninguna fase**. Es captación, y no tiene dueño.
- La investigación sigue afirmando que "monocromo cálido + `#C8F135` + Space Grotesk se mantiene".
  Está **superada por ADR-0010**, pero el texto sigue ahí y contradice a `DESIGN.md`. Cita muerta que
  hay que retirar o marcar como histórica.
- **Sombras.** `DESIGN.md` dice "nada de sombras en UI". La librería externa recomienda
  `[46] Dimensional Layering` con cuatro sombras escaladas, y `[39] Bento` con sombra. Es el choque
  de dirección más caro: el modo oscuro se ve plano sin elevación, y el repo prohíbe la herramienta
  que lo resuelve. **Decisión del propietario.**
- **Mezcla de familias tipográficas.** `DESIGN.md:92-93` la prohíbe; `typography.csv` propone
  Inter + Russo One + Fira Code (cifras).
- **Breakpoints.** `DESIGN.md` **no fija ninguno** y `adaptive` los da con nombre
  (`WIDTH_DP_MEDIUM/EXPANDED`) y comportamiento. Hueco real del contrato actual.
- **Micro-interacciones 50-100 ms con háptica.** El CSV las asigna a "Mobile apps, touchscreen
  UIs" y es la capa táctil que `DESIGN.md` no cubre. Candidata a entrar en el contrato.
- **Mis cinco ideas que incumplen `DESIGN.md`** (§8.3): cada una necesita enmienda al contrato o
  descarte. No se ejecutan "porque el skill lo diga".

## 7. Ejecución: Glimmer es otra cosa, y `GVMotion` tiene la mitad muerta

### 7.1 Glimmer no es una librería de animación

`display-glasses-with-jetpack-compose-glimmer` es **Jetpack Compose Glimmer**, el toolkit de UI de
**Android XR para gafas con pantalla** (Projected Activity), de Google. Coordenadas reales
`androidx.xr.glimmer:glimmer` y `androidx.xr.glimmer:glimmer-google-fonts`; **ninguna versión**
aparece en los 32 ficheros. La licencia se declara en un `LICENSE.txt` que **no existe** en el
directorio (los fuentes llevan cabeceras Apache 2.0).

**Como dependencia, no.** Instalarlo **prohíbe `MaterialTheme`**, exige fondo negro puro obligatorio
y una `Projected Activity` nueva; `ProjectedContext` pide API 34/35/36 según el miembro. Es un factor
de forma que GameVision no tiene, y esa decisión es de producto (`PLAN-MAESTRO-2026`), no de motion.

**Como fuente de técnicas, sí**, porque el código se reimplementa sin coste de build. Cuatro
portables:

| Técnica | API real | Coste |
|---|---|---|
| Profundidad en 2 capas de sombra lerpeadas por progreso | `Modifier.depthEffect(...)`, `DepthEffectLevels.level1..level5` (radios 12 → 56 dp) | Cero dependencias; sustituye a `elevation` |
| Borde vivo cónico que rota con el foco | `RuntimeShader` AGSL de 4 colores | **API 33+**; el repo ya está en `minSdk 33` |
| Scrim con desenfoque progresivo bajo la barra inferior | 2 pasadas horizontal + vertical con `RenderEffect.createRuntimeShaderEffect` | GPU-intensivo: 2 shaders por elemento |
| Specs de muelle calibrados | press `spring(0.84f, 8000f)` / `spring(0.85f, 50f)` con suelo de 300 ms; snap de pila `spring(0.56f, 118f)` | Son dos `val` nuevos en `GVMotion` |

**Aviso de accesibilidad:** Glimmer **no menciona accesibilidad ni reducción de movimiento en
ninguno de sus 32 ficheros**, y su pulso ambiental es una animación infinita. Si se copia cualquier
técnica, el gate de reducir-movimiento hay que escribirlo aquí. Ver el §7.2.

Glimmer **no cambia el diagnóstico**: no aporta `graphicsLayer`, `drawBehind`/`Canvas`,
`animateContentSize` ni `AnimatedContent`. Es ortogonal a `GVSharedTransition` (no documenta shared
elements en ningún fichero) y complementario de `GVMotion`.

### 7.2 `GVMotion`: 5 de sus 8 miembros nunca se conectaron

Medido símbolo a símbolo, no por impresión:

| Miembro de `GVMotion.kt` | Usos reales |
|---|---|
| `EnterEasing` / `ExitEasing` | 4 + 4, todos en `NavHost.kt` |
| `springStandard` / `springBouncy` | **1 + 1**, los dos en `BottomBarNavigation.kt` |
| `DURATION_FAST` / `DURATION_NORMAL` / `DURATION_SLOW` | **0** |
| `STAGGER_INCREMENT_MS` | **0** |
| **`LocalReduceMotion`** | **1**, y es su propia línea de declaración (44) |

`LocalReduceMotion` es un `staticCompositionLocalOf { false }` con un comentario que explica
exactamente para qué existe ("true cuando el usuario pide reducir movimiento en el sistema").
**Nadie lo provee. Nadie lo consume. El valor por defecto está fijado a `false`, así que aunque
alguien lo consumiera nunca podría ser `true`.**

Esto afina el hallazgo anterior. No es que falte soporte de reducción de movimiento: es que
**alguien escribió la API, la documentó y no la conectó al sistema operativo**. Es el mismo patrón que
en toda la capa de movimiento: el sistema se construyó por delante de su consumo.

La buena noticia es que el arreglo es pequeño y el mecanismo ya está en uso: `CompositionLocalProvider`
aparece 4 veces (`MainActivity.kt` ×2, `GVSharedTransition.kt` ×2). Conectar `LocalReduceMotion` a
`LocalMotionDurationScale` y proveerlo en `MainActivity` son unas tres líneas, más el gate en los
sitios que animen.

## 8. Las skills locales, y las cinco cosas que mi brainstorm incumple

### 8.1 Qué son de verdad

Skills de proveedor (**Google LLC**, septiembre de 2026). Ninguna menciona GameVision ni sus tokens.

- **`styles`** cubre la **API `Style` experimental de Compose**. Requiere foundation
  ≥ `1.12.0-alpha01` o BOM ≥ `2026.04.01`, `compileSdk ≥ 37` (el repo cumple), opt-in
  `ExperimentalFoundationStyleApi` y `jvmTarget 17`. Firma exigida: quitar
  `backgroundColor/shape/textStyle/contentPadding` y dejar `style: Style = Style`. Última propiedad
  gana, no son aditivas; precedencia directos > style > modificador > padre.
  **Límite duro:** `styles/SKILL.md:23-24` **no soporta Styles de componentes Material**, así que
  todo el design system del repo (`GVButton`, `GVChip`, `GVSearchField`) queda **fuera de su
  alcance**.
- **`adaptive`** cubre barra↔rail, multi-pane con Nav3, Grid/FlexBox y MediaQuery. Prohíbe
  `ListDetailPaneScaffold` y `SupportingPaneScaffold`, ordena `GridCells.Adaptive`, fija Grid por
  debajo de 800 dp a 2×4, y MediaQuery a 20/18/16 sp.
- `adaptive` **se contradice a sí misma en versiones**: su `SKILL.md` dice que Grid llega en Compose
  1.11.0-beta01, pero sus propios documentos de flexbox y grid exigen **1.13.0-alpha03**. El repo
  está en **1.12.1**. No es aplicable tal cual.

### 8.2 Contradicciones duras con `DESIGN.md`

| `DESIGN.md` dice | Las skills locales dicen | Gravedad |
|---|---|---|
| §7: **cabecera FIJA**, vive fuera del scroll | `adaptive/SKILL.md:260-266`: ocultar app bars al hacer scroll (`exitUntilCollapsed`/`enterAlways`) | **La más fuerte** |
| §1 y §8: **nada de sombras** en UI | `fundamentals.md:25`: `dropShadow`/`innerShadow` son propiedades de `Style`; `theming.md:68-84`: `interactiveShadowAtomic` | Alta |
| §1 y §8: **nada de gradientes** | `fundamentals.md:22/332/361`: `background`/`contentBrush` aceptan `Brush` | Alta |
| §5: física de muelles, **nunca tweens** | `state-animations.md:199-204`: `animate {}` sin `animationSpec` usa el default del API | Media |
| §5: pulsado **encoge** a 0.95 | `state-animations.md:248-250`: al pulsar **crece** a `scale(1.2f)` | Media |
| §3: **sin mezcla de familias** | `typography.csv` propone Inter + Russo One + Fira Code | Media |
| §7: la vuelta vive en el `leading` | `adaptive/SKILL.md:174`: las pantallas de detalle **no deben mostrar flecha de volver** en list-detail | Media |
| (no fija ningún breakpoint) | `adaptive` fija `WIDTH_DP_MEDIUM/EXPANDED` con nombre y comportamiento | Hueco |

### 8.3 Mi brainstorm incumple el contrato de tu propio repo

Esto es una corrección a mi trabajo, y es la parte más útil de todo el ejercicio. De
[brainstorm-wow-2026.md](brainstorm-wow-2026.md):

| Idea que propuse | Dónde | Contra qué choca |
|---|---|---|
| **E4**: cabecera colapsable con `LargeTopAppBar` + `exitUntilCollapsedScrollBehavior` + parallax | `:140` | `DESIGN.md:185` **cabecera FIJA**. Y lo grave: coincide con la skill de Google, o sea que **me alineé con Google y no con tu contrato** |
| **A4**: stagger con `tween(delayMillis = ...)` | `:98` | `DESIGN.md:109` "nunca tweens" |
| "nada de animar `padding`, `offset` en dp, `width`/`height`" | `:158` | Las skills **sí** animan `size` y `externalPadding`: son propiedades de `Style`. Mi regla era una traducción demasiado literal de CSS |
| **D3** sombras tintadas y **D1** grano global | `:129`, `:127` | `DESIGN.md:46/:203` "nada de sombras"; y D1 contradice mi propio §4 ("nada de ruido permanente") |
| **E2** `FontFamily.Monospace` para cifras | `:138` | `DESIGN.md:79/:92-93` "sin mezcla de familias" |

**Conclusión honesta:** traté `DESIGN.md` como punto de partida cuando es un **contrato**. Cinco ideas
necesitan o una enmienda explícita a `DESIGN.md` o ser descartadas. No vale ejecutarlas "porque el
skill lo dice": el skill no manda aquí.

### 8.4 Lo que la librería externa empuja en dirección contraria

De `ui-ux-pro-max` (`styles.csv`, `colors.csv`, `ui-reasoning.csv`):

- Sus estilos recomendados para entretenimiento y móvil son **`[46] Dimensional Layering`** (4
  sombras escaladas), **`[39] Bento Box Grid`** (con sombra) y **`[14] Liquid Glass`** (blur 15 px).
  Los tres van **contra el "nada de sombras, nada de gradientes"** de `DESIGN.md`. Es una decisión
  del propietario, no un error de nadie.
- **`[16] Micro-interactions`** (50-100 ms, háptica, feedback) es la capa más barata y la que
  `DESIGN.md` no tiene. El CSV la asigna literalmente a "Mobile apps, touchscreen UIs". Es lo más
  aprovechable de toda la librería.
- **`[7] Dark Mode (OLED)`** propone `#000000` y `#121212`. `DESIGN.md` ya usa `#0E0E10`, que es
  mejor: prohíbe negros puros. Mantener lo del repo.
- `colors.csv` tiene una vertical **Gaming** con morado `#7C3AED`, y `ui-reasoning.csv` repite el
  anti-patrón **"AI purple/pink gradients"** en más de veinte filas. Traducido: si algún día se va a
  morado, **plano y nunca como gradiente de fondo**.
- La columna de librerías de `charts.csv` (Chart.js, Recharts, D3) es **relleno inaplicable** a
  Android; el tipo de gráfico sí sirve. `web-interface.csv` es ~70 % inaplicable en Compose.

### 8.5 Trazabilidad

Los tres conflictos abiertos de **Inter**, **blanco puro** y **serif** vienen de los seis skills
globales (`.skills/design/`), **no** de estas dos skills locales. No hay que atribuírselos.

## 9. Estado de esta iteración

Los cuatro digests están producidos en `.skills/_digests/`:

| Digest | Tamaño | Qué cierra |
|---|---|---|
| `05-skills-locales-repo.md` | — | Qué son `styles` y `adaptive`, 39 + 40 reglas, y las contradicciones con `DESIGN.md` |
| `06-investigacion-ux-competencia.md` | 30,6 KB | Benchmark competitivo, decisiones tomadas, huecos, y la separación producto / sistema |
| `07-ui-ux-pro-max-aplicado.md` | 63,4 KB / 679 líneas | Los 9 CSV: 5 estilos candidatos, 99 guías UX, 20 reglas de mayor impacto, gráficos por tipo de dato |
| `08-glimmer-motion.md` | 19 KB | Qué es Glimmer de verdad, 4 técnicas portables, y `GVMotion` símbolo a símbolo |

Con esto la reinterpretación está cerrada. Lo que **no** cierra este documento, porque no se
cierra auditando: las mecánicas de producto del §6.2 del digest 06 (import/export, ELO, mood tags,
estantería, Rewind) siguen **sin fase asignada**.
