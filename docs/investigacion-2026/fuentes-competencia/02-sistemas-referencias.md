## Dimension 02: Sistemas y mecánicas de las apps de referencia (IMDb/Letterboxd para X)

> Objetivo: inventariar los **sistemas y mecánicas** (no las apps en sí) de los referentes de tracking + valoración + social, evaluar por qué funcionan, su coste de implementación y su transferibilidad a un tracker de videojuegos tipo GameVision.
> Nota metodológica: 16 búsquedas independientes vía `web_search`. El proveedor devolvió resultados ruidosos en varias consultas (Beli, TV Time, Spotify Wrapped no dieron fuentes primarias fiables); los puntos no confirmados se marcan explícitamente como **[no confirmado]**.

### Current State

El ecosistema "IMDb/Letterboxd para X" converge en un **núcleo común de 4 capas**: (1) **registro** de una obra con fecha, (2) **valoración** en una escala, (3) **listas/estados** (watchlist, backlog, completado), y (4) **capa social** (feed, reseñas, seguidores, likes). Sobre ese núcleo cada referente añade un "gancho" diferenciador: Letterboxd añadió el **diario con rewatch + Top 4** y una comunidad con identidad propia; StoryGraph añadió **mood tags y estadísticas ricas**; Trakt/Last.fm añadieron **scrobbling automático**; Untappd añadió **check-in + badges (gamificación)**; Vivino añadió **escaneo de etiqueta con cámara**; Beli añadió **ranking relativo (ELO) en vez de nota absoluta**; Beli/StoryGraph desintermedian el "gusto propio" frente al promedio de la masa.

El estado actual del sector muestra tres tendencias claras para 2026:
1. **De base de datos → comunidad**: IMDb (base de datos) pierde relevancia cultural frente a Letterboxd (comunidad) — Letterboxd pasó de ~1,5M usuarios (2019) a >14M (2024).
2. **Del promedio → la identidad personal**: el valor ya no es "la nota media", sino *tu* diario, *tus* cuatro favoritos, *tus* stats.
3. **Automatización del registro**: scrobbling/sync (Trakt, Last.fm, import CSV) reduce la fricción de entrada, que es la principal barrera de retención.

En videojuegos, **Backloggd** ("Letterboxd for games") ya replica el núcleo Letterboxd (journal, rating, likes, feed), pero **no domina** el espacio como Letterboxd en cine; hay hueco para un producto que combine varios sistemas a la vez. **[Evidencia de dominio parcial, no definitiva]**

### Key Evidence

