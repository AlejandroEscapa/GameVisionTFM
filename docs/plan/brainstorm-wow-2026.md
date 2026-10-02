# Brainstorm UI 2026 — de "design system bien hecho" a "efecto wow"

> Documento de trabajo. **No sustituye a [`DESIGN.md`](../DESIGN.md)**, que sigue siendo la fuente
> única de verdad visual. Esto es diagnóstico + banco de ideas priorizado por impacto/esfuerzo.
> Generado el 02/10/2026 a partir de los skills de diseño del propietario, trasladados a
> `.skills/` (fuera del repo, por diseño) y destilados en `.skills/_digests/`.
>
> ⚠️ **Correcciones del 02/10/2026 ([ADR-0013](../metodologia/adr/0013-el-contrato-visual-manda.md)).**
> Este documento se escribió tratando `DESIGN.md` como punto de partida, y **es un contrato**: cinco
> de sus ideas lo incumplían. Quedan marcadas en su sitio (❌ descartada · ⚠️ enmendada): **A4**
> (tween con retardo → muelle), **D1** (grano global, descartada), **D3** (sombras tintadas,
> descartada), **E2** (monoespaciada → cifras tabulares de Inter), **E4** (cabecera colapsable,
> descartada). Además, el orden de impacto de §3 está revisado en
> [auditoria-2026-reinterpretada.md](auditoria-2026-reinterpretada.md): el wow es **producto →
> dirección → ejecución**, y este documento solo cubría la ejecución.

## 0. De dónde sale esto

Se han leído y destilado seis skills de diseño del propietario
(`design-taste-frontend`, `redesign-existing-projects`, `gpt-taste`, `minimalist-ui`,
`industrial-brutalist-ui`, `stitch-design-taste`) más la librería `ui-ux-pro-max`.

**Este documento continúa el trabajo de [auditoria-diseno-2026-10-02.md](auditoria-diseno-2026-10-02.md)**,
que es la auditoría por pantallas (adopción del design system, 4 bugs de estado, barrido de copy,
contraste y accesibilidad, ejecutada en los commits `f4ac73e`, `aacfe5b` y `ef1eccf`). Aquel informe
cerró **la adopción**; este abre **el movimiento**, que es la deuda que aquel dejó apuntada.

Producto de esa lectura, dos digests con la doctrina ya traducida a Kotlin/Compose:

- `.skills/_digests/01-doctrina-taste.md` — reglas duras, prohibiciones, pre-flight y traducción Compose.
- `.skills/_digests/02-lenguajes-visuales.md` — comparativa de los cuatro lenguajes visuales y veredicto.

**Veredicto de los skills para este producto:** base `minimalist-ui`, gobernanza
`stitch-design-taste` (el `DESIGN.md`), `gpt-taste` solo en onboarding/captación y
`industrial-brutalist-ui` acotado a las vistas de datos (estadísticas, rankings). Es decir: el
re-anclaje "galería limpia estilo Apple" que ya está en `DESIGN.md` **está alineado con la doctrina**;
no hay que rehacerlo.

## 1. Diagnóstico con evidencia

Conteo sobre `app/src/main/java/es/androidtfm/gamevision` (100 ficheros `.kt`).

| Señal | Medición | Lectura |
|---|---|---|
| `MaterialTheme.typography` | 209 usos / 31 ficheros | La tipografía del sistema **sí se consume** |
| `GVSpacing` | 151 usos / 20 ficheros | La escala de espaciado **se está aplicando** |
| `GVShape(s)` | 58 usos / 17 ficheros | Los radios del sistema se usan |
| `contentDescription` | 90 usos / 28 ficheros | Cobertura de accesibilidad razonable |
| `SharedTransition` / `sharedElement` | 33 usos / 6 ficheros | La transición carátula→ficha **ya existe** |
| `colorScheme.primary` | **50 usos / 23 ficheros** | Zona de riesgo #1: la regla "el acento es para lo que se toca" |
| `RoundedCornerShape(` | **50 usos / 19 ficheros** | Radios crudos conviviendo con `GVShapes`: deriva posible |
| `spring(` | **2 usos / 2 ficheros** | `GVMotion` existe pero casi nadie lo consume |
| `animateFloatAsState` | 7 / 4 | Animación puntual, no sistema |
| `AnimatedVisibility` | 15 / 2 | Concentrado en dos pantallas |
| `graphicsLayer` | **0** | No hay animación por capa en toda la app |
| `drawBehind` / `drawWithCache` / `Canvas` | **0** | Cero superficies o gráficos propios |
| `animateContentSize` | **0** | Nada crece ni se expande con animación |
| `AnimatedContent` / `Crossfade` | **0** | Los cambios de contenido son instantáneos |
| `Modifier.placeholder` | **0** | Los skeletons no usan la API de placeholder |
| `Modifier.blur` | **0** | Cero uso de desenfoque (API 31+) |
| `LocalMotionDurationScale` / `areAnimatorsEnabled` | **0** | **"Reducir movimiento" no se respeta en ningún sitio** |
| `Color.Black` / `Color.White` | 1 / 2 | Violaciones menores de "nada de negro/blanco puros" |
| `Color(0x…)` | 34 / 1 fichero | Contenido en el fichero de tema: correcto |
| `substring(` | 3 / 3 ficheros | Riesgo ya documentado (`StringIndexOutOfBounds` con datos de RAWG) |

