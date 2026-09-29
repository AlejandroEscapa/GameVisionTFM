# GameVision · Dive 03: Logros, ofertas y colección con valor (patrones UX)

- **Fecha:** 29/09/2026 · **Ámbito:** app Android nativa (Kotlin + Jetpack Compose).
- **Método:** ~24 búsquedas web + verificación directa de páginas (Steamworks, docs oficiales de
  RetroAchievements, psn.gg, Deku Deals, IsThereAnyDeal, CheapShark API docs, clz.com, soporte de
  Discogs vía API Zendesk, iTunes Search API, Google Play). Las páginas protegidas por Cloudflare
  (TrueAchievements, PSNProfiles, GG.deals) se citan solo por snippets de búsqueda y se marcan con
  confianza Media/Baja. Nada inventado: lo no verificado se dice expresamente.
- **Nota de recuperación:** este informe fue completado por el agente principal a partir del
  transcript del investigador original (interrumpido antes de escribir el archivo); toda la
  evidencia proviene de las verificaciones realizadas por ese investigador.

---

## Resumen ejecutivo

1. **La rareza de logros tiene estándares de facto por plataforma**: PlayStation usa 4 bandas
   oficiales (Ultra Rare / Very Rare / Rare / Common) sin cortes públicos exactos (Ultra Rare ≈ ≤5 %
   observado); Xbox marca como "rare" (diamante) lo conseguido por <10 % de jugadores; Steam muestra
   el % global crudo con barra de progreso; RetroAchievements calcula rareza contra sus jugadores y
   re-puntúa con "TrueRatio". Los trackers third-party (PSNProfiles, TrueAchievements) añaden una
   5ª banda intermedia y umbrales propios.
2. **El patrón de tarjeta de oferta es consistente** en todos los servicios serios: precio original
   tachado + precio de oferta + badge de % + **"mínimo histórico"** ("Lowest price ever" en Deku
   Deals; flags N/H/S en IsThereAnyDeal) + fecha de fin + tienda. Las alertas de wishlist son el
   corazón del producto (Deku Deals: "saved $614"; Steam notifica por email/push con descuentos
   ≥20 % y >8 h).
3. **El valor de colección se presenta con honestidad estadística y privacidad**: Discogs muestra un
   "valor estimado" basado en las últimas 30 ventas del marketplace (excluye ítems sin histórico y
   avisa de que es aproximado); CLZ Games muestra valores diarios de PriceCharting por condición
   (Loose/CIB/New). Ambos lo tratan como dato personal; la visibilidad de la colección se controla
   en privacidad.
4. **La privacidad del perfil es la causa #1 de fallo** en tracking automático (Exophase lo documenta
   como su FAQ más frecuente) — confirma el diseño de GameVision de una pantalla guiada de "perfil
   privado" para Steam.
5. Las apps de logros de consola en Android viven en un ecosistema frágil (clientes no oficiales,
  4.4★, pidiendo features como ocultar logros); Microsoft retiró los logros de su app companion y
   los usuarios lo reclaman en reseñas de apps de terceros → hueco de mercado real.

---

## A) Logros y trofeos — presentación y rareza

### PlayStation (oficial)
- El API oficial (documentación comunitaria mantenida del API v2 de PSN, andshrew/PlayStation-Trophies)
  define `trophyRare` con **4 niveles: 0=Ultra Rare, 1=Very Rare, 2=Rare, 3=Common**, y
  `trophyEarnedRate` = "Percentage of all users who have earned the trophy". También expone
  `rarestTrophies` ("the trophy where earned is true with the lowest trophyEarnedRate"). Confianza:
  Alta (doc del API real).
- psn.gg (artículo "PlayStation Trophy Rarity Explained"): PlayStation ordena cada trofeo en bandas
  según el % de jugadores que lo tienen; **Sony no publica los cortes exactos**; el único consistentemente
  observado: **Ultra Rare ≈ 5 % o menos**. El % de PSN se calcula contra todas las cuentas con la lista
  registrada; los trackers de terceros usan su propia base (auto-seleccionada) → porcentajes distintos.
  Confianza: Alta.
- **Trofeos ocultos** (semántica oficial del API): `trophyHidden` = "True if this is a secret trophy
  (further details are not displayed by default unless earned)". Es decir: el spoiler se protege por
  defecto y se revela a demanda. Confianza: Alta.

