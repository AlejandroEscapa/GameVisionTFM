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

1. **Dentro del repo.** 26 skills locales en `.zcode/skills/` sin usar, entre ellas **`styles`**
   (la guía de estilo del propio proyecto) y **`display-glasses-with-jetpack-compose-glimmer`**
   (364 KB sobre animación en Compose). Auditar la app contra skills externas mientras se ignoran
   las del repo es un error de método.
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

### Decisiones abiertas que este trabajo añade

- Estantería con lomos: la referencia la quiere en **acento**; ADR-0012 dice que el acento es solo
  para lo que se **toca**. Un lomo es contenido. **Choca.**
- Material You / color dinámico: el modo A de la investigación lo proponía; `DESIGN.md` lo prohíbe
  por la regla del acento único. Ganó `DESIGN.md`, y conviene dejar de citarlo como si siguiera vivo.
- **Onboarding TTFV < 60 s** ("elegir 3 juegos ya jugados") está decidido en la investigación y **no
  aparece como tarea de ninguna fase**. Es captación, y no tiene dueño.
- La investigación sigue afirmando que "monocromo cálido + `#C8F135` + Space Grotesk se mantiene".
  Está **superada por ADR-0010**, pero el texto sigue ahí y contradice a `DESIGN.md`. Cita muerta que
  hay que retirar o marcar como histórica.

## 7. Estado de esta iteración

Producidos en `.skills/_digests/`:

- `06-investigacion-ux-competencia.md` (30,6 KB) — benchmark competitivo, decisiones ya tomadas,
  huecos, y la separación entre "¿está aplicado el sistema?" y "¿es el producto correcto?".

En producción: `05-skills-locales-repo.md` (skills `styles` y `adaptive` del propio repo),
`07-ui-ux-pro-max-aplicado.md` (los CSV de estilos, tipografías, paletas y guías UX) y
`08-glimmer-motion.md` (la skill local de animación para Compose).

Cuando aterricen, este documento se cierra con: las reglas del skill `styles` del repo, los estilos
candidatos de `styles.csv`, el catálogo de gráficos para un tracker y el veredicto sobre Glimmer como
acelerador de la capa de movimiento.
