## Dimension 05: Marketing, ASO y lanzamiento (2026)

### Current State

En 2026, la optimización de ficha en Google Play (ASO) ha dejado de ser un ejercicio de "meter keywords" para convertirse en un sistema de tres capas: **metadatos** (que dan visibilidad en búsqueda), **conversión** (que decide si la impresión se convierte en instalación) y **señales de retención/calidad** (que determinan si esa visibilidad se mantiene). El consenso de la industria es que Google Play no publica "ranking factors" oficiales con nombre ni anuncios de actualización —a diferencia de Google Search—: su sistema de ranking evoluciona de forma continua por refinamientos internos. Google sí publica, en cambio, guías oficiales que señalan que la visibilidad de una app depende de **relevancia, calidad de la app, contenido editorial y publicidad** [^asohot]. En 2026 dos cambios refuerzan esa dirección: la búsqueda conversacional (**Ask Play**) y la integración más profunda con **Gemini**, que empujan el ranking hacia "intención" y valor post-instalación, no hacia densidad de keywords [^asohot].

Para el caso de GameVision (tracker + red social de videojuegos para Android, sin equivalente nativo porque el líder Backloggd es solo web [^nerdburg][^resetera]), esto tiene implicaciones fuertes: el producto vive de la **retención y del hábito** (registrar partidas, ver backlog, social), justo las señales que el algoritmo de Play premia cada vez más. La ficha no debe vender "más listas" sino el hábito de "registrar lo que juegas" y la ventaja de tenerlo por fin en una app nativa.

En **lanzamiento indie**, la ortodoxia 2026 es un plan de 30 días con *soft launch* en 3-5 países seleccionados por *market fit*, localizando primero título/subtítulo/capturas, y publicar solo tras cubrir una checklist técnica y de ficha [^applaunchflow][^appscreens][^indiecircle]. La **gestión de primeras reseñas y del rating** es crítica porque el rating es filtro de descarga: la mayoría de usuarios solo considera apps de **4.0+**, y un 4.5+ es hoy el umbral para *rankear* de forma competitiva [^appdrift][^letsnurture][^appdna].

En **canales sin presupuesto** para un nicho gaming, el mapa real es: comunidades de Reddit y Discord específicas de "backlog/tracking/retro", X/Twitter, TikTok/YouTube Shorts, y lanzamientos en Product Hunt / Hacker News. Los creadores de contenido y streamers que ya loguean juegos son el multiplicador más barato. La publicidad de pago (Google App Campaigns) funciona, pero con CPI realista de **~1,5-4,6 USD** por instalación según categoría y país, lo que la hace poco rentable para audiencias pequeñas y apps sin monetización agresiva [^apptweak][^boacpi][^pubscale][^strata].

### Key Evidence

