# Integraciones y APIs "ultra pro" — propuesta para toda la app (2026)

> **Qué es:** la síntesis de la investigación profunda de 3 frentes (Steam · consolas/trofeos ·
> coleccionismo/precios/arte), con verificación en vivo de endpoints, para decidir qué APIs
> adoptamos y dónde se usan. Material crudo: [`fuentes-apis/`](fuentes-apis/).
> **Fecha:** 29/09/2026 · **Estado:** **ACEPTADO íntegramente** por el propietario (29/09/2026, «acepto todo»).

---

## 1. Resumen ejecutivo

Con lo que ya hay (RAWG + HLTB) cubrimos catálogo y duración. El salto "pro" viene de 4 capas:

| Capa | Qué aporta | Fuente principal | Veredicto |
|---|---|---|---|
| **A. Steam Inside** | Biblioteca real, horas jugadas, logros + % global, precios/capturas, reseñas, "jugando ahora", ofertas | **Steam Web API oficial** (clave) + Storefront + CheapShark | **Adoptar (P1)** — el mayor salto de valor por esfuerzo |
| **B. Coleccionado multi-fuente** | Logros con % comunitario (retro + consolas + Steam) | **RetroAchievements (oficial)** + OpenXBL (Xbox) + psn-api (PSN, con cuidado) | **Adoptar RA ahora; Xbox piloto; PSN aplazado con condiciones** |
| **C. Coleccionista / valor** | Valor de la estantería física, arte espectacular, live | PriceCharting (pago+licencia), SteamGridDB, Twitch Helix | Arte ahora; valor **después** (post-ingresos); live opcional |
| **D. Crítica agregada** | Nota de crítica (Mighty/Strong…) | OpenCritic/Metacritic | **Hoy no hay vía limpia** — contactar en el futuro; no wrappers |

**Tres verdades incómodas** (para decidir con los ojos abiertos):
1. Las APIs de **PSN/Xbox para terceros son NO oficiales** (PSN: NPSSO ≈ contraseña, riesgo real de baneo;
   Xbox: OpenXBL de pago). El "canal oficial" de logros de consola solo existe para los propios estudios.
2. El **valor de colección** serio (PriceCharting) exige suscripción + **licencia comercial escrita** para
   mostrarlo en una app, **no tiene histórico** (habría que construir la serie nosotros) y obliga a purgar al cancelar.
3. La **clave de Steam no puede vivir en la app** (ToS: no ceder la clave; 100.000 llamadas/día por clave)
   → primera pieza de **backend propio** (Cloud Functions de Firebase) como proxy + caché.

---

## 2. Tabla maestra

