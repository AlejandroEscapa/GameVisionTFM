# Fase 1 — El corazón del tracker

**Estado:** 🔵 Aprobada · **Estimación:** 2 semanas · **Depende de:** F0 ✅ · **Bloquea a:** F2, F3, F4

## Progreso por bloques

| Bloque | Contenido | Estado |
|---|---|---|
| **1 · Biblioteca rica** | Estados (T1.1/T1.2), nota (T1.3), reseña (T1.4), favorito (T1.7) | ✅ **Hecho y verificado** (29/09/2026) |
| **2 · Diario y tiempo** | Partidas/rejugadas (T1.5/T1.6), diario (T1.8/T1.9), horas (T1.10) | ✅ **Hecho y verificado** (29/09/2026) |
| **2b · Duración HLTB** | Scraper HLTB con caché + respaldo manual (T1.11) | ✅ **Hecho y verificado** (29/09/2026) |
| **3 · Estadísticas** | Pantalla de stats (T1.12), «Tu año en un vistazo» (T1.13) | ✅ **Hecho y verificado** (29/09/2026) |
| **4 · Navegación + cierre** | Filtros/orden (T1.14/T1.15), instrumentación, auditoría de F1 | ⬜ Pendiente |
| **5 · Migración `uid`** | ADR-0008 (clave única), antes de F2 | ⬜ Pendiente |

### Bloque 3 cerrado (29/09/2026) — Estadísticas (T1.12/T1.13)
- **Pantalla de estadísticas** (ruta `stats`, acceso desde el perfil): KPIs (juegos, horas, nota media),
  distribución por estado y por nota, géneros y plataformas favoritos, y juegos por año.
- **«Tu año en un vistazo»** (semilla del Rewind de F3): completados del año, añadidos, mejor nota y más jugado.
- **Lógica pura** `StatisticsUtils` (todo se calcula en el cliente, decisión D1.5) + **8 tests nuevos** → **63 unitarios**.
- **Verificado E2E** (usuario QA): juegos=3, horas=1 h, nota media=3,5, estados (2 jugando / 1 completado),
  géneros (Action 2, RPG 2, …), plataformas y años — todo cuadra.
- Lint 0 errores; debug + release OK.

### Bloque 2b cerrado (29/09/2026) — Duración HLTB (T1.11)
- **Cliente HLTB aislado** (`HltbClient`, OkHttp): token + búsqueda, con **un reintento** si el token caduca (403).
  Es un flujo no oficial → si falla, devuelve `null` y **la app no se rompe**.
- **Caché Room** (`hltb_cache`) por nombre normalizado, con **TTL de 90 días** (los tiempos derivan muy
  lento). Base de datos subida a **versión 2**.
- **Valor manual que gana**: `playtimeManual` en la ficha (en minutos); si está, la tarjeta muestra
  «Tu estimación» y **oculta** los tiempos de HLTB.
- **UI**: tarjeta «Duración estimada» en la ficha con Historia / +Extras / Completista. **Sin dato,
  el bloque se oculta** (regla del propietario).
- **Tests**: 5 nuevos (`HltbUtilsTest`) → **55 unitarios**. Lint 0 errores; debug + release OK.
- **Verificado E2E** (usuario QA): la ficha de *Elden Ring* mostró **60 h 6 min / 101 h 19 min /
  136 h 13 min** (coincide con la verificación del 29/09) y, al fijar manualmente **100 h**, la tarjeta
  pasó a «Tu estimación: 100 h» (`playtimeManual=6000` en Firestore).

### Bloque 1 cerrado (29/09/2026)
- **Panel de biblioteca en la ficha** (`LibraryPanel`): los 7 estados, nota con medias estrellas,
  reseña, favorito y quitar.
- **Pista de nota no bloqueante** (D1.2): al marcar Completado/Coleccionado sugiere puntuar.
- **Lista** con pestañas para los 7 estados + Todos/Favoritos/Historial, y la nota visible en la tarjeta.
- **Verificado E2E** con el usuario QA: estado `completado`, nota `3.5` y reseña persistidos en
  Firestore (`users/{uid}/library`), con los **contadores correctos** (`stats/summary`: `gamesTotal=2`,
  `gamesByStatus.completado=1`, `ratingSum=3.5`, `ratingCount=1`).
