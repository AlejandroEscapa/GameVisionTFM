# Dimension 02: Monetización y anuncios (2026)

> Contexto: GameVision es un tracker de videojuegos Android (Kotlin/Compose). Modelo previsto: gratis con anuncios no intrusivos + posible premium para quitar anuncios. Audiencia inicial pequeña.
> Nota metodológica: las cifras de eCPM son rangos variables por vertical, formato, país y estacionalidad; NO se fabrican números. Cuando una fuente da un rango, se refleja como rango. Varias fuentes provienen de blogs/foros (confianza media/media-baja) y se marcan como tal.

## Current State

En 2026 el ecosistema de monetización por anuncios para apps Android sigue girando alrededor de **Google AdMob** como punto de entrada por defecto (gratuito, integrado con Play y con los SDK de Google Mobile Ads / GMA Next-Gen), con **mediación (mediation / in-app bidding)** como capa estándar para agregar demanda de varias redes (AppLovin MAX, Unity LevelPlay —antes ironSource—, etc.). Los formatos vigentes de AdMob son: **banner, interstitial, rewarded, rewarded interstitial (en migración al SDK Next-Gen), native (incl. Native Advanced) y app open**.

Para una app tipo **tracker (utilidad, no juego)**, el consenso de las guías es evitar formatos disruptivos en flujos de uso intensivo y apostar por (a) **banner discreto en zona baja/no interactiva**, (b) **rewarded ads opcionales** para desbloquear funciones (por ejemplo, sincronización extra, estadísticas, temas), y (c) **native ads** integradas en feeds/listas (p. ej. recomendaciones de juegos), cuidando que no imiten elementos de UI reales. Los **interstitials** son viables pero con frecuencia limitada; los **app open ads** deben mostrarse solo cuando sea apropiado para no degradar la primera impresión.

El modelo dominante de ingresos en 2026 sigue siendo **freemium** (base gratuita con anuncios + capa premium): una fuente de benchmark afirma que las apps freemium concentran ~90% de los ingresos del mercado móvil frente a ~3% de las apps de pago. Para utilidades de nicho, la recomendación repetida por analistas es **suscripción freemium con prueba** o, alternativamente, **compra única "quitar anuncios"** como puente simple, aunque el potencial de ingresos de un "remove ads" puntual es bajo con audiencia pequeña.

En cumplimiento, son obligatorios: **UMP (User Messaging Platform)** de Google para el consentimiento GDPR/CCPA en la UE/regiones aplicables (formulario certificado conforme a IAB TCF), **Play Billing Library 8** para cobros (con fecha límite de migración en 2026), y el respeto a las políticas de AdMob sobre clics accidentales, tráfico no válido y apps infantiles (COPPA/Families).

## Key Evidence

