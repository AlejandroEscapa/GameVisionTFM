## Dimension 06: Modelos de negocio y crecimiento de las referencias

### Current State

El ecosistema de apps de tracking + social se financia casi siempre con **un modelo freemium en el que el núcleo social (registrar, puntuar, reseñar, seguir) es gratuito para siempre**, y el dinero se obtiene por tres vías: (a) suscripción opcional que quita anuncios y añade estadísticas/cosméticos, (b) anuncios y publirreportajes nativos en la capa gratuita, y (c) afiliación/partnerships con estudios, editoriales o comercios.

Las referencias se dividen en dos arquetipos claros:

- **Arquetipo "Letterboxd"** (social-first, empresa con ánimo de lucro, varios ingresos): gratis con ads + Pro/Patron barato; crece por viralidad de sus "Year in Review" y por efecto red; ya pasó de 10 M a 18 M+ miembros (la referencia del sector).
- **Arquetipo "Backloggd"** (indie, equipo mínimo, sin app nativa): se sostiene con **Patreon** (micromecenazgo) en vez de anuncios ni suscripción clásica; crece de forma orgánica y lenta hasta ~650.000 usuarios tras años.

Para GameVision (tracker+social de videojuegos Android, dev único asistido por IA) la evidencia apunta a que **el core social debe regalarse** y monetizarse con una suscripción muy barata (tipo Letterboxd Pro) o micromecenazgo (tipo Backloggd), evitando el error de cobrar lo que antes era gratis.

### Key Evidence

| App | Modelo | Precio | Qué se paga | Qué se regala | Comunidad/Tamaño | Fuente |
|-----|--------|--------|-------------|---------------|------------------|--------|
| **Letterboxd** | Freemium + ads + partnership | Pro **19 $/año** (~1,58 $/mes); Patron **49 $/año** | Pro: sin ads, estadísticas, filtros de streaming, avisos de watchlist. Patron: todo lo de Pro + pósters/backdrops personalizados y estadísticas extra | Registro, puntuación, reseñas, listas, feed social y watchlist | **>18 M miembros**, +50 % solo en 2024; >10 M en 2023 | [^lb-pro] [^lb-patron] [^lb-growth] [^lb-10m] |
| **Letterboxd (ingresos)** | Ads + suscripción (propiedad de Tiny Ltd) | — | Anuncios segmentados a estudios (con atribución gracias al log de películas); tiers de pago | Toda la capa gratuita con ads | Adquirida ~2023; "se mantuvo en su fuerte crecimiento" en 2026 | [^lb-rev] [^lb-ads] [^lb-tiny] |
| **Backloggd** | **Patreon** (micromecenazgo), sin app nativa | Tiers bajos (Patreon; ~**1 $/3 $** según el brief) | Apoyo al desarrollo; insignia de "backer", acceso a betas cerradas | Todo el tracking/registro/reseñas gratis | **>650.000 usuarios** registrados (fin 2025); 31,5 M partidas y 1,5 M reseñas en 2025 | [^bg-users] [^bg-hltb] [^bg-patreon] [^bg-kemono] |
| **The StoryGraph** | Freemium (Plus opcional) | Plus **4,99 $/mes** o **49,99 $/año** (4,17 $/mes), 30 días de prueba | Estadísticas avanzadas, recomendaciones extra, funciones "premium" | Toda la base gratis (tracking, retos, mood tags) | Base "más pequeña pero real y de rápido crecimiento" frente a Goodreads | [^sg-plus] [^sg-rev] [^sg-anygen] [^sg-growth] |
| **Trakt** | Freemium (VIP) | VIP ~**30 $/año** original → subido a **60 $/año** (2024); ~2,50 $/mes histórico | Sin ads, filtrado avanzado, más funciones de listas | Tracking básico; pero **free limitado a 100 ítems por lista** (desde ene 2025) | Indexa >1 M de películas/series; ~500 K usuarios llegados de TV Time en 2026 | [^tr-vip] [^tr-troypoint] [^tr-price] [^tvtime] |
| **MyAnimeList** | Freemium (premium) | ~**4,99 $/mes** | Sin ads + contenido exclusivo | Base de datos y tracking gratis (con ads) | Comunidad anime enorme; clientes de terceros cobran **pago único 2,99 $** (Kitsune) | [^mal] [^mal-kitsune] [^mal-saa] |
| **Goodreads** | Gratis, sin premium | 0 $ | — (monetiza indirectamente vía Amazon) | Todo gratis | ~15 M usuarios ya en 2013; comprada por Amazon ese año | [^gr-amazon] [^gr-goodereader] |