| Sistema/Mecánica | App referente | Cómo funciona | Por qué engancha | Transferible a juegos (sí/no/adaptar) | Fuente |
|---|---|---|---|---|---|
| **Escala de valoración 1–10 entera** | IMDb | El usuario puntúa con enteros 1–10; IMDb muestra medias con 1 decimal mediante media ponderada anti-manipulación. | Escala amplia = granularidad percibida; la media agregada da autoridad de "referencia". | **Adaptar** (1–10 o 1–100). Los juegos se benefician de granularidad fina. | [^imdb-scale] |
| **Watchlist / "quiero ver"** | IMDb, Letterboxd, Trakt, MyDramaList | Lista de títulos marcados como pendientes; en IMDb es una acción de un clic desde la ficha. | Captura de intención sin exigir consumo; rellena el "backlog personal" y da motivo de retorno. | **Sí** (backlog de juegos). Mecánica imprescindible. | [^imdb-watchlist] [^letterboxd] |
| **Listas de usuario (creadas por usuarios)** | IMDb, Letterboxd, MyDramaList | Los usuarios crean listas temáticas ("Top terror 2020", "Juegos para empezar"); las listas pueden ser públicas y descubribles. | Convierte al usuario en curador; contenido generado por usuario = descubrimiento viral y SEO. | **Sí**. Enorme fuente de contenido y descubrimiento ("Mejores roguelikes de 2026"). | [^letterboxd] [^mdl-lists] |
| **Top 250 (ranking agregado curado)** | IMDb | Lista generada por la propia plataforma a partir de las notas de usuarios + filtros (nº de votos mínimo). | Ancla cultural: símbolo de "obra maestra"; da meta alcanzable a títulos y orgullo a fans. | **Adaptar** (Top 250 de juegos con umbral de votos). Riesgo de brigading ya conocido. | [^imdb-top250] |
| **Diario con fecha + rewatch** | Letterboxd | Cada visionado se registra con fecha; se puede marcar "rewatch" (revisionado); el diario muestra la línea temporal. | Convierte el consumo en un *histórico personal* con narrativa temporal; el rewatch reconoce rejugabilidad. | **Sí** (especialmente fuerte: "replay" es nativo en juegos; logros 100%, NG+, runs). | [^letterboxd] |
| **Top 4 favoritos (pinned)** | Letterboxd | El usuario fija 4 títulos favoritos que aparecen destacados en su perfil (visualmente "posters"). | Identidad y señal social potente; generó el fenómeno viral "Four Favorites" con cineastas y celebs. | **Sí (alta prioridad)**. "Tus 4 juegos" = identidad instantánea + marketing viral. | [^fourfavorites] |
| **Reseñas cortas + likes/comentarios** | Letterboxd, Serializd, Backloggd | Reseñas de texto libre (a menudo una línea ingeniosa) con sistema de likes, comentarios y feed de actividad de seguidos. | La reseña corta es compartible (screenshots a redes); el like es refuerzo social de bajo coste. | **Sí**. Los juegos tienen cultura de opinión fuerte (reviews, hot takes). | [^letterboxd] [^serializd] [^backloggd] |
| **Feed social invertido (razón social)** | Letterboxd, Goodreads, Fable | Un feed muestra actividad de amigos/seguidos; el descubrimiento viene de personas, no de un algoritmo de catálogo. | Confianza interpersonal > recomendación algorítmica; incentiva consumo "como tus amigos". | **Sí**. Alto valor de retención; requiere masa crítica o arrancar con clanes. | [^letterboxd] [^fable] |
| **Freemium con tiers Pro / Patron** | Letterboxd | Gratis el núcleo; Pro/Patron añaden stats avanzadas, sin ads, selección de póster, temas de perfil; Patron es el tier más alto. | Modelo de ingresos sin bloquear el core; monetiza *vanity + analítica personal*, no el acceso. | **Sí**. Monetizar stats propias y personalización, nunca el logueo básico. | [^patron] |
| **Stats perfiladas + "Year in Review"** | Letterboxd, StoryGraph, Trakt, Last.fm | Estadísticas agregadas (nº de horas, géneros, país, actores/directores más vistos) + resumen anual tipo "Wrapped". | Espejo del yo consumidor; el "Wrapped" es un ritual social anual con picos masivos de compartir. | **Sí (alta prioridad)**. "Tu año en juegos" (horas, géneros, indie vs AAA) = viralidad garantizada. | [^storygraph] [^letterboxd] |
| **Escala con medias estrellas / cuartos** | StoryGraph, Letterboxd | StoryGraph permite medias e incluso cuartos de estrella (parcialmente); Letterboxd usa 1–5 en pasos de 0,5. | Granularidad = fidelidad expresiva; reduce la frustración de "no cabe en 4 ni en 5". | **Adaptar**. 1–5 en pasos de 0,5 es un buen punto medio para juegos. | [^storygraph-halfstar] [^letterboxd] |
| **Mood tags / descriptores de sensación** | StoryGraph | Además del género, se etiqueta por "mood" (apegado, oscuro, ligero...) y ritmo/páginas largas; alimenta recomendaciones por afinidad emocional. | Recomendar por *sensación* conecta mejor que por género rígido; ayuda a expresar el "por qué". | **Adaptar**. Tags tipo "relajante", "difícil", "narrativo", "corto", "rejugable" son muy aplicables. | [^storygraph] |
| **Content / trigger warnings comunitarios** | StoryGraph | Los usuarios marcan avisos de contenido (violencia, muerte, etc.) que se muestran antes de elegir. | Seguridad y autocuidado; función muy valorada y diferenciadora frente a Goodreads. | **Sí** (adaptar a "horas", "dificultad", "requiere online", "accesibilidad"). | [^storygraph] |
| **Scrobbling / registro automático** | Last.fm, Trakt | Un cliente o integración envía automáticamente lo que el usuario reproduce/ve, sin registro manual. | Elimina la fricción = mayor retención y datos completos. | **Adaptar**. En juegos: sync con Steam/PSN/Xbox/Nintendo (horas, logros). | [^lastfm] [^trakt] |
| **Collections vs Watchlist (posesión vs intención)** | Trakt, Discogs | Trakt separa "colección" (lo que tienes) de "watchlist" (lo pendiente); Discogs separa colección de wishlist. | Distingue propiedad de deseo; sirve para coleccionistas (físico) y para planificación. | **Sí**. "Tengo / Quiero comprar / Jugaré" + colección física y digital. | [^trakt] [^discogs] |
| **Check-in + badges (gamificación)** | Untappd | Cada consumo es un "check-in" con foto, localización, nota y comentario; desbloquea badges/logros y rachas. | La gamificación (badges, streaks) convierte el registro en juego y dispara frecuencia. **Riesgo ético** documentado (fomento del consumo). | **Sí, con cuidado**. Logros por explorar géneros/rétro = positivo; evitar fomentar juego compulsivo. | [^untappd] |
| **Escaneo con cámara (fricción cero)** | Vivino | El usuario fotografía la etiqueta; la app identifica el vino y muestra su nota agregada + datos. | Elimina la barrera de buscar en catálogo; "escanea y ya". | **Adaptar (parcial)**. En juegos no hay "etiqueta"; alternativa: escanear carátula o caja física (coleccionistas). | [^vivino] |
| **Ranking relativo ELO (en vez de nota)** | Beli | En vez de poner una nota absoluta, el usuario compara opciones cara a cara; un algoritmo tipo ELO ordena sus favoritos. | Elimina "inflación de notas" y el problema de la escala; produce listas ordenadas personales que la gente presume. | **Sí (adaptar)**. "Ordena tus juegos" por duelos = ranking personal más honesto que estrellas. | [^beli-elo] |
| **Reacciones emocionales por episodio** | TV Time | Al marcar un episodio visto, se añade una reacción emocional (emojis) por escena/episodio. | Emoción de baja fricción, muy compartible; expresa opinión sin escribir. | **Sí**. Reacciones por capítulo/hito/boss; funciona en narrativos. | [^tvtime] |
| **Estados de lista normalizados** | MyAnimeList, AniList, MyDramaList | Estados estándar: Viendo / Completado / En pausa / Abandonado / Planificado (a veces con fecha de inicio/fin, progreso por episodios). | El "dropped" (abandonado) da permiso a no terminar y a documentar por qué; el progreso por episodio estructura el maratón. | **Sí**. "Jugando / Completado / Dropped / Backlog"; en juegos, progreso por % y horas. | [^mal-anilist] [^mdl-lists] |
| **Sync/import CSV entre plataformas** | Letterboxd, Trakt, Discogs | Exportar la biblioteca/ratings de otra app (p.ej., IMDb CSV) e importarla para "migrar" sin perder historial. | Reduce el coste de cambio (lock-in inverso): facilita *entrar* al ecosistema. | **Sí**. Importar de Backloggd, HLTB, API de Steam/PSN. Crítico para captar usuarios. | [^import-csv] |
| **Clubes de lectura / grupos** | Fable, Goodreads | Espacios grupales para leer y discutir el mismo título en paralelo, con anotaciones, highlights y comentarios. | Convierte el hobby solitario en actividad social programada; retención por compromiso grupal. | **Sí** (adaptar). "Game clubs" / retos comunitarios / juegos del mes. | [^fable] |
| **Journal semanal/mensual (curaduría personal)** | Backloggd | Diarios/entradas periódicas que agrupan lo jugado y opinado en un periodo. | Crea ritmo de publicación y da estructura al backlog. | **Sí**. "Diario de juego" con entradas fechadas. | [^backloggd] |

