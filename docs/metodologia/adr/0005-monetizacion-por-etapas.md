# ADR-0005 — Monetización por etapas con disparadores medibles

- **ADR:** 0005
- **Título:** GameVision se monetiza por etapas (gratis+donaciones → ads+pago único → premium anual), cada etapa activada por un disparador medible
- **Estado:** Aceptado
- **Fecha:** 2026-09-29
- **Decisores:** Alejandro Olivares Escapa
- **Supersede a:** — (concretifica la recomendación D-C6 del [Plan Maestro](../../plan/PLAN-MAESTRO-2026.md) y la D-E2 de la [síntesis](../../investigacion-2026/sintesis-2026.md); las reglas de oro del core gratuito ya estaban en §3.5 de ese plan)
- **Fase relacionada:** F5 (Publicación) — vertiente V4 (Comercial)

## Contexto

La vertiente comercial estaba investigada pero sin decidir. La evidencia de mercado
([dimension 06 — modelos de negocio](../../investigacion-2026/fuentes-competencia/06-modelos-negocio.md))
establece: el ancla de precio de un premium de nicho es **19–50 $/año** (Letterboxd Pro 19 $);
Backloggd sostiene ~650 K usuarios **solo con Patreon**, sin ads ni app nativa; Trakt demuestra
el coste reputacional de cobrar/encarecer lo que era gratis (subida 30→60 $/año + límites a
listas free, 2025); los eCPM de anuncios **no son fiables** como garantía de ingresos (contradicción
declarada en la propia síntesis §8); y el público del nicho es hardcore y sensible a la deriva
publicitaria. La constitución de producto (Plan Maestro §3.5) ya fija: registro, listas, feed y
Rewind básico **gratis para siempre**, y nunca cobrar lo que antes era gratis.

## Decisión

Monetización **por etapas**, cada una activada por un disparador medible:

| Etapa | Modelo | Disparador de activación |
|---|---|---|
| 1 · Lanzamiento | Core completo gratis + donaciones/Patreon opcional | Al publicar (F5) |
| 2 · Crecimiento | + AdMob discreto (banner/native/rewarded opcional, **sin** interstitial ni app-open) con UMP desde el día uno + **«quitar anuncios» pago único** (Play Billing v8) | ≈ **1.000 usuarios activos** (mismo umbral que la auditoría de costes) o riesgo de sostenibilidad |
| 3 · Escala | + **Premium anual ≈ 19 $/año** (stats avanzadas, cosméticos, export extra) | Base de usuarios que vuelve tras F2/F3, y costes recurrentes que el pago único no cubre |

Nunca: cobrar el registro, las listas, el feed o el Rewind básico; cobrar lo que antes era
gratis; «bait-and-switch» de precios. Absorbe la D-E3 (orden de formatos de anuncio) como
detalle de la etapa 2.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| Ads + pago único desde el día 1 (D-E2 literal) | Ingresos desde el lanzamiento | eCPM trivial con pocos usuarios; UMP+Billing = superficie extra antes de F1; riesgo de marca en nicho hardcore | El ingreso no justifica el coste reputacional ni el trabajo adelantado |
| Premium anual desde el día 1 | Ingreso recurrente temprano | Cold-start: no hay base a quien venderle stats avanzadas | Paywall sobre producto sin comunidad |
| Solo micromecenazgo (Backloggd puro) | Marca impecable, cero ads nunca | Techo de ingresos bajo y dependiente de la buena voluntad | Se mantiene como etapa 1; se reserva el resto del camino si los datos lo piden |

## Consecuencias

**Positivas**
- Cada palanca se activa con datos reales, no con fechas: no hay ads «porque sí» en un nicho que los castiga.
- Backloggd valida la etapa 1 en este nicho exacto; Letterboxd valida el ancla de precio de la etapa 3.
- El core gratuito queda blindado por decisión, no por promesa.

**Negativas / coste asumido**
- Los costes fijos (cuenta Play, Firebase) los asume el propietario hasta el disparador de etapa 2.
- Tres disparadores que vigilar; si se ignoran, la decisión se degrada a «algún día».

**Riesgos y mitigación**
- Que ~1.000 usuarios activos llegue antes de que F5 esté lista la infraestructura de anuncios → la etapa 2 se prepara como parte del hito de publicación, se *activa* con el disparador.
- Deriva de precio en el futuro (lección Trakt) → cualquier cambio de precio o de qué es gratis requiere ADR nuevo.

## Seguimiento

- Vigilar el disparador de etapa 2 en el análisis de datos que se instrumente al cerrar F1 (Analytics).
- Condición de revisión: costes mensuales sostenidos por encima de lo asumible, o ~1.000 usuarios activos; en cualquiera de los dos casos, activar etapa 2 o escribir ADR que supersede a este.