| Dato/Táctica | Descripción | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Límites de metadatos en Google Play | Título máx. 30 caracteres, descripción corta máx. 80, descripción larga máx. 4.000; hasta 8 capturas por tipo de dispositivo (mín. 2 para publicar) | Verificado 11 sep 2026 | "30 characters Google Play app-name maximum ... 80 characters Google Play short-description maximum ... 4,000 characters Google Play full-description maximum" | Alta | [^appdrift] |
| El título es el campo con más peso | El título de 30 caracteres concentra la mayor señal de relevancia por keyword | 2 abr 2025 | "The 30-character title holds the strongest ranking weight, demanding precise keyword selection." | Media | [^asoworld] |
| Play da más espacio que iOS | Android ofrece más margen de texto que la App Store para indexar | 4 ago 2026 | "Google Play gives you considerably more room. Your 30-character title, 80-character short description, and 4,000-character long description all [are indexed]" | Media | [^moburst] |
| Tres familias de señales de ranking | Metadatos + Conversión + Retención explican la visibilidad | 22 sep 2026 | "Google Play ranking signals can be grouped into three key areas: Metadata, Conversion, and Retention Signals." | Media | [^asohot] |
| Google no publica "core updates" para Play | El ranking cambia de forma continua, no en oleadas anunciadas | 22 sep 2026 | "Google Play does not. Its ranking shifts through continuous adjustments, not identifiable update events." | Alta | [^asohot] |
| Visibilidad = relevancia, crecimiento = valor | Metadata da descubrimiento; la experiencia post-install decide el crecimiento sostenible | 22 sep 2026 | "metadata still helps apps get discovered, but user experience determines whether that visibility turns into sustainable growth" | Media | [^asohot] |
| Descubrimiento conversacional (Ask Play / Gemini) | La búsqueda en lenguaje natural premia claridad de propuesta de valor | 22 sep 2026 | "With Ask Play and deeper Gemini integration, users can describe what they need in natural language instead of relying only on keyword-based searches." | Media | [^asohot] |
| Reseñas: importan sentimiento y frescura, no solo estrellas | Un rating algo menor pero con sentimiento reciente en mejora puede ser mejor señal que un rating alto en declive | 22 sep 2026 | "an app with a slightly lower average rating but improving recent feedback may present stronger current quality signals than an app with a higher rating but declining user sentiment" | Media | [^asohot] |
| Android Vitals: umbrales oficiales de calidad | Crashes: 1,09% usuarios/día global, 8% por modelo. ANR: 0,47% global, 8% por modelo; ventana de 28 días | 22 sep 2026 | "Google defines a bad behavior threshold of 1.09% of daily users across all device models and 8% for a specific phone model. For user-perceived ANRs, the thresholds are 0.47% overall and 8% per phone model." | Alta | [^asohot] |
| Caso de conversión ficha | App de productividad pasó de 18,4% a 24,7% de conversión y +31% instalaciones orgánicas en 8 semanas al pasar de mensajes de features a intención de usuario | 22 sep 2026 | "Within eight weeks, conversion increased from 18.4% to 24.7%, and weekly organic installs grew by 31%." | Media (no verificable) | [^asohot] |
| Métrica de ficha en Play = CTR | Los informes actuales de Play miden CTR (clic en Instalar/Abrir/Pre-registrar); la adquisición se reporta aparte | Verificado 11 sep 2026 | "Current listing reports use button clicks and click-through rate. Completed acquisitions remain available separately" | Alta | [^appdrift] |
| El rating filtra descargas | La mayoría solo considera apps con 4.0+; 4.5+ es el umbral competitivo; "por debajo de 4.0: invisible en búsqueda" | 11 feb 2026 | "Key thresholds: 4.5+ stars: Baseline for ranking competitively. Below 4.0: Effectively invisible in search. Review velocity: 10+ new [reviews]" | Media | [^appdna] |
| Los usuarios leen reseñas antes de instalar | 79% de usuarios consulta ratings antes de descargar; 4.0+ es el mínimo de consideración | 2026 | "79%. Users who check ratings before downloading. 4.0+. Minimum rating for most users to consider" | Media | [^appdrift] |
| Responder reseñas recientes influye más | Priorizar respuestas a reseñas recientes (impactan más la ficha que las antiguas) | 9 oct 2025 | "Prioritize replies to recent reviews (they influence the store listing most)." | Media | [^appfollow] |
| CPI EE.UU. por categoría (Apple Ads) | CPI mediano EE.UU. desde $1,33 (Shopping) a $26,70 (Sports); **Games $4,63** | 19 ago 2026 | "In the U.S., median cost per install (CPI) ranges from $1.33 for Shopping to $26.70 for Sports, with Games at $4.63" | Alta | [^apptweak] |
| CPI global Android | CPI iOS global $1,5–$3,5; Android/Google Play $1,5–$4,00; Android app CPI ≈ $1,22 | 27 feb 2025 | "iOS CPI globally - $1.5 to $3.5 Android CPI globally (Google Play market) - $1.5 to $4.00" | Media | [^boacpi] |
| Google App Campaigns CPI | Rango típico $1,50–$4,50 por instalación (promedio global ~$1,75–$4,00) | 6 ene 2025 | "Google App campaigns: roughly $1.50 to $4.50 per install. by vertical in 2025 data, with global averages clustering between $1.75 and $4.00." | Media | [^strata] |
| CPI en sector gaming es caro | "En sectores competitivos como gaming... puede superar los $10" | 2025 | "in competitive sectors like gaming, finance, or technology, it can exceed $10" | Baja | [^wask] |
| CPI = pago por instalación | Definición oficial de Google Ads | — | "Cost per install (CPI) means that you pay for each app installation on a user device. For CPI bidding campaigns, you need to set a maximum cost per install" | Alta | [^gads] |
| Soft launch por países | Elegir 3-5 países top por market fit; localizar primero título, subtítulo y capturas | 9 feb 2026 | "Start with your top 3-5 countries by market fit and demand signal, then localize titles, subtitles, and screenshots first." | Media | [^applaunchflow] |
| Checklist de publicación | Verificar build, metadatos, capturas y previews antes de cada envío | 6 abr 2026 | "Use this App Store Connect release checklist before every iOS submission: verify the selected build, metadata, screenshots, app previews if used" | Media | [^appscreens] |
| Rechazos de tienda | Apple rechazó 1,93M envíos en 2024; plan indie de 4 semanas para enviar sin rechazos | 30 mar 2026 | "Apple rejected 1.93M submissions in 2024. The 7-step indie timeline (4 weeks to submission) that ships to App Store + Play Store rejection-free." | Media | [^appscreenshot] |
| Marketing en Reddit | "Cómo hago marketing de mi juego" es la pregunta nº1 en r/gamedev y canales de Discord | 12 jul 2021 | "'How do I market a game' or 'How do I get visibility' is the number one question on any r/gamedev Reddit, on any #General channel in a game [Discord]" | Media | [^htmag] |
| Reddit para marketing de juegos | Guía específica de marketing de videojuegos en Reddit (orgánico y ads) | 15 abr 2025 | "Discover how game developers & publishers can use Reddit for video game marketing -from organic posts to paid ads." | Baja/Informativa | [^cloutboost] |
| Re-engagement: push es refuerzo, no canal principal | Push apoya una campaña, no la sostiene; el email lleva el peso | — | "Push doesn't carry a re-engagement campaign. It supports one." | Media | [^vero] |
| Canales de reactivación | Push, mensajes in-app y email; enfoque omnicanal para recuperar inactivos | 1 nov 2024 | "Choose the best channel for your re-engagement campaign - Push notifications - In-app messages - Emails - Go omnichannel." | Media | [^pushwoosh] |
| Email de reactivación | Reparar relaciones con usuarios que "se han caído por las grietas" | 26 ene 2024 | "These re-engagement strategies directly address and repair those mobile user relationships who have slipped between the cracks." | Media | [^onesignal] |
| Backloggd es el competidor de referencia (web) | Considerado el "Letterboxd de los videojuegos": log, rating 1-5 estrellas, listas | 29 nov 2025 | "Backloggd is widely considered the most faithful Letterboxd alternative due to its diary-style logging, review system, and social feed." | Alta | [^nerdburg] |
| No existe app nativa equivalente | Backloggd funciona como tracker web/social, sin app nativa Android | 16 jul 2024 | "A Video Game Collection Tracker. Keep a virtual backlog of your video game collection, then rate and review the ones you've played to share with [friends]" | Alta | [^resetera] |
| Alternativas existentes | PlayDex y similares ya ocupan parte del nicho de "organizar tu colección de juegos" | — | "The ultimate way to organize your games' collection, your unused game keys (securely), and your games wish list collection - whether physical or digital" | Media | [^playdex] |
| Búsqueda = canal dominante en tienda (dato histórico iOS) | ~70% de visitantes usa búsqueda; ~65% de descargas siguen a una búsqueda (Apple, 2022) | 2022 (citado 2026) | "Almost 65% App Store downloads that follow a search ... 70% App Store visitors who use search to discover apps" | Media (histórico) | [^appdrift] |
| Ranking por relevancia + calidad + editorial + ads | Guía oficial de Google sobre descubrimiento | 22 sep 2026 | "Google's official guidance highlights several key areas that influence app discovery and ranking - including relevance, app quality, editorial content, and advertising." | Media | [^asohot] |
| Experimentos de ficha en bucle | El A/B testing de ficha no es un rediseño puntual: cadencia continua de capturas, jerarquía, propuesta de valor y variantes por mercado | 22 sep 2026 | "Store listing optimization is not a one-time redesign or a single A/B test. Strong ASO teams treat experimentation as a continuous process." | Media | [^asohot] |
| Keywords en título mejoran ranking | Apps con keyword relevante en el título tienen +10,3% de probabilidad de rankear más alto | 31 may 2023 | "Research shows that apps with a relevant keyword in their title score 10.3% more chance to rank higher in the app store." | Baja | [^techexactly] |