**Fuentes (Key Evidence):**

[^imdb-scale]: IdeaXchg / Cinemapeedika. "Movie Rating Systems" / "How IMDb Ratings Work". 2022. http://www.ideaxchg.com/ · http://cinemapeedika.com/
[^imdb-watchlist]: IMDb. Noticias/portada (watchlists curadas). 2024. https://www.imdb.com/
[^imdb-top250]: IMDb / Scribd. Referencias a "Top 250 Movies". 2024. https://www.imdb.com/ · https://www.scribd.com/
[^fourfavorites]: IndieWire. "Inside the Art of Letterboxd's 'Four Favorites'". 14 ago 2024. https://www.indiewire.com/
[^letterboxd]: Letterboxd. Portada/perfiles (diario, listas, Top 4). 2024. https://letterboxd.com/ · HopeForFilm. "5 Film Industry Trends, Explained Through Data". 2024. https://www.hopeforfilm.com/ · LifestyleReviewer. "Letterboxd 2026". 2026. https://www.lifestylereviewer.com/
[^patron]: Facebook / Drowned in Sound. Discusiones sobre tiers "Pro" vs "Patron" de Letterboxd. 2024. https://www.facebook.com/ · https://community.drownedinsound.com/
[^storygraph]: TCK Publishing / ERAzine / toolso.ai. "The StoryGraph Review". 2023–2026. https://www.tckpublishing.com/ · https://www.era-zine.com/ · https://toolso.ai/
[^storygraph-halfstar]: Booked Up (melovebooks). "Goodreads vs StoryGraph". 31 jul 2023. https://melovebooks.wordpress.com/
[^serializd]: stack.liuhuo.org. "Serializd - Free TV Show Tracking and Review Community". 2026. https://stack.liuhuo.org/
[^backloggd]: ResetEra / NeoGAF / NerdBurglars. "Backloggd (Letterboxd for video games)". 2024–2025. https://www.resetera.com/ · https://www.neogaf.com/ · https://nerdburglars.net/
[^mdl-lists]: Google Play / Apple App Store. "MyDramaList - Asian Drama DB". 2026. https://play.google.com/store/apps/details?id=com.mydramalist.app · https://apps.apple.com/
[^mal-anilist]: MyAnimeList. Perfil de usuario (estados de lista, mean score). 2024. https://myanimelist.net/
[^trakt]: Medium / Apple App Store / NexusM (GitHub). "How to build your collection (Trakt)". 2024–2026. https://medium.com/ · https://apps.apple.com/ · https://github.com/
[^lastfm]: Hacker News / Apple App Store. "Last.fm scrobbling y recomendaciones por filtrado colaborativo". 2024. https://news.ycombinator.com/ · https://apps.apple.com/
[^tvtime]: TikTok. "Best Tracking App After Tv Time". 31 ago 2026. https://www.tiktok.com/ (referencia indirecta; **[no confirmado a nivel primario]**)
[^untappd]: ACM / ResearchGate (Smed, J. et al.). "Longitudinal ethical analysis of Untappd: gamification through badges and streaks". 2026. https://dl.acm.org/ · https://www.researchgate.net/
[^vivino]: Vivino. "The complete guide to the Vivino experience". 28 may 2026. https://www.vivino.com/ · VinoWithSteveO. "The Vivino Wine Scanning App". 2019. https://www.vinowithsteveo.com/
[^beli-elo]: SavorTheApp. "Why You Should Track Your Favorite Restaurants — Beli: Social Ranking with ELO". 2026. https://www.savortheapp.com/
[^discogs]: Discogs. Foros (colección, wantlist, valor de colección). 2024–2026. https://www.discogs.com/
[^import-csv]: dejaVu / Stremio Custom Lists Docs. "Import from IMDb" / "Letterboxd Lists - Import personal CSV". 2024–2026. https://dejavu.plus/ · https://docs.stremiocustomlists.com/
[^fable]: Apple App Store / Lemon8. "Fable: Track & Discuss Books". 2026. https://apps.apple.com/ · https://www.lemon8-app.com/