### PSNProfiles (tracker de referencia, 3.º)
- Usa **5 bandas propias**: Common, Uncommon, Rare, Very Rare, Ultra Rare. Umbrales citados por la
  comunidad (foro SomethingAwful): **Ultra Rare <5 % · Very Rare 5–10 % · Rare 10–20 %** (y
  Uncommon/Común por encima). Ejemplos verificados en guías: "4.80 % → Ultra Rare", "8.9 % → Very
  Rare". Confianza: Media (fuente comunitaria; el sitio está tras Cloudflare y no permite verificación
  directa).

### Xbox (oficial)
- **"Rare" = conseguido por <10 % de jugadores**, con icono de diamante. Fuentes: xbox.fandom wiki
  ("If an unlock percentage is below 10% it will become a rare achievement") y consenso comunitario
  (r/XboxSeriesX: "less than 10% of Xbox gamers have gotten it"). Microsoft no publica el corte
  oficial. Confianza: Media-Alta.
- **Actualización 2024 del sistema de logros** (Xbox Wire vía Vice): icono renovado con **laurel bajo
  el diamante** para los rare, notificaciones que toman el color del perfil, **destacado de juegos
  completados al 100 %** con color extra, **filtro para ver todos los 100 %** en una lista, y
  **ocultar juegos con % de completado bajo** (ocultar no elimina el Gamerscore). Confianza: Alta
  (anuncio oficial).

### TrueAchievements / TrueTrophies
- TrueAchievements integra la rareza de Xbox ("rare achievement indicators and filters") y muestra
  "Unlocked by X% of gamers" por logro; su **TA Ratio re-puntúa** el valor de cada logro "to better
  represent a task's difficulty" (citado hasta en un paper académico de 2016 sobre sistemas de
  logros). TrueTrophies hace lo propio en PlayStation ("This is an Ultra Rare trophy on PSN" con %).
  Confianza: Media (sitios tras Cloudflare; verificado por snippets y artículo).

### RetroAchievements (documentación oficial)
- **Puntos por dificultad** (docs oficiales de achievement-scoring): guías de puntos 0/5 (Easy) ·
  10 (Medium) · 25 (Hard) · 50 (Very Hard) · 100 ("Impossible", típico de bonus sets); entre los
  factores para valorar dificultad está expresamente "**What percent of players earned the
  achievement?**". Confianza: Alta.
- **Modelo de datos del API** (api-docs oficiales): por logro expone `Points`, `NumAwarded`,
  `NumAwardedHardcore` y `TrueRatio` (ponderación por rareza, a.k.a. RetroRatio); por juego:
  `NumDistinctPlayers` y **medianas de tiempo** (`MedianTimeToBeat/Complete/Master`); y
  `get-achievement-distribution` = "how rare that overall mastery is" (el gráfico de distribución de
  maestría de las páginas de juego). Conceptos de estado: **beaten** (solo progresión) vs **mastery**
  (100 % del set). Confianza: Alta.
- RA no publica bandas de rareza con nombre (muestra el % crudo y la ponderación) — a diferencia de
  PSN/PSNProfiles. Confianza: Alta (ausencia verificada en docs).

### Steam (oficial)
- La página comunitaria de logros de cada juego (verificado en vivo, p. ej. Hades) muestra **cada
  logro con su % global y una barra de progreso** (clases `achieveFill`/`achievePercent`), ordenada
  de más a menos conseguido. Confianza: Alta.
- **Logros ocultos**: "does not show up on a user's Community page (at all) until they have achieved
  it" (Steamworks docs). Confianza: Alta.
- **Vitrinas de perfil**: existe el "Achievement Showcase" (eliges logros) y el **"Rarest Achievement
  Showcase"** (auto-poblado por rareza; se compra con Steam Points). Confianza: Media-Alta
  (verificado por fuentes comunitarias y la Points Shop).
- **Wishlist → notificación oficial** (Steamworks, doc de marketing de wishlists): "An email or
  mobile push notification will be issued to users with your game on their wishlist" cuando el
  descuento es **≥20 %**, afecta al **paquete más barato** y dura **>8 horas**. Confianza: Alta.

### Apps Android del ecosistema (verificadas en Google Play)
- Cliente Android de RetroAchievements (p. ej. `com.akissame.retroachievements`, **4.44★**): reseñas
  piden "hide unlocked achievements" y flags de missables — señal de qué esperan los usuarios de
  una lista de logros seria. Confianza: Alta (ficha verificada).
