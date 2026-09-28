# Dimension 04: Diseño y UX 2026

> Investigación preparada para GameVision. Objetivo: elevar un design system ya existente (DESIGN.md — monocromo cálido + verde ácido `#C8F135`, Space Grotesk, física de muelles, skeletons, componentes `GameCard` / `NewsCard` / `RatingBadge`) al nivel de "producto publicable 2026". Idioma: español. Fecha de la investigación: 2026-09-28.
>
> **Nota metodológica / limitación de fuentes.** Se ejecutaron 15 búsquedas independientes. El proveedor de búsqueda devolvió, en varias consultas, resultados de baja calidad o fuera de tema (documentos académicos irrelevantes, foros antiguos). Se priorizó descartar fuentes basura y conservar solo resultados verificables y citables. Cuando una afirmación proviene de una fuente secundaria o no oficial, se marca con **Confianza Media/Baja**. Las citas textuales son *verbatim* del snippet recuperado.

---

## Current State

GameVision parte con una base de diseño sólida y coherente, pero **pre-publicación**: un sistema monocromo cálido con un único acento ácido (`#C8F135`), tipografía de display Space Grotesk, física de muelles (springs) ya adoptada, uso de *skeletons* en lugar de spinners y una biblioteca de componentes propia (`GameCard`, `NewsCard`, `RatingBadge`).

Frente a esto, el estado del arte en 2026 presenta cinco frentes que GameVision debe cerrar para considerarse "publicable":

1. **Lenguaje visual de referencia (Material 3 Expressive).** Google consolidó en 2025–2026 un salto expresivo de su sistema: física de movimiento basada en muelles reales, formas y color más saturados, y diseño *adaptive* multiplataforma (Android 16, Wear OS 6, Pixel 10). Es el estándar de facto contra el que se comparará cualquier app Android 2026.
2. **Tendencias de diseño de app 2026.** La dirección dominante: *contenido primero*, tipografía como estructura, monocromo + acento (justo el camino de GameVision), skeletons (ya adoptados) en vez de spinners, y dark mode como modo de primera clase, no como tema secundario.
3. **UX de tracking/coleccionismo.** El nicho está definido por el "modelo Letterboxd aplicado a X": diario, watchlist/backlog, listas, "Top 4", estados ricos, reseñas cortas y estadísticas de perfil. Backloggd es el caso canónico en videojuegos.
4. **Onboarding / time-to-first-value (<60 s).** El valor debe llegar en el primer minuto: registrar algo y verlo en el perfil. Los errores clásicos (muros de registro, tour obligatorio, catálogo vacío) son las principales causas de churn.
5. **Accesibilidad y adaptabilidad como obligación creciente.** WCAG 2.2 (publicada en oct. 2023) añadió criterios como *Target Size* (2.5.8) y *Dragging Movements* (2.5.7); el diseño adaptativo (teléfono/tablet/plegable) deja de ser "nice to have" y pasa a requisito de plataforma (clases de tamaño de ventana en Material 3).

El gap de GameVision no es estético (el sistema ya tiene carácter) sino **de sistema operativo de producto**: convertir tokens en fuente única de verdad, formalizar patrones de tracking (diario/listas/stats), diseñar el "momento Wrapped" propio, y blindar accesibilidad + adaptabilidad. Además, debe evitar los errores de diseño que matan la retención de apps de nicho.

**Fuentes (Current State)**
[^m3-google]: Google (blog.google). "Android and Wear OS are getting a big refresh". 2025. https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch
[^your-tier]: Tier (yourtier.com). "Letterboxd for games: what it means, which app fits". 2026-09-07. https://www.yourtier.com/
[^wcag22]: RapidDevelopers. "Build a White-Label AI Digital Accessibility Compliance Tool". 2025. https://www.rapidevelopers.com/

---

## Key Evidence