### Tensions & Counter-arguments

1. **Escala: ¿absoluta (estrellas/1–10) o relativa (ELO como Beli)?**
   La nota absoluta es más simple y comparable entre usuarios, pero sufre inflación y "nota de cortesía" (todo se puntúa 7–10, problema documentado en IMDb con sesgo hacia 10 en series). El ranking ELO de Beli produce listas personales más honestas y *presumibles*, pero exige más esfuerzo por ítem y complica la media agregada. **Recomendación GameVision:** ofrecer **1–10 entero como default** y un **modo "ordena tus favoritos" tipo ELO opcional** para el Top personal. No forzar uno solo.

2. **¿Ranking agregado tipo Top 250 vs "mi Top personal"?**
   El Top 250 de IMDb da autoridad y SEO, pero es vulnerable a *brigading* y a la tiranía de la mayoría. Letterboxd apostó por lo *personal* (Top 4) y ganó relevancia cultural. GameVision es "2026-ready": liderar con **identidad personal** y usar el agregado como capa secundaria informativa, no como dogma.

3. **Gamificación (Untappd) — arma de doble filo.**
   Los badges/streaks disparan la frecuencia de registro, pero existe literatura que critica el fomento del consumo compulsivo (alcohol en Untappd). En juegos, el análogo (gamificar horas jugadas) puede premiar conducta problemática. **Transferible con adaptación**: gamificar *exploración y diversidad* (probar géneros, terminar el backlog, jugar indies), nunca *volumen de horas*.

