# Rediseño UI 2026 — Contexto, propuestas y decisión

> Documento de origen de la fase de UI. Registra **de dónde venimos**, las **5
> direcciones propuestas** para llevar GameVision a "2026 peak design", la
> **recomendación senior** y la **decisión final tomada** — para que cualquier
> persona (o agente) pueda volver aquí, entender por qué se eligió cada cosa y
> cambiar de dirección a sabiendas si en el futuro se quiere otra.
>
> Fecha de decisión: **27/09/2026**. Estado del código en el que se tomó:
> `master` `752cb40` (post-upgrade-2026 completo, ver
> [previous-repo-state.md](previous-repo-state.md) y [AGENTS.md](../AGENTS.md)).

---

## 1. Contexto de origen

### 1.1 El recorrido

1. **TFM congelado (ene-feb 2025)**: app Compose funcional pero sin capa de
   diseño ni de datos — estado congelado en
   [previous-repo-state.md](previous-repo-state.md).
2. **Upgrade 2026 (27/09/2026)**: 4 fases + 4 hitos post-plan dejaron el
   proyecto en el stack 2026 (Gradle 9.6, AGP 9.4.1, Kotlin 2.2 integrado,
   compileSdk 37, Compose BOM 2026.09, Material 3 puro, Hilt + Repository,
   Credential Manager, kotlinx.serialization, R8, 11 tests). Trazabilidad en
   [upgrade-2026-changelog.md](upgrade-2026-changelog.md).
3. **Ahora: la capa visual.** El código está listo para rediseñarse sin riesgo.

### 1.2 Auditoría de la UI en el punto de partida

