# Análisis competitivo y de mercado 2026 — cierre

> **Qué es.** El cierre del análisis de mercado iniciado el 28/09/2026: radiografía de la
> competencia, matriz de **sistemas a adoptar** (qué copiamos de cada referente y por qué),
> dolores reales de los usuarios, tendencias de consumo y economía del sector. Termina con
> las **decisiones de producto (D-C1…D-C12)** que alimentan el
> [plan maestro](../plan/PLAN-MAESTRO-2026.md).
>
> **Método.** 6 frentes en paralelo, ~80 búsquedas, cada afirmación con nivel de confianza y
> contradicciones señaladas. Material crudo en [`fuentes-competencia/`](fuentes-competencia/).
> Fecha: **28/09/2026**.
>
> **Regla de oro de este documento:** copiamos la **mecánica** (qué hace), reinterpretamos la
> **estética** (cómo se ve). Si un patrón obliga a romper nuestro design system, se adapta el
> patrón, no el sistema.

---

## 1. Resumen ejecutivo

1. **Nadie domina el nicho de trackers de videojuegos en móvil.** El líder cultural
   (Backloggd, ~650 K usuarios) es **solo web**, sin app nativa. Los que tienen app
   (Stash, GG) son pequeños. **La grieta está en Android nativo + hábito diario.**
2. **El foso no es el catálogo: es la comunidad y el hábito.** RAWG/IGDB/HowLongToBeat son
   infraestructura compartida y barata de reutilizar; la diferenciación por datos no existe.
   El valor defendible es **UX nativa + diario + recap viral + red social**.
3. **El dolor real del jugador 2026 es la abundancia**: bibliotecas enormes, finalización
   baja, "no sé qué jugar" y **culpa del backlog**. Quien resuelva eso con diseño **anti-culpa**
   gana confianza.
4. **El motor de crecimiento probado y gratis es el "Year in Review"** (Letterboxd creció casi
   sin publicidad gracias a su recap anual compartible). GameVision ya lo tiene planeado (F3).
5. **La economía del nicho es dura pero clara**: núcleo social gratis para siempre + premium
   barato (≈15–25 $/año) + opcionalmente micromecenazgo. El error fatal está documentado:
   **cobrar lo que antes era gratis** (caso Trakt: crisis reputacional con la comunidad).
6. **La captación se gana con importación/exportación universal** (Steam, HLTB, Backloggd,
   CSV). El miedo a migrar es el mayor freno del sector → regalar la salida es la mejor entrada.

---

## 2. La competencia directa (radiografía)

| App | Plataforma | Modelo | Fuerza | Debilidad explotable |
|---|---|---|---|---|
| **Backloggd** | Web | Gratis total + Patreon (1/3 $) | Mejor UI del sector, comunidad de reseñas, ~650 K usuarios, 31,5 M partidas registradas en 2025 | **Sin app nativa**; catálogo English-centric (IGDB) |
| **Stash** | **Android + iOS** | Gratis (+premium no confirmado) | El rival **móvil** más directo; BD con DLC/screenshots/trailers; listas temáticas | Menos comunidad; propuesta menos "social" |
| **GG (ggapp.io)** | Web + app + escritorio | Freemium (no confirmado) | Registro en 10 s; separa "Beaten" de "Completed" | Datos de tamaño/precio no verificados |
| **GameTrack** | iOS / iPadOS | Gratis | Minimalista, recuento anual, escaneo de código de barras | **Sin Android** |
| **HowLongToBeat** | Web | Gratis | **Estándar de facto** de duración (historia/+extras/completista); datos vía IGDB; foro activo | Sin app propia; tracking social limitado |
| **Grouvee** | Web | Gratis + tier de apoyo | Import de Steam, timelines, export CSV, "shelves" | Reseñas poco profundas; UI antigua |
| **IGN Playlist** | Web (dentro de IGN) | Gratis | Respaldo editorial; tiempos HLTB integrados | Es una feature, no un producto |
| **RAWG** | Web + API | Gratis/API | Mayor BD de descubrimiento; status tracking | Más catálogo que tracker personal |
| **Completionator** | Web | Gratis | Progreso + colección cross-platform + stats | Sin app móvil |
| **MyVideoGameList** | Web | Gratis | Perfil social, reseñas | Comunidad minúscula |
| **GameFAQs** | Web | Gratis (ads) | Colecciones junto a guías | Tracking secundario, sin app |
| **Backloggery** | Web | Gratis | Manual puro (control total) | Sin base de datos propia |

