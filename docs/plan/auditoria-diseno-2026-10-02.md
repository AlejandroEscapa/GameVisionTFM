# Auditoría de diseño/UI — 02/10/2026

> **Qué es.** La foto del estado visual de GameVision tras el re-anclaje (ADR-0010), hecha con
> evidencia del código (43 ficheros de `ui/`, ~11.000 líneas), **más el plan de adopción**.
> Diseño: buscadores, sombras, formas, contraste, copy, accesibilidad y disposición.
>
> **Veredicto en una línea:** el design system es sólido; **su adopción no**. Los tokens
> (`GVTheme`, `GVTypography`, `GVShapes`, `GVSpacing`, `GVMotion`) están bien construidos, pero
> **19 ficheros maquetaban a mano al margen del sistema**: la app se leía como dos productos
> distintos según la pantalla que abrieras.

---

## 1. Lo ejecutado (commits `f4ac73e` y siguientes)

### Buscadores (4 → 1)
El sistema tenía **cuatro** campos de búsqueda distintos: Buscar (`TextField` relleno con
`surfaceVariant`, sombra de 4 dp y esquinas de 24), el Top 4, Social y Amigos. Ahora hay uno:
**`GVSearchField`** (píldora, borde hairline, progreso/limpiar/acción en el hueco derecho), que
usan los cuatro.

### Búsqueda en vivo
La pantalla Buscar **solo buscaba al pulsar la lupa**. Ahora `SearchViewModel.buscarEnVivo` busca en
cada carácter con debounce de 350 ms y mínimo de 2 (menos no toca la red y vuelve al
descubrimiento). `hasSearched` pasa del Composable al ViewModel: con búsqueda en vivo el término
llega en cada carácter y la pantalla no puede adivinarlo.

### Emergencia primero: los dos bugs de la primera impresión
- **CRASH real** en Buscar: `released.substring(0, 4)` sobre un campo que RAWG deja vacío en juegos
  sin fecha. Al caer dentro de la composición de un ítem de `LazyColumn`, **no se pintaba la lista
  entera**. Nuevo helper guardado `anioDe()`.
- **`isDarkTheme!!`** en `HomeScreen` (parámetro nullable, `startDestination` del NavHost): la
  primera pantalla del producto podía crashear. Ahora cae al tema del sistema.