| Dato | Valor | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| eCPM medios por región (apps) | US/Canadá ~$6.20; Europa ~$4.80; APAC ~$4.30 | 2023 (publ. feb-2025) | "By 2023, app publishers in the US and Canada enjoyed eCPM rate – $6.20, in Europe – $4.80 and Asia-Pacific – $4.30." | Media | [^1] |
| eCPM por formato (Tier 1) | Banner $0.50–$1.50; interstitials desde ~$5.00 (rango truncado en la fuente) | sept-2025 | "Realistic eCPM expectations vary dramatically by format and region. Tier 1 countries see banner eCPMs of $0.50-$1.50, interstitials at $5.00-$ ..." | Media-baja (rango incompleto) | [^2] |
| eCPM por país (ejemplos bajos) | Argelia $0.35; Angola $0.15; Antigua y Barbuda $0.31 | 2024 (tabla "2025") | "Admob eCPM Rates by Country 2025 ; Algeria, $0.35 ; American Samoa, $0.05 ; Andorra, $0.05 ; Angola, $0.15 ; Antigua and Barbuda, $0.31." | Baja (agregador) | [^3] |
| Benchmark de monetización de anuncios | Informe anual con eCPM por país y por formato | 13-ago-2026 | "Ad Monetization Benchmark Report 2026. Get eCPM, ad monetization data and insights by country and ad format, so you can maximize revenue." | Media | [^4] |
| Fuente de benchmarks alternativa | eCPM medios de miles de millones de impresiones, por país | jul-2024 | "The Appodeal Benchmarks represent the average eCPMs from billions of ad impressions served during the analyzed period for each country worldwide" | Media | [^5] |
| Formato rewarded (definición oficial) | Mecánica de recompensa en 4 pasos | s.f. (acceso 2026) | "The rewarded ad funnel has four steps: the ad prompt, the ad impression, the ad reward, and the moment the item is used." | Alta (oficial AdMob) | [^6] |
| Rewarded ads (definición SDK) | Recompensas por ver vídeo/playable/encuesta | s.f. (acceso 2026) | "Rewarded ads offer users in-app items for interacting with video ads, playable ads, and surveys." | Alta (oficial) | [^7] |
| Interstitial + pantalla previa | La pantalla previa no debe incitar clics accidentales | 04-abr-2017 | "The only requirement is that the screen doesn't contain any elements that might encourage users to accidentally click on the interstitial ad" | Alta (blog oficial Google) | [^8] |
| Causas de suspensión (tráfico no válido) | Autoclics, usuarios que repiten clics, instalaciones incentivadas indebidas | s.f. | "Common reasons AdMob accounts are suspended for invalid traffic · Clicking the ads on your own app · One or more users repeatedly clicking the ads on your app · Ad ..." | Alta (ayuda oficial) | [^9] |
| Sanción por tráfico no válido | Ad serving desactivado en todo el contenido durante un periodo fijo | s.f. | "If your account was suspended due to invalid traffic, ad serving has been turned off on all of your content for a fixed period (most frequently ...)" | Alta (blog oficial) | [^10] |
| Uso de anuncios de prueba | Usar test ads durante desarrollo bajo riesgo de suspensión | s.f. | "When building and testing your apps, make sure you use test ads rather than live, production ads. Failure to do so can lead to suspension of ..." | Media (foro, cita de doc) | [^11] |
| Origen del tráfico no válido | Alta densidad de anuncios, interstitials inesperados, banners mal colocados | s.f. | "Invalid traffic comes mostly from Accidental clicks, it happens due to high ad density, unintentional or unexpected Interstitial ads, Banner or ..." | Baja (Reddit) | [^12] |
| Regla de colocación de banner | No colocar banners junto a botones interactivos | 2019 | "To avoid accidental clicks, banner ads should not be placed next to interactive buttons, such as a 'next' button or a custom app menu bar, next ..." | Media-baja (foro dev) | [^13] |
| Play Billing v8 (fecha límite) | Deadline doc. 31-ago-2026; email 1-nov-2026 | sept-2026 | "Google's documentation puts the deadline at 31 August 2026. The warning email landing in developer inboxes puts it at 1 November 2026. Both are ..." | Media | [^14] |
| Play Billing v8 (obligación) | Nuevas apps/actualizaciones requieren v8; extensión hasta 1-nov-2026 | 31-ago-2026 | "new apps and updates need version 8 of the Play Billing Library, with an extension available until 1 November 2026." | Media | [^15] |
| Comisiones Google Play (suscripciones) | Fee dividido en 10% servicio + 5% facturación (≈15% para la mayoría) | 25-jun-2026 | "Google Play subscription fees now split into a 10% service fee plus a 5% billing fee, still 15% for most apps." | Media | [^16] |
| Cuándo evaluar LevelPlay | Para estudios gaming con >100K DAU | s.f. | "... AdMob Mediation or AppLovin MAX. Both support in-app bidding from major demand sources. If you are a gaming studio doing over 100K DAU, evaluate LevelPlay ..." | Media-baja (blog) | [^17] |
| Panorama de mediación | El grueso de publishers usa intermediarios de mediación | dic-2025 | "most publishers distribute their ad inventory through mediation platforms such as AppLovin MAX, Google AdMob or Unity LevelPlay to multiple ..." | Media | [^18] |
| Peso del freemium | ~90% de los ingresos; apps de pago ~3% | 03-feb-2026 | "Freemium apps dominate mobile app revenue globally, contributing about 90% of total revenue, while paid apps remain a niche, used by only 3% ..." | Media | [^19] |
| Recomendación modelo (no gaming) | Freemium con suscripción + prueba de 7 días | 26-may-2026 | "For most non-gaming consumer apps in 2026, a freemium subscription with a 7-day free trial is the best starting point." | Media-baja (blog) | [^20] |
| Consentimiento UMP/GDPR | UMP SDK integrado en GMA, formulario conforme IAB TCF | s.f. | "Googlov odgovor je User Messaging Platform (UMP) SDK, sloj privolitve, ki je priložen Google Mobile Ads SDK. Prikaže obrazec privolitve, skladen z IAB TCF ..." (trad.: la respuesta de Google es el SDK UMP, capa de consentimiento adjunta al GMA SDK; muestra un formulario conforme IAB TCF) | Media | [^21] |
| Consentimiento en pruebas | Hay que consentir a todos los partners publicitarios | s.f. | "When testing, you must consent to all ad partners. For more details, see Adding ad partners to published European regulations messages." | Alta (oficial) | [^22] |
| Límite práctico rewarded (consulta) | Se cuestiona >30 rewarded/usuario/día con 60s entre ads | s.f. | "Would showing more than 30 rewarded ads per user per day, with a 60-second interval between each ad, violate any of AdMob's guidelines" | Media-baja (foro oficial) | [^23] |
| eCPM bajo reportado en países Tier 1 | eCPM "muy bajo" en USA/UK/Europa/Japón (queja recurrente) | 29-ago-2024 | "Ecpm very low in big countries like USA ,UK, Europe ,Japan and average in uae countries and nigeria." | Baja (foro) | [^24] |
| Formatos/mediación (lista) | AppLovin MAX soporta 25+ redes; LevelPlay = antigua ironSource | 27-jul-2026 | "Unity LevelPlay, Best for maximizing ad earnings. AppLovin MAX is an in-app ad mediation platform. AppLovin MAX supports 25+ SDK ad networks ..." | Media-baja | [^25] |
| AppLovin MAX (modelo) | Subasta unificada con pujas simultáneas | s.f. | "AppLovin MAX runs a unified auction where all networks bid simultaneously. Unity LevelPlay is the mediation product formerly marketed under the ironSource brand ..." | Media-baja | [^26] |
| Rewarded interstitial (migración) | Existe migración a GMA Next-Gen SDK para este formato | s.f. (acceso 2026) | "Migrate your existing rewarded interstitial ad implementation to GMA Next-Gen SDK for Android." | Alta (oficial) | [^27] |