| Servicio | Aporta | Requisitos / coste | Límites y condiciones clave | Adoptar |
|---|---|---|---|---|
| **Steam Web API** (oficial) | Perfil, biblioteca + horas, recientes, logros, **% global de logros**, CCU | Clave gratis (cuenta Steam) | 100k llamadas/día; biblioteca solo si el perfil es público; clave **solo en servidor** | **P1 (ahora)** |
| Steam Storefront (`appdetails`/`appreviews`) | Precio EUR/rebajas, capturas HD, categorías, badge de reseñas ("Muy positivas · 5,2M") | Sin clave; **no oficial** | ~200 req/5 min (atribuido por la comunidad); caché obligatoria | **P1 (ahora)** |
| **CheapShark** | Ofertas PC multi-tienda | Sin clave; usar sus **redirects** | Rate 429; prohibido construir catálogo cacheado; User-Agent identificable | **P1 (ahora)** |
| **IsThereAnyDeal** | Wishlist sync, **histórico de precios**, **webhooks** de bajada | Registro de app; key u OAuth | 1.000 req/5min; comercial OK si app pública; no competir con ITAD | **P2 (después)** |
| SteamSpy | Owners (aprox.), tiempo medio, **tags de usuario**, CCU | Sin clave | 1 req/s; refresco diario; son **estimaciones** | P2 |
| SteamGridDB | Arte: póster vertical, hero, logo, icono | Clave gratis | Sin límites documentados; revisar ToS antes de monetizar | **P1 (ahora)** |
| ProtonDB (no oficial) | Badge compatibilidad Deck/Linux | Sin clave | No documentado/frágil; "best effort", nunca bloqueante | P1 con cautela |
| **RetroAchievements** (oficial) | Logros retro, puntos, **% comunidad por logro** | Web API key del usuario | "Rate limiting"; cachear; solo retro | **P2 (ahora para "Coleccionado")** |
| **OpenXBL** (Xbox, 3.º) | Logros, gamerscore, presencia | Key; Free $0 (150 req/h)* ; $5–35/mes | *disputa: free tier podría ser 150/día → medir | P2 (piloto) |
| psn-api (PSN, no oficial) | Trofeos, **rareza/% comunitario**, horas | NPSSO del usuario (¡equivale a la contraseña!) | Riesgo de **baneo**; tratar como secreto extremo; backend; opt-in explícito | **P3 (aplazado, con condiciones duras)** |
| **IGDB** (oficial) | Franquicias, similares, artworks, TTB, idiomas | OAuth Twitch | **Gratis solo no comercial**; comercial = partner program; 4 req/s; server-side (CORS) | **Migración futura (gate: monetización)** |
| Twitch Helix | 🔴 espectadores en vivo por juego | App Twitch + token | 800 pts/min; mapeo por `game_id` (IGDB) | P3 (opcional) |
| **PriceCharting** | Valor de colección física por condición | Suscripción de pago + **licencia comercial** | Sin histórico; purga al cancelar; 1 req/s | **P3 (post-ingresos)** |
| eBay Marketplace Insights | Ventas reales 90 días | Aprobación "por unidades de negocio" | Inalcanzable para indie | Descartar |
| MobyGames | Créditos retro profundos | Comercial desde $99,99/mes | Cláusulas de no-competencia | Descartar (reconsiderar si hay ingresos) |
| OpenCritic / Metacritic | Nota de crítica agregada | Sin doc pública / sin API | Zona gris / scraping | Descartar hoy; contactar mañana |
| Merch (Amazon Creators, MFC, Printful) | Figuras/camisetas | Amazon exige ventas previas; MFC cerró (noai); Printful sin encaje | — | Solo **enlaces** (parking) |
| SteamDB / GG.deals / GOG / Epic / cromos | — | No hay API pública utilizable | — | Descartar (documentado) |

---

## 3. Extrapolación vista por vista (app completa)

| Vista | Qué añaden las APIs |
|---|---|
| **Búsqueda (catálogo)** | (Ya: chips HLTB.) + precio Steam y "Muy positivas" son **candidatos P2** para no saturar; la búsqueda no cambia en F1 |
| **Ficha del juego** | Lo adoptado (HLTB rico) **+ P1**: precio/rebajas Steam por región, capturas HD, badge reseñas, "jugando ahora" (CCU), arte SteamGridDB (hero/póster), badge ProtonDB; **P2**: rareza de logros (%, schema) |
| **Biblioteca** | **P1 — importación Steam**: biblioteca real + horas por juego → cruzar con HLTB para el progreso ("12 h de ~60 h" con **tus** horas reales), orden por "lo que más juegas", "sigue jugando" |
| **Diario** | P2: enriquecer sesiones con "jugado ahora en Steam (2 semanas)" si hay cuenta vinculada |
| **Estadísticas** | P1: totales reales desde Steam + estimado del backlog; P2: "top logros raros", comparativa con % comunitario |
| **Perfil** | P2: **vitrina «Coleccionado» multi-fuente** (Steam % + RetroAchievements + Xbox) con insignias reales |
| **Social (F2)** | El **% comunitario de logros** (Steam es público; RA oficial) alimenta el feed/foro de logros sin inventar datos; ITAD/CheapShark habilitan "tu amigo tiene X en deseados y está de oferta" |
| **Rewind (F3)** | "Tu año" con datos reales de Steam/RA (logros del año, horas verificadas) |
| **Notificaciones** | **Ofertas de tu lista de deseados** (CheapShark ya; ITAD webhooks después), logros conseguidos (RA/Steam), disponibilidad Deck |
| **Widget (F4)** | Ofertas activas + progreso del juego en curso |
| **Ajustes** | Sección **«Conexiones»**: vincular Steam (OpenID o vanity), RetroAchievements (key), Xbox (piloto), PSN (si algún día) — con estado y revocación |

---

## 4. Packs de adopción (el checklist del debate)