| Área | Estado | Diagnóstico |
|---|---|---|
| Paleta | Grises planos manuales (`Theme.kt`: #404040/#BFBFBF/#0D0D0D…) | Sin identidad de marca; el gris es decisión no tomada |
| Color dinámico | Ausente — toggle manual claro/oscuro | Android 2026 espera Material You como base |
| Tipografía | Default M3 (Roboto) | Sin personalidad; única familia sin escala propia |
| Componentes | Inline en pantallas, duplicados | 12 pantallas con archivos de 400-500 líneas (god-files) |
| Motion | Casi nulo: sin transiciones entre pantallas ni springs | No hay momento memorable |
| Carga | Spinners genéricos | El patrón explícitamente anti-2026 (skeletons) |
| Imagen | Portadas con `AsyncImage` simple | **Las portadas de juegos son el mejor activo visual y no se explotan** |
| Adaptativo | Solo teléfono | Sin list-detail, sin rail en tablet |
| Navegación | navigation-compose 2.10 funcional | Nav3 existe; migración reservada |

### 1.3 Skills disponibles que fundamentan las propuestas

- **Globales** (`~/.zcode/skills`): `gpt-taste` (tipografía editorial, motion,
  anti-patrones IA), `minimalist-ui` (monocromo cálido + acento pastel + bento),
  `redesign-existing-projects` (auditoría anti-genérica, skeletons),
  `design-taste-frontend` (sistemas de diseño reales, audit-first),
  `stitch-design-taste` (DESIGN.md como fuente única de verdad).
- **De proyecto** (`.zcode/skills`): `styles` (theming Compose con
  `Modifier.styleable`), `adaptive` (Nav3 Scenes, Grid/FlexBox),
  `navigation-3`, `edge-to-edge` (ya aplicada).

---

## 2. Las 5 direcciones propuestas (brainstorm)

### Modo A — "Expressive Nativo": Material 3 Expressive al límite

- **Filosofía:** máxima fidelidad a la plataforma. ADOPTAR el lenguaje oficial
  de Google 2026 (M3 Expressive): color dinámico Material You, sistema de
  shapes con morphing, motion con física de muelles, tipografía variable.
- **Cambios concretos:** `dynamicColorScheme()` con fallback de marca;
  shapes expresivos (button groups, chips asimétricos); springs globales;
  tipografía Roboto Flex variable; icono launcher temático (monochrome).
- **Skills:** `styles`, `edge-to-edge` (hecho). **Esfuerzo:** medio-bajo
  (1-2 sesiones). **Riesgo:** bajo (APIs oficiales, algunas en alpha).
- **Cuándo elegirlo:** si se prioriza "nativo Google 2026" y velocidad.

### Modo B — "Editorial Minimalista": el gris como identidad premium

- **Filosofía:** la app ya es gris — elevarlo a decisión editorial de alto
  diseño: monocromo cálido, jerarquía 100% tipográfica, bento grids,
  acento cromático único en microdosis, ultra-flat.
- **Cambios concretos:** tipografía con carácter (prohibida Inter según
  `gpt-taste`), bento en home, un spot color, whitespace generoso,
  filas de tarjetas iguales prohibidas (patrón más genérico de IA).
- **Skills:** `minimalist-ui`, `gpt-taste`, `redesign-skill`.
  **Esfuerzo:** medio (2 sesiones). **Riesgo:** medio (depende del gusto).
- **Cuándo elegirlo:** si se quiere distintivo, "de culto", anti-Material.

### Modo C — "Cinematográfico": la app como experiencia de medios

- **Filosofía:** GameVision es contenido visual (portadas, screenshots); el
  contenido ES la interfaz, como Netflix/Steam.
- **Cambios concretos:** shared element transitions (la portada vuela de la
  tarjeta al detalle — el momento wow), heros edge-to-edge con scrim y
  parallax, skeletons shimmer, motion staggered, crossfade Coil 3.
- **Skills:** `redesign-skill`, `gpt-taste`. **Esfuerzo:** medio-alto
  (2-3 sesiones). **Riesgo:** medio (motion mal dosificado pesa; medir).
- **Cuándo elegirlo:** si el objetivo es demo impactante de contenido audiovisual.

### Modo D — "Design-system-first": fundación de ingeniería

- **Filosofía:** ninguna dirección visual sobrevive sin chasis. No elige
  estética: construye la infraestructura que cualquier estética necesita.
- **Cambios concretos:** `DESIGN.md` (fuente única de verdad), paquete
  `ui/designsystem/` (theme, tokens de tipografía/formas/motion, componentes
  de negocio: GameCard, NewsCard, RatingBadge, Skeleton, EmptyState…),
  descomposición de los 5 god-files sin cambio visual, previews por componente.
- **Skills:** `design-taste-frontend`, `stitch-skill`. **Esfuerzo:** medio
  (2 sesiones). **Riesgo:** bajo (refactor verificable).
- **Cuándo elegirlo:** siempre, como paso 0 de cualquier rediseño serio.

### Modo E — "Adaptive 2026": list-detail, plegables y escritorio

- **Filosofía:** una app 2026 que solo mira el teléfono está a medias.
- **Cambios concretos:** migración a Navigation 3 con `ListDetailSceneStrategy`
  (lista y detalle lado a lado en pantallas grandes), `NavigationSuiteScaffold`
  (bottom bar ↔ rail automático), predictive back, tamaño de ventana.
- **Skills:** `adaptive`, `navigation-3`. **Esfuerzo:** alto (2-3 sesiones,
  incluye la migración Nav3 reservada en M4b). **Riesgo:** alto sin dispositivo
  para validar runtime.
- **Cuándo elegirlo:** si el objetivo incluye tablet/plegable o arquitectura completa.

### Comparativa

| Modo | Impacto visual | Esfuerzo | Riesgo | ¿Excluyente? |
|---|---|---|---|---|
| A Expressive | Medio | Bajo | Bajo | No — compatible con todos |
| B Editorial | Alto (identidad) | Medio | Medio | Alternativo a C como dirección |
| C Cinematográfico | Muy alto (wow) | Medio-alto | Medio | Alternativo a B como dirección |
| D Design system | Nulo (invisible) pero habilitante | Medio | Bajo | **Prerrequisito de A/B/C** |
| E Adaptive | Medio | Alto | Alto (sin dispositivo) | Fase final independiente |

---

## 3. Recomendación senior (27/09/2026)

**"Design-system-first cinematográfico con identidad editorial"** — el combo:

1. **D primero** (chasis: DESIGN.md + design system + descomposición).
2. **C encima** (cinematográfico: shared elements, heros, skeletons, motion).
3. **Elementos de A** integrados en el tema (dynamic color, tipografía variable,
   shapes expresivos) — son gratis con Expressive y eliminan el toggle manual.
4. **E al final**, cuando haya emulador/dispositivo para validar Nav3 en runtime.

De B se toma la disciplina (monocromo cálido + spot color + tipografía con
carácter + anti-genérico); de C, los momentos hero. No parece plantilla, se
siente premium y es sostenible.

## 4. Decisión final tomada

> **ElCombo recomendado se aprueba** (D → C → A → E), con dos decisiones de
> diseño concretas tomadas por el propietario el 27/09/2026:
>
> - **Acento cromático: verde ácido** (~`#C8F135`) sobre la base monocroma
>   cálida (carbón #0D0D0D / superficies #1A1A1A / texto #F2F2F2 / gris #8C8C8C).
>   Look gaming premium de alto contraste.
> - **Tipografía: grotesca variable** de carácter (Space Grotesk / Outfit),
>   única familia, jerarquía por tamaño y peso (display 700 tight vs body 400),
>   sin serif.
>
> Plan de ejecución aprobado: Fase 1 (design system) → Fase 2 (cinematográfico)
> → Fase 3 (expressive pulido) → Fase 4 (adaptativo, condicionada a dispositivo).
> Detalle de fases y verificaciones en el plan aprobado; commits por fase en
> rama `ui-redesign-2026` con merge a `master`.

Si en el futuro se quiere cambiar de dirección (p. ej. B puro sin cinematográfico,
o Expressive canónico de Google), este documento es el punto de partida: el
design system de la Fase 1 está diseñado para soportar cualquier estética.
