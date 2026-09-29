# GameVision · Vistas y features de apps de tracking exitosas de otros campos (Dive 01)

- **Fecha de investigación:** 29 de septiembre de 2026 (Europe/Madrid).
- **Método:** ~30 búsquedas web distintas + verificación directa en navegador de fichas de Google Play y de páginas de producto (IMDb title page, Letterboxd film page, Backloggd game page, Goodreads book page, trakt.tv, anilist.co, backloggd.com) el mismo día. Cuando algo no pudo verificarse, se indica expresamente.
- **Apps cubiertas:** IMDb, Letterboxd, Trakt, MyAnimeList, AniList, Goodreads, Untappd, Discogs y Backloggd (bonus, tracker de juegos).
- **Convención de confianza por bloque:** 🟢 Alta (verificado directamente por mí en tienda/sitio) · 🟡 Media (fuente secundaria fiable o evidencia parcial) · 🔴 Baja (indicio débil / no verificado esta pasada).
- **Aviso:** en el momento de escribir, no verifiqué las notas de **App Store (iOS)** de ninguna app (solo Google Play); se señala donde aplica. Los números de tienda cambian a diario.

---

## Resumen ejecutivo

1. Las apps de tracking exitosas de todos los verticales (cine, TV, anime, libros, cerveza, vinilo, juegos) convergen en la misma anatomía: **catálogo fuerte → registro personal rápido (diario/check-in) → capa social (reseñas, feed de amigos, listas) → estadísticas personales y "recap" anual → ficha de ítem rica con "dónde verlo/comprarlo"**.
2. La unidad de valor no es el dato de catálogo (commodity), sino **el historial personal + la identidad que genera** (Letterboxd "Your life in film", Untappd "uniques" y badges, Backloggd tiempo jugado, IMDb watchlists).
3. El registro manual es el loop central en todas… excepto **Trakt, que gana precisamente por scrobbling/sync automático** con media centers (Plex/Kodi/Emby/Jellyfin/Stremio) y por ser la capa de datos de un ecosistema de terceros. El sync es, en cada campo donde existe, la killer feature; en videojuegos ese rol lo puede jugar Steam (biblioteca + horas + logros).
4. Las **fichas de ítem** repiten un orden: cabecera/póster → nota comunitaria + histograma → acciones de estado (quiero/viendo/visto…) → sinopsis → metadatos → "dónde verlo o comprarlo" (JustWatch en Letterboxd, streaming en IMDb, marketplace en Discogs) → reseñas (populares y recientes) → listas que lo contienen → similares/relacionados → estadísticas de comunidad ("X miembros lo vieron").
5. Las **listas** y los **rankings** (Top 500 de Letterboxd, Top 100 de AniList, listas de IMDb "added by 4.5M users", Lists de Backloggd) son contenido viral permanente, no un accesorio.
6. El **recap anual tipo "Wrapped"** (Letterboxd Year in Review) es la palanca de crecimiento estacional mejor documentada del sector.
7. En señales de tienda (Google Play, verificado 29-sep-2026): Untappd 4,7★ / IMDb 4,6★ / Goodreads 4,5★ / Trakt 4,3★ / Letterboxd 3,7★ / MyAnimeList 3,5★ / **Discogs 2,4★**. La lección: una marca enorme y una base de datos única **no** salvan una app mala (Discogs), y las apps "segunda pantalla" de un servicio web (Goodreads) se llevan reseñas duras.
8. Los **dolores que más se repiten en reseñas**: publicidad intrusiva que "abre sola" (Letterboxd), re-logins recurrentes (Discogs), navegación confusa (Trakt, Letterboxd en menor grado), rediseños que empeoran flujos (IMDb), apps "demasiado básicas" frente a la web (Goodreads) y límites/paywalls percibidos como abusivos (Trakt VIP "too expensive", watchlists free capadas a 100 ítems).
9. La **portabilidad de datos** es un tema emocional: el apagón de TV Time (julio 2026) provocó un éxodo de usuarios que reclaman importar/exportar sus historiales hacia Trakt y otros (varias reseñas de Trakt con "TV Time refugees" frustrados por el proceso).
10. Para GameVision: la ventana está abierta (en juegos no hay Letterboxd móvil dominante; Backloggd es solo web y manual). Las reglas de oro heredadas: explorar sin cuenta; log en 1–2 toques; nota con histograma; estados cortos tipo backlog/playing/completed/dropped; ficha con logros + ofertas + tiempo de juego; stats personales + recap; monetización Pro anual discreta; cero ads intrusivos.

---

## Análisis por app

### IMDb (películas / TV / celebrities)
**Confianza global: 🟢 Alta** (Play verificado + title page verificada en imdb.com).

#### 1. Estructura de la app/vistas
- Google Play la describe como "**IMDb: Personalized entertainment & celebrity guide**" y en "About this app" la ficha muestra categoría *Entertainment* y etiqueta *Events & offers* (Play, verificado 29-sep-2026).
- En la **página de título** (verificada en `imdb.com/es/title/tt0816692/`, Interestelar) el orden de bloques es: póster + reproductor de trailer → cabecera (título, título original, año, certificado B, "2h 49min") → **CALIFICACIÓN DE IMDb** (8.7/10, "2.6 M" votos) + **TU CALIFICACIÓN** (botón "Calificar") + **POPULARIDAD** (rank 92) → contadores ("7.5k opiniones de usuarios", "468 opiniones de los críticos") → sinopsis → **STREAMING / RENT·BUY** ("Watch on Prime Video", "Watch on HBO Max", "Establece tus servicios preferidos") → acciones ("Marcar como visto", añadir a lista — "Agregado por **4.5 M usuarios**") → Videos (31) y Fotos (99+/703) → **elenco principal** (44) → pestañas de sección: *Elenco y equipo · Opiniones de usuarios · Trivia · Preguntas Frecuentes · IMDbPro* → **histograma de notas** (10★: 1.066.204 … 1★: 19.008) → reseñas de usuarios.
- La descripción de la app (vía listado apkmirror) se presenta como "**the world's most popular and authoritative source for movie, TV, and celebrity information. Watch trailers, get showtimes, and buy tickets**" — el ciclo completo trailer→horarios→entradas vive en la app.
- *No verificado:* la composición exacta del bottom-bar de la app Android (pestañas nativas) en esta pasada. 🔴

#### 2. Las 2–3 features que explican su éxito
1. **Base de datos + reviews/ratings como referencia universal**: la reseña más votada de Play lo resume: *"IMDb is a go-to platform for movie and TV show enthusiasts, offering a vast database of titles, ratings, and reviews"* (3.450 personas la encontraron útil).
2. **Watchlist → personalización**: la propia web promociona "Watchlist **for personalized recommendations**" (imdb.com) y en la title page la watchlist muestra prueba social ("Agregado por 4.5 M usuarios").
3. **Utilidad transaccional** (trailers, showtimes, tickets, dónde ver streaming) que convierte la ficha en herramienta de decisión, no solo de archivo. 🟢