**Correcciones a la hipótesis inicial** (importante para no perder el tiempo):
- **Darkadia está cerrado** (RIP) → no es competencia viva.
- **PlayDex** es un tracker de **juegos de mesa**, no de videojuegos → fuera del mapa.
- **Emergentes a vigilar:** Vaultkeeper, GamersVault, Memorycard, Gamelix, GameTrekker (todos
  pequeños y orientados a posicionarse en "best tracker" por SEO).

**Conclusión competitiva:** el mapa está **fragmentado y sin dueño**. Los líderes son web;
los móviles son pequeños. Ese es, literalmente, el hueco de GameVision.

---

## 3. Matriz de sistemas a adoptar (la "hoja de ruta de mecánicas")

Extraída de IMDb, Letterboxd, StoryGraph, Trakt, Last.fm, Untappd, Vivino, Beli, TV Time,
MyAnimeList/AniList y Backloggd. **Qué copiamos, qué adaptamos y qué descartamos.**

| # | Sistema | Referente | Decisión | Fase | Nota |
|---|---|---|---|---|---|
| S1 | **Watchlist / backlog** | IMDb, Letterboxd | ✅ ADOPTAR | F1 | Mecánica núcleo; en nuestro modelo es un **estado**, no un módulo aparte |
| S2 | **Diario con fecha + replay** | Letterboxd | ✅ ADOPTAR | F1 | El *replay* es nativo en juegos (NG+, 100 %, runs) |
| S3 | **Estados ricos normalizados** | AniList, Backloggd | ✅ ADOPTAR | F1 | Jugando/Completado/Dominado/En pausa/Retirado/Abandonado/Deseado **sin "shelves" manuales** |
| S4 | **Escala de valoración** | IMDb (1–10), Letterboxd (5★) | ⚠️ **DECIDIR (ADR)** | F1 | La investigación recomienda **1–10 entero** para juegos (granularidad); la F1 actual propone 5★. Conflicto a cerrar |
| S5 | **Top 4 fijado** | Letterboxd | ✅ ADOPTAR | F2 | Identidad instantánea + marketing viral ("Four Favorites") |
| S6 | **Listas de usuario** | Letterboxd, IMDb | ✅ ADOPTAR | F2 | Contenido generado por usuarios = descubrimiento y SEO |
| S7 | **Reseñas cortas + likes** | Letterboxd, Backloggd | ✅ ADOPTAR | F1–F2 | La reseña de una línea es lo compartible |
| S8 | **Feed social de actividad** | Letterboxd, Goodreads | ✅ ADOPTAR | F2 | Requiere masa; arrancar con clanes/retos |
| S9 | **Estadísticas de perfil + "Wrapped"** | Letterboxd, StoryGraph | ✅ ADOPTAR | F1 stats · F3 Rewind | **Alta prioridad**; motor viral de coste cero |
| S10 | **Mood / tags de sensación** | StoryGraph | ✅ ADAPTAR | F3 | Tags tipo "relajante/difícil/narrativo/corto" alimentan "¿qué juego ahora?" |
| S11 | **Avisos de contenido** | StoryGraph | ✅ ADAPTAR | F1/F4 | Adaptar a "horas, dificultad, requiere online, accesibilidad" |
| S12 | **Import/export universal** | Letterboxd, Trakt, Grouvee | ✅ ADOPTAR | **F0–F1 (alta)** | Arma de captación contra el vendor lock-in |
| S13 | **Colección vs Deseos (tengo/quiero)** | Trakt, Discogs | ✅ ADOPTAR | F1 | Distingue propiedad de intención; sirve al coleccionista |
| S14 | **Scrobbling / sync (Steam/PSN/Xbox)** | Last.fm, Trakt | ✅ ADAPTAR | F2/F4 | La "automatización" es superviviente nº1 del abandono; automatizar el **registro**, dejar manual la **valoración** |
| S15 | **Ranking por duelos (ELO)** | Beli | ✅ ADOPTAR | F3 | Diferenciador real: ranking personal honesto, sin inflación de notas |
| S16 | **Reacciones emocionales** | TV Time | ✅ ADAPTAR | F2 | Emoción de baja fricción, muy compartible |
| S17 | **Clubes / retos comunitarios** | Fable, Goodreads | ✅ ADOPTAR | F2/F4 | Convierte hobby solitario en actividad social programada |
| S18 | **Badges / gamificación** | Untappd | ⚠️ ADAPTAR **con ética** | F3/F4 | Gamificar **exploración y diversidad**, **nunca volumen de horas** |
| S19 | **Top agregado (Top 250)** | IMDb | 🔶 DIFERIR | F4+ | Vulnerable a brigading; es capa secundaria, no dogma |
| S20 | **Escaneo con cámara** | Vivino | 🔶 DIFERIR | F4+ | En juegos no hay "etiqueta"; solo útil para cajas físicas |
| S21 | **Journal periódico** | Backloggd | ✅ ADOPTAR | F1 (cubierto por S2) | Ya contenido en el diario |

