## Dimension 05: Minería de reseñas — dolores, peticiones y abandono

### Current State

La minería de reseñas (16 búsquedas independientes sobre r/backloggd, r/Letterboxd, r/books, r/patientgamers, r/Steam, r/truegaming, r/JRPG, r/CozyGamers, ResetEra, foros de Trakt, Insert Credit, App Store/Play Store y blogs de comparativas) arroja un patrón consistente: **los usuarios no abandonan la categoría (seguir registrando les importa), pero sí abandonan cada app concreta** por causas muy repetidas y predecibles.

Los dolores no son de "features que faltan" en abstracto, sino de **fricción emocional y estructural**:

1. **Culpa y ansiedad por el backlog** ("pile of shame") — el registro deja de ser un placer y pasa a ser una obligación autoimpuesta. Es el meta-dolor más citado y el mayor motivo de abandono silencioso.
2. **Datos imprecisos o incompletos** (tiempos de juego de HowLongToBeat, catálogos que dependen de IGDB y son "English-centric").
3. **Estados de biblioteca pobres**: falta "no terminado / abandonado", "rejugando", "relectura", "draft de reseña", priorización de la watchlist.
4. **Miedo y coste de migrar** (vendor lock-in): el usuario se queda en una app que odia porque mover su historial es arriesgado o tedioso.
5. **Monetización percibida como hostil** (doblado de precio de Trakt VIP + API tras paywall) → abandono con resentimiento y búsqueda activa de alternativas.

El patrón de cambio de app está bien documentado: el detonante casi nunca es "una feature nueva", es **una subida de precio, un cambio de UI/API, un bug largo sin arreglar o la acumulación de pequeñas fricciones diarias**.

Fuentes:
[^1]: Reddit — r/Steam. "How long to beat is invaluable and Valve need to partner". 2020. https://www.reddit.com/r/Steam/comments/gqu1w0/how_long_to_beat_is_invaluable_and_valve_need_to/
[^2]: Reddit — r/truegaming. "Gamer fatigue - Have games gotten longer?". 2017. https://www.reddit.com/r/truegaming/comments/702wq7/gamer_fatigue_have_games_gotten_longer/
[^3]: Reddit — r/JRPG. "How accurate is howlongtobeat?". https://www.reddit.com/r/JRPG/comments/1twdrag/how_accurate_is_howlongtobeat/
[^4]: Reddit — r/Letterboxd. "What feature does Letterboxd need?". https://www.reddit.com/r/Letterboxd/comments/1i22a0e/what_feature_does_letterboxd_need/
[^5]: Reddit — r/Letterboxd. "Any features you wish letterboxd had?". https://www.reddit.com/r/Letterboxd/comments/1g8dg58/any_features_you_wish_letterboxd_had
[^6]: Reddit — r/Letterboxd. "Letterboxd feature request: I propose votes in watchlist". https://www.reddit.com/r/Letterboxd/comments/1mahtyj/letterboxd_feature_request_i_propose_votes_in
[^7]: Reddit — r/Letterboxd. "Letterboxd's feature request and bug report forum has been shut down". https://www.reddit.com/r/Letterboxd/comments/1nxebke/letterboxds_feature_request_and_bug_report_forum
[^8]: Reddit — r/books. "GoodReads Alternatives?". https://www.reddit.com/r/books/comments/1jhf0bh/goodreads_alternatives
[^9]: Reddit — r/books. "GoodReads vs Storygraph, Or, the story of how I went back". https://www.reddit.com/r/books/comments/1jovfzt/goodreads_vs_storygraph_or_the_story_of_how_i
[^10]: Simmons Voice. "Goodreads vs. The StoryGraph: A Reader's Thoughts". https://simmonsvoice.com/13123/showcase/goodreads-vs-the-storygraph-a-readers-thoughts
[^11]: Trakt Forums — Product Updates. "New Trakt Feedback" (p. 18). 31 oct 2025. https://forums.trakt.tv/
[^12]: Ettayeb.fr. "Trakt locks its API behind a VIP subscription and rattles the community". 12 ago 2026. https://ettayeb.fr/
[^13]: Apple App Store — reseñas de "trakt.TV Time To Watch". https://apps.apple.com/
[^14]: Reddit — r/patientgamers. "I solved my backlog anxiety, I hope this helps!". https://www.reddit.com/r/patientgamers/comments/12tq3z0/i_solved_my_backlog_anxiety_i_hope_this_helps
[^15]: Reddit — r/CozyGamers. "does anyone ever feel overwhelmed by your gaming [backlog]". https://www.reddit.com/r/CozyGamers/comments/18jl96g/does_anyone_ever_feel_overwhelmed_by_your_gaming
[^16]: Insert Credit Forums. "game tracking apps - video games". 2022. https://forums.insertcredit.com/
[^17]: Two Average Gamers. "Backloggd Review: The Best Game Tracker? I Tested It for [6 months]". sep 2026. https://www.twoaveragegamers.com/
[^18]: ResetEra. "Do we have a Letterboxd equivalent to gaming?". 2026. https://www.resetera.com/
[^19]: Reddit — r/Letterboxd. "What's the best way to request/recommend features?". https://www.reddit.com/r/Letterboxd/comments/x2wcji/whats_the_best_way_to_requestrecommend_features
[^20]: AlgVets. "These Platforms Give Players a Better Way to Track Their [Games]". https://algvets.org/