[^1]: Business of Apps — Mobile Advertising Rates (2025). 27-feb-2025. https://www.businessofapps.com/
[^2]: Playwire — AdMob eCPM Benchmarks: What Publishers Should Expect. 17-sep-2025. https://www.playwire.com/
[^3]: SR Zone — Admob eCPM Rates by Country 2025. 01-ene-2024. https://www.thesrzone.com/
[^4]: Tenjin — Ad Monetization Benchmark Report 2026. 13-ago-2026. https://tenjin.com/
[^5]: Appodeal — Mobile ECPM Report / Appodeal Benchmarks. 05-jul-2024. https://appodeal.com/
[^6]: Google AdMob — Rewarded Ads Playbook. s.f. https://admob.google.com/home/resources/rewarded-ads-playbook
[^7]: Google — Rewarded ads | Android (developers.google.com/admob). s.f. https://developers.google.com/admob/android/rewarded
[^8]: Google (blog) — Using Splash Pages to Avoid Unexpected Launch Interstitials. 04-abr-2017. https://blog.google/
[^9]: Google AdMob Help — Invalid activity: Suspended account. s.f. https://support.google.com/admob/answer/6213019
[^10]: Google AdMob Blog — Understanding account suspensions due to invalid traffic. s.f. https://blog.google/products/admob/understanding-account-suspensions-due-invalid-traffic
[^11]: Stack Overflow — Will my AdMob account be suspended if I show real ads (cita de doc oficial). s.f. https://stackoverflow.com/questions/58620277/
[^12]: Reddit r/admob — Account Suspension invalid traffic. s.f. https://www.reddit.com/r/admob/comments/15r65u9/account_suspension_invalid_traffic
[^13]: B4X Community — Android Question: policy violations. 27-jun-2019. https://www.b4x.com/
[^14]: Foresight Mobile — Google Play Billing Library 8 Migration 2026: What Breaks. 01-sep-2026. https://foresightmobile.com/
[^15]: Cleeng Blog — Google Play Billing, Done Right: 2026 Guide. 31-ago-2026. https://blog.cleeng.com/
[^16]: Adapty — What Google Play's new billing rules mean for subscriptions. 25-jun-2026. https://adapty.io/
[^17]: Kanopy Labs — Mobile App Monetization Strategies That Actually Work 2026. s.f. https://kanopylabs.com/
[^18]: Aarki — Solving the Supply Commodification Problem. 12-dic-2025. https://www.aarki.com/
[^19]: Mirava — Freemium vs Paid Apps: Revenue Benchmarks by Region. 03-feb-2026. https://www.mirava.io/
[^20]: Catdoes — 9 App Monetization Strategies (2026). 26-may-2026. https://catdoes.com/
[^21]: FlexyConsent — Google UMP SDK: implementacija privolitve GDPR. s.f. https://flexyconsent.com/
[^22]: Google — Troubleshoot privacy settings (AdMob). s.f. https://developers.google.com/admob/ios/ad-inspector/troubleshoot-privacy-settings
[^23]: Google AdMob Help (foro) — Inquiry About Limitations for Showing Rewarded Ads. s.f. https://support.google.com/admob/thread/276102829
[^24]: Google AdMob Help (foro) — eCPM very low in big countries like USA, UK, Europe, Japan. 29-ago-2024. https://support.google.com/admob/thread/293659061
[^25]: Indie Media Club — 10 Best Mobile Advertising Platforms In 2026. 27-jul-2026. https://indiemedia.club/
[^26]: NewAgeSysIT — App Monetization Integration: RevenueCat, Stripe & AdMob. s.f. https://newagesysit.com/
[^27]: Google — Migrate rewarded interstitial ads | Android (GMA Next-Gen SDK). s.f. https://developers.google.com/admob/android/next-gen/migration/migrate-rewarded-interstitial