| Dato/Patrón | Descripción | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| M3 Expressive = estándar 2026 | Google relanza la capa expresiva de Material 3 para Android 16 / Wear OS con personalización y animación reforzadas | 2025 | "Material 3 Expressive brings huge personalization options to Android. Expect smoother animations and more intuitive interactions." | Alta | blog.google [^ev-google] |
| Física de muelles y "deep color" | El salto expresivo se apoya en *motion physics* y color profundo, no solo en estética | 2025 | "Google has launched Material 3 Expressive with the Pixel 10, bringing a fresh, lively feel to Android through motion physics and deep color ..." | Alta | YouTube/Android coverage [^ev-pixel10] |
| Bold colors + adaptive layouts | Descripción temprana del lenguaje: colores atrevidos, layouts adaptativos, theming dinámico, movimiento fluido | 2025-05-15 | "Material 3 Expressive is Google's latest design language for Android, featuring bold colors, adaptive layouts, dynamic theming, and more fluid ..." | Media | techyheaven [^ev-techy] |
| Rollout progresivo a apps reales | El lenguaje ya se despliega en apps de producto (p. ej. Gmail 2025.05.11), lo que fija la expectativa del usuario | 2025 | "Android 16's design language is rolling out to the Gmail app version 2025.05.11, introducing major changes to the overall appearance of the app" | Alta | Android Police [^ev-ap] |
| Modelo "Letterboxd para juegos" | El nicho se define por diario + ratings + reseñas cortas + listas; Backloggd es el referente | 2026-09-07 | "A Letterboxd for games means a diary, ratings, short reviews and lists. Backloggd is the best known." | Alta | Tier [^ev-tier] |
| El diario como función ancla | La propuesta central de Backloggd es el *journal* de la colección personal | 2026-09-10 | "Backloggd is a place to keep your personal video game collection. Every game from every platform is here for you to log into your journal." | Alta | Backloggd [^ev-backloggd] |
| Traducción del patrón cine → juegos | La clave del éxito fue trasplantar el patrón diario+reseña de Letterboxd al videojuego | 2026-07-21 | "Backloggd did something the games side of the internet needed: it took Letterboxd's diary-and-review model and applied it to games - log ..." | Alta | Buck the Critics [^ev-buck] |
| Fechas + notas por partida | Evidencia de uso real: el valor diferencial es poder fechar y anotar cada sesión de juego | 2019-01-06 | "My main use for backloggd is the extensive journal feature they have: you can add dates and notes on each time you play a game." | Media | ResetEra (foro) [^ev-resetera] |
| Diario como núcleo de marca | Letterboxd se autodefine como diario social de registro/discovery personal | n/d | "Letterboxd is a global social network for grass-roots film discussion and discovery. Use it as a diary to record and share your opinion about films as you watch ..." | Alta | Letterboxd FAQ [^ev-lbd] |
| Track & share como promesa | Goodreads comunica discovery + tracking + sharing del "reading journey" en su ficha de tienda | n/d | "Discover new books, read reviews, track and share your reading journey" | Alta | Google Play / Goodreads [^ev-gr] |
| Year-end review = motor de re-engagement | Los "Wrapped" se diseñan explícitamente para crecimiento viral y re-activación | 2025-12-10 | "Learn how to create year-end wrapped experiences that drive viral growth and re-engagement for consumer platforms." | Media | Trophy [^ev-trophy] |
| Origen del patrón Wrapped | Spotify Wrapped (2016) inauguró el "year in review" que miles de empresas imitaron | 2024-01-13 | "The infamous year in review feature started with Spotify Wrapped in 2016. Since then, 100s (more likely 1000s) of companies have followed suit." | Media | UX Design Collective [^ev-uxd] |
| Escala del impacto UX | Wrapped 2025 movió miles de millones de impresiones por decisiones de UX | 2025-12-08 | "Spotify Wrapped 2025 generated 2.3 billion impressions through smart UX design." | Media | UX Playbook [^ev-uxp] |
| Gamificación oscura definida | Los dark patterns gamificados buscan engañar al usuario hacia acciones no deseadas | n/d | "Dark patterns are the deceptive User eXperience (UX) design that intend to trick and mislead users to do something they might not do otherwise." | Media | ResearchGate [^ev-rg] |
| Dark patterns en juegos móviles (académico, citado 92×) | Estudio peer-reviewed sobre cómo surgen los dark patterns en el desarrollo/comercialización de juegos | 2022 | "We contribute an understanding of how dark patterns arise in the development, use and commercialisation of mobile games, their effects on players and industry." | Alta | Aagaard & Knudsen (DTU) [^ev-aagaard] |
| Rachas como anti-patrón | Las rachas pueden volverse el objetivo en sí mismas (Goodhart / sobrejustificación) | 2025 | "Gamification Gone Wrong: When Streaks Become the Point ... Goodhart's Law, the overjustification effect, and dark patterns in gamification" | Media | nerdSIP [^ev-nerdsip] |
| Interés del desarrollador ≠ interés del usuario | Definición de dark pattern orientada al beneficio del creador | n/d | "Dark patterns are design strategies that are used to benefit developers rather than the target audience, using unethical applications such as coercion ..." | Media | Nyström (UNL) [^ev-nystrom] |
| WCAG 2.2 y nuevos criterios | WCAG 2.2 añade 9 criterios, incl. Target Size (tamaños táctiles) y Dragging Movements | 2023-10 (publicación) | "WCAG 2.2 was published October 2023 and adds 9 new success criteria (including 2.5.7 Dragging Movements and 2.5.8 Target Size)." | Alta | RapidDevelopers [^ev-wcag] |
| Adaptabilidad + accesibilidad nativas | El tooling de diseño Android 2026 se posiciona en torno a "adaptive, accessible, modern" | 2026 | "Master Material Design 3 and Jetpack Compose with the Mobile Android Design Claude Code skill. Build adaptive, accessible, and modern native Android apps." | Baja | mcpmarket [^ev-mcp] |