Fuentes:
[^asohot]: AsoHot. *2026 Google Play Ranking Factors: What Changed and How to Adapt Your ASO*. 22 sep 2026. https://www.asohot.com/blog/google-play-ranking-factors
[^appdrift]: AppDrift. *ASO Statistics & Benchmarks 2026*. Revisado 11 sep 2026. https://appdrift.co/aso-statistics
[^apptweak]: AppTweak. *Apple Ads benchmarks 2026: CPT, CPI, CR & TTR by category*. 19 ago 2026. https://www.apptweak.com/en/aso-blog/apple-ads-benchmarks
[^boacpi]: Business of Apps. *Cost per Install (CPI) Rates (2025)*. 27 feb 2025. https://www.businessofapps.com/ads/cpi/research/cost-per-install/
[^pubscale]: PubScale. *Cost Per Install Advertising: The Best Guide For 2025*. 2025. https://pubscale.com/
[^strata]: Strataigize. *The Costs of Mobile App Install and Event-Based Campaigns*. 6 ene 2025. https://www.strataigize.com/
[^wask]: WASK. *Google Ads CPI (Cost Per Install) Cost Tool*. https://www.wask.co/
[^gads]: Google Ads Help. *Cost per install: Definition*. https://support.google.com/google-ads/answer/13278731
[^asoworld]: ASOWorld. *Play Store ASO: How to Add Keywords & Boost Your App's Ranking*. 2 abr 2025. https://asoworld.com/
[^moburst]: Moburst. *ASO for Android vs iOS, Two Platforms Two Strategies*. 4 ago 2026. https://www.moburst.com/
[^techexactly]: TechExactly. *How to Get Your App Ranked Top on Google Playstore*. 31 may 2023. https://techexactly.com/
[^applaunchflow]: AppLaunchFlow. *App Launch Checklist 2026: 30-Day Plan for iOS & Android*. 9 feb 2026. https://www.applaunchflow.com/
[^appscreens]: AppScreens. *App Store Connect Release Checklist for Indie*. 6 abr 2026. https://appscreens.com/
[^appscreenshot]: AppScreenshotStudio. *App Launch Checklist 2026: 7 Steps*. 30 mar 2026. https://appscreenshotstudio.com/
[^indiecircle]: Indie App Circle. *Get Actionable App Feedback From Indie Developers*. https://www.indieappcircle.com/
[^devto]: DEV Community. *How to Plan Your First Indie App Launch*. 3 abr 2025. https://dev.to/
[^htmag]: How To Market A Game (Chris Zukowski). *How To Market Your Indie Game in 2024: A 10 Step Plan*. 12 jul 2021. https://howtomarketagame.com/
[^cloutboost]: Cloutboost. *How to Market a Video Game on Reddit*. 15 abr 2025. https://www.cloutboost.com/
[^appdna]: AppDNA. *App Store Optimization (ASO) Guide 2026: How to Rank*. 11 feb 2026. https://www.appdna.ai/
[^letsnurture]: LetsNurture. *17 Strategies to Boost Mobile App Downloads*. 8 jul 2025. https://www.letsnurture.ca/
[^appfollow]: AppFollow. *30 Mobile App Analytics Metrics You Can't Ignore (Part 3)*. 9 oct 2025. https://appfollow.io/
[^pushwoosh]: Pushwoosh. *How to re-engage inactive users: Top tips and real-life*. 1 nov 2024. https://www.pushwoosh.com/
[^vero]: Vero. *How to Re-Engage Inactive Users with Email and Push*. https://www.getvero.com/
[^onesignal]: OneSignal. *Re-Engagement Email Tactics to Bring Users Back to Your App*. 26 ene 2024. https://onesignal.com/
[^reteno]: Reteno. *How to Re-Engage Inactive Customers: 9 Best Practices*. 14 may 2026. https://reteno.com/
[^appcues]: AppCues. *7 ways to bring inactive users back from the dead*. https://www.appcues.com/
[^nerdburg]: NerdBurglars. *Best Apps for Tracking Video Games Like Letterboxd*. 29 nov 2025. https://nerdburglars.net/
[^resetera]: ResetEra. *Backloggd |OT| Letterboxd for Video Games*. 16 jul 2024. https://www.resetera.com/
[^playdex]: AlternativeTo. *PlayDex Game Library: The ultimate way to organize*. https://alternativeto.net/