### Primera impresión del producto
- **`HomeScreen`**: fuera el `delay(1000)` que ponía **un segundo de esqueletos grises como
  bienvenida**; fuera el gradiente; y fuera los **dos botones primarios idénticos** ("Iniciar
  sesión" y "Continuar como invitado"), que hacían que la pantalla no dijera cuál es el camino.
  Ahora: marca, una acción primaria y "Explorar sin cuenta" en tinta suave (sin acento).
- **`LoginScreen`**: tarjeta de **32 dp con sombra de 8** (M2), "Bienvenido" en azul de acción con
  peso **Black (900)**, labels de 14 sp a mano, y un mensaje de error a **~3,3:1** (dos rosas del
  mismo tono): fallaba AA justo el texto que hay que leer. Todo al sistema.

### Barrido
| Qué | Antes | Ahora |
|---|---|---|
| Sombras/elevación de tarjeta | 14 sitios con 4–8 dp | 0: la separación la da el tono |
| `surfaceVariant` | 8 pantallas | `surfaceContainer` (fuera el vocabulario M2) |
| Colores literales | `Color.Red`, `Color.White/Black` por tema manual | roles del tema |
| Ancho del buscador de resultados | tarjeta full-bleed de 200 dp con panel translúcido al 90% | **fila escaneable** (portada 64×86 + nombre + año·géneros + nota) |
| Orden de resultados | FAB con sombra de 8 y `primaryContainer` | acción en el `GVScreenHeader` |
| "✓" en texto | menú de orden | icono Lucide |
| Emoji como icono | ⭐ ✅ 🚫 🔁 🎉 ★ | iconos Lucide |
| Copy | "Location", "Contact", "Metacritic Score", **"RAWG Rating"**, "Error:" | español; fuera la jerga del proveedor |
| Margen del perfil | 20 dp + degradado de 100 dp con engranaje flotante | `GVSpacing.screenPadding`, sin degradado |
| Cabecera `GVScreenHeader` | `top = 10.dp` crudo | `GVSpacing.sm` |

Además: botón **inerte** del feed ("Reintentar desde el icono de refresco") → reintento real;
**"me gusta" sin estado visible** (el color se calculaba y se tiraba, y el icono era el mismo con y
sin like) → estado con acento; contraste del texto "tuyo" (1,9:1) y de los iconos del dock (2,9:1)
corregidos; cajas táctiles bajo 48 dp del Top 4 y del dock ampliadas; `RatingBadge` ya no
documenta el "verde ácido" retirado y usa icono en vez del carácter "★".

## 2. Lo que queda (el plan, por impacto)

### Primera pasada (pendiente)1. **Margen de pantalla único**: Login/Pass/Register/Onboarding a 24 y el perfil a 20 → todo a
   `GVSpacing.screenPadding`. Dentro de `EditProfileScreen`, el título va a 16 y sus tarjetas a 24:
   **no comparten línea**, que es la primera regla del sistema.
2. **Formas**: 68 `RoundedCornerShape(n.dp)` a mano (5/12/16/20/24/32) frente a 3 tokens. Quedan las
   tarjetas de Pass/Register y las de `EditProfileScreen`.
3. **Copy y errores**: `GameDetails` interpola `e.message` al usuario en **7 sitios** (el hallazgo H1
   que ya se corrigió para el catálogo). Misma lección, sin aplicar fuera del ViewModel.
4. **Estados vacíos**: `SocialScreen` y `SearchScreen` pintan un `Text` suelto, y Biblioteca/Diario
   usan `icon = Lucide.X` (una ✕ significa cerrar, no "aquí no hay nada").
5. **Accesibilidad de `RatingStars`**: hoy son **cinco nodos** ("1 estrellas", "2 estrellas"…) para
   expresar una nota de 4,5. Debe ser **un** nodo con `progressBarRangeInfo`.
6. **`Switch`/`RadioButton` de M3** sin estilizar (Ajustes, Editar perfil, diálogos): el toggle de M3
   desentona con el lenguaje del sistema. Pide `GVSwitch`/`GVRadio`.

### Refactor mayor
7. **Consolidar duplicados**: quedan **3 tarjetas de juego** (una de ellas, `designsystem/GameCard.kt`,
   es **código muerto**; `NewsCard.kt` también) y **5 cabeceras de sección** distintas.
8. **Ficha (`GameDetails`)**: es la pantalla más rica y la más desalineada (7 `e.message`, 3
   divisores, metadatos con jerga, hero a `displayLarge` **sin `maxLines`** —un título largo tapa la
   portada—).
9. **Movimiento**: `GVMotion` solo se usa en el dock y en las transiciones del NavHost. Entrada por
   sección, stagger y muelle en los toggles es la capa que más "premium" se percibe por línea.
10. ~~**Decisión de sistema: qué significa Action Blue.**~~ **DECIDIDA Y EJECUTADA (02/10)** — el
    propietario eligió **acento solo para acción e interacción**. El azul pintaba además datos
    informativos (números de Estadísticas, totales del Diario, fuente de Noticias, éxito de Steam,
    antetítulos de tarjeta, iconos de metadatos) y ninguno de esos elementos es interactivo. Ahora
    esos datos van a tinta (`onSurface` / `onSurfaceVariant`) y **la jerarquía la da el tamaño**: las
    cifras de Estadísticas y Diario suben a `headlineLarge` (un estilo que estaba definido y sin
    usar en toda la app). Se queda con acento lo que se toca: CTAs, chips de opción, enlaces,
    buscador, el icono de Ajustes, el cuadro de acción del perfil, indicadores de selección y las
    barras de progreso.

### Pulido
11. `NewsScreen` mete "Cargando noticias..." junto a los skeletons (el skeleton ya lo dice).
12. Barras de progreso hechas a mano con dos `Box` (Estadísticas) en vez del componente.
13. `EditProfileScreen` mantiene parámetros sin uso (`isDarkTheme`), y `GameListEmptyState` recibe
    `selectedList` sin usarlo.

## 3. Lo que está bien (y no se toca)

`GVSpacing` es una escala real y las pantallas nuevas la respetan · **`GVDock` es lo mejor de la
interfaz** (píldora + hairline + muelle: sistema e implementación coinciden) · `OfflineBanner` y
`EmptyState` son honestos y tokenizados · `GameCover` resuelve bien el placeholder de iniciales y su
comentario explica el porqué · `GVScreenHeader` fija buenas reglas · la migración a Lucide es
completa y coherente · los skeletons son sistemáticos · **los tokens están bien pensados**.

## 4. Cómo se verifica esto

`./gradlew testDebugUnitTest lintDebug assembleDebug` + emulador (Pixel 9, API 36, cuenta QA).
Capturas de la iteración en `memory/shots-2026-10-02/` (workspace, no en el repo): portada de
entrada, Login re-anclado, búsqueda en vivo, resultados y perfil sin degradado.
