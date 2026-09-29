# DESIGN.md — GameVision Design System

> ⚠️ **PROVISIONAL (29/09/2026).** El propietario ha confirmado que GameVision usará un **nuevo
> design system** (documento Markdown entrante). Cuando llegue, **sustituirá a este documento como
> fuente única de verdad visual**. El re-anclaje (tokens, tipografía, componentes y motion) se
> ejecuta en **F4.5** — ver [ADR-0009](docs/metodologia/adr/0009-reanclaje-design-system.md) y la
> decisión **DX.8** en `docs/roadmap/fase-4-5-diseno-animaciones.md`. Hasta entonces, este documento
> sigue siendo la referencia vigente.

> **Fuente única de verdad** del lenguaje visual de GameVision.
> Decisión registrada en `docs/ui-redesign-2026-proposals.md` (27/09/2026).
> Dirección: *design-system-first cinematográfico con identidad editorial* —
> monocromo cálido + spot verde ácido, tipografía grotesca variable, contenido
> (portadas) como protagonista. Cualquier cambio visual pasa por aquí primero.

## 1. Atmósfera

Gaming premium de alto contraste, editorial y contenido-first: la interfaz es un
escenario neutro monocromo que desaparece para que **la portada del juego sea el
color**. El verde ácido es una microdosis con propósito: acción principal,
rating, foco. Nada de gradientes decorativos, sombras pesadas ni filas de
tarjetas idénticas. Todo se mueve con física de muelles, nunca con tweens
lineales.

## 2. Color

### Dark (tema por defecto, identidad principal)

| Rol | Hex | Uso |
|---|---|---|
| `background` | `#0D0D0D` | Fondo global (carbón cálido) |
| `surface` / `surfaceContainer` | `#1A1A1A` / `#242424` | Tarjetas, barras |
| `onBackground` / `onSurface` | `#F2F2F2` | Texto principal |
| `onSurfaceVariant` | `#8C8C8C` | Texto secundario, metadatos |
| `outline` | `#2E2E2E` | Bordes divisores |
| `primary` | `#C8F135` | **Verde ácido**: CTA, rating, foco, badges |
| `onPrimary` | `#0D0D0D` | Texto sobre verde |
| `primaryContainer` | `#2E3A0A` | Verde apagado para contenedores |
| `onPrimaryContainer` | `#E4FF87` | Texto sobre contenedor verde |
| `error` | `#FF5449` | Errores |

### Light (monocromo espejo)

| Rol | Hex |
|---|---|
| `background` | `#F2F2F2` |
| `surface` | `#FAFAFA` |
| `onBackground` | `#0D0D0D` |
| `onSurfaceVariant` | `#5A5A5A` |
| `primary` | `#4A6B00` (verde ácido oscurecido para contraste AA sobre claro) |
| `onPrimary` | `#F2F2F2` |

### Reglas de color

1. El color de imagen (portadas) no se compite: superficies neutras siempre.
2. El verde ácido aparece como **spot**: máx. 1-2 elementos en pantalla.
3. Color dinámico Material You: **opt-in del usuario** en ajustes; la identidad
   verde/monocromo es el fallback y el default.

## 3. Tipografía — Space Grotesk (variable, única familia)

Descargada vía Google Fonts provider (`ui-text-google-fonts`); jerarquía por
**tamaño y peso**, tracking apretado en display.

| Token | Size/Line | Weight | Tracking | Uso |
|---|---|---|---|---|
| `displayLarge` | 32/1.1 | 700 | -0.02em | Título de detalle de juego |
| `headlineMedium` | 24/1.15 | 700 | -0.02em | Títulos de sección |
| `titleLarge` | 18/1.2 | 600 | -0.01em | Título de juego en tarjeta |
| `titleMedium` | 16/1.25 | 600 | 0 | Títulos de fila |
| `bodyLarge` | 15/1.45 | 400 | 0 | Descripciones |
| `bodyMedium` | 14/1.45 | 400 | 0 | Texto general |
| `labelLarge` | 13/1.2 | 600 | +0.02em | Botones, CTA |
| `labelSmall` | 11/1.2 | 600 | +0.05em | Badges, metadatos, mayúsculas |

Regla: **nunca más de 2 tamaños por pantalla visible** fuera de la ficha de detalle.

## 4. Forma

| Token | Radio | Uso |
|---|---|---|
| `shape.small` | 8 dp | Chips, badges, inputs |
| `shape.medium` | 16 dp | Tarjetas, cards |
| `shape.large` | 24 dp | Sheets, diálogos, hero cards |
| `shape.full` | 999 dp | Avatares, pills, botones redondos |

Portadas de juego: `medium` (16 dp) — la imagen manda, borde 0, sin elevación.
Elevación por superficie (tono #1A1A1A sobre #0D0D0D), nunca por sombra.

## 5. Motion (GVMotion)

| Token | Valor | Uso |
|---|---|---|
| `durationFast` | 150 ms | Cambios de estado (pressed, selection) |
| `durationNormal` | 300 ms | Entradas de elementos, fades |
| `durationSlow` | 500 ms | Transiciones de pantalla, shared elements |
| `springStandard` | stiffness 380, damping 0.9 | Movimiento por defecto (posicional) |
| `springBouncy` | stiffness 600, damping 0.6 | Micro-interacciones juguetonas (badges, morph) |
| `staggerIncrement` | 40 ms | Retardo entre items de lista en entrada |

Reglas: entra con fade+slide 16 dp, nunca más de 3 elementos animando a la vez;
`LocalContentColor` del contenido nunca anima; respetar "reducir movimiento"
del sistema.

## 6. Componentes del sistema (`ui/designsystem/`)

| Componente | Spec |
|---|---|
| `GameCard` | Portada (16 dp), título `titleLarge`, rating badge superpuesto; ancho fijo en carrusel, fill en grid |
| `GameCover` | AsyncImage + crossfade + placeholder monocromo con iniciales |
| `RatingBadge` | Pill `shape.full`, fondo verde ácido, texto `labelSmall` oscuro |
| `NewsCard` | Imagen 16:9 recortada + titular `titleMedium` + fuente/fecha `labelSmall` gris |
| `FriendAvatar` | Círculo con iniciales; anillo verde si conectado |
| `GVSkeleton` | Bloque #242424 con shimmer; replica la forma del contenido (nada de spinners) |
| `EmptyState` | Icono outline + título `titleMedium` + hint `bodyMedium` gris, centrado |
| `GVButton` | Primary: relleno verde, texto `labelLarge` oscuro, radius `full`; Secondary: outline `#2E2E2E` |
| `GVChip` | Filtro: outline; seleccionado: relleno verde, texto oscuro |

## 7. Layout y pantallas

- Márgenes de pantalla: 16 dp; gutter de grid: 12 dp.
- Listas verticales con contentPadding que respete insets (nada de padding doble).
- Pantalla detalle de juego: hero full-bleed (portada 2:3) + scrim gradiente +
  contenido en sheet `shape.large` que se solapa.
- Nada de filas de 3 tarjetas idénticas: grids asimétricos o carruseles
  horizontales de anchos variables.
