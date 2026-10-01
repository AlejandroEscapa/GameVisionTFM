# Acta — Perfil: Top 4 con buscador y listas donde se ven (02/10/2026)

> **Qué es.** La revisión en la que el **Top 4** y las **listas curadas** dejan de vivir escondidos
> en «Editar perfil» y pasan al perfil propio, con un **buscador de catálogo** para elegir los
> cuatro juegos y con el Top 4 convertido en una **lista real**.
> Complementa a las fuentes de verdad: estado en [docs/roadmap/](../roadmap/README.md), decisiones
> en [docs/metodologia/adr/](../metodologia/adr/) y el sistema visual en
> [DESIGN.md](../../DESIGN.md).

---

## 1. El problema (lo que había)

| # | Hallazgo | Consecuencia real |
|---|---|---|
| 1 | El Top 4 solo se editaba en **Editar perfil** y solo con los **12 primeros juegos de la biblioteca** | Para algo que es «tu carta de presentación» había que entrar a *editar la cuenta*; sin buscador, con listas largas era inmanejable |
| 2 | Crear listas también vivía en **Editar perfil** (`CreateListCard`) | El propio diálogo de la biblioteca mandaba allí: *«Créalas desde Editar perfil»*. Y el nombre escrito se perdía al recomponer |
| 3 | **Tu propio perfil no mostraba tu Top 4** (solo el de otros) | Lo que te define como jugador no se veía en tu propia pantalla |
| 4 | La miniatura del Top se resolvía **contra la biblioteca del espectador** | Un juego del Top que no estuviera en la biblioteca se pintaba **«#1 Sin definir»** en el perfil público |

## 2. Decisiones (del propietario, 02/10)

| Decisión | Resultado |
|---|---|
| ¿Dónde se crean las listas? | En el **perfil**, en la sección «Mis listas» **justo debajo del Top 4**; y además una entrada **light y moderna** en «Para ti». Fuera de «Editar perfil» |
| ¿Qué busca el buscador del Top 4? | **Todo el catálogo (RAWG)**, no solo la biblioteca |
| ¿Cómo se guarda entonces? | `topGames: [{gameId, name, coverUrl}]` en el perfil, además de `topGameIds` |
| ¿El Top 4 es una lista real? | **Sí**, documento `users/{uid}/gamelist/top4`, sincronizado **en silencio** (sin hito en el feed) |

## 3. Lo implementado

**Dominio y datos**
- `TopGame` (`data/model/TopGame.kt`): id + nombre + portada, con `MAX = 4` y parseo tolerante.
- `UserProfile.topGames` + `topGamesResolved(porId)`: el orden manda `topGameIds`; la miniatura
  propia; si falta, respaldo por biblioteca (cuentas anteriores al 02/10).
- `TopGamesLogic` (`data/library/`): lógica **pura** del Top (capacidad, alta, baja, toggle, orden).
  Añadir el quinto devuelve `null` en vez de recortar el primero en silencio.
- `SocialViewModel.syncTop4List(...)`: escribe la lista `top4` y **borra el lado contrario** de la
  colección (público ↔ privado) para no dejar copias huérfanas (CA2.4 por construcción).
- `SocialViewModel.createList(...)`: alta manual de lista + hito `list_published` solo si nace
  pública. **El Top 4 nunca publica**: editar tu Top no es una noticia para el feed.
- `SearchViewModel.buscarParaTop(...)`: canal de búsqueda **independiente** del buscador de la
  pantalla Buscar, con debounce de 350 ms y mínimo de 2 caracteres.

**UI**
- **Perfil**: `TopGamesCard` (4 slots siempre visibles, contador `n/4`, buscador en píldora,
  resultados con portada/nombre/año, quitar por slot) justo **debajo de la tarjeta de información**,
  y `MisListasSection` debajo. La rejilla de acciones pasa a 1×4: Estadísticas · Seguidos ·
  **Nueva lista** · Editar.
- **Para ti**: `TarjetaCrearLista`, una línea con acción, en sección propia «Tus listas».
- **Perfil público**: lee `topGames` (adiós al «Sin definir»); las listas usan `ListaPublicaCard`.
- **Editar perfil**: queda con lo que dice su nombre (privacidad + datos). El cambio de privacidad
  **mueve** la lista `top4` de colección.
- Componentes nuevos en `ui/views/composables/profile/` (junto a su pantalla: son contenido, no
  vocabulario compartido) y regla registrada en `DESIGN.md` §6.

## 4. Verificación (con evidencia)

- **151 unitarios en verde** (18 nuevos: 13 de `TopGamesLogicTest` + 5 del buscador del Top).
  `lintDebug` limpio, `assembleDebug` OK.
- **Emulador Pixel_9 (API 36)**, cuenta QA, por UI con `adb`/uiautomator:
  - Perfil: Top 4 debajo de la biografía con **2/4**, dos portadas con su número, dos huecos `+`,
    buscador y «Mis listas · 2 listas» (capturas en `memory/shots-2026-10-02/`).
  - Buscador: `zelda` → 6 resultados con portada y año → tocar *Zelda (2023)* → **3/4** con el slot
    3 pintado (nombre y portada del catálogo, **sin estar en la biblioteca**).
  - Crear lista desde el diálogo del perfil → «3 listas» y documento nuevo en
    `users/{uid}/gamelist` (leído con la service key).
  - **Silencioso confirmado**: el feed no ganó ningún hito de Top 4; el hito `list_public` apareció
    una vez al crear una lista **manual** (y se limpió la prueba).
  - Editar perfil: solo privacidad + 4 campos + guardar (sin Top 4 ni creación de listas).
- **Datos de la cuenta QA restaurados** tras la prueba (Top 4 a sus dos juegos originales, hito y
  lista de prueba borrados).

## 5. Qué NO se hizo (y por qué)

- **Reordenar el Top arrastrando**: el orden es el de selección (1..4) y para cambiarlo se quita y
  se vuelve a añadir. Un drag-and-drop pide gesto largo y accesibilidad propia; no entraba en esta
  iteración.
- **Añadir juegos a una lista existente desde el perfil**: hoy se añaden desde la Biblioteca
  (`AddToListDialog`). El perfil crea y muestra; el detalle de lista es la siguiente iteración.
- **Borrar listas**: `deleteList` está cableado en el ViewModel pero sin superficie en la UI.

## 6. Pendiente del propietario

1. **Validación visual** en su dispositivo: el cuadro del Top 4, el buscador y la entrada de «Para
   ti» (capturas en `memory/shots-2026-10-02/` en el workspace, no en el repo).
2. Decidir si el buscador del Top debe **filtrar por tus géneros** o mostrar resultados puros de
   RAWG (hoy puros: el mismo catálogo que la pantalla Buscar).
3. Los diez minutos de siempre: cronómetro de CA1.1 y revisar el run de instrumentados en CI.

## 7. Deuda abierta

1. Un juego del Top que **no esté en tu biblioteca** no aparece en las estadísticas ni cuenta para
   nada más: el Top es identidad, no biblioteca. A vigilar si el propietario quiere que añadirlo
   ofrezca «¿lo añado también a tu biblioteca?».
2. Los perfiles anteriores al 02/10 con Top se **reconcilian** al abrir su perfil (se escribe la
   lista `top4` una vez). No hay migración por script: se arregla solo con el uso.
