# ADR-0013 — El contrato visual manda: precedencia ante el corpus de diseño externo

- **ADR:** 0013
- **Título:** `DESIGN.md` es el contrato y prevalece sobre cualquier skill de diseño externa; las excepciones se enmiendan por escrito, no se improvisan
- **Estado:** Aceptado
- **Fecha:** 2026-10-02
- **Decisores:** propietario (Alejandro)
- **Relacionado:** [ADR-0009](0009-reanclaje-design-system.md) (regla de convivencia), [ADR-0010](0010-reanclaje-adelantado-fuente-recibida.md) (re-anclaje), [ADR-0012](0012-alcance-del-acento.md) (alcance del acento)
- **Fase relacionada:** F4.5 (cierra **DX.1–DX.7**); transversal al design system

## Contexto

El propietario mantiene un **corpus de skills de diseño** (seis de contenido propio más una librería
ampliada) y pidió usarlas como base de conocimiento para auditar el producto. Al contrastarlas con
`DESIGN.md`, el resultado fue que **se contradicen entre sí en ocho puntos concretos**:

| `DESIGN.md` dice | El corpus externo dice |
|---|---|
| §7 cabecera **FIJA** fuera del scroll | Ocultar app bars al hacer scroll (`exitUntilCollapsed`/`enterAlways`) |
| §1 y §8 **nada de sombras** en UI | Cuatro sombras escaladas de "Dimensional Layering" |
| §1 y §8 **nada de gradientes** | `background`/`contentBrush` con `Brush`; cristal con blur |
| §5 física de muelles, **nunca tweens** | `animate {}` sin especificación |
| §5 pulsado **encoge** a 0.95 | Al pulsar **crece** a `scale(1.2f)` |
| §3 **sin mezcla de familias** | Inter + Russo One + Fira Code |
| §7 la vuelta vive en el `leading` | Las pantallas de detalle no muestran flecha de volver |
| (no fija breakpoints) | Breakpoints con nombre y comportamiento por clase de ventana |

Además, una auditoría previa (no esta) propuso **cinco ideas que incumplían el propio contrato**:
cabecera colapsable, `tween` con retardo, prohibición de animar `size`, sombras y grano global, y
monoespaciada para cifras. Eso demuestra el riesgo real: **el corpus externo es útil como corrector
de sesgos y peligroso como autoridad**, porque no conoce ni el producto ni su contrato.

Dato que inclina la decisión: los skills de diseño web **declaran la app nativa fuera de su alcance**
(`design-taste-frontend` §13: *"Native mobile (use Apple HIG / Material directly)"*). El único de
alcance real sobre Android es `ui-ux-pro-max`, que trae 52 guías específicas de Jetpack Compose.

## Decisión

**`DESIGN.md` es el contrato. El corpus externo es material de consulta, nunca autoridad.**

1. **Precedencia.** Ante conflicto, gana `DESIGN.md`. Una idea externa solo entra si (a) es
   compatible, o (b) se **enmienda `DESIGN.md`** por escrito con su porqué y su fecha. No hay tercera
   vía: "lo dice el skill" no es un argumento.
2. **Sombras y profundidad.** Se mantiene **nada de sombras en UI**. La profundidad en modo oscuro
   se resuelve con la **escalera de superficies** que ya existe (`#161619` → `#272729` → `#2A2A2C`),
   no con elevación. La única sombra del sistema sigue siendo la de una imagen de producto apoyada
   en una superficie.
3. **Una sola familia tipográfica.** Se mantiene **sin mezcla de familias**. Las **cifras tabulares**
   —necesarias para que los contadores no bailen al animar— se consiguen con
   `fontFeatureSettings = "tnum"` sobre la misma familia, **no** añadiendo una monoespaciada.
   La sustitución de Inter por una grotesca con más carácter **no se decide aquí**: es una decisión
   de dirección y exige su propio ADR.
4. **Breakpoints explícitos.** `DESIGN.md` no fijaba ninguno y eso dejaba el comportamiento
   adaptativo a la improvisación de cada pantalla. Se declaran los tres de Material 3 con las
   constantes que el código **ya usa** (`WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND`), sin inventar
   valores nuevos.
5. **Cabecera fija.** Se mantiene §7: la cabecera vive fuera del scroll. Queda **descartada** la
   cabecera colapsable, y con ella cualquier efecto que dependa de que la cabecera se vaya.
6. **Motion.** Se mantiene muelles sobre tweens, con la precisión de qué tween sí está permitido
   (opacidad y entradas/salidas con los easings del sistema). Se añaden las formas **permitidas**
   que el contrato no nombraba (`animateContentSize`, `graphicsLayer`, stagger con retardo sobre
   muelle) y se cierra el gate de **movimiento reducido** como obligatorio y conectado al sistema.

El corpus se conserva en `.skills/` (fuera del repo) con sus destilados en `.skills/_digests/`, y su
uso declarado: **corrector de sesgos en superficies de marca y web**, y `ui-ux-pro-max` como fuente
de guías de Compose.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| A. Adoptar el corpus externo como autoridad | Un solo marco, sin debates | Contradice el re-anclaje recién ejecutado (ADR-0010) y obliga a rehacer tokens y pantallas; los skills web no conocen el producto | Coste altísimo y reabre una decisión cerrada hace un día |
| B. **Contrato manda; el corpus corrige sesgos y las excepciones se enmiendan** (elegida) | Cero trabajo tirado; mantiene la coherencia; deja entrar lo bueno por la puerta documentada | Exige disciplina: cada idea externa necesita veredicto | — |
| C. Ignorar el corpus | Cero ruido | Se pierden hallazgos reales (52 guías de Compose, micro-interacciones de 50-100 ms, benchmark competitivo) | El corpus aportó los dos mejores hallazgos de la auditoría |

## Consecuencias

**Positivas**
- El contrato deja de ser ambiguo: hay un criterio escrito para cada conflicto futuro.
- Las cinco ideas que incumplían `DESIGN.md` quedan resueltas (enmendadas o descartadas) en lugar de
  ejecutarse por inercia.
- `DESIGN.md` gana tres huecos que sí tenía: **breakpoints**, **qué tween se permite** y **cómo se
  hacen cifras tabulares sin romper la regla de una sola familia**.

**Negativas / riesgos asumidos**
- El modo oscuro se queda sin sombras: si el propietario lo ve plano, la vía es **extender la
  escalera de superficies**, nunca introducir elevación (eso reabriría este ADR).
- Inter se mantiene aunque dos de los skills la desaconsejen. Es deliberado: cambiarla es una
  decisión de dirección con su propio ADR, y hacerlo ahora invalidaría la capa de movimiento que
  todavía no existe.
- La disciplina de "enmienda por escrito" depende de que la revisión de UI la exija.

## Verificación

- Los cuatro conflictos quedan escritos en [DESIGN.md](../../DESIGN.md): §3 (familias y cifras
  tabulares), §4 y §8 (sombras y profundidad), §5 (motion permitido y movimiento reducido) y §7
  (breakpoints).
- `DX.1`–`DX.7` de [F4.5](../roadmap/fase-4-5-diseno-animaciones.md) cerradas con fecha.
- Las cinco ideas que incumplían el contrato quedan marcadas en
  [brainstorm-wow-2026.md](../plan/brainstorm-wow-2026.md).
- `node scripts/check-docs/check-docs.js` sin deriva.