- App de logros Xbox de terceros (`ar.com.indiesoftware.xbox`): reseña verificada — "Microsoft took
  achievements out of their companion app. One of the major reasons why I used it often was the
  ability to see how many hours…". Confianza: Alta (reseña real).
- Exophase (tracker multi-plataforma): cubre Steam/PSN/Xbox/Epic/RetroAchievements/Google Play con
  **perfiles públicos y sin contraseñas**; NO publica app en Play/App Store (es PWA). Su FAQ
  documenta la causa #1 de fallo: "**Typically this is due to incorrect privacy settings… Most
  services opt to hide your gaming activity by default**". Confianza: Alta.
- No se pudo verificar una app oficial de GG.deals en tiendas US (búsquedas en Play e iTunes sin
  resultado claro) ni la app "Completionist" — se declara no verificado.

---

## B) Ofertas y wishlist — presentación

### Deku Deals (Switch/PS/Xbox/Steam — la referencia UX)
- **Tarjeta de oferta** (verificado en vivo): imagen + título + **precio original tachado (€19,99) +
  precio de oferta (€7,99) + badge de % (-60 %)** + badge "**Lowest price ever**" o "**Matches
  previous low**" + "**Sale ends <fecha>**" + "1 hour ago at Nintendo eShop" (tienda + frescura).
  Confianza: Alta.
- **Página de juego** (verificado, p. ej. Hades): sección "Current prices" con precios por edición
  (digital/physical), "Available on other platforms", sección **"Price history"** con "All time low:
  Digital €6,24 (-75 %)" y selector de rango **6m / 1y / 2y / All**, y botón "Add to wishlist — Get
  notified next time this goes on sale". Confianza: Alta.
- **Navegación por colecciones de deals** (verificadas en home): Hottest Deals · Recent Price Drops ·
  eShop Sales · Bang for your Buck · Ending Soon · Most Wanted · Deepest Discounts. Confianza: Alta.
- Apps móviles verificadas: **Android existe** (`com.dekudeals.app`); **iOS 4.6★ (76 ratings)**
  (iTunes API). Confianza: Alta.
- Reseñas de usuarios (Google Play, verificadas): "track sales, monitor historical low prices, and
  set instant price alerts for my wishlist… saved $614… push notifications hit right when a game on
  my watchlist hits a deep discount"; "Notifies or emails you whenever your wishlist game is on sale
  or drops in price". Crítica: "The app always defaults to the hottest deals… I want it to default to
  what's recently discounted". Confianza: Alta.

### IsThereAnyDeal (el comparador "pro")
- Página de deals (verificada en vivo): ordenaciones por Hottest / Newest / Highest price cut / Lowest
  price / Expiring soon / Most waitlisted / Most collected…; filtros de **Price Cut ≥25/50/75/90 %**;
  y **flags por deal: "N — New historical low", "H — Historical low", "S — Lowest store price"**.
  Confianza: Alta.
- Página de juego (verificada): tabs **Prices | History | Stats | Regions**; botones **Wait (20.932) ·
  Collect (39.921) · Ignore**; mecánica social "Cool deal? Heat it up!"; stats que integran jugadores
  de Steam, reseñas, OpenCritic/Metacritic y **tiempos de HowLongToBeat** (Main Story 24 h, Main+Sides
  49 h, Completionist 95 h). Confianza: Alta.

### CheapShark (la API gratuita que usaremos)
- Documentación oficial verificada: cada deal expone `title`, `salePrice`, `normalPrice`, `isOnSale`,
  `savings`, `metacriticScore`, `steamRatingText/Percent`, `dealRating`, `lastChange`; y el endpoint
  de juego expone `cheapest` y **`cheapestPriceEver`** (mínimo histórico listo para usar).
  Confianza: Alta.

### GG.deals
- Sitio tras Cloudflare; no se pudo verificar app oficial en tiendas US. Por fuentes secundarias:
  "checks prices across fifty different stores… shows the lowest price ever recorded" y gráfico de
  histórico por página. Se deja como no integrable hoy (sin API pública documentada). Confianza:
  Media-Baja.

---

## C) Colección con valor de mercado

### CLZ Games (el modelo de negocio de "estantería con valor")
- Web oficial (verificada): "Game values **powered by PriceCharting**. Game values are **updated
  daily**, based on **Loose / CIB / New status**". Escaneo por código de barras contra su base CLZ
  Core. Confianza: Alta.
