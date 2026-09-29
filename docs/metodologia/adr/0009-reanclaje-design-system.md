# ADR-0009 — Re-anclaje del design system visual

- **ADR:** 0009
- **Título:** GameVision adoptará un nuevo design system; `DESIGN.md` pasa a provisional
- **Estado:** Aceptado (pendiente de recibir la fuente; el re-anclaje se ejecuta en F4.5)
- **Fecha:** 2026-09-29
- **Decisores:** propietario (Alejandro)
- **Fase relacionada:** F4.5 (Diseño, animaciones y auditoría de experiencia); afecta a V2

## Contexto

La dirección visual actual se decidió el 27/09/2026 y está implementada: `DESIGN.md` es la fuente
única de verdad y `ui/designsystem/` (14 archivos: `GameVisionTheme`, `GVTypography`, `GVShapes`,
`GVMotion`, `GVSharedTransition` y los componentes `GameCard`, `GameCover`, `NewsCard`,
`RatingBadge`, `FriendAvatar`, `GVButton`, `GVChip`, `GVSkeleton`, `EmptyState`, `OfflineBanner`)
ya está en el código.

El propietario ha comunicado que **GameVision usará otro design system** y que aportará su
documento (Markdown) cuando corresponda. Es decir: la identidad visual está **congelada como
provisional**, no descartada. El proyecto necesita una decisión registrada para que ni un humano ni
un agente futuro traten `DESIGN.md` como definitivo, y para que el trabajo que se haga mientras
tanto no se tire.

La [auditoría UI 2026](../../plan/ui-adopcion-hallazgos-2026.md) añade el contexto técnico: el hueco
real de la interfaz no es la identidad, sino la **superficie de catálogo** (0 `LazyRow`,
0 `LazyVerticalGrid`) y el **estado modelado** (0 `UiState`, 40 `collectAsState`). Eso es agnóstico
al design system.

## Decisión

**GameVision re-anclará su lenguaje visual al design system que aportará el propietario.** Hasta
recibirlo:

- `DESIGN.md` queda **provisional** (referencia vigente, ya no definitiva) y se marca como tal.
- El **re-anclaje** (sustituir tokens, tipografía, forma, componentes y motion) se ejecuta en
  **F4.5**, como decisión **DX.8** y bloque de tareas **DX-T23…DX-T28** de
  [fase-4-5](../../roadmap/fase-4-5-diseno-animaciones.md).
- Todo el trabajo **agnóstico al sistema** (estados `UiState`, rutas tipadas, paginación,
  estructura de la superficie de catálogo y la higiene Compose) **sigue adelante sin esperar** al
  documento entrante.
- **Regla de convivencia:** mientras no llegue el sistema nuevo, no se introducen tokens ni
  componentes visuales "a mano" fuera de `ui/designsystem/`. Así la migración será de tokens y
  componentes, **no de pantallas**.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| A. Mantener el design system actual y no re-anclar | Cero coste; ya está implementado | Ignora una decisión explícita del propietario | El propietario ya ha decidido lo contrario |
| B. Re-anclar ahora, con un sistema provisional propio | Avance inmediato | Trabajo que probablemente se reharía al llegar el documento real | Doble coste sin beneficio |
| C. **Re-anclar al sistema entrante, difiriendo la estética y avanzando lo agnóstico** (elegida) | No se tira nada; la migración es de tokens/componentes | La app queda "provisional" visualmente hasta F4.5 | Es el orden que menos desperdicia |

## Consecuencias

**Positivas**
- El trabajo no agnóstico (estados, arquitectura de navegación, superficie de catálogo) avanza ya.
- La migración futura queda acotada a `ui/designsystem/` y a los tokens, no a las 12 pantallas.

**Negativas / coste asumido**
- La identidad visual no se considerará definitiva hasta F4.5.
- Hay que mantener la disciplina de no crear estilos "a mano" fuera del design system.

**Riesgos y mitigación**
- **Riesgo:** que el documento entrante cambie supuestos estructurales. **Mitigación:** mantener la
  UI desacoplada de tokens concretos (todo pasa por `GVTheme`/`GVTypography`/`GVShapes`).
- **Riesgo:** que `DESIGN.md` y el sistema entrante convivan y se contradigan. **Mitigación:** al
  recibirlo, `DESIGN.md` se sustituye (DX-T23) y se retiran los restos del anterior (DX-T28).

## Seguimiento

- **Condición de disparo para completar este ADR:** recepción del documento del nuevo design system.
- Al ejecutarse: marcar DX-T23…DX-T28 y CA4.5.7 en F4.5, actualizar `DESIGN.md` como fuente única y
  cerrar este ADR con un "Actualización" (a la manera de ADR-0004).
- Revisar la regla de convivencia si, antes de F4.5, aparece una pantalla nueva que necesite estilo.