**Patrones transversales de la evidencia:**
- El precio "ancla" de un premium en apps de tracking de nicho ronda **19–50 $/año** (1,5–4 $/mes), con la parte gratuita suficientemente capaz como para no perder usuarios.
- Lo que **se regala**: logging, puntuación, reseñas, listas, feed social. Lo que **se cobra**: sin-anuncios, estadísticas avanzadas, cosméticos/personalización, filtros y notificaciones.
- **Backloggd demuestra** que un dev/equipo mínimo puede sostener un tracker de nicho con Patreon y llegar a cientos de miles de usuarios **sin app nativa ni publicidad**.

### Tensions & Counter-arguments

1. **Cobrar lo que antes era gratis enfurece a la comunidad.** Trakt subió el VIP de 30 $→60 $/año y limitó las listas gratuitas a 100 ítems (ene 2025); usuarios denuncian que funciones VIP pagadas desaparecieron después, y que no se respeta el precio original con la autorenovación. Es el riesgo nº 1 a evitar. [^tr-price] [^tr-roadmap]
2. **El micromecenazgo (Patreon) sostiene pero limita.** Backloggd prueba que funciona para equipo mínimo, pero el techo de ingresos es bajo y dependiente de la buena voluntad; no escala como un freemium con ads + suscripción. [^bg-patreon] [^bg-users]
3. **Los ads generan dinero pero tensionan la marca.** Letterboxd monetiza la capa gratis con publicidad/publirreportajes; parte de su comunidad teme la deriva corporativa (rumores de compra por Netflix/Sony/Paramount). La venta a Tiny Ltd ya había despertado recelos. [^lb-ads] [^lb-co]
4. **El "Year in Review" viral es el mayor motor de crecimiento de coste-cero.** Letterboxd creció "raramente con grandes campañas publicitarias"; su resumen anual compartible convirtió la app en canal de marketing masivo (casi la mitad de la audiencia de *The Brutalist* supo de ella por ahí). Es replicable por GameVision. [^lb-noads] [^lb-co]
5. **Supervivencia del nicho: las apps mueren y sus usuarios migran.** El cierre de TV Time (2026) empujó ~500 K usuarios a Trakt: oportunidad de captación para quien ofrezca importación fácil. Backloggd y HowLongToBeat/Grouvee conviven sin dominar el nicho de videojuegos (nadie lo ha ganado aún). [^tvtime] [^bg-nelson]
6. **Contra-argumento al "gratis con ads":** en apps pequeños los anuncios rinden poco y pueden espantar al público "hardcore" que es justo el de este nicho; de ahí que Backloggd prefiera Patreon y Letterboxd combine ads con un Pro barato que los elimina.
7. **Qué encaja mejor para un equipo mínimo (evidencia, no recomendación cerrada):** el patrón que mejor combina sostenibilidad + crecimiento sin quemar la comunidad es **core social gratis para siempre + suscripción barata anual (≈15–25 $/año) por sin-ads/estadísticas/cosméticos + un "resumen anual" viral**, y opcionalmente Patreon/donaciones al inicio. Goodreads (gratis total, respaldo corporativo) y MyAnimeList (ads) muestran que sin ingresos directos la app depende de un dueño grande; Trakt muestra el coste reputacional de subir precios y cerrar funciones. [^lb-biz] [^tr-price] [^sg-rev]

---

### Fuentes