`GVMotion.kt` son 44 líneas con dos muelles (`springStandard`, `springBouncy`) y se consume en la
barra inferior y en `TopGamesCard`. **La capa de movimiento está construida y sin usar.**

### Conclusión del diagnóstico

El problema de GameVision **no es el diseño**. La capa de sistema (tokens, tipografía, espaciado,
formas, componentes, transición compartida) está hecha y adoptada, con deuda conocida y acotada.
Lo que falta es lo que el propietario pide literalmente: **movimiento, reacción y profundidad**.
La app hoy es correcta y plana; el "wow 2026" es una capa de motion y superficie que se puede
añadir **sin tocar la identidad visual**.

## 2. Tres conflictos doctrinales que hay que resolver antes de tocar nada

No son bugs: son decisiones que la doctrina de los skills y `DESIGN.md` resuelven distinto. Hay que
elegir y escribirlo.

1. **Inter.** `design-taste-frontend` la desaconseja como fuente por defecto ("es la Inter de
   Android es Roboto: cambiarlo es el movimiento de taste"). `DESIGN.md` la eligió como sustituta de
   SF Pro. La vía de escape del propio skill permite Inter si el brief es neutro/estilo Linear —
   pero GameVision es un producto de **videojuegos**: ahí una grotesca con carácter (Outfit, Satoshi,
   Geist) o un display para titulares es diferenciación gratis. **Decisión pendiente.**
2. **Blanco puro `#FFFFFF`.** El skill lo prohíbe como fondo; `DESIGN.md` lo usa como superficie
   dominante en claro. En una "galería" el blanco puro es justificable, pero choca con la regla.
   **Decisión: o se documenta la excepción en `DESIGN.md`, o se pasa a `#FCFCFD`/`#FBFBFD`.**
3. **Serif.** El digest registra que `DESIGN.md` cita como aceptables precisamente las dos serif que
   el skill prohíbe por defecto (`Fraunces`, `Instrument_Serif`). Si no se van a usar, mejor
   eliminarlas del documento para no invitar a un AI-tell.

## 3. El banco de ideas: efecto wow, por capas

Cada idea lleva **impacto** (1-5), **esfuerzo** (1-5) y la API concreta. Regla de oro de la doctrina:
*"motion must be motivated"* — toda animación se justifica en una frase (jerarquía, feedback,
transición de estado o narrativa) o se borra.

### Capa A — Fundación de movimiento (hacer primero, todo lo demás depende)

| # | Idea | I/E | Cómo |
|---|---|---|---|
| A1 | **Respetar "reducir movimiento"** en toda la app | 4/1 | `LocalMotionDurationScale.current.scaleFactor == 0f` → degradar a estado final. Hoy hay 0 usos. Es accesibilidad, no adorno |
| A2 | Ampliar `GVMotion` con una escala de muelles con nombre (press, enter, exit, sheet, layout) | 4/2 | `spring(dampingRatio = 0.86f, stiffness = 100f)`; ojo: el `damping: 20` de Framer equivale a ζ≈1.0, no a `dampingRatio = 20f`. Documentar la conversión en `DESIGN.md` |
| A3 | **Press scale universal 0.95** ya declarado en `DESIGN.md` §5, verificado en todos los clicables | 3/2 | `interactionSource.collectIsPressedAsState()` + `animateFloatAsState` + `Modifier.graphicsLayer { scaleX/scaleY }` (hoy `graphicsLayer` = 0) |
| A4 | Stagger de entrada en listas, con tope | 4/2 | ⚠️ **ENMENDADA (ADR-0013)**: el retardo no puede ir en un `tween` (`DESIGN.md` §5: muelles, nunca tweens). Se hace `LaunchedEffect` con el retardo y **después** muelle de `GVMotion`. Tope `min(index, 6)`: sin tope, el ítem 25 arranca a los 1,5 s y se percibe como bug |
| A5 | Animación de reordenación en listas | 3/1 | `Modifier.animateItem()` en `LazyColumn`/`LazyVerticalGrid` (hoy 1 uso) |
| A6 | Sustituir `AnimatedVisibility` suelto por un único dueño de animación por propiedad | 2/3 | Evitar dos APIs animando el mismo nodo |

### Capa B — Transiciones entre pantallas (donde más se nota sin riesgo)

| # | Idea | I/E | Cómo |
|---|---|---|---|
| B1 | **Container transform carátula→ficha** ya iniciado: extenderlo a Top 4, feed y listas | 5/3 | `SharedTransitionLayout` + `Modifier.sharedElement(rememberSharedContentState("cover-$id"), animatedVisibilityScope)`; clave unificada `cover-{gameId}` |
| B2 | Transición de tema claro/oscuro animada | 3/2 | `animateColorAsState` sobre los tokens de superficie; hoy el toggle cambia en seco |
| B3 | Predictivo "atrás" hacia atrás (Android 14+) | 3/2 | `PredictiveBackHandler`; mejora la sensación de sistema operativo moderno |
| B4 | Morph del botón en su propio diálogo | 3/3 | "Añadir a lista" que se expande en vez de abrir un diálogo opaco |
| B5 | Entrada escalonada de las tarjetas de una pantalla de resultados | 4/2 | Contenedor que revela hijos con delay acumulado, una sola vez (`LaunchedEffect`), nunca en recomposición |

### Capa C — Micro-interacciones (baratas, alto retorno percibido)

| # | Idea | I/E | Cómo |
|---|---|---|---|
| C1 | **Valoración con física**: la estrella que se rellena con rebote y háptica | 5/2 | `spring(DampingRatioMediumBouncy, 600f)` + `HapticFeedbackType.LongPress` |
| C2 | "Me gusta" con contador que salta y corazón que escala | 4/1 | `animateFloatAsState` + `animateContentSize` (hoy 0 usos) |
| C3 | Progreso de lista que se llena al entrar | 4/2 | `animateFloatAsState` sobre el ratio; barras **activas** sí llevan acento (regla §2 del DESIGN.md) |
| C4 | Pull-to-refresh con física propia, no el spinner genérico | 3/3 | `PullToRefreshBox` + indicador custom dibujado con `drawBehind` |
| C5 | Chips y selección con transición de color y escala | 3/1 | `animateColorAsState` (hoy 2 usos) |
| C6 | Añadir a biblioteca: confirmación que no abre diálogo | 4/2 | El propio botón cambia de estado con relleno direccional |

### Capa D — Superficie y profundidad (el "2026" visual)

| # | Idea | I/E | Cómo |
|---|---|---|---|
| D1 | ~~Grano/noise global a opacidad 0.02-0.04~~ | — | ❌ **DESCARTADA (ADR-0013)**: `DESIGN.md` §1 prohíbe el chrome decorativo y este propio documento lo veta en §4 ("nada de ruido permanente"). Contradecía el contrato y a sí misma |
| D2 | Blob radial ambiental a 0.03 de opacidad, movimiento de 20 s+ | 4/3 | `rememberInfiniteTransition` moviendo el `center` de un `Brush.radialGradient` en `Canvas` de fondo |
| D3 | ~~Sombras tintadas donde haya elevación real~~ | — | ❌ **DESCARTADA (ADR-0013)**: `DESIGN.md` §1 y §8 prohíben sombras en UI. La profundidad se consigue **subiendo un peldaño de la escalera de superficies** (`#161619` → `#272729` → `#2A2A2C`) |
| D4 | Translucidez + borde interior de 1 px en la barra inferior | 3/3 | En Android **no hay `backdrop-filter`**: `Modifier.blur` desenfoca el propio contenido, no el fondo. Solo translucidez + borde + highlight, con fallback opaco obligatorio |
| D5 | Textura tactile/scanline como **efecto puntual** en la retrospectiva anual | 3/2 | `drawWithCache` cacheando offsets; jamás global |

### Capa E — Datos que se sienten vivos

| # | Idea | I/E | Cómo |
|---|---|---|---|
| E1 | Contadores animados en Estadísticas y Diario | 5/2 | `animateIntAsState` sobre el total; jerarquía por tamaño y peso, sin acento (regla §2) |
| E2 | **Cifras tabulares** | 4/1 | ⚠️ **ENMENDADA (ADR-0013)**: `FontFamily.Monospace` rompe "sin mezcla de familias" (`DESIGN.md` §3). Se consigue lo mismo —que los contadores no bailen— con `fontFeatureSettings = "tnum"` **sobre Inter**. Hoy hay **0 usos** de cifras tabulares |
| E3 | Sparkline de horas jugadas por semana | 4/3 | `Canvas` + `Path` con `drawPath`; sin librería de gráficos |
| E4 | ~~Cabecera colapsable con parallax de carátula en la ficha~~ | — | ❌ **DESCARTADA (ADR-0013)**: `DESIGN.md` §7 fija la **cabecera FIJA**, fuera del scroll. El parallax de carátula sí cabe, pero dentro del contenido con `graphicsLayer`, no colapsando la cabecera |
| E5 | Anillo de progreso de backlog por estado | 3/2 | `Canvas` + `drawArc` animado por `animateFloatAsState` |

### Capa F — Onboarding y captación (único sitio donde `gpt-taste` aplica al 100%)

| # | Idea | I/E | Cómo |
|---|---|---|---|
| F1 | Hero de onboarding con titular de 2 líneas y tipografía cinética | 4/3 | Regla del skill: si el titular pasa de 3 líneas, es error de tamaño, no de copy |
| F2 | Imagen inline dentro del titular | 3/4 | `AnnotatedString` + `InlineTextContent` (técnica firma de `gpt-taste`) |
| F3 | Revelado de secciones al hacer scroll, una sola vez | 3/2 | `derivedStateOf` sobre `LazyListState.layoutInfo`, nunca listener manual |

## 4. Lo que NO hay que hacer (anti-slop aplicado a móvil)

- **Nada de bucles infinitos por tarjeta.** El skill lo subraya: si la sección es informativa, se
  queda quieta. Un loop perpetuo visible a la vez, máximo.
- **Nada de GSAP ni pinning/scrubbing de scroll.** En Compose no hay `ScrollTrigger` con scrub
  arbitrario; el coste/beneficio es malo. Se emula con `derivedStateOf` sobre `LazyListState` y
  `graphicsLayer` **dentro del contenido** (la cabecera es fija y no se colapsa, §7 del contrato).
- **Nada de `window`/listeners manuales de scroll** → `derivedStateOf` sobre `LazyListState`.
- **Nada de animar `padding`, `offset` en dp ni `width`/`height`** → solo `graphicsLayer` y `alpha`.
- **Nada de ruido o scanline global permanente**: cansa en una app de uso diario.
- **Nada de acento en datos.** `colorScheme.primary` aparece 50 veces en 23 ficheros: hay que
  auditar cada uno contra la regla "¿el usuario puede tocar esto?".
- **Nada de un segundo idioma, emoji ni glifos como icono** (`★`, `✓`).
- **Nada de `CircularProgressIndicator` como estado de carga de listas** (hoy 3 usos en 2 ficheros;
  el de `GVSearchField` es legítimo: progreso de búsqueda en vivo).

## 5. Plan propuesto (por fases, con verificación)

| Fase | Contenido | Verificación |
|---|---|---|
| **W0 — Auditoría fina** | Barrido de los 50 `colorScheme.primary` y los 50 `RoundedCornerShape` contra `DESIGN.md` §2 y §4. Cerrar la deuda de espaciado ya declarada (login/pass/register a 24, perfil a 20, relleno de tarjeta variable). Resolver los 3 conflictos de §2 | Checklist de PR + revisión de los 3 ficheros con `substring(` |
| **W1 — Fundación** | A1-A6. `GVMotion` completo, reducción de movimiento, press scale universal, stagger con tope | Tests de UI en emulador + verificación de que con animaciones desactivadas no hay stagger |
| **W2 — Transiciones** | B1-B5. Extender la transición compartida y animar el cambio de tema | Emulador `Pixel_9` (API 36), grabación antes/después |
| **W3 — Micro-interacción** | C1-C6. Valoración, me gusta, progreso | Prueba manual + háptica en dispositivo físico |
| **W4 — Superficie y datos** | D1-D5, E1-E5 | `JankStats`/Macrobenchmark: sin frames perdidos con el grano y los contadores activos |
| **W5 — Captación** | F1-F3 | Solo si se decide invertir en onboarding |

## 6. Decisiones abiertas para el propietario

1. **Inter vs grotesca con carácter** (Outfit/Satoshi/Geist) para titulares. Afecta a toda la app.
2. **Blanco puro**: documentar la excepción o cambiar a `#FCFCFD`.
3. **Densidad / Varianza / Movimiento**: los skills proponen ejes numéricos explícitos. Propuesta:
   densidad **6**, varianza **5**, movimiento **4** (por encima del baseline de densidad porque hay
   listas, por debajo de la varianza 8 porque en móvil la asimetría extrema rompe el pulgar, y
   movimiento 4 porque la animación perpetua es un impuesto de batería).
4. **¿Se acepta `industrial-brutalist-ui` acotado a Estadísticas?** Es el diferenciador visual más
   fuerte frente a Backloggd o HowLongToBeat, y el riesgo está contenido si no sale de ahí.
5. **¿Fase W0 antes que W1**, o se asume la deuda de acento/radios y se va directo al movimiento?
