# ADR-0012 — Alcance del acento: Action Blue es para lo que se toca

- **ADR:** 0012
- **Título:** El acento del sistema se reserva a acción e interacción; los datos se jerarquizan por tamaño y peso, no por color
- **Estado:** Aceptado
- **Fecha:** 2026-10-02
- **Decisores:** propietario (Alejandro)
- **Relacionado:** [ADR-0010](0010-reanclaje-adelantado-fuente-recibida.md) (re-anclaje al sistema Apple — esto **define** su "único acento interactivo"), [ADR-0009](0009-reanclaje-design-system.md) (regla de convivencia)
- **Fase relacionada:** transversal al design system; afecta a F4.5 (auditoría de experiencia)

## Contexto

El re-anclaje (ADR-0010) adoptó del documento fuente una regla explícita: **un solo acento
interactivo, Action Blue**. `DESIGN.md` la recogió como "TODO lo clicable".

La auditoría de diseño del 02/10/2026 encontró que el código había ido más allá: el acento se usaba
**también como color informativo**. Cinco casos, ninguno interactivo:

| Sitio | Uso real |
|---|---|
| Estadísticas | las cifras grandes de cada KPI |
| Diario | los totales del resumen |
| Noticias | el nombre del medio |
| Steam | el resumen de éxito de una sincronización |
| Varias tarjetas | los antetítulos ("Biografía", "Tu Top 4", "Mis listas") y los iconos de metadatos |

El efecto es el contrario del que buscaba el ADR-0010: **si el acento pinta datos, deja de señalar lo
que se puede pulsar**, que es exactamente lo que la regla del único acento existía para proteger. Es
una decisión de sistema, no de pantalla: cualquier pantalla nueva volvería a debatirla.

## Decisión

**El acento es para lo que se TOCA.**

1. **Con acento** — CTAs y botones, enlaces de texto, chips seleccionados, foco de campo y buscador,
   iconos interactivos (engranaje de Ajustes, cuadro de acción del perfil, ✕ de quitar del Top 4),
   indicadores de selección activos ("me gusta" marcado, check del menú de orden) y barras de
   progreso activas.
2. **Sin acento** — **datos** (cifras, totales, notas, fuentes de noticias), **iconos decorativos**
   (los que acompañan a una etiqueta que ya dice lo mismo), **antetítulos de tarjeta** y
   **resúmenes de éxito o estado**. Van a `onSurface` o `onSurfaceVariant`.
3. **La jerarquía de un dato se da por tamaño y peso**, no por color. `displayLarge` y
   `headlineLarge` estaban definidos y sin usar en toda la app: son el recurso para el número
   editorial. Las cifras de Estadísticas y Diario suben a `headlineLarge` en tinta normal.
4. **Antipatrón explícito:** `color = colorScheme.primary` en un `Text` que no se pulsa. Ante la
   duda, la pregunta es una: ¿el usuario puede tocar esto? Si no, es tinta.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| A. Dejarlo como estaba (acento para acción y dato) | Cero trabajo; más colorido | El acento deja de señalar lo pulsable; contradice la regla del "único acento" que el propio ADR-0010 escribió | Es el estado que la auditoría marcó como la mayor dilución del re-anclaje |
| B. **Acento solo acción, dato en tinta con jerarquía por tamaño** (elegida) | El acento recupera su función; menos chrome; usa estilos que ya existían | Hay que tocar 5 pantallas | — |
| C. Un tercer color para "dato editorial" | Mantiene color en los datos | Añade un token al tema y hay que calibrarlo en claro y oscuro; introduce un segundo acento de facto | "Un solo acento por tema" es regla dura del sistema |

## Consecuencias

**Positivas**
- El acento vuelve a significar una sola cosa: **aquí se pulsa**.
- Menos color por pantalla: la galería se lee más limpia y las portadas mandan (objetivo de atmósfera).
- Se aprovecha la escala tipográfica que estaba sin usar, lo que sube la sensación editorial.

**Negativas / riesgos asumidos**
- Las pantallas de datos (Estadísticas, Diario) quedan más sobrias: el "peso visual" pasa a depender
  del tamaño. Si el propietario las ve frías, el ajuste es de tamaño/tono, **no** de volver a pintar
  el dato de azul (eso reabriría este ADR).
- Es una regla que hay que vigilar en cada pantalla nueva: la revisión de UI debe comprobar que
  ningún dato lleve acento.

## Verificación

- Ejecutado el 02/10/2026 (commit `ef1eccf`): cifras de Estadísticas y Diario a `headlineLarge` en
  tinta, fuente y fecha de noticia a `onSurfaceVariant`, antetítulos e iconos decorativos a tinta.
- `testDebugUnitTest` + `lintDebug` en verde y verificación en emulador (Pixel 9, API 36).
- Regla escrita en [DESIGN.md](../../DESIGN.md) §2 ("Alcance del acento") y en §8 (Do/Don't).