- Modelo: **app de suscripción** — "CLZ Games is a paid subscription app, costing US $1.99 per month
  or US $19.99 per year. Use the free 7-day trial…" (ficha de Google Play verificada). Rating Android:
  **4.11★**. Confianza: Alta.
- Campos personales verificados en la descripción: completeness, condition, location, purchase
  date/price/store, notes; y wishlist para "monitor their price".
- Reseñas (verificadas en clz.com y Google Play): "Everything scans in perfectly… 17 systems… well
  over a thousand games… even found my imported, Spanish-only F1 game from 2000" (elogio del
  escaneo/catálogo); "An app that catalogs all your games… it's current value based on its condition"
  (valor por condición); y crítica útil: "Aunque el valor de las ediciones coleccionista no siempre
  se parece a las reales que se ve en páginas de compra venta, para llevar un control de la colección
  va bastante bien" (los valores son orientativos). Confianza: Alta.

### Discogs (el patrón de honestidad estadística + privacidad)
- Artículo oficial de soporte (vía API Zendesk, verificado): "you will see the **estimated value of
  your Collection, based on the last 30 sales** on Discogs, using the Marketplace Sales History.
  Items for which no sales history is available are **not included** in the estimated value. Please
  keep in mind that the estimated value of your Collection is **only an approximate calculation**."
  Confianza: Alta.
- La app iOS permite **ordenar la colección por "Collection values"** (doc oficial iOS verificada).
  La visibilidad de colección/wantlist se gestiona en **Privacy Settings** (doc oficial). No se
  verificó un control específico de ocultación del valor en sí — se declara. Confianza: Alta/Media.
- Señal de tienda (iTunes API + Google Play, verificadas): **iOS 4.8★ (67 k) vs Android 2.4★
  (21,1 k)** — con la queja Android estrella: "At least once a week it makes me log in again". Lección:
  la misma marca con dos calidades de app → dos notas opuestas. Confianza: Alta.

---

## D) Reseñas de usuarios (muestra citada)

| App | Elogio (verificado) | Crítica (verificado) |
|---|---|---|
| Deku Deals | "saved $614… push notifications hit right when a game hits a deep discount" | "always defaults to the hottest deals" (orden por defecto) |
| CLZ Games | "Everything scans in perfectly… even found my Spanish-only F1 game from 2000" | "el valor de las ediciones coleccionista no siempre se parece a las reales" |
| Discogs (Android) | (marca y base de datos) | "At least once a week it makes me log in again" → 2.4★ |
| RA client Android | 4.44★ con desarrollo activo | piden "hide unlocked achievements" y flags de missables |
| App logros Xbox (3.º) | llenaba el hueco de la oficial | "Microsoft took achievements out of their companion app" |
| PlayStation App (iOS) | 4.22★; "View other players' profiles and trophy collections" | (no profundizado esta pasada) |

---

## Patrones recomendados para GameVision

### Pantalla "Logros del juego"
1. Cabecera con **progreso global** ("18/42 · 43 %") + (si hay conexión) tu progreso.
2. Cada logro: icono, nombre, descripción, **% global con barra** (patrón Steam, verificado) y
   **etiqueta de rareza** cuando aplique.