### Key Evidence

| Queja/Petición | Apps afectadas | Frecuencia/Evidencia | Cita textual | Oportunidad para GameVision | Fuente |
|---|---|---|---|---|---|
| Tiempos de juego imprecisos / desactualizados | HowLongToBeat | **Alta** — ≥3 hilos independientes (r/Steam, r/truegaming, r/JRPG) | "howlongtobeat is VERY inaccurate, at least for modern games… Bloodborne is no way a 35 hour Main [story]" | Mostrar **rangos por perfil de jugador** (completionista, rush, casual) y tiempos **reales de tu propia biblioteca**, no una media única | [^2][^3][^1] |
| Falta estado "no terminado / abandonado" y "rejugando/relectura" | Letterboxd, Goodreads, Backloggd | **Alta** — petición repetida en hilos de feature requests | "I would LOVE a draft review feature. I would also like a feature for 'did not finish'." | **Estados de biblioteca ricos** (abandonado, en pausa, rejugando, multijugador, DLC) nativos, sin "shelves" manuales | [^4][^10] |
| Estados forzados con "shelves"/etiquetas manuales | Goodreads | **Alta** — motivo nº1 de migración a StoryGraph | "The StoryGraph has built in options to mark books as 'did not finish' and 'owned', while on Goodreads you have to make your own shelves" | Cero trabajo manual: los estados son de primera clase en el modelo de datos | [^8][^10] |
| Foro/portal de sugerencias cerrado → no se puede pedir features | Letterboxd | **Media-Alta** — hilo dedicado + queja recurrente | "Letterboxd's feature request and bug report forum has been shut down." | **Roadmap público + votación de features** como diferenciador y canal de retención | [^7][^19] |
| Watchlist/backlog sin priorización (faltan votos/orden) | Letterboxd, Trakt | **Media** | "All that can be solved with a voting system within watchlist." | **Cola priorizada "¿qué juego siguiente?"** con votos/reordenación y filtros por duración y ánimo | [^6] |
| Bug de UI largo sin arreglar (orden de watchlist) + soporte insensible | Trakt | **Media** | "the long standing bug of not being able to change the sort order of the watchlist still remains. Sigh"; "unresponsive to bugs reported in the forums" | Compromiso de **respuesta rápida a bugs + changelog público** como seña de fiabilidad | [^13] (comentario Syncler/Reddit) |
| Precio doblado + API/keys tras paywall | Trakt | **Alta** (2025-2026) | "the VIP price doubling may force me to look elsewhere for media tracking"; "Trakt began requiring a $4.99/month VIP subscription to create API applications" | **Tier gratis con valor real, precio estable y API/export gratuitos**; evitar el "bait-and-switch" | [^11][^12][^13] |
| Miedo y coste de migrar / pérdida de datos | Goodreads, StoryGraph, Trakt, TV Time | **Media-Alta** — aparece en casi todos los hilos de "¿me cambio?" | "I am nervous to make the switch…"; "It's easy to transfer your data by downloading it as a single file from Goodreads" | **Import/export universal** (Goodreads, HLTB, Steam, Trakt, MAL, CSV) y **API abierta**: elimina el miedo = arma de captación | [^8][^9] |
| Backlog como "pile of shame" → ansiedad, deja de ser placer | Backloggd, HLTB, Grouvee y trackers en general | **Alta** — múltiples hilos r/patientgamers, r/CozyGamers, r/Steam | "The biggest mistake you can make is looking at a backlog as a pile of shame…"; "I don't want to make gaming a chore" | **Diseño anti-culpa**: foco en disfrute, sin contadores agresivos de "pendientes", lenguaje positivo, "elige por ánimo" | [^14][^15] |
| Catálogo incompleto / sesgo anglosajón (depende de IGDB) | Backloggd | **Media** — mención repetida en foros y reviews | "it's English centric as its game list pulls from IGDB. Users can add things to IGDB but it can take some [time]" | **Catálogo multilingüe** con obtención rápida de juegos retro, regionales, indie y de nicho | [^16][^17] |
| Fragmentación: 3-5 apps para juegos/cine/libros/ánime | Múltiples | **Media** | "Sites such as Backloggd, Grouvee, Infinite Backlog, HowLongToBeat, and GG App give players a more organized way to keep track of their gaming history" | **Un solo grafo** de identidad y gustos cross-media (juegos + cine/series + libros + ánime) | [^18][^20] |
| UI anticuada / sin evolución (ánime) | MyAnimeList | **Baja / anecdótica** — señal indirecta en blogs y comparativas, sin hilo contundente capturado | "I've heard quite a lot of complaints…" (aparición indirecta) | UI moderna y rápida como tablestaca para captar usuarios de MAL | (evidencia débil) |
| Demasiadas peticiones de tracking/ads que saturan la app | Apps móviles en general | **Baja / anecdótica** | "they clutter the app so much, it's basically unusable" | **Sin ads, privacidad por diseño, sin telemetría intrusiva** | (evidencia débil) |