### Tensions & Counter-arguments

**1. La mayor parte del "conocimiento ASO" es contenido de vendedores, no de Google.** El propio informe de ranking de 2026 admite que sus conclusiones no son factores oficiales: *"These trends reflect industry analysis and ASO research, rather than officially published Google ranking factors"* [^asohot]. Y la fuente que publica los datos más limpios hizo una corrección explícita borrando claims inflados: *"The previous version included unsupported growth and cost claims... Universal localization, screenshot, rating and retention lifts are no longer presented as established benchmarks"* [^appdrift]. Conclusión operativa: hay que tratar los "lifts" mágicos de capturas/localización (+X% descargas) como **hipótesis a testear en tu propia ficha**, nunca como garantía. La única verdad verificable son los **límites de campo oficiales** y las métricas que da tu propio Play Console.

**2. Conflicto de interés en las fuentes.** AsoHot vende "keyword installs" con cuentas reales y "ratings & reviews" gestionados —es decir, **tiene incentivo económico en exagerar el peso de keywords, instalaciones y reseñas** [^asohot]. Su "caso" de conversión (18,4%→24,7%) es anónimo y no reproducible. Úsalo como ilustración direccional, no como dato duro.

**3. Pago por instalación vs. nicho pequeño: probablemente NO tiene sentido para GameVision (aún).** Con CPI de Games ~$4,63 en EE.UU. y $1,5–4,0 global [^apptweak][^boacpi][^strata], conseguir 1.000 instalaciones cuesta del orden de **$1.500–4.600**. Un tracker social de nicho con LTV bajo/monetización aún no probada no recupera ese coste. Google App Campaigns además necesita volumen de datos para optimizar; con audiencia pequeña, el algoritmo "no aprende". Las campañas de pago tienen sentido más tarde, y sobre todo **re-targeting / pujas por marca** una vez exista demanda orgánica, no como motor inicial.