#### 3. Ficha de ítem (bloques y orden)
Igual a lo anterior; destaco el patrón clave: **nota + histograma con conteos reales por estrella**, doble capa "nota global / tu nota", y bloque de disponibilidad (streaming/rent/buy) integrado arriba, antes del contenido editorial. 🟢

#### 4. Onboarding y sync
- **Explorar sin cuenta: sí** (navegué la ficha completa sin login); las acciones requieren sesión ("Inicia sesión para obtener más acceso").
- **Sync**: no existe scrobble automático; el tracking es manual (rate/watchlist). Login propio de IMDb/Amazon. No hay concepto de "conectar tu Netflix". 🟢

#### 5. Señales de Google Play (verificado 29-sep-2026)
- Nota media: **4,6★**; ~**970K reseñas**; **100M+ descargas**; badge "**Contains ads**". Distribución: 5★ 678.005 / 4★ 153.466 / 3★ 30.905 / 2★ 10.266 / 1★ 38.895.
- Reseñas concretas: (a) queja por **anuncios** en la galería de fotos — *"I can deal with the intermittent ads whenever I go through a movie's picture gallery but what I cannot deal with is when…"* (55 "helpful"); (b) rechazo al **rediseño/UX**: *"hate the new update. Terrible movement of search bar meaning extra uncomfortable clicks…"* (61 "helpful"); (c) elogio masivo ya citado (3.450 "helpful").
- iOS: **no verificado** en esta pasada.

#### 6. Monetización no intrusiva
- Gratis con **ads** (verificado). Líneas B2B separadas: **IMDbPro** (herramientas de industria) y "Licencia de datos de IMDb" visibles en el footer del sitio. 🟡

---

### Letterboxd (películas — el análogo más cercano a lo social/cultural de GameVision)
**Confianza global: 🟢 Alta** (Play + film page verificadas).

#### 1. Estructura de la app/vistas
- Tagline oficial: "**Letterboxd — Your life in film**" (footer del sitio) y "The popular social network for film lovers, right in your pocket" (Play).
- Navegación web verificada: **FILMS / LISTS / MEMBERS / JOURNAL** + Search (nav superior).
- La **app móvil** (descripción en listados): "log films and catch up on your friends' activity"; release notes de apkmirror: "**Log and review films and edit past entries** — View member profiles (including diaries and films) and cast/crew filmographies". Los nombres exactos de las pestañas del bottom-bar móvil no quedaron verificados en esta pasada. 🟡 (estructura general) / 🔴 (pestañas móviles exactas)