3. **Umbrales de rareza propuestos** (alineados con los estándares verificados):
   - **Ultra Raro <5 %** (≈ banda "Ultra Rare" de PSN observada; UR de PSNProfiles) — color ácido del
     design system (#C8F135), el momento "wow" de la colección.
   - **Muy Raro 5–10 %** (Very Rare PSN/PSNProfiles).
   - **Raro 10–20 %** (Rare PSNProfiles; nota: Xbox considera "rare" <10 % — lo indicamos en la
     fuente cuando venga de Xbox).
   - **Común ≥20 %** (sin etiqueta o "Común" discreta).
   - Steam/RA: % crudo + barra (sin bandas oficiales); podemos aplicar nuestras bandas propias
     etiquetadas como "según GameVision".
4. **Logros ocultos** = semántica PSN: "Logro oculto — toca para revelar" (protege spoilers por
   defecto; revelado a demanda).
5. Filtros mínimos: Todos · Pendientes · Conseguidos · **Ocultar conseguidos** (lo piden los usuarios
   reales de apps de logros) · Raros primero.

### Vitrina de logros (perfil)
- Patrón Steam "Rarest Achievement Showcase": **top N rarezas auto-seleccionadas por %** + contadores
  (logros totales, juegos completados). Los 100 % destacados (patrón Xbox 2024: color especial +
  filtro "completados al 100 %").

### Sección "Ofertas"
- **Tarjeta de oferta** (patrón Deku Deals verificado): precio tachado + precio + badge % + chip
  **"Mínimo histórico"** (usar `cheapestPriceEver` de CheapShark; equivalente a "Lowest price ever" /
  flag H/N de ITAD) + "Termina el <fecha>" + tienda; CTA "Ir a la tienda" SIEMPRE por redirect de
  CheapShark (requisito del servicio).
- **Alertas de precio** (opt-in): notificación diaria única y agrupada cuando un juego de tu lista
  **Deseado** cumpla: descuento ≥ umbral configurable **(por defecto 30 %**; Steam usa ≥20 % como
  oficial**) O nuevo mínimo histórico**. Umbral ajustable por juego (patrón Deku/ITAD).
- El hogar natural de las ofertas es la **lista Deseados** (chips "en oferta" + filtro "solo en
  oferta") + la ficha; en Inicio, como mucho UNA fila compacta si hay deals activos.

### Colección con valor (parking P3 — diseño futuro)
- Valor por condición (Loose/CIB/New, patrón CLZ/PriceCharting) + total de estantería con
  **disclaimer honesto** estilo Discogs ("estimado basado en las últimas ventas; aproximado").
- **Privado por defecto**; visibilidad opt-in.
- Escaneo de código de barras como entrada rápida (patrón CLZ) cuando llegue la fase.

---

## Fuentes

**PlayStation / PSN**
- psn.gg — PlayStation Trophy Rarity Explained — https://psn.gg/news/playstation-trophy-rarity-explained
- andshrew/PlayStation-Trophies APIv2 docs (trophyRare 0-3, trophyEarnedRate, trophyHidden,
  rarestTrophies) — https://github.com/andshrew/PlayStation-Trophies
- PlayStation App (iOS, iTunes API) — https://apps.apple.com/us/app/playstation-app/id410896080

**Xbox**
- xbox.fandom — Achievement (rare <10 %) — https://xbox.fandom.com/wiki/Achievement
- Vice — "Xbox Is Finally Updating Its Achievement System in 3 Big Ways" (2024) — https://www.vice.com/en/article/xbox-is-finally-updating-its-achievement-system-in-3-big-ways
- App de logros Xbox 3.º (reseñas Play) — https://play.google.com/store/apps/details?id=ar.com.indiesoftware.xbox

**RetroAchievements**
- Docs oficiales de scoring — https://docs.retroachievements.org/developer-docs/achievement-scoring.html
- API docs oficiales (NumAwarded, TrueRatio, distribución, medianas) — https://api-docs.retroachievements.org/
- Cliente Android — https://play.google.com/store/apps/details?id=com.akissame.retroachievements

**Steam**
- Página de logros globales (patrón de barra/%) — https://steamcommunity.com/stats/Hades/achievements
- Steamworks — Achievements (logros ocultos, % global) — https://partner.steamgames.com/doc/features/achievements
- Steamworks — Wishlists (notificación email/push ≥20 %, >8 h) — https://partner.steamgames.com/doc/marketing/wishlist

**Ofertas**
- Deku Deals (home, página de juego, patrones verificados) — https://www.dekudeals.com/ · app Android:
  https://play.google.com/store/apps/details?id=com.dekudeals.app · iOS (iTunes API)
- IsThereAnyDeal (deals + página de juego) — https://isthereanydeal.com/deals/ · https://isthereanydeal.com/faq/
- CheapShark API docs — https://apidocs.cheapshark.com/

**Colección con valor**
- CLZ Games (valores PriceCharting diarios, Loose/CIB/New) — https://clz.com/games/ · reseñas:
  https://clz.com/games/reviews/ · Google Play: https://play.google.com/store/apps/details?id=com.collectorz.javamobile.android.games
- Discogs — "How Does The Collection Feature Work?" (valor estimado, últimas 30 ventas) —
  https://support.discogs.com/hc/en-us/articles/360007331514 · app iOS (API Zendesk/iTunes)
- Exophase FAQ (perfiles públicos, causa #1 = privacidad) — https://www.exophase.com/faq/

*Fin del informe — Dive 03 (completado por el agente principal con la evidencia del transcript).*
