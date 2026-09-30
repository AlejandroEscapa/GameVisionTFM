# ADR-0010 — Re-anclaje del design system adelantado: fuente recibida

- **ADR:** 0010
- **Título:** El documento del design system nuevo llegó: el re-anclaje de tokens y componentes se ejecuta AHORA (antes de F3), no en F4.5
- **Estado:** Aceptado
- **Fecha:** 2026-10-01
- **Decisores:** propietario (Alejandro)
- **Supersede parcial:** [ADR-0009](0009-reanclaje-design-system.md) (su condición "pendiente de recibir la fuente" queda satisfecha)
- **Fase relacionada:** F3 (nace con el look final) y F4.5 (pierde el re-anclaje, conserva auditoría de experiencia + animaciones)

## Contexto

El [ADR-0009](0009-reanclaje-design-system.md) decidió que GameVision adoptará un design system
nuevo, posponiendo el re-anclaje a F4.5 **porque la fuente aún no existía**. El 01/10/2026 el
propietario aportó la fuente: **análisis del design system Apple** (tokens YAML + principios:
paleta blanco/pergamino/tiles casi-negro con **Action Blue `#0066cc`** como único acento
interactivo, tipografía peso 600 con tracking negativo, radios 8/11/18/píldora, filosofía
"fotografía primero, chrome invisible").

F3 fabricará las piezas más visibles del producto (tarjeta del Rewind, imagen compartible,
pantalla de recomendación). Construir esas piezas con el look provisional para re-estilizarlas
en F4.5 era el desperdicio que el ADR-0009 quería evitar: su condición de espera ha desaparecido.

## Decisión

**Adelantar el re-anclaje a un bloque propio antes del trabajo visual de F3:**

1. **Mapeo de tokens** del documento a `ui/designsystem/`: `GVTheme`, `GVTypography`,
   `GVShapes`, `GVMotion`. Fuente: **Inter** (variable, Google Fonts) como sustituta de SF Pro,
   según recomienda el propio documento para plataformas no-Apple.
2. **Re-estilizado de los ~14 componentes** existentes (losGV*: GameCard, GameCover, GVButton,
   GVChip, GVSkeleton, EmptyState, OfflineBanner…). Como todas las pantallas los consumen, la
   migración es de tokens y componentes, **no de pantallas** (promesa del ADR-0009 que esto cumple).
3. **Dark mode derivado** (regla ya vigente del análisis competitivo: "derivado, no invertido"):
   superficies `#272729/#2a2a2c`, texto pergamino `#f5f5f7`, enlaces `#2997ff`
   (`primary-on-dark` del documento). Requiere validación visual del propietario.
4. **F3 en paralelo:** T3.1 (motor de recomendación, lógica pura) no depende del look y arranca
   sin esperar; las piezas visuales nuevas (tarjeta del Rewind, tarjeta de recomendación, render
   de imagen compartible) **nacen directamente con el sistema nuevo** como componentes
   `GV*` en `ui/designsystem/` (regla de convivencia del ADR-0009, vigente).
5. **F4.5 se redefini:** conserva auditoría de experiencia, animaciones y pulido; pierde el
   bloque de re-anclaje (DX-T23…DX-T28 se marcan como ejecutados antes, con este ADR como
   referencia).

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| A. Mantener el plan del ADR-0009 (re-anclar en F4.5) | Cero cambio de plan | F3 entera con look provisional que se rehace; el Rewind es LA pieza viral y nacería dos veces | El gatillo del aplazamiento (fuente pendiente) ya no existe |
| B. Re-anclar solo lo que toca F3, el resto en F4.5 | Menos trabajo inicial | Dos sistemas conviviendo = el estado mixto que la regla de convivencia prohibía | Peor que A |
| C. **Re-anclaje completo de tokens y componentes ahora** (elegida) | F3 nace final; una sola migración; F4.5 se aligera | 1-2 días de trabajo antes del producto | El coste es menor que rehacer la fase demo |

## Consecuencias

**Positivas**
- El Rewind y la recomendación — la demo que impresiona — nacen con el look definitivo.
- El defecto documentado de contraste AA del verde ácido desaparece (un solo acento verificado).
- Señal premium coherente con el modelo de monetización (D-C6) y la captación ASO (D-C7).

**Negativas / riesgos asumidos**
- El documento no trae variante dark: el mapeo derivado necesita validación visual del propietario.
- Action Blue es un acento familiar: la memorabilidad de marca recae en tipografía, motion y
  tarjetas del Rewind (anotado como riesgo de marca).

## Verificación

- Build debug/release + suite en verde tras el re-estilizado (nada funcional cambia).
- Capturas antes/después de las pantallas principales en claro y oscuro, para validación del propietario.