4. **Masa crítica social vs utilidad individual.**
   El feed social (Letterboxd/Goodreads) solo engancha con suficiencia de amigos. Al principio, un tracker social vacío es peor que uno individual. Mitigación: arrancar con **funciones individuales fuertes** (diario, stats, backlog) y sembrar social vía **clubes/retos** y **listas descubribles** antes de depender del feed de seguidos.

5. **Fricción de registro: manual vs automático.**
   El scrobbling (Trakt/Last.fm) reduce fricción, pero en videojuegos la "reproducción" no es por ítem (un juego son 40 h) y el sync con consolas es frágil. **Contra-argumento:** automatizar demasiado puede robar el acto de *valorar*, que es el núcleo de la identidad del usuario. La clave es automatizar **el registro** (horas/logros) y dejar **manual y protagonista la valoración/reseña**.

6. **Datos no confirmados que conviene validar antes de decidir.**
   - Estado actual real de **TV Time** (¿cerrado/absorbido en 2025–2026?) — solo referencia indirecta de TikTok. **[no confirmado]**
   - Detalle exacto del **"Year in Review"** de Letterboxd (¿solo Pro/Patron? ¿público?) — **[no confirmado a nivel de fuente primaria]**.
   - Métricas exactas de **Beli** ("100M ratings") — provienen de Lemon8, no de Beli. **[a verificar]**
   - Crecimiento de Letterboxd (1,5M → 14M) proviene de blogs secundarios, no de Letterboxd. **Cifra plausible pero secundaria.**

7. **Backloggd ya existe: ¿hueco o copia?**
   Backloggd replica el núcleo Letterboxd para juegos pero no ha alcanzado el dominio cultural de su referente. La tesis de GameVision ("2026-ready eligiendo lo mejor de cada uno") implica **combinar** ELO + mood tags + scrobbling + clubs + stats Wrapped, cosa que ningún tracker de juegos hace hoy de forma integrada. **[Ventana de oportunidad; confirmar con análisis de la Dim. 01/03]**

**Fuentes (Tensions):**

[^t1]: TuringMachine / StackExchange. "What does an IMDb rating mean?" (sesgo hacia 10). 2013–2024. https://turingmachine.org/ · https://movies.stackexchange.com/
[^t2]: ACM / ResearchGate. Crítica ética de la gamificación de Untappd. 2026. https://dl.acm.org/
[^t3]: TikTok. Referencia a cierre de TV Time. 2026. https://www.tiktok.com/ **[no confirmado]**
[^t4]: Lemon8. Ranking Beli "100M ratings". 2026. https://www.lemon8-app.com/ **[a verificar]**