**Fuentes (Key Evidence)**
[^ev-google]: Google. "Android and Wear OS are getting a big refresh". 2025. https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch
[^ev-pixel10]: YouTube. "Material 3 Expressive Hits Pixel 10 - Was the Leap Worth It?". 2025. https://www.youtube.com/watch?v=qlrYMenL3EY
[^ev-techy]: TechyHeaven. "Android 16 x Material 3 Expressive: A First Glimpse". 2025-05-15. https://techyheaven.com/
[^ev-ap]: Android Police. "Android 16's Material 3 Expressive design is already rolling out to Gmail". 2025. https://www.androidpolice.com/google-rolling-out-android-16-material-3-expressive-to-gmail
[^ev-tier]: Tier. "Letterboxd for games: what it means, which app fits". 2026-09-07. https://www.yourtier.com/
[^ev-backloggd]: Backloggd. "A Video Game Collection Tracker". 2026-09-10. https://backloggd.com/
[^ev-buck]: Buck the Critics. "Backloggd Alternative? 4 Trackers Compared". 2026-07-21. https://buckthecritics.com/
[^ev-resetera]: ResetEra (foro). "We are getting closer to a Letterboxd for games.". 2019-01-06. https://www.resetera.com/
[^ev-lbd]: Letterboxd. "Frequent questions". n/d. https://letterboxd.com/
[^ev-gr]: Google Play / Goodreads. "Goodreads: Book Tracker & More". n/d. https://play.google.com/store/apps/details?id=com.goodreads
[^ev-trophy]: Trophy. "How to Build a Wrapped Feature for Your App". 2025-12-10. https://trophy.so/
[^ev-uxd]: UX Design Collective (uxdesign.cc). "Monzo, Depop and Strava Wrapped — who did it better?". 2024-01-13. https://uxdesign.cc/
[^ev-uxp]: UX Playbook. "What UX Designers Can Learn From Spotify Wrapped 2025". 2025-12-08. https://uxplaybook.org/
[^ev-rg]: ResearchGate. "Gamification for Good: Addressing Dark Patterns in Gamified UX Design". n/d. https://www.researchgate.net/publication/339487229
[^ev-aagaard]: Aagaard, J. & Knudsen (DTU Orbit / CHI). "Designing Healthy, Highly-Engaging Mobile Games". 2022. https://orbit.dtu.dk/files/282189915/3491101.3519837.pdf
[^ev-nerdsip]: nerdSIP. "Gamification Gone Wrong: When Streaks Become the Point". 2025. https://nerdsip.com/blog/gamification-gone-wrong-when-streaks-become-the-point
[^ev-nystrom]: Nyström, T. (UNL). "Exploring the Darkness of Gamification: You Want It Darker?". 2023. http://novateach.unl.pt/wp-content/uploads/2023/05/paper202a.pdf
[^ev-wcag]: RapidDevelopers. "Build a White-Label AI Digital Accessibility Compliance Tool". 2025. https://www.rapidevelopers.com/
[^ev-mcp]: mcpmarket. "Mobile Android Design: Claude Code Skill for Jetpack Compose". 2026. https://mcpmarket.com/

---

## Tensions & Counter-arguments

### 1. Expresividad (M3 Expressive) vs. rendimiento, batería y sobriedad
Material 3 Expressive empuja hacia animación intensa, color saturado y física de muelles. GameVision ya usa springs, lo que la alinea bien — pero hay tensión real:
- **Motion físico ≠ motion gratuito.** Muelles en cada transición encarecen en GPU/batería en gama baja. Recomendación: reservar la expresividad para momentos de recompensa (log de partida, "Wrapped"), y mantener navegación utilitaria sobria.
- **Contracara estética:** el *monocromo cálido + un único acento ácido* de GameVision es, de hecho, una postura contraria al "color profundo/dinámico" de Material You. No es un defecto: es diferenciación. La lección de 2026 no es "usar todo el color", sino "usar el color con intención". GameVision debería **tomar la física y el motion de Expressive y no su paleta**, defendiendo su identidad monocroma.