**Los 5 sistemas que definen nuestro "2026-ready"** (combinación que **ningún tracker de juegos
hace hoy integrada**): **S3 estados ricos + S9 Wrapped + S12 import/export + S15 duelos ELO +
S10 mood tags.**

---

## 4. Patrones de diseño y UX a adoptar

De las referencias y tendencias 2026. Nuestra identidad (monocromo cálido + `#C8F135` +
Space Grotesk + física de muelles + skeletons) **se mantiene**; se sube el listón funcional.

| Patrón | Referente | Decisión |
|---|---|---|
| Perfil como identidad curada: KPIs + tabs (Diario/Biblioteca/Listas/Reseñas) | Letterboxd | ✅ Adoptar |
| Perfil público por defecto, **con privacidad granular por entrada** | Letterboxd | ✅ Adaptar (público pero con control visible) |
| Ficha de juego con **acciones above the fold** (registrar sin scroll) | Backloggd | ✅ Adoptar |
| Biblioteca en grid de carátulas con **densidad ajustable** | Letterboxd | ✅ Adoptar |
| **Estantería con lomos** + widget de estantería | ShelfLife | ✅ Adoptar (F4) — alto valor percibido, barato |
| Watchlist como **estado**, no módulo aparte | Goodreads, Trakt | ✅ Adoptar |
| **Wrapped en formato historia vertical** (tarjetas una a una, export 9:16) | Strava Year in Sport | ✅ Adoptar (F3) |
| Recap **personal + comparable** ("más horas que el 82 %") | Strava, Spotify | ✅ Adoptar (F3) |
| Dark mode **derivado**, no invertido | tendencias 2026 | ✅ Regla de sistema |
| Tipografía display sobredimensionada en momentos clave | tendencias 2026 | ✅ Adoptar |
| **Skeletons + UI optimista** (ya tenemos) | Google, Medium | ✅ Mantener y extender |
| **Feed de actividad + reacciones ligeras** | Letterboxd | ✅ Adoptar (F2) |
| **Listas colaborativas y comparación** ("N de tus amigos lo tienen") | Letterboxd | ✅ Adoptar (F2) |
| **Gamificación ética** (rachas opcionales, sin castigo público) | Duolingo | ✅ Adoptar con cuidado |
| **TTFV < 60 s**: elegir 3 juegos ya jugados → biblioteca poblada | Spotify, Netflix | ✅ Adoptar (onboarding) |
| **Importación masiva** como atajo al primer valor | Goodreads, Backloggr | ✅ Adoptar |
| **Empty states accionables** (nunca pantalla vacía "rota") | uxmatters | ✅ Adoptar |
| **Targets ≥ 44 px**, contraste AA, foco visible | WCAG 2.2 / Material | ✅ Obligatorio |
| **Adaptativo**: list-detail (biblioteca ↔ ficha) en tablet/plegable | Material 3 | ✅ Adoptar (F4, Nav3) |
| **Nunca monetizar el Recap** (error de Strava: paywall de 80 $) | Strava | ✅ Regla: el Rewind básico es del usuario y gratis |

---

## 5. Dolores reales del usuario (la mina de oro)

Extraído de Reddit (r/patientgamers, r/Letterboxd, r/CozyGamers, r/Steam, r/JRPG), foros de
Trakt, ResetEra y reseñas. **Cada dolor = una oportunidad concreta.**

| Dolor (evidencia alta) | Qué pasa | Nuestra respuesta |
|---|---|---|
| **Culpa / ansiedad del backlog** ("pile of shame") | El registro pasa de placer a obligación; abandono silencioso | **Diseño anti-culpa**: foco en disfrute, sin contadores agresivos de "pendientes", "elige por ánimo" |
| **Estados pobres** (falta abandonado/rejugando/borrador) | Obliga a "shelves" manuales → fricción | **Estados de primera clase** en el modelo de datos (S3) |
| **Watchlist sin priorización** | No hay forma de decidir qué va primero | **Cola priorizada "¿qué juego ahora?"** (F3) |
| **Miedo/coste de migrar** (vendor lock-in) | Se quedan en apps que odian por no perder historial | **Import/export universal** (S12) — "regala la salida para ganar la entrada" |
| **Monetización hostil** (Trakt: 30→60 $ + API en paywall) | Huida masiva con resentimiento | **Tier gratis real, precio estable, API/export gratis** |
| **Datos imprecisos** (HLTB) | Media correcta pero inútil para un individuo | **Estimaciones personalizadas** por tu propio historial + perfil |
| **Catálogo English-centric** | Backloggd depende de IGDB, sesgo anglosajón | **Catálogo en español** (condición de disparo de IGDB, ver ADR-0001) |
| **Sin canal para pedir features** | Letterboxd cerró su foro → frustración | **Roadmap público + votación** (prometer poco, cumplir rápido) |
| **Bugs sin respuesta** | Abandono por acumulación de fricciones | **Changelog público + respuesta rápida** |