**4. La ventaja "app nativa vs Backloggd web" no se vende sola.** Backloggd ya tiene producto maduro, comunidad y hábito, y hay alternativas (PlayDex, etc.) y trackers multiplataforma [^nerdburg][^resetera][^playdex]. La ventaja nativa ayuda a *conversión* (mejor UX móvil, notificaciones, captura offline) pero no genera *descubrimiento*. Por sí sola no crea demanda; hay que ir a buscar a los jugadores donde ya discuten su backlog.

**5. Rating: existe un umbral duro, pero el "número mágico" es engañoso.** Aunque el 4.0/4.5 aparece repetido [^appdna][^appdrift], el propio consenso matiza que **frescura, velocidad y sentimiento** de reseñas pesan más que el promedio histórico [^asohot]. Riesgo práctico: lanzar y sufrir 2-3 reseñas de 1★ por bugs de juventud puede hundir el rating por debajo de 4.0 justo cuando más importa. De ahí la importancia del *soft launch* en países pequeños y del **beta test cerrado** para cazar crashers/ANR antes del gran lanzamiento (los umbrales de Android Vitals son duros y penalizan visibilidad [^asohot]).

**6. Cuándo usar cada canal de tracción (contra-argumentos al "haz de todo").**
- **Reddit**: potencia enorme pero riesgo de ban; requiere participación previa y no-spam. La pregunta "cómo marketing" es la nº1 en r/gamedev [^htmag] → hay guías, pero la comunidad castiga la autopromoción descarada [^cloutboost]. Mejor aportar valor (compartir datos de backlog, hilos de "¿qué estáis jugando?") y enlazar con sutileza.
- **Discord**: altísimo fit para gaming (servidores de retro/tracking, servidores de creadores) pero es atención prestada y requiere presencia continua; no escala sola.
- **TikTok/YouTube Shorts**: mejor ROI orgánico potencial para gaming, pero formato-first (clips cortos de valor: "así se ve tu backlog", "POV: registras todo lo que juegas") y muy volátil. Requiere constancia y producción, no un post.
- **Product Hunt / Hacker News**: buen pico de tráfico para *herramientas/software*, no para juegos Android de consumo; útil para founders/devs y algo de prensa tech, poco para la audiencia final de jugadores.
- **Streamers/creadores**: el multiplicador más eficiente si logras que alguien que ya loguea juegos lo haga en tu app en directo; coste = relaciones, no dinero.