- **Tests:** 7 nuevos (`RatingUtilsTest`) → **44 unitarios** en verde.

## Objetivo

Convertir las tres listas planas actuales en una **biblioteca rica**: estados reales,
valoración con estrellas, reseña, diario con fechas y horas, rejugadas, duración del
juego y estadísticas de perfil. Es lo que separa "app de catálogo" de "tracker".

## Resultado verificable

Un usuario nuevo puede **registrar un juego en menos de 60 segundos** eligiendo estado y
nota, **añadir una sesión de juego al diario** y **ver sus estadísticas** (horas, notas,
géneros) sin que nada sea de pago.

## Por qué esta fase y no otra

Es el **mínimo del mercado**: Backloggd distingue Completado/Dominado/Retirado/En pausa/
Abandonado, Letterboxd tiene diario y estadísticas, y HowLongToBeat da la duración. Sin
esto no competimos. Además, el diario es el **dato crudo del Rewind** (F3): sin F1 no hay
nada que celebrar. Y las estadísticas, que en los líderes son **de pago**, aquí se regalan
como diferenciación directa.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D1.1 | **Definición exacta de los estados** | (a) Completado = ver créditos, Dominado = 100 % · (b) Nuestra propia definición | ✅ **CERRADA (28/09/2026): Completado = ver créditos; «Coleccionado» (antes «Dominado») = todos los logros / 100 %** | ✅ |
| D1.2 | **¿Nota y reseña obligatorias al registrar?** | (a) Todo opcional · (b) Nota obligatoria · (c) Nota obligatoria sólo al completar | ✅ **CERRADA (29/09/2026): (c) en versión NO bloqueante — nada impide registrar; al marcar Completado/Coleccionado se sugiere la nota en un toque (saltable); reseña siempre opcional** | ✅ |
| D1.3 | **Escala de nota** | (a) 5 estrellas con medias · (b) 10 puntos · (c) Ambas | ✅ **CERRADA (28/09/2026; revisada el mismo día): escala 0,5–5,0 con medias estrellas**, alineada con el formato de RAWG. Ver ADR-0003 (supersede al ADR-0002) | ✅ |
| D1.4 | **Duración del juego: fuente** | (a) Scraper HowLongToBeat · (b) Sólo campo propio · (c) No incluirla | ✅ **CERRADA (29/09/2026): (a) scraper ligero de HLTB con caché + respaldo manual** — verificado en vivo con 5 casos de prueba y caso «sin datos»; cruces externos OK. Ver [verificación](../investigacion-2026/hltb-verificacion-2026.md). IGDB queda como candidato futuro (TTB oficial) | ✅ |
| D1.5 | **Estadísticas: ¿cliente o servidor?** | (a) Calcular en el cliente · (b) Cloud Function | **(a) en F1**: con los datos del propio usuario es rápido y evita dependencia de Functions; (b) cuando F3 necesite agregados | ✅ (29/09/2026) |
| D1.6 | **¿El diario es automático o manual?** | (a) Manual (el usuario apunta) · (b) Automático (al cambiar de estado) | **(a) manual, con atajos**: el diario es un acto de voluntad (como Letterboxd) y su valor está en que el usuario lo escribe | ✅ (29/09/2026) |
| D1.7 | **Importación de Steam/PSN/Xbox** | (a) En F1 · (b) Fase posterior · (c) Descartar | **(b) → concretada**: importación = pack **P1 «Steam Inside»** (Steam Web API + capa Cloud Functions) en **F2**; Xbox/RA por su vía segura (P2); PSN aplazado | ✅ (29/09/2026) |

---

## Tareas

### A. Biblioteca
- [x] T1.1 UI de estado: selector con los 7 estados (Jugando, Completado, Coleccionado, En pausa, Retirado, Abandonado, Deseado) — `LibraryStatusSelector` en la ficha (Bloque 1)
- [x] T1.2 Cambio de estado rápido desde la ficha del juego y desde la lista (panel de biblioteca en la ficha + pestañas por estado en la lista)
- [x] T1.3 Valoración con medias estrellas (crear, editar, borrar) — `RatingStars` + `RatingUtils` (0,5–5,0, con «Quitar»)
- [x] T1.4 Reseña escrita por juego, con formato corto destacado — `ReviewEditor` (máx. 280, contador)
- [x] T1.5 Múltiples partidas por juego (rejugada = nuevo `log`) — botón «Empezar rejugada» en el panel de la ficha
- [x] T1.6 Plataforma jugada por partida (PC, PS5, Switch…) — selector de plataforma en la rejugada y en la sesión (`lastPlatform`)
- [x] T1.7 Favorito (para el Top 4 de F2) — corazón en el panel de biblioteca; `favorite` en la ficha y pestaña «Favoritos»