### Tensions & Counter-arguments

- **"No quiero más features, quiero menos fricción."** Varios hilos de r/patientgamers y r/CozyGamers muestran que la solución percibida como válida para la ansiedad del backlog es *jugar y dejar de trackear con culpa*, no añadir gamificación. Riesgo claro para GameVision: **más mecánicas de engagement pueden empeorar el dolor**, no resolverlo. La oportunidad real es un tracker que *reduzca* la sensación de obligación (registro rápido, "cero backlog pendiente" como opción, no como meta).
- **Contra-argumento a "HLTB es malo":** parte del rechazo es que la media es estadísticamente correcta pero inútil para un individuo; los usuarios que defienden HLTB lo hacen por su utilidad para *decidir el siguiente juego*, no por precisión. La lección no es "sustituir HLTB" sino **personalizar** la estimación.
- **Los feature requests a menudo chocan con el producto:** Letterboxd cerró su foro de sugerencias (señal de fatiga de la propia comunidad pidiendo todo). Un roadmap público puede generar expectativas que no se cumplen → riesgo de decepción. GameVision debería prometer poco y cumplir rápido.
- **La monetización es un arma de doble filo:** el caso Trakt (precio doble + API en paywall) demuestra que **el mismo cambio que salva el negocio puede detonar la huida masiva** a alternativas. Un segmento grande de usuarios de trackers es sensible al precio y valora export abierto; pero sostener un producto sin ingresos es inviable → tensión real sin solución trivial.
- **Evidence caveats:** las afirmaciones sobre **MyAnimeList** (UI anticuada) y **TV Time / apps móviles con exceso de ads y tracking** son **débiles o anecdóticas** en esta ronda: aparecen de forma indirecta, sin un hilo o reseña contundente capturado. Deben validarse con una ronda específica (r/MyAnimeList, r/TVTime, reseñas de la Play Store). Igualmente, **GG App** no produjo resultados en la búsqueda directa: su evidencia aquí es solo inferencial (aparece como alternativa en listas y comparativas). El resto de filas (HLTB, Letterboxd, Goodreads/StoryGraph, Trakt, ansiedad de backlog, catálogo Backloggd) están respaldadas por **múltiples fuentes independientes y consistentes**.
- **Sesgo de plataforma:** las reseñas de las stores (App Store/Play Store) están sobrerrepresentadas por usuarios enfadados (sesgo negativo); los foros y subreddits por early adopters técnicos. La "voz del usuario medio" (móvil, casual, no anglosajón) está infrarrepresentada — precisamente el segmento donde GameVision podría diferenciarse.

### Fuentes adicionales de esta sección
[^17]: Two Average Gamers. "Backloggd Review: The Best Game Tracker?". sep 2026. https://www.twoaveragegamers.com/
[^18]: ResetEra. "Do we have a Letterboxd equivalent to gaming? - Page 2". 2026. https://www.resetera.com/
[^20]: AlgVets. "These Platforms Give Players a Better Way to Track Their [Games]". https://algvets.org/
