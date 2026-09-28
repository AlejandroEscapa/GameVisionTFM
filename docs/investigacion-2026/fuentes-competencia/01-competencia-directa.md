# Dimension 01: Competencia directa — trackers de videojuegos

## Current State

- El patrón cultural dominante del sector es "el Letterboxd de los videojuegos": la propia Backloggd se presenta como "Keep a virtual backlog of your video game collection, then rate and review the ones you've played to share with your friends" [^backloggd-site], y foros como ResetEra llevan años discutiendo "We are getting closer to a Letterboxd for games" [^resetera-letterboxd]. El sector es un conjunto de proyectos independientes, no un mercado consolidado.
- **Backloggd** es el líder de facto entre los trackers web de tipo "diario/crítica social". Un análisis de producto le atribuye ~650.000 usuarios, "the best UI of any game tracker" y señala que "every functional feature is free" (logging, ratings, reviews, lists, folders) [^twoaveragegamers-backloggd]. Su propuesta es 100% navegador: no hay app nativa destacada, lo que deja abierto el flanco móvil Android [^backloggd-site].
- **GG (ggapp.io)** se posiciona como "your gaming companion, a place to connect with fellow gamers, discover new games, and keep track of your experiences all in one platform" [^gg-appstore]. Es multiplataforma (web + app) y tiene una versión de escritorio listada en Microsoft Store [^gg-msstore]. (No se han encontrado cifras verificadas de usuarios ni precios concretos en esta ronda; ver Tensions.)
- **Stash (stash.games)** es el competidor móvil más directo de GameVision: existe como app en Google Play y App Store, y su pitch es "Track your games, manage your backlog, discover new titles and share your gaming journey with friends" [^stash-resetera]. Ofrece "a huge database of video games with complete entries, DLCs, screenshots, trailers, and themed lists" [^stash-androidayuda] y tiene reseñas activas en la App Store [^stash-appstore].
- **GameTrack** es la alternativa fuerte en iOS/iPadOS: "an iOS and iPadOS app that makes managing your video game collection easy and enjoyable" con cuenta para compartir la colección [^gametrack-alternativeto]; organiza biblioteca, "currently playing", backlog y wishlist multi-plataforma [^gametrack-gamerdawn]. No se ha confirmado versión Android nativa.
- **HowLongToBeat (HLTB)** es el estándar de facto para estimaciones de duración y, de forma secundaria, tracker: "Create a backlog, submit your game times and compete with your friends" [^hltb-resetera], con foro activo [^hltb-forum]. Sus tiempos se han convertido en infraestructura que otros reutilizan (IGN Playlist los integra) [^ign-hltb]; está "powered by IGDB" [^hltb-quartertothree].
- **Grouvee** es un tracker web gratuito con "optional paid support tier", importación de Steam, timelines, exportación CSV y "deep shelves"; usa la API de Giant Bomb y "tracks different game versions" [^grouvee-shouldiuse]. Un usuario histórico lo describe como fácil de usar pero "lacks the ability to write in details" [^grouvee-resetera]. Tiene foro propio (discuss.grouvee.com) [^grouvee-forum].
- **IGN Playlist** es un tracker incrustado en un gran medio: permite "create playlists for games and find IGN's own reviews and guides for them", con tiempos de HowLongToBeat integrados [^ign-hltb]. Su fuerza es el respaldo editorial; su límite, ser una feature dentro de IGN y no un producto independiente.
- **RAWG** es ante todo base de datos/descubrimiento ("largest video game discovery and info service") con tracking ligero: "cross-platform sync, instant status tracking, IGDB-powered data, wishlist with release alerts" [^rawg-alternativeto], y perfiles tipo "keep all games in one profile, see what friends are playing" [^rawg-resetera]. Su API la convierte en proveedor de datos para terceros.
- **Completionator** es un tracker web orientado a progreso y colección: "visual breakdown of your cross-platform collection, tracks gaming achievements and statistics" [^completionator-steam], muy recomendado en comunidades de backlog (p. ej. junto a Backloggery y HLTB) [^completionator-resetera].
- **MyVideoGameList** es un tracker web social de perfil público: "create your own profile, keep track of your video games, meet new friends, and even write reviews" [^mvglist-alternativeto].
- **GameFAQs** cubre tracking/colecciones como función secundaria de un portal de guías: "suited to players who are comfortable opening a browser for reference material, tracking games, managing collections" [^gamefaqs-itechguides]; es 100% navegador y sin app nativa [^gamefaqs-itechguides].
- **Darkadia, citado en la misión, está MUERTO**: era "a beloved game collection tracker that is no longer operating" [^darkadia-nexorious]; el aviso de cierre se discutió en foros de coleccionistas [^darkadia-vgcollect]. No es competencia viva.
- **Aclaración importante sobre PlayDex**: la app "Playdex" de la App Store es un tracker de **juegos de mesa**, no de videojuegos ("organize your board game life... Log each session in seconds") [^playdex-appstore]. No es competencia directa tal como se planteó.
- Ecosistema menor/long tail relevante: **Backloggery** (tracker web manual, sin base de datos propia) [^backloggery-mal], **GameTrekker** (app iOS con "500,000+ game database") [^gametrekker-appstore], y nuevos entrantes como GamersVault, Vaultkeeper, Vaulted.Games, Memorycard o Gamelix que compiten por el término "best video game tracker 2025/2026" [^vaultkeeper-ranking][^gamersvault-ranking][^memorycard-appbrain][^gamelix-chromestats].
- Patrón de monetización del sector: mayoritariamente **gratis** (Backloggd "every functional feature is free" [^twoaveragegamers-backloggd]) con tiers opcionales de apoyo (Grouvee)[^grouvee-shouldiuse] o modelos como el de GG/Stash con app de escritorio y móvil [^gg-msstore][^stash-appstore]. No se han hallado cifras de precios concretas verificadas en esta ronda.