### B. Diario y tiempo
- [x] T1.8 Vista de diario cronológico (por mes, con carátulas) — pantalla **Diario** con agrupación por mes
- [x] T1.9 Apuntar sesión ("hoy jugué X minutos") — diálogo desde el botón «+» del diario
- [x] T1.10 Horas acumuladas por juego y totales — totales «Esta semana» y «Total» en el diario
- [x] T1.11 Duración estimada (historia / +extras / completista) vía HLTB con caché de 90 días + valor manual que gana — HltbClient aislado, HltbRepository, tarjeta en la ficha

### C. Estadísticas
- [x] T1.12 Pantalla de estadísticas: horas, distribución de notas, géneros y plataformas favoritas, juegos por año — `StatsScreen` + `StatisticsUtils`
- [x] T1.13 «Tu año en un vistazo» (semilla del Rewind de F3) — tarjeta con la actividad del año actual

### D. Navegación de la biblioteca
- [ ] T1.14 Filtros y orden: por estado, nota, género, plataforma, fecha
- [ ] T1.15 Búsqueda dentro de tu biblioteca (distinta del catálogo)

---

## Criterios de aceptación (con evidencia)

- [ ] CA1.1 Registrar un juego nuevo (buscar → estado → guardar) en **menos de 60 s**
      cronometrado en el emulador.
- [ ] CA1.2 Los 7 estados existen y son visibles en la ficha y en la lista.
- [x] CA1.3 Una rejugada crea un segundo registro sin borrar el primero — `createLog` con `runIndex` (CA1.3).
- [x] CA1.4 El diario muestra las sesiones ordenadas por fecha y suma las horas — verificado en emulador.
- [x] CA1.5 Las estadísticas cuadran con los datos introducidos — verificado E2E con el usuario QA (3 juegos, 1 completado, nota media 3,5).
      un usuario de prueba: 3 juegos, 2 completados, 1 abandonado → los números coinciden).
- [ ] CA1.6 Los filtros combinados (estado + género) devuelven lo esperado.
- [ ] CA1.7 Tests nuevos para el repositorio de biblioteca y el cálculo de estadísticas
      (lógica pura, testeable en JVM sin Firebase).

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Sobrecargar el registro y perder la rapidez | Decisión D1.2: nada obligatorio salvo el estado; medir CA1.1 en cada iteración |
| Las estadísticas se vuelven lentas | Se calculan sobre datos ya cargados del usuario; si crece, pasar a Cloud Function |
| Duplicar el vocabulario del sector y confundir | D1.1: usar las definiciones ya establecidas (Completado vs Coleccionado) |

## Cómo se verifica

Emulador + tests. Capturas: selector de estado, diario con sesiones, estadísticas de un
usuario de prueba con datos variados.

---

## 🔍 Auditoría de cierre de fase

> Aplica el [protocolo de auditoría de cierre](../metodologia/auditoria-de-cierre.md).
> **Particularidad de F1:** es la fase que más pantallas nuevas añade (estados, nota, reseña,
> diario, estadísticas), así que el bloque 6 revisa sus **estados vacío/carga/error** y el
> bloque 2 la lógica pura (estadísticas, cálculo de horas) testeada en JVM.

- [ ] Bloque 1: build debug y release (R8) sin warnings nuevos
- [ ] Bloque 2: unitarios + instrumentados en verde; tests de estadísticas y biblioteca
- [ ] Bloque 3: lint limpio; sin código muerto de las tres listas antiguas
- [ ] Bloque 4: el cálculo de estadísticas vive en una capa testeable (no en la UI)
- [ ] Bloque 5: docs de la fase + ADR-0003 (escala) sincronizados
- [ ] Bloque 6: cada pantalla nueva revisada en vacío/carga/error; registro fluido (CA1.1 < 60 s)
- [ ] Motion: según el [sistema de motion](../roadmap/fase-4-5-diseno-animaciones.md) (si ya existe)
- [ ] Auditoría firmada y validada por el propietario

