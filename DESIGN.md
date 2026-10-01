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

### Alcance del acento (decidido el 02/10/2026)

> Contexto: la auditoría de diseño encontró que Action Blue se usaba **también como color
> informativo** — los números de Estadísticas, los totales del Diario, la fuente de una noticia, el
> resumen de éxito de Steam y los antetítulos de tarjeta. Ninguno de esos elementos es interactivo,
> y pintarlos de azul diluía justo lo que el re-anclaje quería proteger.

**El acento es para lo que se TOCA.** Con acento:

- CTAs y botones (`GVButton`, botones de formulario), y su estado pulsado.
- Enlaces de texto ("He olvidado mi contraseña", "Regístrate").
- Chips de opción seleccionados, focos de campo y el buscador (`GVSearchField`).
- Iconos y elementos **interactivos**: el engranaje de Ajustes, el cuadro de acción del perfil, la ✕
  de quitar del Top 4, los indicadores de selección activos (check del menú de orden, "me gusta"
  marcado) y las barras de progreso **activas**.

**Sin acento** (tinta normal o `onSurfaceVariant`):

- **Datos**: cifras de Estadísticas, totales del Diario, nota de un juego, fuente de una noticia.
  La jerarquía se da por **tamaño y peso** (`displayLarge`/`headlineLarge` están para esto), nunca
  por color.
- Iconos decorativos: los que acompañan a una etiqueta que ya dice lo mismo (metadatos de la ficha,
  ítems de Ajustes, iconos de campo de texto).
- Antetítulos de tarjeta ("Biografía", "Tu Top 4", "Mis listas"): son etiquetas, no acciones.
- Resúmenes de éxito o estado: un resultado no es un botón.

**Antipatrón a evitar:** `color = colorScheme.primary` en un `Text` que no se pulsa. Si dudas,
pregúntate si el usuario puede tocar ese elemento; si no, es tinta.

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

> **El tracking negativo es SOLO para display y titulares (≥17 sp).** Por debajo de 17 sp el
> tracking es `0`: en tamaños pequeños el negativo hacía que el primer glifo sobresaliera del área
> de texto y quedara recortado contra el contenedor (bug detectado el 01/10: "Limbo" se veía como
> "┐imbo" en las carátulas). Regla del documento fuente, ahora además comprobada en el emulador.

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
- **GVSearchField** — **el ÚNICO buscador del sistema** (02/10/2026). Píldora, borde `outlineVariant`
  en reposo y acento al enfocar; lupa a la izquierda y, a la derecha, lo que toque: progreso mientras
  busca, ✕ para limpiar o el disparador manual. Lo usan Buscar, Top 4, Social y Amigos.
  *Motivo de existir:* había **cuatro** buscadores distintos (uno relleno con sombra de 4 dp y
  esquinas de 24, otro píldora sin sombra, otro a 12 dp…). La misma acción resuelta de tres formas es
  exactamente lo que rompe un design system. **No se crea otro campo de búsqueda.**
- **Tarjetas** — superficie de contenedor (pergamino claro / tile-1 oscuro), radio 18, SIN sombra:
  la separación la da el tono.
- **GameCover** — la carátula manda: radio, sin borde, sin elevación.
- **GVSkeleton / EmptyState / OfflineBanner / RatingBadge / FriendAvatar** — mismos
  roles de tema; nada hardcodeado. `RatingBadge` usa el icono `Lucide.Star`, nunca el carácter "★"
  (un glifo de otra fuente en el mismo renglón desalinea la línea tipográfica).
- **Regla de convivencia (ADR-0009, vigente): nada visual fuera de `ui/designsystem/`.**

> **Código muerto a no resucitar.** `ui/designsystem/components/GameCard.kt` y `NewsCard.kt` **no se
> usan** (sus pantallas tienen su propia implementación). Están marcados por la auditoría del
> 02/10/2026: antes de "arreglarlos" o adoptarlos, decidir si se consolidan o se borran. Un
> componente muerto en el design system es peor que uno muerto en una pantalla, porque invita a
> usarlo y a multiplicar el problema.

**Componentes de superficie** (viven junto a su pantalla porque son contenido, no vocabulario
compartido; consumen tokens y componentes del sistema, jamás colores sueltos):
`ui/views/composables/profile/`

- **TopGamesCard** — el cuadro del Top 4: fila de **4 slots siempre visibles** (portada con número
  de posición, o hueco con `+`), contador `n/4` y **buscador de catálogo** en píldora. Un solo
  componente para los dos modos: `editable = true` (tu perfil: buscador + ✕ por slot) y
  `editable = false` (perfil de otro: solo lectura).
- **MisListasSection / CrearListaDialog / TarjetaCrearLista** — listas curadas: sección del perfil
  con su acción, **un único diálogo** para perfil y Home, y la entrada ligera de «Para ti».
- **ListaPublicaCard** — lista en lectura, dentro del perfil de otro.

**Buscador en píldora.** El campo de búsqueda del sistema es **`GVSearchField`**: píldora
(`GVShapeFull`), borde `outlineVariant` en reposo y acento al enfocar. La píldora está reservada a la
ACCIÓN: buscadores, chips y CTA. Es la firma del sistema.

**Búsqueda EN VIVO (02/10/2026).** Los buscadores de la app buscan **mientras se escribe**, con
debounce (~350 ms) y un mínimo de 2 caracteres: por debajo de ese mínimo se limpia y se vuelve al
descubrimiento, sin tocar la red (una consulta de 1 letra devuelve relleno y gasta cuota). El icono
de lupa se queda para forzar la búsqueda ya y cerrar el teclado, **no** para que ocurra algo. Un
buscador que no responde hasta pulsar se siente roto; es criterio de sistema, no de una pantalla.

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

> **Deuda abierta (auditoría del 02/10/2026):** la escala existe, pero **no está aplicada en todas
> las pantallas**. Login/Pass/Register/Onboarding siguen a **24** y el perfil a **20** en lugar de
> `screenPadding`; dentro de `EditProfileScreen` el título va a 16 y sus tarjetas a 24, así que **no
> comparten línea** — que es la primera regla de `GVScreenHeader`. El relleno de tarjeta también
> varía (12/16/20/24) y debería ser uno solo. Barrido pendiente, listado en
> [auditoría de diseño](../docs/plan/auditoria-diseno-2026-10-02.md).

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

**Do:** un acento **y solo para lo que se toca**; superficies planas alternando tono; cuerpo 17;
peso 600 + tracking apretado en titulares; píldora solo para acciones; jerarquía de un dato por
**tamaño y peso**, nunca por color; hairline para bordes de tarjeta; contraste AA verificado en ambos
temas; cero textos en inglés; iconos **Lucide** (nunca emoji ni caracteres como "★" o "✓" haciendo de
icono).

**Don't:** nada de verde ácido (histórico); nada de gradientes decorativos; nada de sombras en
UI (solo sobre imágenes apoyadas); nada de peso 500; nada de negros/blancos puros en fondo de
pantalla; nada de un segundo acento; nada de estilos fuera de `ui/designsystem/`; ningún dato
pintado con el acento; ningún `substring` con índices fijos sobre datos de red (RAWG deja campos
vacíos: un `StringIndexOutOfBounds` dentro de un `LazyColumn` deja la lista entera sin pintar); ningún
componente nuevo que duplique uno existente (ver "código muerto" en §6); ningún `e.message` de una
excepción enseñado al usuario.