## Key Evidence

| App | Plataforma | Modelo | Funciones destacadas | Debilidad | Fuente |
|---|---|---|---|---|---|
| Backloggd | Web (navegador) | Gratis total (todas las funciones) | Logging, ratings, reviews, listas, carpetas, UI referenciada como la mejor; ~650K usuarios | Sin app nativa móvil clara; dependencia del navegador | [^backloggd-site][^twoaveragegamers-backloggd] |
| GG (ggapp.io) | Web + app + escritorio (MS Store) | Freemium (no confirmado) | Conexión con otros jugadores, descubrimiento, tracking en una plataforma | Datos de usuarios/precio no verificados | [^gg-appstore][^gg-msstore] |
| Stash (stash.games) | Android (Google Play) + iOS (App Store) | Gratis con posible premium (no confirmado) | Gestión de backlog, base de datos con DLC/screenshots/trailers, listas temáticas, social | Monetización no verificada; app móvil real (rival directo) | [^stash-resetera][^stash-androidayuda][^stash-appstore] |
| GameTrack | iOS / iPadOS (web parcial) | Gratis (no confirmado) | Biblioteca, "playing", backlog, wishlist multiplataforma, cuenta compartible | Sin Android nativo confirmado | [^gametrack-alternativeto][^gametrack-gamerdawn] |
| HowLongToBeat | Web | Gratis (datos/comunidad) | Estimaciones de duración, backlog personal, foros, "compete with friends"; datos vía IGDB | Tracking social limitado; sin app propia | [^hltb-resetera][^hltb-forum][^hltb-quartertothree] |
| IGN Playlist | Web (dentro de IGN) | Gratis | Playlists, reviews/guias propias, tiempos HLTB integrados | Feature de un medio; poco social independiente | [^ign-hltb] |
| Grouvee | Web | Gratis + tier de apoyo | Shelves, import Steam, timelines, export CSV, versiones de juego, foro | Poco detalle en reseñas ("lacks ability to write in details"); UI antigua | [^grouvee-shouldiuse][^grouvee-resetera] |
| RAWG | Web + API (discovery) | Gratis/API | Mayor BD de descubrimiento, status tracking, sync cross-platform, wishlist con alertas | Más catálogo que tracker personal; social superficial | [^rawg-alternativeto][^rawg-resetera] |
| Completionator | Web | Gratis | Seguimiento de progreso, colección cross-platform visual, logros y estadísticas | UX/alcance menor; sin app móvil | [^completionator-steam][^completionator-resetera] |
| MyVideoGameList | Web | Gratis | Perfil propio, tracking, amistades, reseñas | Base de usuarios pequeña; producto poco actualizado | [^mvglist-alternativeto] |
| GameFAQs | Web | Gratis (contenido+ads) | Colecciones/tracking junto a guías y foros | Sin app nativa; tracking secundario | [^gamefaqs-itechguides] |
| Darkadia | (cerrado) | — | Tracker de colección muy querido históricamente | Ya no opera (RIP) | [^darkadia-nexorious][^darkadia-vgcollect] |
| Playdex | iOS | — | EN REALIDAD tracker de juegos de MESA, no videojuegos | No es competencia directa | [^playdex-appstore] |