**Aviso de honestidad:** el dolor del backlog está **validado cualitativamente** pero **no
cuantitativamente** en la investigación (no hay dato oficial fiable de % de juegos sin
terminar). Hay que **medirlo con datos propios** antes de apoyar KPIs en él.

---

## 6. Consumo del jugador y mercado 2026

| Dato | Valor | Confianza |
|---|---|---|
| Mercado global videojuegos | **~188.900 M$** (2025, +3,4 %) | Media |
| Mercado **móvil** | **>100.000 M$** (2025); 103.100 M$ → 107.700 M$ (2027) | Media |
| Mercado de **suscripción** gaming | 11.500 M$ (2024) → **14.500 M$ (2026)** → 24.200 M$ (2030) | Media |
| Demografía (ESA 2025) | **48 % mujeres**, edad media **41**, **55 % prefiere móvil** | Media |
| Oferta indie | **~20.000 lanzamientos en 2025**, solo **~300 superaron 1 M$** | Media |
| Live-service saturado | Retención cae **hasta ~50 % en 3 meses**; cierres | Baja |
| Gaming burnout | Dolor real: mecánicas repetitivas, grinding | Baja |

**Lecturas estratégicas:**
1. **Abundancia, no escasez.** El juego no es tener catálogo, es **ordenar y decidir**. El
   "¿qué juego ahora?" (F3) es la respuesta directa a esa sobrecarga.
2. **El móvil domina el gasto, pero el coleccionista vive en PC/consola.** No son el mismo
   usuario → segmentar (casual móvil vs. completista PC/consola).
3. **La suscripción agrava el dolor** (pruebas más y termina menos) → nuestro producto debe
   ayudar a **terminar**, no a acumular.
4. **Lo corto, terminable y retro se revaloriza** → el diario y el "completado" cobran sentido.

---

## 7. Economía del sector (cómo se sostienen las referencias)

| App | Modelo | Precio | Se paga por | Se regala | Tamaño |
|---|---|---|---|---|---|
| **Letterboxd** | Freemium + ads + partnerships | Pro **19 $/año**, Patron 49 $/año | Sin ads, stats, filtros, pósters | Registro, notas, reseñas, listas, feed | **>18 M miembros** |
| **Backloggd** | **Patreon** | ~1 $ / 3 $ | Apoyo + betas + insignia | Todo el tracking | ~650 K usuarios |
| **StoryGraph** | Freemium Plus | 4,99 $/mes o 49,99 $/año | Stats avanzadas, recomendaciones | Base completa | Crecimiento rápido |
| **Trakt** | VIP | 30 → **60 $/año** | Sin ads, filtros, listas | Free limitado a 100 ítems/lista | ~500 K de TV Time |
| **MyAnimeList** | Freemium | 4,99 $/mes | Sin ads + exclusivo | BD + tracking con ads | Comunidad enorme |
| **Goodreads** | Gratis (Amazon) | 0 $ | — | Todo | ~15 M usuarios |

**Patrones transversales:**
- **Precio ancla del premium de nicho: 15–25 $/año.**
- Lo que **se regala**: registrar, puntuar, reseñar, listas, feed. Lo que **se cobra**:
  sin-ads, estadísticas avanzadas, cosméticos, filtros.
- **El "Year in Review" viral es el mayor motor de crecimiento de coste cero.**

---

## 8. Confianza, contradicciones y huecos

**Contradicciones no aplanadas:**
1. **Escala de nota:** IMDb recomienda 1–10; Letterboxd usa 5★. Nuestra F1 propone 5★.
   → **Decisión pendiente (ADR).**
2. **Minimalismo vs. maximalismo 2026:** el sector se contradice. Resolución: **monocromo
   disciplinado + acento**, con "maximalismo" solo en momentos de celebración (Wrapped).