## Tensions & Counter-arguments

1. **"Los anuncios no degradan la UX" vs. la realidad de las utilidades.** Los formatos con mejor eCPM (interstitial, rewarded, app open) son precisamente los más intrusivos. En un tracker de uso repetido (pocos segundos por sesión), un interstitial o app open frecuente destruye la retención. La tensión es directa: **ingresos por impresión vs. retención a largo plazo**, que en una audiencia pequeña pesa más que el eCPM marginal.

2. **Los números de eCPM son heterogéneos y poco fiables.** Las fuentes consultadas discrepan fuertemente y mezclan años distintos: una da ~$4.80 de eCPM medio en Europa (año 2023) [^1]; otra da banner Tier 1 de solo $0.50–$1.50 [^2]; los agregadores por país incluyen valores de céntimos en mercados no Tier 1 [^3]. Además, hay quejas recurrentes de eCPM "muy bajo" incluso en USA/UK/Europa/Japón [^24]. Conclusión: **no tratar ninguna cifra de eCPM como garantía**; son orientativas y volátiles. La fuente más útil para planificar es un benchmark por formato/país de 2026 [^4], y aun así los rangos son amplios.

3. **Mediación: ¿cuándo merece la pena?** Para una app de nicho con pocas impresiones, la mediación avanzada (AppLovin MAX, Unity LevelPlay) suele ser **prematura**: el coste de integración/complejidad no se amortiza con volúmenes bajos, y LevelPlay se recomienda explícitamente para estudios gaming con >100K DAU [^17]. Para GameVision, empezar con AdMob simple (y, si acaso, mediación ligera dentro de AdMob) es el camino pragmático; reevaluar solo al superar umbrales de DAU/impresiones.

4. **Freemium vs. "quitar anuncios" (pago único).** El freemium domina el mercado (~90% de ingresos) [^19] y analistas recomiendan suscripción + prueba de 7 días para apps no gaming [^20]. Sin embargo, para una utilidad pequeña la suscripción puede generar muy pocas conversiones, mientras que un "remove ads" único es más fácil de vender pero de ingreso mínimo. La tensión: **predictibilidad/recurrencia (suscripción) vs. simplicidad y menor fricción (pago único)**. Un enfoque híbrido (anuncios + remove ads barato + premium de funciones) suele ser el compromiso realista.

5. **Cumplimiento como riesgo existencial, no trámite.** Google sanciona con desactivación total de ad serving por tráfico no válido [^10], y las causas incluyen autoclics, clics repetidos de usuarios y errores de implementación [^9][^12]. En apps pequeñas el tráfico no válido puede originarse en **malas colocaciones (banners junto a botones) [^13]** o interstitials inesperados [^12]. Además es obligatorio el consentimiento vía UMP en la UE [^21][^22] y el uso de anuncios de prueba en desarrollo, so pena de suspensión [^11]. A esto se suma que **Play Billing v8** es exigido en 2026 [^14][^15] y la estructura de comisiones cambió (~15% en suscripciones) [^16].

6. **Riesgo de "inventar" cifras.** Este informe evita dar un eCPM "esperado" único para GameVision: no existe un dato fiable y específico del vertical "tracker de videojuegos" con audiencia pequeña. Cualquier proyección de ingresos debe presentarse como escenario (p. ej. con rangos de eCPM baja/media) y no como certeza.

### Recomendación preliminar (para validar con el resto del dossier)
- **Fase 1:** AdMob simple con **banner discreto** + **native/rewarded opcional**; nada de app-open/interstitial agresivo. UMP configurado desde el día uno.
- **Fase 2 (si crece):** añadir **"quitar anuncios" (pago único)** vía Play Billing v8 como primer flujo de pago; medir conversión.
- **Fase 3 (escala):** evaluar **suscripción premium** y **mediación** solo al alcanzar umbrales de impresiones/DAU.
- **Nunca:** ocupar pantallas de carga con anuncios que inciten clics, mezclar anuncios con botones de UI, ni usar IDs de producción en pruebas.