[^lb-pro]: Five Ways to Get More Out of Letterboxd. Lifehacker AU. 2025-05-09. https://au.lifehacker.com/
[^lb-patron]: Letterboxd — Film Diary and Review Community (detalle tier Patron). Liuhuo Stack. s.f. https://stack.liuhuo.org/
[^lb-biz]: Letterboxd Deep Dive: Lessons for Cinegraph's Social. GitHub. s.f. https://github.com/
[^lb-rev]: Tiny Ltd. Recent Acquisitions. ReadThinkWrite. 2023-10-31. https://www.readthinkwrite.ca/
[^lb-ads]: Why Private Equity Paid $50,000,000 for Letterboxd. nickywebsite.com. 2023-10-20. https://www.nickywebsite.com/
[^lb-growth]: Letterboxd / Matthew Buchanan — News. IMDb. s.f. https://www.imdb.com/
[^lb-10m]: Firm advises Tiny on acquisition of Letterboxd. Minter Ellison. 2023-11-13. https://minterellison.co.nz/
[^lb-noads]: Communication Theory: Letterboxd. deziiign. s.f. https://pro.deziiign.com/
[^lb-co]: Letterboxd Acquisition Talks with Netflix, Sony, Paramount. LinkedIn. s.f. https://www.linkedin.com/
[^lb-tiny]: Tiny Ltd. Q2 FY2026 earnings call transcript. Yahoo Finance. 2026-08-06. https://finance.yahoo.com/
[^bg-users]: Backloggd Review: The Best Game Tracker?. Two Average Gamers. 2026-09-16. https://www.twoaveragegamers.com/
[^bg-hltb]: HowLongToBeat vs Backloggd: Why I Use Both, Not Just One. Two Average Gamers. 2026-09-16. https://www.twoaveragegamers.com/
[^bg-patreon]: Backloggd creating a video game… — Patreon. Patreon. s.f. https://www.patreon.com/
[^bg-kemono]: 05.22 | Development Update (backloggd Patreon). kemono.cr. s.f. https://kemono.cr/
[^bg-resetera]: Backloggd |OT| Letterboxd for Video Games. ResetEra. 2024-07-16. https://www.resetera.com/
[^bg-nelson]: July 2021 – Nelson's log. WordPress. 2021-07-31. https://nelsonslog.wordpress.com/
[^bg-siuse]: Should I Use Backloggd — A Video Game Collection Tracker?. shouldiuse.io. 2026. https://shouldiuse.io/
[^sg-plus]: StoryGraph Plus. The StoryGraph. s.f. https://app.thestorygraph.com/
[^sg-rev]: StoryGraph Review: Is It Worth It? (2024 UPDATE). anhistorianabouttown.com. 2024. https://anhistorianabouttown.com/
[^sg-anygen]: The StoryGraph: Features, Pricing & Guide. anygen.io. 2026-08-23. https://www.anygen.io/
[^sg-growth]: StoryGraph for Indie Authors. ScribeCount. s.f. https://scribecount.com/
[^tr-vip]: Trakt.tv Premium Features. Facebook. s.f. https://www.facebook.com/
[^tr-troypoint]: Is Trakt VIP Worth the Monthly Fee?. Troypoint Insider. 2022-12-11. https://troypointinsider.com/
[^tr-price]: Trakt Not Honoring Original Price Paid If Auto-Renewal. Emby Media. 2024-12-13. https://emby.media/
[^tr-roadmap]: RSS-feed — Trakt Roadmap. Trakt. s.f. https://roadmap.trakt.tv/
[^tr-forums]: AMA: From Rippple to Trakt. Trakt Forums. 2025-05-23. https://forums.trakt.tv/
[^tr-soft]: Top 10 trakt.TV Time To Watch Alternatives. Soft112. s.f. https://trakt-ios.soft112.com/
[^tvtime]: How to Export Your TV Time History to Trakt (2026 Guide). achriom.com. 2026. https://www.achriom.com/
[^mal]: Apps for Tracking Manga Progress / MyAnimeList Premium. Facebook. s.f. https://www.facebook.com/
[^mal-kitsune]: Kitsune for MyAnimeList — App Store. Apple. s.f. https://apps.apple.com/
[^mal-saa]: MyAnimeList VS The Visual Novel Database. SaaSHub. s.f. https://www.saashub.com/
[^gr-amazon]: The StoryGraph — After Years Of Complaints From Users. Gadgeteer. 2020-09-17. https://gadgeteer.co.za/
[^gr-goodereader]: Making a Choice Between Goodreads and StoryGraph. Good e-Reader. 2024-02-27. https://goodereader.com/