**7. Contradicción real sobre el peso de keywords.** Las guías de vendedores ASO siguen afirmando que "keyword en el título = +10,3% de ranking" [^techexactly] y que el título es "el campo de mayor peso" [^asoworld]; pero el análisis 2026 sostiene lo contrario en la dirección: *"Keywords still matter on Google Play. They just no longer decide where an app ranks"* [^asohot]. La síntesis defendible: **las keywords abren la puerta (indexación/relevancia) y la conversión+retención deciden la posición**. Para GameVision, eso significa: meter keywords correctas ("backlog", "tracker de videojuegos", "lista de juegos pendientes", "diario de juegos") en título/descripción corta, y luego invertir el esfuerzo en capturas, onboarding y retención, que es donde está la palanca real.

**8. Métricas que cada fuente define distinto — no comparar peras con manzanas.** Play mide hoy **CTR** de ficha (clic en Instalar), no "conversión impresión→instalación" como Apple [^appdrift]. Comparar un "conversion rate" de hace dos años con el reporte actual de Play lleva a conclusiones falsas. Fija una definición y mantén país, categoría, fuente y periodo constantes.

**9. Errores que entierran una app de nicho (síntesis de riesgo).**
- Optimizar keywords pero con ficha que no explica el *por qué* (imágenes de features en vez de intención) → CTR bajo y visibilidad que no convierte [^asohot].
- Lanzar sin beta cerrado → crashes/ANR penalizan visibilidad y generan 1★ tempranas [^asohot].
- Lanzar en todos los países a la vez → no aprendes nada, quemas el "shock" inicial de lanzamiento; mejor 3-5 países por market fit [^applaunchflow].
- Ignorar reseñas recientes (sin responder) → pierdes control de la narrativa de calidad [^appfollow].
- Confiar la tracción solo a pago con presupuesto bajo → CPI inviable para LTV de nicho [^apptweak][^boacpi].
- No construir audiencia/waitlist antes de lanzar → el día 1 llega frío; los canales orgánicos (Reddit/Discord/creadores) necesitan calentarse de antemano [^htmag].
- No medir retención (D1/D7/D30) → Play evalúa post-install; un ASO sin retención es un fuego de paja [^asohot][^appdrift].

Fuentes:
[^asohot]: AsoHot. *2026 Google Play Ranking Factors: What Changed and How to Adapt Your ASO*. 22 sep 2026. https://www.asohot.com/blog/google-play-ranking-factors
[^appdrift]: AppDrift. *ASO Statistics & Benchmarks 2026*. Revisado 11 sep 2026. https://appdrift.co/aso-statistics
[^apptweak]: AppTweak. *Apple Ads benchmarks 2026: CPT, CPI, CR & TTR by category*. 19 ago 2026. https://www.apptweak.com/en/aso-blog/apple-ads-benchmarks
[^boacpi]: Business of Apps. *Cost per Install (CPI) Rates (2025)*. 27 feb 2025. https://www.businessofapps.com/ads/cpi/research/cost-per-install/
[^strata]: Strataigize. *The Costs of Mobile App Install and Event-Based Campaigns*. 6 ene 2025. https://www.strataigize.com/
[^asoworld]: ASOWorld. *Play Store ASO: How to Add Keywords & Boost Your App's Ranking*. 2 abr 2025. https://asoworld.com/
[^techexactly]: TechExactly. *How to Get Your App Ranked Top on Google Playstore*. 31 may 2023. https://techexactly.com/
[^applaunchflow]: AppLaunchFlow. *App Launch Checklist 2026: 30-Day Plan for iOS & Android*. 9 feb 2026. https://www.applaunchflow.com/
[^appdna]: AppDNA. *App Store Optimization (ASO) Guide 2026: How to Rank*. 11 feb 2026. https://www.appdna.ai/
[^appfollow]: AppFollow. *30 Mobile App Analytics Metrics You Can't Ignore (Part 3)*. 9 oct 2025. https://appfollow.io/
[^htmag]: How To Market A Game (Chris Zukowski). https://howtomarketagame.com/
[^cloutboost]: Cloutboost. *How to Market a Video Game on Reddit*. 15 abr 2025. https://www.cloutboost.com/
[^nerdburg]: NerdBurglars. *Best Apps for Tracking Video Games Like Letterboxd*. 29 nov 2025. https://nerdburglars.net/
[^resetera]: ResetEra. *Backloggd |OT| Letterboxd for Video Games*. 16 jul 2024. https://www.resetera.com/
[^playdex]: AlternativeTo. *PlayDex Game Library*. https://alternativeto.net/