---

## Registro de decisiones

- **D1.1 — Definición de estados cerrada (28/09/2026)** — Completado = ver créditos; **«Coleccionado»**
  (antes «Dominado») = todos los logros / 100 %. Conecta con el lenguaje real de logros y abre las
  ideas registradas de vitrina de coleccionados y comunidad.
- **D1.3 — Escala de nota: 0,5–5,0 con medias estrellas (28/09/2026; revisión)** — Tras contrastar
  con el formato de RAWG (nota sobre 5 con decimales), la escala queda en **medias estrellas
  (0,5–5,0 en pasos de 0,5)**: congruencia visual con los datos de RAWG y 10 niveles de granularidad.
  Sustituye a la decisión previa del 1–10. Ver **ADR-0003** (`docs/metodologia/adr/0003-escala-medias-estrellas.md`),
  que supersede al ADR-0002.
- **D1.4 — Duración: fuente cerrada (29/09/2026)** — Scraper ligero de HLTB con caché + respaldo
  manual. **Verificado en vivo** con 5 casos (protocolo y valores en
  [`docs/investigacion-2026/hltb-verificacion-2026.md`](../investigacion-2026/hltb-verificacion-2026.md));
  caso «sin datos» (juego no lanzado) verificado; re-ejecutable con `node tools/hltb-check.mjs`.
- **D1.2 — Registro sin fricción con nota sugerida (29/09/2026)** — El registro nunca se bloquea
  (CA1.1 manda); al pasar a «Completado»/«Coleccionado» se ofrece puntuar en un toque (se puede
  saltar); la reseña es siempre opcional.

## 💡 Ideas registradas (28/09/2026)

- **Vitrina de «Coleccionados»** — sección y KPI propios para tus juegos con todos los logros
  (los «platinados»), ligada a T1.12–T1.13 (estadísticas).
- **Separar lo conseguible de lo no conseguible** — solo los juegos con sistema de logros podrán
  «coleccionarse»; (futuro) distinguir además logros aún obtenibles de los descontinuados.
- **Comunidad de logros (F2)** — feed estilo foro y porcentaje de jugadores por logro.
  ⚠️ Requiere **fuente de datos de logros por plataforma (Steam/PSN/Xbox)**; RAWG/IGDB no la ofrecen
  — análisis pendiente antes de planificarlo (ver ideas de fase-2).

## 💡 Ampliación con datos HLTB (29/09/2026)

### En la ficha del juego — **ADOPTADO ✅** (selección del propietario, 29/09)

- **Duración rica (evolución de T1.11):** media + **mediana** + rango **rushed↔leisure** + nº de
  votos por estilo, y **speedrun** cuando exista.
- **Bloque «Comunidad»:** completado / backlog / jugando / retirado + **rejugadores** + nota media
  de reseñas (todo en el mismo fetch).
- **Ficha técnica extra:** desarrollador, editorial, clasificación por edad (PEGI/ESRB) y enlaces
  externos (Steam/IGN) — rellena huecos de RAWG.
- **Relacionados:** fila de «juegos relacionados» en la ficha.
- **Tags/chips:** modos de juego (Un jugador · Co-op · Multijugador), clasificación de edad y rango
  de duración como chips visuales (base para futuros filtros).

### Extrapolación a otras vistas — propuestas (pendientes de selección, 29/09)

- **Búsqueda (catálogo):** chips compactos en cada resultado (modos + duración estimada). Carga
  **progresiva con caché** y cola con límite (evitar ráfagas de peticiones a HLTB).
- **Biblioteca:** **progreso «tus horas / duración estimada»** (barra), chip de duración en la
  tarjeta y orden «más cortos primero» (decidir el próximo juego).
- **Estadísticas:** «Por jugar: ~X h» sumando duraciones de tu lista (tono positivo, sin deuda),
  top géneros (RAWG + HLTB) y comparativa «tu nota vs. comunidad».
- **Diario:** contexto por sesión («12 h de ~60 h»).
- **F3 (nota):** filtro «algo que pueda terminar en <10 h» dentro de «¿Qué juego ahora?».
- **F4 (nota):** los **Steam App IDs** ya vienen en el dato → importación/sync de Steam más cerca.