### 2. Gamificación sana vs. gamificación tóxica (el filo más peligroso)
La evidencia es contundente: existe una literatura académica peer-reviewed que documenta cómo la gamificación deriva en *dark patterns* que benefician al desarrollador a costa del usuario (Aagaard & Knudsen 2022; Nyström 2023; Gunawan et al. 2021, presentado en la FTC).
- **El riesgo concreto para un tracker de coleccionismo:** rachas ("streaks") y métricas de vanidad reducen la motivación intrínseca por el *efecto de sobrejustificación* ("cuando las rachas se vuelven el objetivo").
- **Diseño recomendado:** celebrar la acumulación (biblioteca, estadísticas de perfil, "Year in Review" estilo Wrapped) sin castigar la ausencia. Nunca romper una racha del usuario ni usar presión social/notificaciones culposas. El *Year in Review* es legítimamente sano cuando **espeja** lo que el usuario ya hizo, y tóxico cuando **presiona** para generar más.

### 3. Skeletons vs. spinners: adoptado, pero con matices
GameVision ya usa skeletons, lo que está alineado con la tendencia 2026. Contra-argumento: los skeletons **deben reflejar el layout real**; un skeleton genérico puede percibirse como *más lento* y provocar saltos de layout (CLS) si el contenido final difiere. Además, en listas largas, un skeleton infinito sin estados de error/vacío es peor que un spinner honesto. Recomendación: skeleton + *timeout* + estado vacío diseñado.

### 4. Onboarding "time-to-first-value <60 s" vs. curaduría/social
El brief pide registrar algo en <60 s. Tensión: los trackers sociales (Letterboxd, Backloggd) ganan por red y curaduría, lo que tienta a pedir registro, perfil y conexiones antes de dar valor. La evidencia del nicho (jornada diaria + datos por sesión) sugiere lo contrario: **primero el acto privado de registrar, después el social**. Contra-argumento a vigilar: si el onboarding es *demasiado* sin fricción, el perfil llega vacío y el efecto "wow" estadístico ("Tu año en juegos") no se activa hasta muy tarde. Equilibrio: permitir un modo "dato de ejemplo / importar" opcional para que el Wrapped sea visible en la primera sesión.

### 5. Monocromo + acento vs. accesibilidad (riesgo de contraste)
Un sistema monocromo es elegante pero **arriesga AA**: grises sobre grises y el verde ácido `#C8F135` (muy luminoso) fallan contraste sobre blanco para texto. El verde ácido funciona como *fill*/acento sobre fondos oscuros, no como texto sobre claro. Con WCAG 2.2 y el criterio de *Target Size* (2.5.8) ya vigentes, accesibilidad es contractual. Iconos solo-color, estados activos solo por color y targets táctiles <24 px son fallos típicos de sistemas monocromos.

### 6. Design tokens como "fuente única de verdad": deseable, con coste de gobernanza
Mantener `DESIGN.md` + tokens como SSOT es la práctica correcta, pero la evidencia disponible sobre escalado de design systems fue **débil en esta ronda de búsqueda** (los resultados sobre "tokens/SSOT" fueron mayoritariamente ruido). Contra-argumento honesto: un DESIGN.md que nadie ejecuta se desincroniza del código. La recomendación estándar (no validada aquí con fuente fuerte) es **generar** tokens de código desde la fuente, y no mantener dos listas en paralelo.

### 7. Fiabilidad de esta investigación
Varias consultas devolvieron resultados irrelevantes. Las evidencias de mayor confianza aquí son **Material 3 Expressive** (fuentes oficiales + prensa tech) y la **literatura académica sobre dark patterns** (peer-reviewed, altamente citada). Las afirmaciones sobre "tendencias 2026" y "design tokens SSOT" son de **confianza media-baja** y deberían re-verificarse con fuentes primarias (m3.material.io, WCAG W3C, NN/g) antes de congelar decisiones.

**Fuentes (Tensions)**
[^t-aagaard]: Aagaard, J. & Knudsen. CHI/DTU. "Designing Healthy, Highly-Engaging Mobile Games". 2022. https://orbit.dtu.dk/files/282189915/3491101.3519837.pdf
[^t-nerdsip]: nerdSIP. "Gamification Gone Wrong: When Streaks Become the Point". 2025. https://nerdsip.com/blog/gamification-gone-wrong-when-streaks-become-the-point
[^t-nystrom]: Nyström, T. UNL. "Exploring the Darkness of Gamification". 2023. http://novateach.unl.pt/wp-content/uploads/2023/05/paper202a.pdf
[^t-ftc]: Gunawan, J. et al. (FTC PrivacyCon). "A Comparative Study of Dark Patterns Across Mobile and Web Modalities". 2021. https://www.ftc.gov/system/files/ftc_gov/pdf/PrivacyCon-2022-Gunawan-Pradeep-Choffnes-Hartzog-Wilson-A-Comparative-Study-of-Dark-Patterns-Across-Mobile-and-Web-Modalities.pdf
[^t-wcag]: RapidDevelopers. WCAG 2.2 (9 nuevos criterios, Target Size 2.5.8). 2025. https://www.rapidevelopers.com/
[^t-google]: Google. "Material 3 Expressive". 2025. https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch
