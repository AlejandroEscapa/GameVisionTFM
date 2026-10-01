# DESIGN.md — GameVision Design System

> **Fuente única de verdad** del lenguaje visual de GameVision (v2, 01/10/2026).
> Dirección: **galería limpia estilo Apple** — superficies blanco/pergamino/tile casi-negro,
> **un único azul interactivo**, tipografía Inter apretada, contenido (carátulas) como protagonista.
> Re-anclaje ejecutado según [ADR-0010](docs/metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md);
> documento fuente del sistema: [design-system-fuente-apple.md](docs/plan/design-system-fuente-apple.md).
> El look anterior (monocromo cálido + verde ácido) queda conservado en el
> [snapshot del diseño provisional](docs/plan/snapshot-design-provisional-2026-09.md) (tag
> `diseno-provisional-2026-09`). Cualquier cambio visual pasa por aquí primero.

## 1. Atmósfera

El chrome desaparece para que **la carátula del juego sea el color**. Interfaz de galería:
superficies planas alternando claro/oscuro — el cambio de superficie hace de divisor, sin bordes
decorativos, sin gradientes, sin sombras (la única sombra del sistema es para imágenes de producto
apoyadas en una superficie). Un solo acento interactivo: **Action Blue**. Si dudas sobre énfasis,
alterna superficie antes de añadir chrome.

## 2. Color

### Claro

| Rol | Token | Uso |
|---|---|---|
| Canvas / superficie | `#FFFFFF` | fondo dominante y tarjetas planas |
| Pergamino | `#F5F5F7` | tarjetas de utilidad, bloques alternos |
| Perla | `#FAFAFC` | botones "fantasma" secundarios |
| **Acento interactivo** | **`#0066CC` (Action Blue)** | TODO lo clicable: CTAs, enlaces, foco, chips seleccionados |
| Tinta | `#1D1D1F` | titulares, cuerpo, botón utilitario oscuro |
| Secundario | `#6E6E73` | copy secundario |
| Hairline | `#E0E0E0` · Divider `#F0F0F0` | bordes de tarjeta, separadores |
| Error | `#B3261E` | |

### Oscuro (derivado de los tiles, NUNCA invertido)

| Rol | Token | Uso |
|---|---|---|
| Fondo | `#0E0E10` | negro apagado, nunca puro |
| Superficies (escalera) | `#161619` → `#272729` (tile-1) → `#2A2A2C` (tile-2) | tarjetas y bloques |
| Texto | `#F5F5F7` · secundario `#CCCCCC` | pergamino sobre oscuro |
| **Acento** | **`#2997FF` (Sky Blue)** | interacción sobre oscuro (Action Blue desaparece) |
| Texto sobre acento | `#062033` | derivado, contraste AA |

Regla dura: **un solo acento por tema, en todo el tema**. Prohibido introducir un segundo color de
marca, glow, o sombras en tarjetas/botones/texto.

## 3. Tipografía

**Inter** (Google Fonts descargable) como sustituta de SF Pro — pesos **400 / 600 / 700**.
El peso **500 no existe** en el sistema.

| Estilo | Especificación | Uso |
|---|---|---|
| display | 40/600, línea 44, tracking 0 | héroe de pantalla |
| headline | 34/600 o 28/600, tracking −1,1 % | secciones |
| title | 21/600 (+1,1 % — el único tracking positivo) / 17/600 (−2,2 %) | tarjetas, encabezados de bloque |
| **body** | **17/400, línea 1.47, −2,2 %** | párrafos — el ritmo de lectura de la marca, nunca 16 |
| caption | 14/400 y 14/600 | secundarios, botones utilitarios |
| fine | 12/400 | legal, micro |

Reglas: titulares siempre peso 600 con tracking apretado ("Apple tight"); cuerpo nunca por debajo
de 17 sp en lectura; sin serifa, sin mezcla de familias; el énfasis dentro de un titular es
*cursiva o negrita de la misma familia*, nunca otra fuente.

## 4. Formas

Escala cerrada: **8** (utilidad compacta: badges, inputs pequeños) · **11** (perla, imágenes
inline) · **18** (tarjetas de utilidad, sheets) · **píldora** (CTA, chips de opción, búsqueda —
reservada a la ACCIÓN). Los bloques full-bleed son rectangulares: el cambio de color es el divisor.
No mezclar gramáticas de radio sin regla documentada.

## 5. Motion

Física de muelles (nunca tweens lineales) + la micro-interacción universal del sistema:
**escala 0.95 en estado pulsado** en todo botón. Entrada fade+slide 16 dp con emphasized
decelerate; máximo 3 elementos animando a la vez; respeta reducir-movimiento. Skeletons, cero
spinners.

## 6. Componentes (`ui/designsystem/`)

- **GVButton** — primario: píldora Action Blue texto blanco (oscuro: Sky Blue texto `#062033`);
  secundario: píldora outline.
- **GVChip** — píldora outline; seleccionada rellena del acento.
- **Tarjetas** — superficie de contenedor (pergamino claro / tile-1 oscuro), radio 18, SIN sombra:
  la separación la da el tono.
- **GameCover/GameCard** — la carátula manda: radio, sin borde, sin elevación.
- **GVSkeleton / EmptyState / OfflineBanner / RatingBadge / NewsCard / FriendAvatar** — mismos
  roles de tema; nada hardcodeado.
- Regla de convivencia (ADR-0009, vigente): nada visual fuera de `ui/designsystem/`.

## 7. Espaciado y pantallas (`GVSpacing`)

Primera escala de espaciado del sistema (iteración 02/10/2026): **antes no existía** y convivían
4/8/10/12/16/18/20/24 dp como margen de pantalla, mezclados dentro de la misma pantalla.

| Token | Valor | Uso |
|---|---|---|
| `screenPadding` | 16 dp | margen horizontal de TODA pantalla: título y contenido comparten línea |
| `headerGap` | 20 dp | aire entre el título de la cabecera y el primer elemento |
| `xs` / `sm` | 4 / 8 dp | separación dentro de un componente (icono↔texto, chips) |
| `md` | 12 dp | entre elementos de contenido (tarjetas, filas) |
| `xl` / `xxl` | 24 / 32 dp | separación entre secciones |

**Cabeceras (`GVScreenHeader`).** Una sola forma para toda la app:

- El título es **texto simple** — sin tarjetas, sin fondos, sin estilos propios copiados.
- Tipografía `headlineLarge`, tinta sobre superficie. Nada de variantes sueltas.
- **Cabecera FIJA**: vive FUERA del contenedor con scroll; solo se desplaza el contenido.
- `leading` (opcional) para acciones a la **izquierda** — ahí vive la vuelta ("Volver"), no en la
  zona de la derecha.
- `actions` (opcional) a la derecha (iconos de refresco, menús).

**Portadas.** El placeholder de `GameCover` (sin imagen) dibuja las iniciales **centradas y
escaladas al tamaño de la portada**; los títulos de juego usan `maxLines` **con `Ellipsis`** —
sin `overflow` Compose corta los glifos a media letra.

## 8. Do / Don't

**Do:** un acento; superficies planas alternando tono; cuerpo 17; peso 600 + tracking apretado en
titulares; píldora solo para acciones; hairline para bordes de tarjeta; contraste AA verificado en
ambos temas.

**Don't:** nada de verde ácido (histórico); nada de gradientes decorativos; nada de sombras en
UI (solo sobre imágenes apoyadas); nada de peso 500; nada de negros/blancos puros en fondo de
pantalla; nada de un segundo acento; nada de estilos fuera de `ui/designsystem/`.