## Tensions & Counter-arguments

- **"Sin app nativa fuerte" es la grieta del sector en Android.** Casi todos los líderes (Backloggd, Grouvee, Completionator, HLTB, GameFAQs, MyVideoGameList) son productos web. GameVision, siendo Android nativo (Kotlin + Compose), puede explotar esa carencia — pero el propio dato hay que tratarlo con cautela: no he podido verificar en esta ronda si Backloggd o Grouvee ofrecen PWA/paso móvil suficiente, así que la ventaja móvil debe confirmarse antes de asumirla como "moat".
- **La cifra "650K usuarios" de Backloggd proviene de un blog de producto (twoaveragegamers), no de una fuente oficial ni de la propia empresa** [^twoaveragegamers-backloggd]: úsala como orden de magnitud, no como dato duro. Igual ocurre con los rankings "best tracker" de Vaultkeeper/GamersVault, que son comparativas de marketing de productos emergentes [^vaultkeeper-ranking][^gamersvault-ranking].
- **NO se han encontrado cifras verificables en esta ronda** para: usuarios activos de GG, Stash, GameTrack, Grouvee o Completionator; precios/suscripciones exactos de GG y Stash; y ratings/descargas concretas en Google Play/App Store. Se han evitado deliberadamente números inventados; si GameVision necesita TAM competitivo, requiere una ronda específica (Sensor Tower / AppBrain / Similarweb).
- **La "social" es el campo de batalla real, no el catálogo.** Todos ofrecen tracking y puntuación; casi ninguno ha logrado el efecto red tipo Letterboxd [^resetera-letterboxd]. El foso no es la base de datos (RAWG, IGDB, Giant Bomb, HLTB son reutilizables vía API) sino la comunidad/feed; GameVision —hoy "catálogo + noticias + social básico"— compite precisamente donde el mercado demuestra que es más difícil ganar.
- **Riesgo de "commoditización" por datos.** Que RAWG/IGDB/HLTB sean infraestructura compartida [^rawg-alternativeto][^hltb-quartertothree] abarata construir el catálogo pero también borra diferenciación por contenido; la ventaja defendible sería UX nativa Android + hábito de registro diario, no la ficha del juego.
- **Corrección de la hipótesis de la misión:** dos de los nombres citados no encajan como competidores — Playdex es de juegos de mesa [^playdex-appstore] y Darkadia está cerrado [^darkadia-nexorious]. Conviene reasignar el foco a Stash, GameTrack, Backloggd, GG y los emergentes móviles.