### P1 — «Steam Inside» (recomendado: SÍ)
Importación de biblioteca + horas, % global de logros, CCU, precio/reseñas, arte, ofertas básicas.
**Fase:** cierra la duda de importación (D1.7) y aterriza en **F2** (o F1.5 si se quiere adelantar).
**Coste real:** pequeña capa servidor (Cloud Functions) para la clave + caché + sync incremental.
**Riesgos:** perfiles privados (UX guía), cuota 100k/día (caché + sync, nunca polling por usuario).

### P2 — «Coleccionado multi-fuente» (recomendado: SÍ en su versión segura)
RetroAchievements (oficial) + Xbox piloto vía OpenXBL + pasarela `AchievementProvider` para añadir más.
**Fase:** F2 — alimenta la vitrina, el feed de logros y el futuro foro.
**PSN: aplazado** (solo opt-in, secretos en backend, cuenta dedicada, advertencias) — o nunca, si no compensa el riesgo.

### P3 — «Coleccionista / valor» (recomendado: parking con disparador)
PriceCharting (valor de estantería, escaneo de código de barras estilo CLZ) → **cuando haya ingresos**
(hay que pagar suscripción + licencia comercial + construir histórico propio). Interim: enlaces externos.
Twitch live: opcional y de baja cobertura; se reevalúa con datos reales de uso.

### P4 — «Crítica agregada» (recomendado: contactar, no integrar)
OpenCritic: pedir acceso/condiciones por la vía oficial cuando toque; mientras, ni wrappers ni scraping.
MobyGames/eBay/Metacritic: descartados documentadamente (coste o acceso).

---

## 5. Requisitos técnicos transversales (lo que habilita todo)

1. **Backend proxy (Cloud Functions)**: las claves (Steam, IGDB, OpenXBL…) no pueden vivir en la app;
   además caché central compartida entre usuarios (una consulta de un usuario sirve a todos).
2. **Tabla de mapeo de identidades**: RAWG id ↔ HLTB game_id ↔ Steam appid ↔ SteamSpy ↔ Twitch/IGDB
   game_id ↔ RetroAchievements gameId ↔ PSN/Xbox titleId ↔ PriceCharting id. Sin esto, las features
   "pro" se rompen en los bordes (es trabajo de datos, no de API).
3. **Políticas de red**: cachés con TTL (HLTB 90 d · SteamSpy 24 h · precios 6–24 h), reintentos con
   `Retry-After`, límites por servicio, degradación silenciosa (nunca romper la app).
4. **Privacidad**: NPSSO/tokens de consola tratados como secretos críticos; consentimiento explícito
   por conexión; revocación fácil; nada de credenciales en claro en el dispositivo.

## 6. Decisiones (cerradas: 29/09/2026 — «acepto todo»)

- **D-AI-1 ✅** P1 «Steam Inside» adoptado: es la importación definitiva (cierra D1.7) e incluye la
  capa servidor (Cloud Functions: proxy de claves + caché compartida + sync incremental).
- **D-AI-2 ✅** P2 adoptado en versión segura: RetroAchievements ya; Xbox en piloto (OpenXBL);
  PSN aplazado con condiciones duras (opt-in, secretos en servidor, advertencias).
- **D-AI-3 ✅** P3 en parking con disparador de ingresos (PriceCharting, Twitch live).
- **D-AI-4 ✅** P4: contactar a OpenCritic cuando toque; Metacritic/MobyGames/eBay cerrados.
- **D-AI-5 ✅** Arte SteamGridDB entra **con P1 en F2** (la ficha de F1 mantiene su alcance HLTB ya
  cerrado; el proxy de claves llega con P1, no antes).

**Requisitos nuevos del propietario (misma fecha):** reseñas visibles en la ficha del juego;
vinculación Steam desde el perfil con recuperación de horas reales por juego (si un dato no
llega, se oculta con degradación silenciosa); cada detalle se decide cláusula a cláusula;
onboarding muy profesional; cohesión de producto aprendiendo de las apps exitosas de Play Store.

**Siguiente paso diseñado a raíz de la adopción:** mapa de integración por vistas —
`docs/plan/integracion-por-vistas-2026.md` (dónde vive cada integración, vistas nuevas y flujo de
vinculación Steam + onboarding).