3. **Comunidad pública vs. privacidad 2026:** el éxito de Letterboxd es "público por defecto",
   pero choca con la sensibilidad de privacidad. Resolución: **público por defecto + control
   granular visible**.
4. **Gamificación retiene vs. cansa:** rachas funcionan pero pueden ser coercitivas.
   Resolución: **rachas opcionales, sin castigo público**.

**Huecos declarados (no confirmados):**
- Tamaños/usuarios de GG, Stash, GameTrack, Grouvee, Completionator; precios de GG/Stash.
- Tasa de finalización real del jugador (no hay dato oficial) — **medir con datos propios**.
- % de canal de descubrimiento (tienda vs. creadores vs. amigos).
- Mercado del coleccionismo físico (no se halló cifra fiable).
- Crecimiento exacto de Letterboxd (1,5 M → 18 M) proviene de blogs secundarios.

**Sesgo de fuentes:** mucha evidencia viene de foros y subreddits de *early adopters* técnicos
y anglosajones; la "voz del usuario medio" (móvil, casual, no anglosajón) está
infrarrepresentada — **precisamente el segmento donde GameVision puede diferenciarse.**

---

## 9. Decisiones de producto (cierre del análisis)

| # | Decisión | Recomendación | Fase |
|---|---|---|---|
| **D-C1** | Escala de valoración | Cerrar como ADR: **1–10 entero** (granularidad para juegos) con opción 5★ | F1 |
| **D-C2** | Identidad pública por defecto | **Público por defecto + privacidad granular visible** | F2 |
| **D-C3** | Import/export universal | **Prioridad alta desde F0/F1**: es la mejor arma de captación | F0–F1 |
| **D-C4** | Wrapped gratuito | **El Rewind básico nunca se cobra**; se monetizan extras | F3 |
| **D-C5** | Gamificación ética | Gamificar **exploración/diversidad**, nunca horas; rachas opcionales | F3–F4 |
| **D-C6** | Modelo de negocio | **Core social gratis + premium 15–25 $/año** (tipo Letterboxd Pro); Patreon/donaciones al inicio; anuncios discretos más tarde | Publicación |
| **D-C7** | Tracción | **Orgánico (comunidad + creadores) + ASO + Rewind viral**; cero pago al principio | Publicación |
| **D-C8** | Diferenciación | **Android nativo + UX + diario + Wrapped + duelos ELO**, no el catálogo | Transversal |
| **D-C9** | Diseño anti-culpa | Sin contadores agresivos de pendientes; lenguaje positivo | Transversal |
| **D-C10** | Roadmap público | **Roadmap + votación de features** como diferenciador y retención | F2+ |
| **D-C11** | Segmentación | Dos personas: **casual móvil** y **completista PC/consola** | Transversal |
| **D-C12** | Catálogo español | Activar la migración parcial a IGDB cuando se cumpla un disparador (ADR-0001) | F1+ |

---

## 10. Implicaciones para el roadmap

**Nada de esto cambia F0–F4**; los **refuerza y concreta**:

- **F0 (Cimientos)** gana un requisito claro: **import/export** (D-C3) y el modelo de datos
  rico en estados (S3).
- **F1 (Corazón del tracker)** confirma: estados ricos, diario con replay, stats, colección
  vs. deseos. Añade: cerrar la escala de nota (D-C1) y avisos de contenido (S11).
- **F2 (Social)** confirma: perfil público, Top 4, listas, feed, seguir. Añade: reacciones,
  listas colaborativas, roadmap público, import desde otras apps.
- **F3 (Wow)** se vuelve más nítido: **"¿Qué juego ahora?"** con mood tags (S10) + **Wrapped
  story vertical** (S9) + **duelos ELO** (S15). Es la combinación que nadie tiene.
- **F4 (Nativo)** confirma: widget, notificaciones, estantería. Añade: list-detail adaptativo,
  gamificación ética.

El detalle por fases y vertientes está en el
[**Plan Maestro 2026**](../plan/PLAN-MAESTRO-2026.md).

---

## Fuentes

Material crudo por frente en [`fuentes-competencia/`](fuentes-competencia/):
[01 · Competencia directa](fuentes-competencia/01-competencia-directa.md) ·
[02 · Sistemas IMDb/Letterboxd](fuentes-competencia/02-sistemas-referencias.md) ·
[03 · Consumo y mercado](fuentes-competencia/03-consumo-mercado-2026.md) ·
[04 · UI/UX](fuentes-competencia/04-ui-ux-referencias.md) ·
[05 · Minería de reseñas](fuentes-competencia/05-mineria-resenas.md) ·
[06 · Modelos de negocio](fuentes-competencia/06-modelos-negocio.md).