[^backloggd-site]: Backloggd. Sitio oficial (home). 2026. https://backloggd.com/
[^twoaveragegamers-backloggd]: Two Average Gamers. "Backloggd Review: The Best Game Tracker?". 2026-09-16. https://www.twoaveragegamers.com/
[^gg-appstore]: Apple App Store. "GG - App Store". 2026. https://apps.apple.com/
[^gg-msstore]: Microsoft Store. "GGapp - Free download and install on Windows". 2026-08-12. https://apps.microsoft.com/
[^stash-resetera]: ResetEra. "Stash is one of the best video game tracker/logging [apps]". 2024-11-16. https://www.resetera.com/
[^stash-androidayuda]: AndroidAyuda (EN). "Stash Game Collection Tracker: Organize, record...". 2026. https://en.androidayuda.com/
[^stash-appstore]: Apple App Store. "Stash - Video Games Tracker - Ratings & Reviews". 2026. https://apps.apple.com/
[^gametrack-alternativeto]: AlternativeTo. "KTOMG Alternatives: Game Library Managers". 2026-07-09. https://alternativeto.net/
[^gametrack-gamerdawn]: GamerDawn. "Top 5 Apps Like Letterboxd for Video Games". 2026-06-24. https://gamerdawn.com/
[^hltb-resetera]: ResetEra. "Why do games rarely tell me how close I am to beating them?". 2021-07-14. https://www.resetera.com/
[^hltb-forum]: HowLongToBeat. Foro oficial ("Unified Backlog"). 2026. https://www.howlongtobeat.com/
[^hltb-quartertothree]: Quarter to Three (foro). "Where can I store my retro game reviews?". 2025-10-28. https://forum.quartertothree.com/
[^ign-hltb]: Jason Journals. "You can create playlists for games... HowLongToBeat playtimes are also integrated". 2026. https://jasonjournals.com/
[^grouvee-shouldiuse]: shouldiuse.io. "Should I Use Grouvee? Honest Review, Pricing & Gotchas". 2026. https://shouldiuse.io/
[^grouvee-resetera]: ResetEra. "How do you track and list your games ERA?". 2019-05-23. https://www.resetera.com/
[^grouvee-forum]: Grouvee. Foro ("Top Games of 2023"). 2023-12-17. https://discuss.grouvee.com/
[^rawg-alternativeto]: AlternativeTo. "Apps with 'Game database' feature". 2026. https://alternativeto.net/
[^rawg-resetera]: ResetEra. "How do you keep track of what games you own?". 2020-04-22. https://www.resetera.com/
[^completionator-steam]: Steam Community. "A Collection of Steam Tools". 2026. https://steamcommunity.com/
[^completionator-resetera]: ResetEra. "Game Blitz 2025". 2025-01-16. https://www.resetera.com/
[^mvglist-alternativeto]: AlternativeTo. "Completionator Alternatives: Top 22 Game Library [Managers]". 2026-07-27. https://alternativeto.net/
[^gamefaqs-itechguides]: iTechGuides. "GameFAQs Review (2026): Pricing, Pros & Cons". 2026-09-20. https://www.itechguides.com/
[^darkadia-nexorious]: GitHub (drzero42/nexorious). "Nexorious was inspired by Darkadia (RIP)... no longer operating". 2026. https://github.com/
[^darkadia-vgcollect]: VGCollect (foro). "Darkadia... existence of the website may be in jeopardy". 2026. https://vgcollect.com/
[^playdex-appstore]: Apple App Store. "Playdex". 2026-02-26. https://apps.apple.com/
[^resetera-letterboxd]: ResetEra. "We are getting closer to a Letterboxd for games." 2019-01-06. https://www.resetera.com/
[^backloggery-mal]: MyAnimeList (foro). "Backloggery is the best site for video games... no database". 2017-03-27. https://myanimelist.net/
[^gametrekker-appstore]: Apple App Store. "GameTrekker". 2026. https://apps.apple.com/
[^vaultkeeper-ranking]: Vaultkeeper. "Best Video Game Collection Tracker Apps 2026". 2026-08-08. https://www.vaultkeeper.app/
[^gamersvault-ranking]: GamersVault. "Best Video Game Tracker 2025". 2025. https://gamersvaultapp.com/
[^memorycard-appbrain]: AppBrain. "Memorycard: Game Tracker". 2026-08-31. https://www.appbrain.com/
[^gamelix-chromestats]: Chrome-Stats. "Gamelix: Track Games | User reviews and ratings". 2026. https://chrome-stats.com/