#### 2. Las 2–3 features que explican su éxito
1. **Diary + rating de media estrella + reviews como red social**: "Log movies, rate them with **half-stars**, post reviews, check friends' feeds, compile custom watchlists, and look back at [yearly stats]" (snippet worldos.cc); deep-dive independiente: "Film Diary — Track what you watch, when you watched it; Reviews & Ratings — **Half-star ratings (0.5–5.0)** with text reviews; Lists — Curated, shareable lists" (github.com).
2. **Lists culture**: las listas son objetos de primera clase y shareables, con listas "oficiales" (Letterboxd's Top 500 / Top 250 Films with the Most Fans, en la propia film page).
3. **Year in Review ("Wrapped")**: feature anual que resume los logs del año — "Letterboxd Wrapped 2025 is the annual year-in-review feature summarizing users' movie logs…" (academicjobs.com, ene-2026) y fenómeno mediático ("Letterboxd Year-in-Review will be released in [late year]", trillmag). Crecimiento documentado: de **11,4M miembros (final 2023) a 17M (2024)** (tesi.luiss.it) y "**more than 17 million users**, up from 1.8 million just four years ago" (sherwood.news, ene-2025); "más de **30 millones** de usuarios" según nota de prensa citada por simplywall.st (2026). 🟡 (tercero: ~1,8M→17M→30M según medios).

#### 3. Ficha de ítem (bloques y orden) — film page verificada (Interstellar)
1. **WHERE TO WATCH** (JustWatch; enlaces RENT/BUY/DISC: Amazon, Apple TV, Google Play Movies) — arriba del todo.
2. **Trailer**.
3. **Sinopsis** (1–2 frases + "more").
4. Pestañas **CAST / CREW / DETAILS / GENRES / RELEASES** (CAST por defecto; duración "169 mins"; enlaces "More at IMDB · TMDB").
5. **Rating 4.5 + "Rating Distribution"** con histograma de medias estrellas (de ½★ a ★★★★★; para Interstellar: ★★★★★ 3.4M (53%) … ½★ 9.795 (0%)).
6. **POPULAR REVIEWS** (con likes: "65,925 likes") y **RECENT REVIEWS**.
7. **SIMILAR FILMS** ("Powered by Nanocrowd").
8. **MENTIONED BY POPULAR LISTS** (Letterboxd's Top 500, "Movies everyone should watch at least once during their lifetime", Top 250 Films with the Most Fans).
9. Barra lateral de **estadísticas de comunidad**: "Watched by 8.471.547 members", "Appears in 986.388 lists", "Liked by 4.226.748 members", "№ 33 in the Letterboxd Top 500".
10. Sin sesión: botón "**Sign in to log, rate or review**" + "Share". 🟢

#### 4. Onboarding y sync
- **Explorar sin cuenta: sí** (film page completa sin login; toda la plataforma es navegable). Registrar requiere cuenta.
- **Sin scrobble**: el loop es manual (log/diary/rate). La portabilidad importa: existen importadores de otros servicios (no verificado en detalle esta pasada 🔴).
- El gate de tracking es suave: "Sign in to log…" en el lugar del botón de acción. 🟢

#### 5. Señales de Google Play (verificado 29-sep-2026)
- Nota media: **3,7★** (contrasta con la percepción "amada" del servicio): 5★ 15.941 / 4★ 4.651 / 3★ 3.975 / 2★ 2.305 / **1★ 6.321**. **10M+ descargas**, "**Contains ads · In-app purchases**". Badge destacado: "#10 top for €0 social".
- Reseñas: (a) elogio con matiz: *"I loved this app. 5★… I want to scroll and get the top reviews, why would I want a 50 person lon[g review]…"* (463 "helpful"); (b) fricción de navegación: *"I'm just starting to use the app and so far I like it well. The navigation between sections could be more intuitive thou[gh]"* (10); (c) **ads agresivos**: *"Ads are auto-opening without clicking on them or something??? I keep getting pulled onto websites, which ends up making…"* (**240 "helpful"**).
- iOS: no verificado.

#### 6. Monetización no intrusiva
- Tiers de pago **Pro (~19 $/año) y Patron (~49 $/año)**, con "advanced statistics" etc.; el servicio base "is completely free" (au.lifehacker.com, may-2025 + zendesk de Letterboxd + iphonea2 sobre features Pro/Patron). 🟡
- En paralelo, la versión gratuita **tiene ads** (Play "Contains ads"; botones "**REMOVE ADS**" verificados por mí en la film page web; la queja de arriba). El modelo: gratis con ads → Pro quita ads añade stats; **no rompe features core con paywall** (logs, listas y reviews son gratis). 🟢

---

### Trakt (TV/series y películas — la app de referencia en sync/scrobbling)
**Confianza global: 🟢 Alta** (Play + web verificadas) / 🟡 en detalles VIP.

#### 1. Estructura de la app/vistas
- Homepage verificada (app.trakt.tv, en español): "Gratis en web, iOS y Android"; bloques **descubre / registra / comparte** + sección "EN TENDENCIA"; copy: *"Haz seguimiento de lo que has visto, encuentra qué ver a continuación y descubre dónde se transmite esta noche"*.
- Posicionamiento oficial (roadmap.trakt.tv): "**Track, Discover, Share** brings together what Trakt is for: keeping your watching history, finding your next favorite, and sharing it with others."
- Vistas nativas conocidas por la comunidad: **Up Next, Progress y Calendars** (post oficial-comunidad "Goodbye Hide, Hello Drop": "Tap the ⊖ icon for any show in your **Up Next, Progress, and Calendars**"; reddit r/trakt). También **watchlists/history/lists** (makeuseof: "track the movies and TV shows… using **lists and histories**"). 🟡 (nombres de vistas móviles)

#### 2. Las 2–3 features que explican su éxito
1. **Scrobbling y sync bidireccional con todo el ecosistema**: "Trakt.tv has a lot of **plugins to automatically scrobble** the movies and episodes you watch from your media center" (pypi.org); Stremio añadió sincronización 2-vías — "this **imports your entire Trakt history** to Stremio's library for 2 way [sync]" (blog.stremio.com); herramientas comunitarias hacen "playstate sync for Plex, Emby, Jellyfin **and Trakt**… in sync across all [devices]" (github). Y el propio foro de Trakt lo vende: "**Automatically sync your streaming services!**".
2. **Calendario de estrenos + "dónde se transmite"** — convierte el tracking en utilidad semanal ("encuentra qué ver a continuación y descubre dónde se transmite esta noche", app.trakt.tv; App Store: "**DISCOVER what's hot and where to stream it**").
3. **Posición de "capa de datos" (API)**: apps y media centers de terceros se integran con Trakt para enriquecer watchlists/estadísticas — efecto red que ningún tracker cerrado replica. 🟢 (para 1–2) / 🟡 (para 3).

#### 3. Ficha de ítem
- No verificada en profundidad esta pasada (la web redirige a la app y requiere sesión para vistas internas). Por evidencia de descripciones: cada película/episodio soporta **watched/ratings/comments/lists** y enlaces a "where to watch". 🔴 (marcar como pendiente de otra pasada).

#### 4. Onboarding y vinculación/sync — **el punto clave**
- Cuenta gratis; luego conectar: media centers vía plugin (Kodi/Plex/Emby/Jellyfin), apps (Stremio con "Authenticate next to Trakt Scrobbling… Log in to Trakt and approve access" — guides.viren070), etc. El flujo típico OAuth es "log in + approve", de baja fricción. 🟢
- **Importación tras TV Time**: reseña en Play de un refugiado: *"As a person who used tvtime for 10+ years… After I uploaded my data [it] asked me to check all…"* — evidencia de que Trakt ofrece importar datos de TV Time (con fricción). El contexto: TV Time cerró el **15-jul-2026** y su herramienta de export ya no existe ("TV Time's export tool went offline with the July 15, 2026 shutdown" — cinopsysapp; "It is no longer possible to request a new data export from TV Time" — tvtrack.io). 🟡

#### 5. Señales de Google Play (verificado 29-sep-2026)
- Nota media: **4,3★**; **8,19K reseñas**; **500K+ descargas**; badge "In-app purchases" (sin "Contains ads" en el listado). Distribución: 5★ 4.878 / 4★ 1.646 / 3★ 288 / 2★ 288 / 1★ 514.
- Reseñas: (a) precio: *"I think it's a good app in general, but I believe the **VIP price is too expensive**. If there was a **one time payment option**…"* (6); (b) refugiado de TV Time: *"this app is so frustrating! After I uploaded my data [it] asked me to check all…"* (2); (c) navegación: *"I'm finding the **navigation too confusing and laborious**…"* (8).
- iOS: no verificado.

#### 6. Monetización no intrusiva
- **VIP** con planes anuales (~**30 $/año** y ~**60 $/año** según episodecompass, sep-2026) que desbloquea "advanced filters, no ads, notifications tied to a calendar, RSS updates" (sourceforge.net). 🟡
- **Límites del free tier**: foros de Trakt (2025) documentan "**Free users are now limited to 100 items in their watchlists and collections**, down from previous limits" — señal de que monetizar recortando el free tier genera fricción (hilo "More Features for All with Usage Limits"). 🟡
- Nota: el "no ads" de VIP implica que el tier free tiene ads (coherente con la reseña de precio). 🟡

---

### MyAnimeList (MAL) y AniList (anime/manga)
**Confianza global: 🟢 Alta** (Play MAL + homepage AniList verificadas) / 🟡 en estructura interna de MAL.

#### 1. Estructura (MAL)
- App oficial "**MyAnimeList Official**" — descripción: "The app for anime and manga tracking, **database, and community**" (Play). El sitio se presenta como "**the largest online anime and manga database in the world**" (myanimelist.net).
- Estructura implícita: listas por estado (viendo/completado/en pausa/abandonado/planificado) + puntuación 1–10 + stats de perfil + foros. Los detalles exactos de pestañas de la app no se verificaron. 🟡

#### 2. Features que explican su éxito (MAL)
1. **Tamaño y antigüedad de la base de datos** (anime/manga): ser "the largest… database in the world" es su foso competitivo.
2. **Scores y rankings comunitarios por temporada**; los usuarios construyen identidad alrededor de sus listas ("Check out Chiorashi's anime and manga lists, stats, favorites…" — perfiles públicos MAL). 🟡
3. (Débil en app) Comunidad/foros: la app es secundaria. 🔴

#### 3. Ficha de ítem (anime)
- No verificada esta pasada. Patrón esperable por el sector: título + score + ranking + sinopsis + staff/cast + reviews + "related". 🔴/🟡 (pendiente).

#### 4. Onboarding y sync (MAL)
- Cuenta MAL; la app tiene fama de pedir pulido (ver reseñas). **Sync**: existe ecosistema de clientes de terceros (p. ej. "MALClient — Client application for MyAnimeList.net which helps you manage your anime list", appbrain) y herramientas de sync multiproyecto (Ani-Sync: "keep your **MyAnimeList, AniList**, Kitsu, and MangaBaka accounts [in sync]", github.com). 🟡

#### 5. Señales de Google Play (MAL, verificado 29-sep-2026)
- Nota media: **3,5★**; **18,8K reseñas**; **1M+ descargas**; badge "**Contains ads**"; PEGI 16. Distribución: 5★ 7.458 / 4★ 3.413 / 3★ 2.338 / 2★ 1.074 / 1★ 3.792.
- Reseñas: (a) fricción al catalogar: *"I just made an account so I have been looking to the related animes to add new ones to my completed list. For some reaso[n]…"* — con **respuesta del desarrollador**: "we agree that many minor polishes are still needed to make the app feel more sleek…" (respuesta de 2020, aún visible); (b) estabilidad: *"the app itself won't load and run. I keep on receiving error…"* (6); (c) features pedidas: *"it would be a lot better if you can add **albums/playlists** kinda thing, so that we can categor[ize]…"* (10).
- iOS: no verificado.

#### 6. Monetización (MAL)
- Gratis con **ads** (badge verificado). No verifiqué tiers premium de MAL. 🟡

#### AniList (por qué lo incluyen los usuarios junto a MAL)
- **No hay app oficial prominente en Google Play**: la búsqueda devuelve un cliente de terceros ("Anilist - Discover Your Anime" by **X ResCode**, 5K+ descargas, verificado) y clientes alternativos en F-Droid (AniHyou). La propia AniList asume el modelo: "**Keep track of your progress on-the-go with one of many AniList apps** across iOS, Android, macOS, and Windows" (anilist.co, verificado). 🟢
- Feature set declarado (anilist.co): "Discover your obsessions — **What are your highest rated genres or most watched voice actors?** Follow your watching habits over time with **in-depth stats**"; "Join the conversation…"; "**Tweak it to your liking** — Customize your **scoring system**, title format, color scheme… dark mode". Navegación: Search / Social / Forum; secciones **Trending Now, Popular This Season, Upcoming Next Season, All Time Popular, Top 100 Anime**; filtros por *Genres & Tags / Year / Season / Format*. 🟢
- Monetización: enlace "**Donate**" en el footer (crowdfunding) + consentimiento que menciona publicidad personalizada (web). 🟢 (donate) / 🟡 (ads).
- Lectura de mercado: AniList se percibe como la experiencia "moderna" (stats, personalización, UI) frente a MAL (base de datos histórica); herramientas de sync existen para no elegir (Ani-Sync). 🟡

---

### Goodreads (libros)
**Confianza global: 🟢 Alta** (Play + book page verificadas).

#### 1. Estructura de la app/vistas
- Posicionamiento: "Discover new books, read reviews, **track and share your reading journey**" (Play).
- Navegación histórica del producto: **Home / My Books / Browse / Community** (verificado en la book page del sitio). La app es un subconjunto de la web (ver queja "necesito la web" abajo). 🟡 (app) / 🟢 (web).

#### 2. Las 2–3 features que explican su éxito
1. **Shelves (estanterías) como modelo mental universal de lectura**: Want to Read / Currently Reading / Read, y desde 2024 también **Did Not Finish** como shelf por defecto: "New default shelf: Readers can mark a book **Did Not Finish** the same way as Want to Read, Currently Reading, or Read" (blog oficial de Goodreads, "top-requested feature"). 🟢
2. **Reading Challenge**: "lets readers choose an annual reading goal and track the books they finish" (bebooksharp; y help.goodreads.com sobre cómo cuentan los libros por fecha de fin). Metagaming anual que engancha. 🟡
3. **Escala de reseñas/social de la comunidad más grande de lectura** + pertenencia al ecosistema **Amazon** (compra integrada). 🟡

#### 3. Ficha de ítem (bloques y orden) — book page verificada (The Hunger Games)
1. Etiqueta de serie ("Book 1 in the The Hunger Games series") → Título → Autora.
2. **Acciones de shelf**: botón "**Want to Read**" + selector de shelf + **"Shop this series"** + "More options to get the book".
3. **Nota media**: "Average rating of **4.36** stars. **10.295.073 ratings** and **278.120 reviews**" + "113.505 people are currently reading" + "1.959.504 people want to read".
4. **Descripción** (blurb) → **Genres** (Young Adult, Dystopia, Fiction…) → "374 pages, Hardcover" → "First published September 14, 2008" → "Book details & editions".
5. **About the author** (80 books · 131k followers; botón Follow).
6. Tabs **Ratings & Reviews / Friends & Following / Community Reviews** → histograma (5★ 5.673.150 (55%) … 1★ 134.631 (1%)) → reviews; "**Create a free account to discover what your friends think of this book!**" (gate social).
7. Bloque de discusión: "1.229 quotes · 3.805 discussions · 517 questions". 🟢

#### 4. Onboarding y sync
- Se puede **hacer browse sin cuenta** (verificado); shelving/reseñar requiere cuenta (Amazon/Goodreads).
- **No hay sync automático de lectura** (Kindle no escribe a Goodreads automáticamente en la app; esto no lo verifiqué esta pasada 🔴 — no afirmar más allá de lo visto). La reseña más votada sobre "app básica" sugiere que las funciones sociales viven en la web. 🟡

#### 5. Señales de Google Play (verificado 29-sep-2026)
- Nota media: **4,5★**; **219K reseñas**; **10M+ descargas**; badge "**Contains ads**". Distribución: 5★ 131.764 / 4★ 56.295 / 3★ 13.553 / 2★ 2.380 / 1★ 2.776.
- Reseñas: (a) **datos sucios en escaneo**: *"so many barcodes from the major UK publishers show up as 'unknown author', or with different cover pi[ctures]"* (29); (b) **app demasiado básica**: *"The app is too basic and would really benefit from the add friend features that are available on the d[esktop]"* (86); (c) **funciones de web ausentes**: *"I can't use it, I need to use the web version…"* (69).
- iOS: no verificado.

#### 6. Monetización no intrusiva
- Gratis con **ads** ("Contains ads"; footer del sitio con "Interest Based Ads"). 🤔 Cursor
- El modelo real es **estratégico para Amazon**: "Shop this series", "More options to get the book" convierten la ficha en escaparate de compra (verificado en la book page). 🟢

---

### Untappd (cerveza)
**Confianza global: 🟢 Alta** (Play verificada; flujo de check-in documentado por Untappd) / 🟡 en ficha de cerveza.

#### 1. Estructura de la app/vistas
- Posicionamiento: "**Drink Socially! Discover, buy, and share beers, breweries, and bars near you!**" (Play). Categoría *Food & Drink*; permisos declarados: "Shares Location".
- El corazón es el **feed de check-ins** (qué beben amigos/venues) + descubrimiento por proximidad: "Untappd shows you what's available at **popular places nearby**" (untappd.com). 🟢

#### 2. Las 2–3 features que explican su éxito
1. **Check-in como ritual social con contexto físico**: "Check-ins allow users to say **what they are drinking, where they are drinking it, and rate it**" (pourmybeer.com); el check-in acepta "a photo, tasting notes, a rating, the serving style, [location, tagged friends]" (help.untappd.com; babygotbeer: "score from 0 to 5… tasting notes, serving style, location, tagged [friends]"). 🟢
2. **Badges (gamificación)**: "Users check in specific beers or at specific breweries to receive a badge. The more beer and taprooms an Untappd user explores, **the more badges** they'll uncover" (lounge.untappd.com, guía oficial de badges). 🟢
3. **Uniques / coleccionismo**: cultura de "cervezas únicas" — usuarios relatan "I have about **8.800 uniques**. I'd say I've checked in almost every unique beer I've had…" (r/Untappd). Contador de colección personal como identidad. 🟡

#### 3. Ficha de ítem (cerveza)
- Campos por evidencia cruzada (scrapers que replican el modelo de datos de la página): "ratings, **ABV, IBU**, breweries, check-in [counts]" (apify.com); "**ABV, IBU, packaging type, flavor tags**" (actowizsolutions.com). La página muestra además distribución de notas, check-ins y conversación social. **Orden exacto de bloques no verificado esta pasada** — pendiente. 🟡

#### 4. Onboarding y sync
- Cuenta + check-in manual (con opción de foto/notas/venue). No hay sync con POS/bares para el usuario medio; la infraestructura de menús/venues es B2B (Untappd for Business). Localización vinculada a Foursquare genera queja (ver abajo). 🟡

#### 5. Señales de Google Play (verificado 29-sep-2026)
- Nota media: **4,7★** (la mejor del set); **305K reseñas**; **5M+ descargas**; "**Contains ads · In-app purchases**". Distribución: 5★ 236.128 / 4★ 59.598 / 3★ 2.264 / 2★ 0 / 1★ 3.870.
- Reseñas: (a) features de pago: *"it does have some issues. Mainly, **you have to subscrib[e]**…"* (5); (b) **fricción de venue/Foursquare**: *"hate foursquare, it's useless. I'd love to use your app more often but until this nonsense of only being able to add loc[ations]…"*; (c) **búsqueda**: *"The search is dreadful. I'd expect to be able to go to 'drinks' and have it show me what I most recently had. No dice."* (27).
- Elogio de App Store (US listing): *"One of the new features I really like is it's easy to find out **what my friends thought** of a particular beer, in addition to the whole untappd world."*
- iOS: no verificado (aunque hay reseña citada del listing US de App Store).

#### 6. Monetización no intrusiva
- **Consumer**: gratis con ads + IAP (verificado); algunas funciones requieren suscripción (reseña). 🟡
- **B2B fuerte** (clave del negocio): "More by Untappd" enlaza **Untappd for Business, Ekos, Inventory by Untappd** (verificado) — las herramientas a bares/cervecerías financian el ecosistema. 🟢

---

### Discogs (vinilo/música — el contraejemplo)
**Confianza global: 🟢 Alta** (Play verificada; sitio verificado parcial) / 🟡 en ficha de release.

#### 1. Estructura de la app/vistas
- Descripción oficial: "Discogs' official app. **Discover, shop, collect, and catalog music**" (Play).
- Estructura del producto (footer/estructura del sitio): "What is Discogs? **Discography · Marketplace · Collection · Wantlist · Statistics**" (discogs.com). Cuatro verbos en la app: descubrir / comprar / coleccionar / catalogar. 🟢 (descripción) / 🟡 (mapping a pestañas).

#### 2. Las 2–3 features que explican su éxito (del servicio)
1. **La base de datos + marketplace de vinilo de referencia mundial** (de facto estándar para catalogar ediciones/prensajes).
2. **Colección + wantlist + Statistics** como sistema de archivo personal de coleccionistas (footer del sitio).
3. **Marketplace con miles de sellers** — la app empuja features de compra: "Launching Your **Seller Matches**, a homepage personalization that matches you with sellers who have the most items from you[r]…" (nota de actualización en Play). 🟡

#### 3. Ficha de ítem (release)
- Evidencia básica: las páginas de release incluyen "**credits, reviews, tracks and shop**" (listing discogs.com) — es decir: créditos, tracklist, reviews y bloque de compra (marketplace) sobre la base de datos de ediciones. Orden exacto no verificado. 🟡

#### 4. Onboarding y sync
- Cuenta Discogs (OAuth); la app es cliente. **Sin sync automático**. Fricción documentada: re-logins (ver reseñas). 🟡

#### 5. Señales de Google Play (verificado 29-sep-2026) — **la campanada**
- Nota media: **2,4★** — la peor del set, y con la distribución más 1★-cargada: 5★ 5.094 / 4★ 1.619 / 3★ 1.009 / 2★ 1.408 / **1★ 11.197**; **21,1K reseñas**; **1M+ descargas**; PEGI 18.
- Reseñas: (a) UI/filtros: *"Setting filters is now much more convenient. As in it's finally **windows 98 quality**. Now on to the gripes…"* (32); (b) la paradoja marca-app: *"I'm a little surprised by the incredibly low score this app has received. I've had this app for well ove[r]…"* (108); (c) **sesión**: *"I rely on this app a lot and it is not great. **At least once a week it makes me log in again** and I have to sit there whil[e]…"* (45).
- iOS: no verificado.

#### 6. Monetización
- Marketplace (comisiones por venta) como modelo; no vi paywall de features core en el listado. 🟡
- **Lección**: marca + base de datos única ≠ app buena. Es el ejemplo perfecto de "la app como vestíbulo de un negocio grande, descuidada como producto".

---

### Backloggd (videojuegos — el espejo directo de GameVision)
**Confianza global: 🟢 Alta** (sitio y game page verificados). No es app móvil: **es web**.

#### 1. Estructura (homepage verificada)
- "**Backloggd — A Video Game Collection Tracker**". Claim: "Discover, collect, analyze your games".
- Bloques "What is Backloggd?" (order in homepage): **Track your personal game collection** ("Log any and every game you've played, are currently playing, and want to play") → **Express your thoughts with reviews** ("Every game has an **average rating** comprised of everyone's rating") → **Keep up with the latest from friends** ("an all-in-one **activity feed**") → **Create and organize games with lists** ("options such as **tracking your progress** or **enabling rankings**").
- Secciones: Recently trending / Popular reviews / Latest news / Popular lists / Coming soon / Recently anticipated / Sleeper hits.
- Escala del sitio (verificada): **Played 70.4M · Games 373K · Ratings 42.7M · Reviews 4.88M · Lists 844K**.
- Navegación: Games, Search, Log In, Register, "Create a free account". Footer: About / Contact / **Backers** / Roadmap / Terms / Privacy. "© Backloggd LLC • v1.18.1 · **IGDB**" (datos de juegos de IGDB). 🟢

#### 2. Las 2–3 features que explican su éxito
1. **Diario de juegos con estados + rating estilo Letterboxd**: estados vistos en la game page: **Plays / Playing / Backlogs / Wishlists** (Elden Ring: 194K / 18K / 44K / 26K) — el vocabulario exacto que los gamers usan ("backlog"). 🟢
2. **Capa social ligera pero presente**: "I like Backloggd the most out of any I've tried **due to social features**" (famiboards.com); "Every game… to **log into your journal**. **Follow friends** along the way to share your reviews and compare ratings" (resetera.com). 🟡
3. **Catálogo completo vía IGDB**: "Backloggd pulls its game data from **IGDB**, so every game exists and **search works well**" (twoaveragegamers.com, prueba de la app). La exhaustividad del catálogo es requisito de entrada. 🟡

#### 3. Ficha de ítem (game page verificada — Elden Ring), orden observado
1. Cabecera: título + dev ("by FromSoftware, Bandai Namco Entertainment") + **Released** (fecha) + plataformas.
2. **GENRES** (Adventure, RPG) + **AWARDS** (Game of the Year, Gameplay, Art Direction, Soundtrack).
3. **Descripción**.
4. **PLATAFORMAS** (Windows PC, Switch 2, PS5, Xbox Series X|S, PS4, Xbox One).
5. **IGDB Avg Rating 4.5** + "**Ratings 143K**" + histograma 1★–5★.
6. **Stats de comunidad** (todas las versiones): Plays 194K · Playing 18K · Backlogs 44K · Wishlists 26K.
7. **Tiempo**: "**149 h average · 96 h to finish · 119 h to master**" (métrica tipo HowLongToBeat integrada).
8. **Lists 23K · Reviews 23K · Likes 31K**.
9. **Related Games: DLC / Editions / Series / Mods** (pestañas) + selector "**All Editions**" (incluye ediciones específicas: Launch Edition, Deluxe, Shadow of the Erdtree edition…).
10. Sección de **Reviews** (pestañas "Trending / Top Liked / Latest").
11. Gate suave: "**Create an account or log in to access tracking features**". 🟢

#### 4. Onboarding y sync
- **Explorar sin cuenta: sí** (toda la ficha y las listas se ven sin login); el tracking está gateado con copy suave ("Create a free account").
- **Sync**: manual (marcar juegos a mano); el changelog 1.17 del Patreon añade granularidad: "log any game that belongs to a bundle while still **keeping track exactly which version you played**" — las **Ediciones** combaten el caos de versiones/ports. Sin integración Steam/consolas verificada esta pasada. 🟡
- Web only: no verifiqué app móvil; el producto es la web (no vi enlaces a app en la homepage). 🟡

#### 5. Señales de tienda
- **No es app**: no hay ficha de Google Play que puntuar (no encontrada; el producto es web). Por tanto: **no hay "nota de tienda"** para Backloggd; su señal pública son testimonios en foros ("Backloggd is the go-to game tracker?", famiboards) y su volumen (70.4M games logged). 🟢
- Cookie consent del sitio menciona "Personalised advertising…" → señal de monetización con publicidad en web. 🟡

#### 6. Monetización
- Footer **"Backers"** + posts en **Patreon** ("Bundles & More | 1.17 Update | Backloggd", patreon.com) → modelo **crowdfunding/Patreon** para features y sostenibilidad. 🟡

---

## Patrones transversales (qué repiten las exitosas)

1. **Anatomía común**: Catálogo (búsqueda/database) → Registro personal (diary/check-in/log) → Social (reviews/feed/listas) → Stats personales (+recap anual) → Ficha de ítem rica. Cada app enfatiza un pilar, pero **ninguna exitosa omite dos de cinco**.
2. **Un modelo de estados corto y compartido**: los estados viven en todas las vistas con el mismo vocabulario (Goodreads: Want to Read/Reading/Read/DNF; Backloggd: Played/Playing/Backlog/Wishlist; MAL: listas por estado). Coherencia total entre biblioteca, ficha y feed.
3. **La nota comunitaria con histograma es estándar**: IMDb 10★ (con conteos por nivel), Letterboxd medias estrellas (10 niveles), Goodreads 5★ + %, Backloggd 5★. El histograma es dato de decisión, no decoración.
4. **"Dónde verlo/comprarlo" va temprano en la ficha**: Letterboxd pone **WHERE TO WATCH arriba del todo** (JustWatch); IMDb integra STREAMING/RENT-BUY en el primer scroll; Discogs integra "shop"; Goodreads "Shop this series". La ficha es también escaparate.
5. **Listas y rankings como contenido de primer nivel**: Top 500/Top 250 (Letterboxd), Top 100 (AniList), listas "added by 4.5M users" (IMDb), 844K listas (Backloggd). Las listas hacen que el catálogo sea expresivo y compartible.
6. **El recap anual es la bomba de crecimiento estacional**: Year-in-Review de Letterboxd es un fenómeno cultural anual; AniList/MAL basan buena parte de su engagement en stats; Untappd en badges/progreso.
7. **Explorar sin cuenta, actuar con cuenta**: IMDb, Letterboxd, Backloggd y Goodreads dejan navegar todo el contenido sin login y colocan el gate en el momento de registrar ("Sign in to log, rate or review" / "Create an account or log in to access tracking features"). Es el mejor onboarding que existe: muestra valor antes de pedir nada.
8. **El sync automático, donde es posible, es imbatible**: Trakt (media centers), y el ecosistema se organiza alrededor de "no volver a marcar a mano". Donde no es posible (cine en general), el loop manual se gamifica con fricción mínima (1 toque).
9. **Monetización no intrusiva = free generoso + tier anual barato + B2B**:
   - Letterboxd: gratis completo + Pro/Patron anuales (~19/49 $) + ads discretas para free.
   - Untappd: gratis + suscripción ligera + **B2B** (for Business/Ekos/Inventory).
   - Trakt: VIP (~30/60 $)... pero capando el free tier a 100 ítems → fricción visible en reseñas.
   - IMDb/Discogs: ads y negocio adyacente (Pro, marketplace).
   - AniList: **donaciones** y API como servicio al ecosistema.
10. **La ficha aguanta el peso de toda la app**: en web Y en móvil, la mayoría de la conversación del usuario con el producto pasa por el ítem (película/juego/libro/cerveza). Invertir en la ficha = invertir en retención.
11. **Apps "segunda pantalla" del negocio web se llevan las peores reseñas** (Goodreads 4,5 con críticas duras de "app básica"; Discogs 2,4): el usuario castiga que la app sea un recorte. Corolario: si GameVision es mobile-first, la ficha y el log tienen que ser **mejores** que la web, no reflejo.
12. **La portabilidad es confianza**: el apagón de TV Time (jul-2026) y el pánico por exports perdidos demuestran que "tus datos son tuyos" (export/import robusto) es parte del contrato emocional del tracking.

---

## Qué quiere la gente (deseos y dolores extraídos de reseñas y guías)

**Deseos (elogios repetidos):**
- *"IMDb is a go-to platform… vast database of titles, ratings, and reviews"* (reseña más votada de IMDb Play, 3.450 helpful).
- *"I loved this app. 5★, had zero issues with it"* (Letterboxd; el amor va a la experiencia de red social, no a la app: misma reseña pide poder saltar reseñas largas).
- *"it's easy to find out what my friends thought of a particular beer, in addition to the whole untappd world"* (Untappd, App Store).
- *"I like Backloggd the most out of any I've tried due to social features"* (famiboards).
- *"Every game from every platform is here for you to log into your journal. Follow friends along the way to share your reviews and compare ratings"* (resetera sobre Backloggd).
- Guías piden y celebran: backlog/playing/completed/dropped, wishlist separada, ediciones ("keeping track exactly which version you played", Backloggd 1.17), tiempo de juego ("96 h to finish", Backloggd), stats del año ("Letterboxd Wrapped 2025… summarizing users' movie logs", academicjobs).
- Guías de tracking de TV valoran el sync automático y la bidireccionalidad ("2 way [sync]", blog.stremio).

**Dolores (quejas repetidas):**
- **Ads intrusivos**: "Ads are auto-opening without clicking on them" (Letterboxd, 240 helpful); ads en galerías (IMDb, 55 helpful). La gente tolera ads estáticos; castiga los que interrumpen o abren solos.
- **Rediseños/UX**: "hate the new update. Terrible movement of search bar" (IMDb, 61); "navigation too confusing and laborious" (Trakt, 8); "navigation… could be more intuitive" (Letterboxd).
- **Precio/límites**: "VIP price is too expensive… one time payment option" (Trakt); "Free users are now limited to 100 items in their watchlists" (foros Trakt); "you have to subscrib[e]" (Untappd).
- **Fiabilidad/sesión**: "At least once a week it makes me log in again" (Discogs, 45); "the app won't load… receiving error" (MAL, 6).
- **Calidad de datos**: "barcodes… show up as 'unknown author' or with different cover" (Goodreads, 29).
- **App recortada vs web**: "The app is too basic… add friend features [like] on the desktop" (Goodreads, 86); "I need to use the web version" (69).
- **Migraciones traumáticas**: refugiados de TV Time frustrados al importar ("After I uploaded my data [it] asked me to check all…", Trakt; y todo el ecosistema de guías para exportar antes del cierre del 15-jul-2026).
- **Búsqueda pobre**: "The search is dreadful… show me what I most recently had. No dice" (Untappd, 27).

**Síntesis de expectativas**: registro en segundos; encontrarlo TODO (catálogo completo); ver mi vida en datos; control de mis datos (export); una app que no me haga repetir logins; gratis útil sin muros molestos; pagar solo si es barato y opcional.

---

## Implicaciones para GameVision (qué copiar, qué evitar, reglas de cohesión)

### Reglas de cohesión vistas ↔ APIs (una intención por vista)
1. **Home/Descubrir = Trending + estrenos + "para ti"**: espejo de "En tendencia / Popular this season / Upcoming" (Trakt, AniList, Backloggd). APIs: tendencias/populares + próximos lanzamientos (Steam/IGDB). Nada de ofertas ni log aquí.
2. **Biblioteca = estados**: modelar 5 estados cortos y estables: **Pendiente (backlog) / Jugando / Completado / Abandonado / Wishlist** (patrón Goodreads DNF + Backloggd Played/Playing/Backlog/Wishlist + Trakt "Drop"). Toda vista usa los mismos estados.
3. **Diario/Log = el loop**: registrar en 1–2 toques desde la ficha y desde búsqueda (patrón "log films" de Letterboxd; check-in de Untappd). API: persistencia local primero; sync después (offline-friendly).
4. **Ficha de juego = la joya** (ver abajo).
5. **Perfil/Stats = identidad**: horas por género, plataforma, año; notas; logros totales; **recap anual** (patrón Letterboxd Wrapped/MAL stats) alimentado por datos locales + APIs (Steam playtime, achievements).
6. **Listas = contenido**: ordenables, rankeables, compartibles ("tracking progress or enabling rankings" — Backloggd). Fáciles de crear; oficiales y de usuario (Top patrón Letterboxd).
7. **Feed social (fase posterior)**: amigos + actividad (patrón Backloggd/Letterboxd). No contaminar ficha y biblioteca con social; el feed vive en su pestaña.
8. **Ofertas = módulo de decisión en ficha + vista propia opcional**: patrón "WHERE TO WATCH" de Letterboxd (arriba, compacto) con precios/descuentos (Steam/ITAD). Nunca pop-ups; nunca intersticiales.
9. **Onboarding: explorar sin cuenta** (patrón universal verificado); el registro se pide en el primer "marcar/valorar". OAuth preferente: **Steam primero** (biblioteca + horas + logros = el "scrobble de juegos"), Google/email después; prometer import/export (lección TV Time).
10. **Logros en la ficha**: bloque con progreso global y rareza (patrón Untappd badges → versión Steam achievements: % de jugadores por logro). Es la gamificación "nativa" del videojuego, equivalente a los badges de Untappd.

### Ficha de ítem GameVision — orden recomendado (basado en el patrón verificado)
1. Cabecera: carátula/arte + título + desarrollador/publicador + plataformas + fecha + **estado propio** (chips de estado + botón "+"/Log).
2. **Nota GameVision** (media + nº de valoraciones) + **tu nota** + histograma (patrón IMDb/LB/Goodreads/Backloggd).
3. **Acciones**: estado (Backlog/Jugando/Completado/Abandonado/Wishlist) + favorito + compartir.
4. **Metadatos**: géneros, tags, duración media ("96h to finish" patrón Backloggd/HLTB), multijugador, ediciones/DLC/series (patrón Backloggd "Related Games: DLC/Editions/Series/Mods" — crítico en juegos por versiones/ports).
5. **Logros** (si hay cuenta vinculada a Steam): total, completado %, logros raros, progreso.
6. **Ofertas/Comprar**: precios actuales, mejor descuento histórico, tiendas (Steam/ofertas).
7. **Sinopsis**.
8. **Reviews**: populares (con likes) y recientes + "escribir reseña" (patrón Letterboxd).
9. **Listas y colecciones que lo contienen** (patrón "Mentioned by popular lists").
10. **Similar games**.
11. **Stats de comunidad**: "X jugadores lo tienen", "Y jugando ahora", "Z lo completaron" (patrón de contadores Letterboxd/Backloggd).

### Qué copiar (con su fuente)
- **Explorar sin cuenta** + gate suave ("Create an account or log in to access tracking features" — Backloggd) → conversión sin muro.
- **Histograma + doble capa de nota** (global/tuya) — IMDb/Letterboxd.
- **Estados cortos con "dropped"** (Goodreads DNF; Trakt Drop).
- **Ediciones y versiones exactas** ("keeping track exactly which version you played" — Backloggd 1.17).
- **Tiempo de juego/duración** ("96 h to finish" — Backloggd; "2h 49min" — IMDb).
- **WHERE TO WATCH/Comprar arriba y compacto** (Letterboxd/IMDb).
- **Listas rankeables + listas "oficiales"** (Letterboxd Top 500; Backloggd rankings).
- **Recap anual** (Letterboxd Wrapped) — el mayor generador de screenshots/SEO del sector.
- **Badges/logros gamificados** (Untappd badges; progreso de logros Steam como dato de ficha).
- **Feed de amigos en pestaña propia** (Backloggd/Letterboxd).
- **Pro anual barato y honesto** (~19–20 $/año estilo Letterboxd) con stats avanzadas/recap extendido/export — y **free sin castraciones dolorosas** (lección Trakt: el cap de 100 ítems es una herida abierta).

### Qué evitar (con la evidencia del fracaso)
- **Ads que abren solos/intersticiales**: generan 1★ masivos en Letterboxd (240 helpful) y IMDb (55). Si hay ads, que sean módulos estáticos no bloqueantes.
- **Paywalls sobre el core** (watchlist/watchlist-limits): Trakt VIP "too expensive" + cap del free → reseñas negativas directas.
- **Re-logins y sesiones frágiles**: Discogs "once a week it makes me log in again" → 2,4★ globales. Token de Steam persistente, refresh silencioso, degradación offline.
- **Reordenar la UI de un día para otro**: IMDb "hate the new update… search bar moved" (61 helpful). Cambios de navegación = comunicarlos y no romper músculo.
- **Ser "la app recortada"**: Goodreads (86-helpful: "too basic… add friend features that are available on the desktop"). GameVision debe tener log, stats y logros de serie en móvil.
- **Búsqueda mediocre**: Untappd "search is dreadful" (27). La búsqueda es la puerta de entrada al loop; debe buscar por título, alias, plataforma y tolerar errores.
- **Datos sucios**: Goodreads "unknown author/different cover" (29). En juegos: normalizar títulos/edicones (IGDB/Steam como fuente canónica, como Backloggd con IGDB).
- **Perder datos de usuarios**: el caos TV Time → import/export de primera clase, y avisar antes de tocar nada.

### Cohesión con las APIs (Steam, logros, ofertas) — vista por vista
- **Steam sync**: vive en Onboarding y en Ajustes; su huella visual son horas de juego y biblioteca ("Jugando ahora" real) y metadatos en la ficha; nunca debe bloquear el registro manual (fallback siempre disponible).
- **Logros**: solo en la ficha (bloque propio) y en Stats de perfil (totales/% completado). Fuera de biblioteca y feed para no saturar.
- **Ofertas**: módulo "Comprar" en ficha + vista opcional "Ofertas" con wishlist-intersection (descuentos de tus juegos wishlisteados — patrón natural post-verificación: "Seller Matches" de Discogs, "where to stream" de Trakt). Nunca en el flujo de log.

---

## Fuentes (URLs)

**Google Play (verificadas en navegador el 29-sep-2026):**
- IMDb: https://play.google.com/store/apps/details?id=com.imdb.mobile
- Letterboxd: https://play.google.com/store/apps/details?id=com.letterboxd.letterboxd
- Trakt: https://play.google.com/store/apps/details?id=tv.trakt.trakt
- MyAnimeList Official: https://play.google.com/store/apps/details?id=net.myanimelist.app
- Goodreads: https://play.google.com/store/apps/details?id=com.goodreads
- Untappd: https://play.google.com/store/apps/details?id=com.untappdllc.app
- Discogs: https://play.google.com/store/apps/details?id=com.discogs.app
- AniList (cliente de terceros, X ResCode): https://play.google.com/store/apps/details?id=com.anilist.android

**Páginas de producto verificadas:**
- IMDb — Interestelar (title page): https://www.imdb.com/title/tt0816692/
- Letterboxd — Interstellar (film page): https://letterboxd.com/film/interstellar/
- Backloggd — home: https://backloggd.com/ · Elden Ring: https://backloggd.com/games/elden-ring/
- Goodreads — The Hunger Games: https://www.goodreads.com/book/show/2767052-the-hunger-games
- Trakt — home: https://app.trakt.tv/
- AniList — home: https://anilist.co/

**Búsquedas / fuentes secundarias citadas:**
- Untappd badges (oficial): https://lounge.untappd.com/everything-you-need-to-know-about-untappd-badges
- Untappd check-in (oficial): https://help.untappd.com/hc/en-us/articles/360034404451-How-to-Check-In-a-Beer
- Untappd site: https://untappd.com/ · reseña de check-in: https://pourmybeer.com/how-untappd-can-drive-your-beverage-sales-to-the-next-level · notas de user: http://babygotbeer.com/2022/10/01/untappd-users · App Store US: https://apps.apple.com/us/app/untappd-find-drinks-you-love/id449141888
- R/Untappd (uniques): https://www.reddit.com/r/Untappd/comments/1d2qma3/people_who_have_1000s_of_checkins_have_you
- Datos de campos (scrapers): https://www.actowizsolutions.com/ · https://apify.com/
- Letterboxd growth: https://tesi.luiss.it/ · https://sherwood.news/ · https://simplywall.st/
- Letterboxd Wrapped: https://www.academicjobs.com/ · https://www.trillmag.com/
- Letterboxd Pro/Patron: https://au.lifehacker.com/ · https://letterboxd.zendesk.com/ · https://en.iphonea2.com/
- Letterboxd feature deep-dive (terceros): https://github.com/ · https://worldos.cc/
- Letterboxd app iOS/release notes: https://apps.apple.com/ · https://www.apkmirror.com/ · http://letterboxd.apk.watch/
- Trakt sync/scrobble: https://pypi.org/ · https://blog.stremio.com/ · https://forums.trakt.tv/ · https://guides.viren070.me/ · https://github.com/ (plembfin)
- Trakt VIP/limites: https://episodecompass.com/ · https://sourceforge.net/ · https://forums.trakt.tv/
- Trakt roadmap: https://roadmap.trakt.tv/changelog
- Trakt Drop/Up Next (comunidad): https://www.reddit.com/r/trakt/comments/1jm1byt/goodbye_hide_hello_drop
- Trakt App Store: https://apps.apple.com/eg/app/trakt-tv-time-to-watch/id1514873602
- TV Time shutdown/éxodo: https://cinopsysapp.com/ · https://tvtrack.io/ · https://moviebase.app/ · https://hobiapp.com/
- MyAnimeList: https://myanimelist.net/ · MALClient (appbrain): https://www.appbrain.com/
- AniList apps de terceros: https://f-droid.org/ (AniHyou) · https://apkcombo.com/
- Ani-Sync (sync multi-cuenta): https://github.com/
- Goodreads DNF shelf (oficial): https://www.goodreads.com/blog/show/3115-new-top-requested-reader-feature-did-not-finish-shelf
- Goodreads Reading Challenge: https://bebooksharp.com/ · https://help.goodreads.com/
- IMDb descripción app: https://www.apkmirror.com/ · watchlist personalization: https://www.imdb.com/
- Discogs estructura: https://www.discogs.com/
- Backloggd contexto: https://www.twoaveragegamers.com/ · https://famiboards.com/ · https://www.resetera.com/ · https://forums.insertcredit.com/ · https://www.patreon.com/
- Letterboxd IMDb TMDB (footers y créditos de datos verificados en páginas).

*Fin del informe — Dive 01.*
