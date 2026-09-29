# GameVision — Dive 02: Onboarding y vinculación de Steam (evidencia + blueprint)

**Fecha:** 2026-09-29 · **Autor:** investigador UX/Producto (subagente) · **Ámbito:** app Android nativa (Kotlin + Jetpack Compose), lanzamiento en Google Play.
**Método:** 24 búsquedas web distintas (`web_search`) + descarga directa de fuentes primarias (docs de Valve, Chrome for Developers, Android Developers, Play Console Help, NN/g, Archivo de Material Design, Apple HIG vía archivo, Page Flows, Zendesk API, Discourse API). Citas textuales cortas en el idioma original; análisis en español; términos técnicos en inglés.

---

## Resumen ejecutivo

1. **El onboarding de GameVision debe ser corto, skippable y con "contenido de valor" antes que "tutorial".** Las tres guías autorizadas coinciden: NN/g recomienda *"avoid creating app onboarding whenever possible"* y, si se hace, mantenerlo breve, opcional y explicando *para qué* se piden datos; Material Design (archivo) propone el modelo **Self-Select** precisamente para apps que "can be customized" y tienen "setup and consent requirements"; Apple HIG pide *"Get to the action quickly"* y *"give people a way to skip"*. (Confianza **Alta**.)
2. **Referencia práctica: Strava (Android, flujo grabado en nov. 2024) completa su onboarding en ~1:47 y 20 pantallas**, con una estructura clara: cuenta → "Tell us about yourself" → 1 pregunta de personalización → find friends → notificaciones → paywall de prueba → welcome → *onboarding tasks* → Home. Es decir: incluso los líderes aceptan bastantes pasos, pero **cada paso desbloquea valor inmediato** y el "tutorial" se sustituye por *tasks* post-onboarding. (Confianza **Alta** para la secuencia; fuente Page Flows.)
3. **El momento de mayor valor para GameVision es importar la biblioteca de Steam, y ese es exactamente el patrón de la industria**: Trakt anunció su importer oficialmente y *"New members will see the importer as part of the website welcome wizard"*; Letterboxd documenta 3 formas de importar (cuenta/lista/watchlist) con formatos CSV/XML. (Confianza **Alta**.)
4. **Técnicamente, la vinculación correcta es Steam OpenID 2.0 dentro de Chrome Custom Tabs (o Auth Tab en Chrome 137+), nunca WebView.** Valve documenta el flujo web (login en `steamcommunity.com` → retorno con OpenID → SteamID64) y Google Play penaliza/desaconseja el OAuth por WebView (*"Since the release of Chrome Custom Tabs, Google has recommended that developers move away from using WebViews for authentication"*). AppAuth-Android añade: *"WebView is explicitly not supported due to usability and security reasons"*. Retorno por **App Links** verificados (`assetlinks.json`, `autoVerify`). (Confianza **Alta**.)
5. **El mayor riesgo de fricción es el perfil privado**: desde 2018 Steam pone **"Game details" en "Friends Only" por defecto** y la API `GetOwnedGames` solo devuelve datos *"if their owned games/game details are visible to you"*. Exophase lo resume: *"Most services opt to hide your gaming activity by default. So, you'll need to make sure everything is public."* → GameVision necesita una **pantalla guiada de "perfil privado"** con instrucciones paso a paso y un fallback manual (Backloggd todavía no tiene import de Steam; la demanda existe). (Confianza **Alta/Media**.)
6. **Consentimiento**: presentar en la app, justo antes de la acción, con opción de cancelar/rechazar y lenguaje claro (Play: *"clear and friendly language, such as 'Agree'"*, *"Give the user an option to decline"*). El copy debe enfatizar: solo lectura, sin contraseñas, revocable. (Confianza **Alta**.)

---

## Evidencia

### A) Onboarding en apps móviles de tracking/social (2024–2026)

**Confianza del bloque: Alta** en guías autorizadas y flujo Strava; **Media** en descripciones de terceros.

#### A.1 Guías autorizadas

**Nielsen Norman Group — "Mobile-App Onboarding: An Analysis of Components and Techniques" (21/06/2020)**
URL: https://www.nngroup.com/articles/mobile-app-onboarding/
- *"We recommend professionals avoid creating app onboarding whenever possible and instead spend your resources making the UI more usable."*
- Solo hay tres situaciones en las que el onboarding aporta: *"You need user information to get started"* / *"The application functionality is highly tailored to the user's context and preferences"* / *"Important app features or workflows are fairly unique to the app"*. (GameVision encaja en la 2ª: personalización por plataformas/biblioteca.)
- Tres componentes típicos: *"feature promotion, customization, and instructions"*.
- Sobre **feature promotion** (carruseles de "mira lo que hacemos"): *"Avoid feature-promotion onboarding at first launch. Users rarely download an app for no reason; therefore, lengthy promotional onboarding will likely be skipped."*
- Sobre **customization** (el componente relevante para GameVision): *"Content customization can create a relevant experience and is more likely to be appropriate for initial app onboarding."* + *"When prompting users to customize their experience, keep it brief. Explain why you want that data and how it will be used"* + en el ejemplo de Fitplan destaca: *"an option to Skip it was available, along with a progress indicator"*.
- Sobre **visual-design customization** (elegir tema/color en onboarding): *"doesn't belong in onboarding"*.
- Sobre **decks of cards** (tutoriales tipo carrusel instructivo): *"We don't recommend deck-of-cards onboarding"*; si se usa: *"ensure there's a highly visible Skip option"*.

**NN/g — "Progressive Disclosure" (03/12/2006, principio vigente)**
URL: https://www.nngroup.com/articles/progressive-disclosure/
- *"Initially, show users only a few of the most important options. Offer a larger set of specialized options upon request."* → Es la base del **progressive profiling**: pedir en onboarding solo lo mínimo; el resto, en contexto.

**Material Design (guía oficial de Google, archivada — sigue siendo la única guía "oficial" de onboarding de Material; MD3 no define un patrón específico de onboarding)**
URL (archivo): https://web.archive.org/web/2021/https://material.io/design/communication/onboarding.html
- *"Onboarding is a virtual unboxing experience that helps users get started with an app."*
- *"Onboarding is one point in a longer journey that begins in the app store and ends with the user taking the first key retention-correlated action in your app."*
- *"Show onboarding to first-time users. Don't show it to returning users."*
- Tres modelos: **Self-Select** (*"Allow users to customize their experiences"*), **Quickstart** (*"Start the user directly in the app"*), **Top User Benefits** (*"Display a carousel or a brief animation highlighting benefits"*).
- Cuándo usar **Self-Select**: *"The UI can be customized"*, *"Your app has setup and consent requirements"*, *"You've already identified the behaviors that correspond to increased engagement (in the first session) or increased retention (in the first seven days)"*.
- Regla de combinación: *"Don't combine Self Select with Top User Benefits"*.
- El modelo Self-Select describe: *"a short series of choices… provides implicit education, giving the user a sense of control and vested interest in the screens to come"* (y muestra pantalla de sign-in).

**Apple Human Interface Guidelines — Onboarding (versión estática iOS, archivo 2021/2022; los principios se mantienen en la HIG actual)**
URL (archivo): https://web.archive.org/web/2021id_/https://developer.apple.com/design/human-interface-guidelines/ios/app-architecture/onboarding/
- *"An optional onboarding experience that's fast, fun, and educational can help people get the most from your app without getting in their way."*
- *"Provide onboarding that helps people enjoy your app, not just set it up… Avoid including setup or licensing details in your onboarding experience."*
- *"Get to the action quickly… If you need to provide tutorials or intro sequences, give people a way to skip them and don't automatically show them when people return."*
- *"Stick to the essentials in tutorials. It's fine to provide guidance for beginners, but education isn't a substitute for great app design."*
- *"Make learning fun and discoverable. Learning by doing is a lot more fun and effective than reading a list of instructions."*

#### A.2 Casos reales

**Strava (Android) — flujo de onboarding grabado (versión nov. 2024), fuente Page Flows**
URL: https://pageflows.com/post/android/onboarding/strava/
Timeline exacto del video (20 pantallas, ~1:47 hasta Home):
- 00:04 Splash · 00:07 Sign up · 00:09 Enter email · 00:18 Set password · 00:25 Captcha
- 00:32 **"Tell us about yourself"** · 00:33 name · 00:42 D.O.B. · 00:54 gender
- 00:59 **"Onboarding question"** (1 sola pregunta de personalización)
- 01:24 Find friends · 01:26 Enable notifications · 01:30 Start trial · 01:33 Subscribe
- 01:37 Welcome · 01:41 **Onboarding tasks** · 01:47 Home
Lectura UX: estructura "cuenta → perfil mínimo → 1 pregunta → social → permisos → monetización → tasks de activación". La educación de producto se entrega como *checklist* post-onboarding (no como tutorial). Mobbin confirma la variante iOS: *"The user goes through an onboarding process, creates an account, and subscribes to the service. The user is then guided through the app's main features."* (URL: https://mobbin.com/explore/flows/a1cc2697-2224-4e70-b255-75b38a1748d9) y Android: *"Onboarding highlights key features, followed by account creation and profile setup. The user then lands on the home screen."* (URL: https://mobbin.com/explore/flows/1805e163-e80b-4bb3-9234-723e53a01c36)

**Trakt — onboarding web con importer integrado**
El anuncio oficial del importer añade: *"New members will see the importer as part of the website welcome wizard."* (URL: https://forums.trakt.tv/t/import-from-imdb-letterboxd-tv-time-csv-or-json-files/32483). Es decir: **la importación de datos forma parte del onboarding**, no es una feature escondida en ajustes.

**Letterboxd — importación como flujo de primera clase**
Documentación oficial: https://letterboxd.com/about/import/
- *"Letterboxd members can import data in one of three ways: directly to their account (as watched films and/or diary entries, with optional ratings, reviews and tags), to a new or existing list, or to their watchlist."*
- Formatos: CSV propio, export de IMDb (CSV), export de Delicious Library (XML), export de iCheckMovies. Descarta pedir a mano lo que se puede importar.

**Nota sobre IMDb / Untappd / MyAnimeList (evidencia parcial):** la búsqueda no encontró teardowns fiables y recientes de sus onboardings de app en fuentes citables; MAL sí es citable por su página de login/import (ver C). No se incluyen afirmaciones no verificadas sobre sus flujos para no inventar. (Confianza **Baja** en esta subsección; sin impacto en el blueprint.)

**Métricas de "time to value" (definiciones citables)**
URL: https://userpilot.com/blog/time-to-value/
- *"Time to value is only a useful metric when you've defined what 'first value' actually means for your specific product."*
- *"Time to first value: is how long it takes users to experience their initial win (activation)."*
→ Para GameVision: *first value* = Home con biblioteca visible (importada o mínima) + primera puntuación; objetivo: **< 2 minutos** desde el primer launch (ver blueprint).

---

### B) Vincular Steam desde apps Android de terceros

**Confianza del bloque: Alta** en documentación técnica (Valve, Chrome, Android, Play Console); **Media** en prácticas observadas de apps de terceros (Exophase, Backloggd) y en "no existe botón/scopes OAuth como en otros proveedores" (análisis).

#### B.1 Cómo lo documenta Valve (fuente primaria)

**Steam Web API docs (público)** — https://steamcommunity.com/dev
- *"Steam can act as an OpenID provider. This allows your application to authenticate a user's SteamID without requiring them to enter their Steam username or password on your site (which would be a violation of the API Terms of Use.)"*
- *"Just download an OpenID library… and use https://steamcommunity.com/openid as the provider. The returned Claimed ID will contain the user's 64-bit SteamID. The Claimed ID format is: https://steamcommunity.com/openid/id/<steamid>"*
- *"If you are using OpenID on your site, we request that you use one of the [provided Steam] buttons as your link to the Steam sign in page."* ← requisito de marca/confianza para el CTA "Conectar Steam".

**Steamworks — User Authentication and Ownership** — https://partner.steamgames.com/doc/features/auth
- *"Steam is an OpenID Provider, as described in the OpenID 2.0 specification."*
- Flujo web textual: *"When using OpenID, the user begins in a web browser at the third-party website. When the user wishes to login/link their account to that website, using OpenID, the site directs the user to a login form on the Steam Community website. Once the user has entered their Steam login credentials, the user's web browser is automatically redirected back to the 3rd party website with some additional OpenID specific data appended to the return URL."*
- OP Endpoint: `https://steamcommunity.com/openid/` (Claimed ID: `http://steamcommunity.com/openid/id/<steamid>`).
- Vinculación de cuentas: *"A user's SteamID can be securely retrieved either in-game or through a web browser and once the initial association has occurred, you can safely allow access to the 3rd party account by merely verifying a user's SteamID."* → clave: **el login OpenID se hace una sola vez**; las re-sincronizaciones usan el SteamID ya vinculado.

**Ownership & biblioteca** — https://partner.steamgames.com/doc/webapi/IPlayerService
- `GetOwnedGames`: *"Returns a list of games owned by the player if their owned games/game details are visible to you."* Parámetros: `include_appinfo` (nombre/iconos), `include_played_free_games` (los free-to-play se excluyen por defecto), `appids_filter`.
- Campos de respuesta (fuente: https://wiki.teamfortress.com/wiki/WebAPI/GetOwnedGames): `appid`, `name`, `playtime_2weeks`, `playtime_forever` (**minutos**), iconos. Este es exactamente el dataset de "juegos + horas" de GameVision.

**Resolución de vanity URL (fallback para entrada manual)** — https://partner.steamgames.com/doc/webapi/ISteamUser
- `ResolveVanityURL` (`url_type` 1 = perfil individual) → convierte `steamcommunity.com/id/<vanity>` en SteamID64. Solo necesario si se ofrece pegar la URL del perfil como alternativa (no necesario con OpenID).

#### B.2 Android: cómo se hace un login web correctamente (fuente primaria)

**Chrome Custom Tabs — Overview** — https://developer.chrome.com/docs/android/custom-tabs/overview
- *"Custom Tabs offer a better user experience than opening an external browser… They allow users to remain within the app while browsing… They accomplish this by being powered directly by the user's preferred browser, and automatically sharing the state and features offered by it. You don't need to write custom code to manage requests, permission grants, or cookie stores."*

**Chrome Auth Tab (Chrome 137+, 2025)** — https://developer.chrome.com/docs/android/custom-tabs/guide-auth-tab
- *"Auth Tab provides a secure and simplified authentication flow for use in Android apps… The tab is stripped down and has limited capabilities, enabling users to focus on the task at hand."*
- *"From Chrome 137, Auth Tab can directly replace existing Custom Tabs authentication integrations. For users whose devices don't support Auth Tab, fallback to Custom Tabs is automatic."*
- Retorno: *"For redirects using the https schema, the browser verifies that the redirect domain and client app are owned by the same publisher using Digital Asset Links."* ← refuerza usar **https redirect + App Links** en lugar de custom scheme.

**Google Play — política/seguridad para SDKs (WebView vs Custom Tabs)** — https://support.google.com/googleplay/android-developer/answer/14514531
- *"Since the release of Chrome Custom Tabs, Google has recommended that developers move away from using WebViews for authentication. Using OAuth for authentication in a WebView can make apps that use your SDK susceptible to security problems and hurt usability by disconnecting the user from single sign-on sessions. Chrome Custom Tabs mitigate these issues."*

**AppAuth for Android (OpenID Foundation)** — https://github.com/openid/AppAuth-Android
- *"The library follows the best practices set out in RFC 8252 - OAuth 2.0 for Native Apps, including using Custom Tabs for authorization requests. For this reason, WebView is explicitly not supported due to usability and security reasons."*

**Android App Links (retorno a la app)** — https://developer.android.com/training/app-links/verify-android-applinks
- Verificación automática vía `autoVerify="true"` + `https://<host>/.well-known/assetlinks.json`; verificación manual con `adb shell pm verify-app-links --re-verify <package>`. Es el mecanismo estándar para que el `return_to` de OpenID vuelva a la app sin interstitial del navegador.

#### B.3 Prácticas observadas en apps de terceros

**Exophase (tracker multi-plataforma con app/PWA)** — https://www.exophase.com/faq/
- Basado en **perfiles públicos** vinculados por servicio (no pide contraseñas): *"All of the platform logins we use are officially supported by the respective platform holders, and do not transmit any passwords back to our servers."*
- Documenta el caso de fallo típico: *"Help! The site isn't tracking my activity. Typically this is due to incorrect privacy settings on the account. Most services opt to hide your gaming activity by default. So, you'll need to make sure everything is public."*
- Ojo: Exophase **no publica app en Play/App Store** ("currently not on the Apple App Store or Google Play Store"), ofrece PWA. → Nicho libre para GameVision.

**Backloggd (tracker social de videojuegos)** — https://backloggd.com/
- No tiene import de Steam (a mayo 2025): comentario de usuario en su web: *"cant wait till we can import our steam libraries guys"*. → La demanda existe y **es un diferenciador real para GameVision**.

**Completionist.me / Steam Hunters:** accesibles solo parcialmente (Cloudflare). Su modelo conocido y verificable por documentación genérica de Steam: perfil público → tracking por SteamID (mismo patrón Exophase). No se cita más para no especular.

**Riesgo/legado (análisis, confianza Media-Baja):** Steam usa **OpenID 2.0**, una especificación antigua (la propia industria migró a OpenID Connect: https://openid.net/ ). Aun así, Valve lo mantiene documentado y es *el* mecanismo oficial; no existe OAuth 2.0 de Steam para terceros. No existen "scopes" ni una página de "apps conectadas" con revocación granular como en Google: el consentimiento es binario (login) y **la revocación debe vivir en GameVision** (botón Desconectar + borrado). Marcar como observación de diseño.

---

### C) Patrones de consentimiento/privacidad para "conecta tu cuenta"

**Confianza del bloque: Alta.**

**Google Play — "Best practices for prominent disclosure and consent"** — https://support.google.com/googleplay/android-developer/answer/11150561
- *"Present the disclosure to the user in the app, right before requesting permission or capability. The message cannot be in the app description or website."*
- *"Give the user an option to decline providing consent. Always provide an option to cancel the flow related to permissions."*
- *"Require the user's explicit consent using clear and friendly language, such as 'Agree' rather than 'Allow access'… or 'Got it' (this is too casual)."*
- *"If the user denies or revokes permission that a feature needs, gracefully degrade your app while enabling your user to continue using your app."*
- Además, la política de User Data de Play exige **privacy policy enlazada en la ficha y dentro de la app** y la sección **Data safety** completada.

**Señales de confianza de servicios reales:**
- MyAnimeList (página de login): *"Beware of phishing sites pretending to be MAL. Always check the domain is myanimelist.net before entering your password."* — https://myanimelist.net/import.php … el propio MAL educa al usuario a mirar el dominio. GameVision debe hacer lo mismo en positivo: *"Tu contraseña de Steam se escribe SIEMPRE en steamcommunity.com"*.
- Valve prohíbe pedir credenciales fuera de Steam (ver B.1: *"which would be a violation of the API Terms of Use"*). Esto debe reflejarse en el copy de consentimiento.
- Letterboxd demostró que la privacidad granular genera confianza en apps de tracking: modos por entrada **Anyone / Close Friends / You / Draft**, con nota explícita: *"Ratings on Close Friends and You entries don't contribute to stats."* y un **default configurable por cuenta** (fuente: página de ayuda "Importing data"/diálogos de la propia app, https://letterboxd.com/about/import/). → Aplicable a GameVision: visibilidad por defecto de la biblioteca + opción "ocultar juego".

**Requisitos mínimos del consentimiento para GameVision (síntesis):**
1. En pantalla propia, justo antes de salir a Steam (no un checkbox perdido en ajustes).
2. Beneficio claro arriba, condiciones de datos después ("esto leeremos"), lenguaje llano, botón [Conectar Steam] + [Ahora no].
3. "Solo lectura", "sin contraseñas", "revocable en Ajustes", link a la privacy policy.
4. Estado post-conexión visible siempre (Ajustes → Conexiones → Steam: cuenta vinculada, última sincronización, [Desconectar]).

---

### D) Time-to-value en importaciones grandes

**Confianza del bloque: Alta** (Letterboxd/Trakt oficiales); **Media** (límites Strava, thread de bugs Trakt).

**Letterboxd — import como flujo multi-destino y multi-formato** — https://letterboxd.com/about/import/
- 3 destinos: cuenta (watched+diary, con ratings/reviews/tags opcionales), lista nueva/existente, o watchlist. Formato propio CSV, exports de IMDb (CSV), Delicious Library (XML), iCheckMovies.
- El flujo vive en una página "Import" dedicada, referenciada desde el menú de account settings ("Import & Export" tab — guía de terceros: listy.is).

**Trakt — importer anunciado con expectativas explícitas** — https://forums.trakt.tv/t/import-from-imdb-letterboxd-tv-time-csv-or-json-files/32483
- *"Easily import your watched history and watchlist from IMDB and Letterboxd… New members will see the importer as part of the website welcome wizard."*
- Soporte humano como fallback: *"If you have a more specific data issue, please email support@trakt.tv and include the files you're trying to import."*
- Guía de terceros del flujo: *"open Settings → Data, upload the ZIP and review the import results"* (moviebase.app). Y sobre Trakt: *"Select the Import section · Upload your diary.csv file · Trakt maps your ratings and watch dates"* (achriom.com). Patrón común: **upload → mapping → review de resultados**.
- Riesgo de correctitud (cita el propio foro de Trakt como señal de qué pasa cuando falla): topic *"Critical bugs in the new Letterboxd importer that corrupt watch dates and block free users, while the legacy importer is broken by Cloudflare"* — https://forums.trakt.tv/t/critical-bugs-in-the-new-letterboxd-importer-that-corrupt-watch-dates-and-block-free-users-while-the-legacy-importer-is-broken-by-cloudflare/114734 → la importación es una feature de confianza: los bugs son públicos y dañan la marca.

**Strava — comunicación de límites en cargas masivas** — https://support.strava.com/hc/en-us/articles/216917877 (artículo "Import Historical Data"; el soporte indica límites: *"Select up to 25 files at a time to upload bulk files. Athletes not subscribed to Strava can upload 15 files at a time."*) → lección: **anunciar límites/cuotas antes**, no fallar después.

**Apps de tracking tipo Slate (Letterboxd import):** promesa "sin límites" para bibliotecas grandes: *"Import your full watch history and ratings directly into Slate. No limits, even if you've logged thousands of…"* (ficha App Store, apps.apple.com). → Mensaje de escala que GameVision puede replicar ("aunque tengas miles de juegos").

**Patrón de UX emergente para imports grandes (síntesis de lo anterior):**
1. Decir **qué va a pasar y cuánto tarda** antes de empezar ("Analizaremos tu biblioteca. Tarda ~1 min. Puedes seguir usando la app").
2. Progreso **no bloqueante** + resumen final con números (importados / fusionados / omitidos).
3. **Idempotencia**: re-sincronizar no debe duplicar (Trakt/Trakt-style "import as many times as you'd like" en el mismo anuncio: *"Existing Trakt members can run the importer too and you can import as many times as you'd like."*).
4. Fallos explicados con causa accionable (Exophase: causa #1 = privacidad; Letterboxd: causa #1 = formato).

---

## Blueprint de onboarding propuesto

> Modelo recomendado: **Self-Select ligero** (Material) + **contenido, no tutorial** (NN/g) + **"get to the action quickly"** (Apple) + **importer dentro del wizard** (Trakt). Todo skippable salvo la creación de cuenta. Primera acción de valor objetivo: **Home con biblioteca visible y 1 punto de personalización ≤ 2 min**.

### Pasos (6 pantallas máx. + home)

**P1 · Bienvenida (1 pantalla, sin carrusel)**
- Qué: logo + claim + "anticipo" de la promesa (una imagen estática del Home, no un tour).
- Copy: **"Todos tus juegos. Todas tus horas. Un solo lugar."** / sub: *"Conecta Steam y arma tu biblioteca en segundos."*
- CTA: **[Empezar]**. Sin skip (es la puerta de entrada).
- Por qué: NN/g desaconseja feature-promotion al primer launch; basta una pantalla. (Evidencia A.1)

**P2 · Cuenta en 1 toque**
- Qué: Google Sign-In como primario + email como alternativa.
- Copy: *"Crea tu cuenta para guardar tu progreso."*
- Nota: nada de pedir perfil/DOB aquí (Strava lo pide, pero NN/g: pedir en contexto; para edad hay canales de store). (Evidencia A.1/A.2)

**P3 · Self-select corto (1-2 preguntas, skippable)**
- Qué: "¿Dónde juegas?" chips multi-selección: **Steam · PlayStation · Xbox · Switch · Retro/otras**. Pregunta 2 opcional: "¿Qué quieres seguir primero?" → *Horas jugadas · Logros · Backlog · Amigos*.
- Microcopy obligatorio (NN/g: explicar por qué): *"Lo usamos para ordenar tu Home y tus recomendaciones. Puedes cambiarlo luego."*
- Botón **[Omitir]** visible + indicador de progreso (NN/g cita Fitplan como buen ejemplo).
- Máximo 2 preguntas; nada de estética/tema (NN/g: visual-design customization fuera del onboarding).

**P4 · Conectar Steam (el acelerador de valor)**
- Qué: si el usuario marcó Steam (o por defecto en primera pasada), mostrar este paso como **el** atajo: 
- Copy: **"¿Importamos tu biblioteca de Steam?"** / *"En 1 minuto tendrás tus juegos y tus horas en GameVision. Solo lectura, sin contraseñas. Puedes desconectar cuando quieras."*
- CTA primario: **[Conectar Steam]** (botón oficial de Valve). Secundario: **[Añadir mis juegos a mano]**. Terciario: *"Ahora no"* (texto).
- Por qué: Trakt mete el importer en el wizard; Letterboxd tiene página de import dedicada; Backloggd no lo tiene aún (diferenciador). (Evidencia B.3/D)

**P5 · Time-to-value inmediato (resultado o semilla)**
- Si conectó: **"¡Listo! Encontramos 412 juegos y 3.924 h de juego."** + CTA **[Ver mi biblioteca]** + mini-tarea *"Marca 3 juegos como 'jugando/favoritos'"* (personalización en contexto).
- Si no conectó: pedir 3 juegos con search rápido (**"Añade 3 juegos que hayas jugado"**) para que el Home nunca esté vacío. 
- Por qué: "first value = initial win" (TTV) y contenido custom relevante (NN/g).

**P6 · Home + checklist de activación (estilo "onboarding tasks" de Strava)**
- Checklist (máx. 4, tachables, dismissible, no reaparece):
  1. ✔ Importa tu biblioteca de Steam *(si aún no)*
  2. Puntúa 1 juego
  3. Añade 1 juego a "Jugando"
  4. Sigue a 2 jugadores / mira 1 lista
- El "tutorial" del producto se sustituye por esto (evidencia A.2: Strava "onboarding tasks" como penúltima pantalla).

### Qué pedir y qué NO pedir en onboarding

| SÍ (mínimo viable) | NO (progressive profiling / contexto) |
|---|---|
| Cuenta (Google/email) | Permiso de **notificaciones** (pedir tras el primer logro/log: "¿Te avisamos cuando…") |
| Plataformas que usa (chips) | Permiso de **contactos** ("Find friends" de Strava exige permiso; hacerlo más tarde y con incentivo) |
| 1-2 intereses (qué seguir) | Fecha de nacimiento / género (solo si hay requisito legal → al final, con explicación) |
| Conexión Steam (con consentimiento) | Tema visual, avatar, bio (NN/g: no en onboarding) |
| | Tour de UI / deck-of-cards (NN/g: no recomendado) |
| | Permisos de notificación "por defecto" durante el flujo (Play: *"right before requesting"*) |

### Métricas de activación para medir el diseño
- **TTV p50** (launch → Home con ≥1 de: biblioteca importada / 3 juegos añadidos): objetivo < 2:00.
- **% skip por paso** (P3/P4) y **tasa de conexión Steam** desde onboarding vs desde Home.
- **Completion de checklist** a 7 días (material: "increased retention (in the first seven days)").

---

## Flujo de vinculación Steam propuesto

**Arquitectura elegida: Steam OpenID 2.0 en Chrome Custom Tab / Auth Tab + retorno por App Link verificado + import server-side con `GetOwnedGames`.** Nunca WebView (política Play + AppAuth + RFC 8252). (Evidencia B.1/B.2)

### Diagrama textual

```
[App: pantalla pre-consent] ──[Conectar Steam]──▶ [Chrome Custom Tab / Auth Tab]
        ▲                                             │
        │                                  usuario inicia sesión en
        │                                  steamcommunity.com (password SOLO allí)
        │                                             │
        │                                  Steam redirige (return_to) a
        │                                  https://api.gamevision.app/auth/steam/callback
        │                                             │
        │                                   [Backend GameVision]
        │                                   · valida assertion OpenID 2.0
        │                                   · extrae SteamID64 (claimed_id)
        │                                   · vincula a la cuenta GameVision
        │                                             │
        │                                   redirect a App Link
        │                                   https://gamevision.app/steam/success
        │                                             │
[App reanuda: pantalla "Conectado"] ◀─────────────────┘
        │
        ▼
[Backend: job de import] GetOwnedGames (API key, server-side)
        │  include_appinfo=1, include_played_free_games=1
        ▼
[App: progreso + resumen]  "412 juegos · 3.924 h · 87 ya en tu lista"
```

### Pantallas y copy

**1) Entry points** (los 4 lugares donde ofrecer "Conectar Steam"):
- Paso P4 del onboarding (principal, momento ideal).
- Card en **Home** para usuarios que hicieron skip ("¿Juegas en Steam? Importa tu biblioteca").
- **Empty state de "Biblioteca"**: "Tu biblioteca está vacía. ¿La importamos desde Steam?".
- **Ajustes → Conexiones → Steam**.

**2) Pantalla de consentimiento (pre-auth, en la app; Play: "right before")**
- Título: **"Conecta tu cuenta de Steam"**
- Bullets:
  - ✅ *"Leeremos: tu perfil público (nombre y avatar), tu lista de juegos y tus horas jugadas."*
  - 🚫 *"Nunca veremos tu contraseña: el inicio de sesión ocurre en steamcommunity.com, la web oficial de Steam."*
  - 🔁 *"Solo lectura. No publicamos nada en tu nombre. Puedes desconectar cuando quieras desde Ajustes."*
- CTA: **[Conectar Steam]** (usar asset oficial de Valve; ver B.1) · **[Ahora no]**

**3) Custom Tab / Auth Tab (fuera de la app, en el navegador)**
- Lanzar la URL OpenID (`openid.mode=checkid_setup`, `openid.realm=https://gamevision.app`, `openid.return_to=https://api.gamevision.app/auth/steam/callback`).
- Implementación: AndroidX Browser (`CustomTabsIntent`) hoy; migrar a **Auth Tab** cuando el target lo permita (Chrome 137+, con fallback automático). Custom tab con color de marca; **sin JS injection ni lectura de cookies** (prohibido y además no necesario).
- El usuario ve la UI de Steam (login o "Sign in" si ya tiene sesión). GameVision no toca credenciales.

**4) Retorno (deep link) y validación**
- Steam → `return_to` https en el backend. El backend **valida la assertion** con librería OpenID mantenida (nunca confiar en parámetros sin validar).
- Extrae `claimed_id` → **SteamID64**; crea el vínculo `user_id ↔ steamid`.
- Redirige a **App Link verificado** `https://gamevision.app/steam/success` (fallback: si el asset link no está verificado en ese dispositivo, el navegador abre una página puente con botón "Volver a GameVision").
- La app reanuda con deep link → pantalla "Conectado".

**5) Pantalla "Conectado" + decisión de import**
- *"Conectado como [nickname] ✔"* + *"Pulsa para importar tu biblioteca (412 juegos detectados*)."* (*contador se rellena tras la llamada server-side; si tarda, estado "Analizando tu biblioteca…").
- CTA: **[Importar ahora]** · [Ahora no].

**6) Import en curso (no bloqueante)**
- Barra de progreso indeterminada corta (la llamada es única) → luego progreso de "matching" en cliente si se hace por lotes.
- Copy: *"Estamos importando tu biblioteca. Puedes seguir usando la app; te avisamos al terminar."* (Si el permiso de notificaciones ya está concedido; si no, banner in-app.)
- Errores parciales: *"42 juegos no se pudieron emparejar con nuestro catálogo. Los añadiremos igualmente sin datos extra."*

**7) Resumen post-import (el momento "wow")**
- *"412 juegos importados · 3.924 h · 87 ya en tu lista (fusionados, sin duplicados)."*
- Acciones: **ocultar juegos concretos del perfil**, marcar backlog, ordenar por horas.
- Guardar `last_synced_at`. **Re-sync** sin OpenID (SteamID + perfil público): botón "Sincronizar ahora" + sync periódico razonable (p. ej., semanal, con throttle).

**8) Caso perfil privado (el fallo más probable)**
- Detección: `GetOwnedGames` devuelve vacío / `game_count: 0` con perfil no público.
- Pantalla guiada:
  - Título: **"Tu perfil de Steam está en privado"**
  - Texto: *"Steam oculta por defecto tus 'Detalles de los juegos' (pasó en 2018 para todos). Para importar tu biblioteca, cámbialo a Público. No hace falta hacer público nada más."* (Evidencia B.3 + ghacks).
  - Pasos numerados: 1) Abre steamcommunity.com → tu perfil → **Editar perfil** → pestaña **Privacidad**; 2) En **"Detalles de los juegos"** elige **Público**; 3) Vuelve a GameVision.
  - CTA: **[Ya lo he cambiado — Reintentar]** · secundario: **[Importar a mano mis juegos]**.
  - Nota legal/UX: dejar claro que **también pueden dejarlo "Solo amigos"** y usar GameVision sin import (graceful degradation; Play lo exige).

**9) Desconexión (revocación)**
- Ajustes → Conexiones → Steam: **cuenta vinculada · última sincronización · [Desconectar]**.
- Al desconectar, preguntar: *¿Mantener los juegos ya importados (con sus horas congeladas) o borrar todo lo importado?* → implementa el requisito Play de "revocar" y la promesa del consentimiento.

### Detalles técnicos (checklist de implementación)

- [ ] Backend: librería OpenID 2.0 mantenida (validación de `openid.sig`, `openid.claimed_id`); stateless; state/CSRF (`return_to` firmado, nonce).
- [ ] Backend: `GetOwnedGames` con **API key solo server-side** (nunca en la app), `include_appinfo=1`, `include_played_free_games=1`; normalizar `playtime_forever` (minutos → horas), cachear.
- [ ] Android: `CustomTabsIntent` (AndroidX Browser) → migrar a `AuthTabIntent` cuando disponible; `android:autoVerify` + `assetlinks.json` para `gamevision.app`; fallback puente web.
- [ ] Idempotencia del import (upsert por `appid`), timestamp, reentrada "import as many times as you'd like" (patrón Trakt).
- [ ] Data safety (Play Console): declarar "App activity → in-app actions" / datos de juego recogidos, finalidad "App functionality", opción de borrado.
- [ ] QA con bibliotecas grandes (1.000–10.000 juegos; free-to-play incluidos/excluidos; family share: `ownersteamid` indica el verdadero dueño — ver doc CheckAppOwnership).

---

## Riesgos y mitigaciones

| # | Riesgo | Evidencia | Mitigación |
|---|--------|-----------|------------|
| 1 | **Percepción de phishing** al abrir un login de Steam desde una app desconocida | MAL advierte phishing en su propio login; Valve prohíbe pedir credenciales fuera de Steam | Copy explícito ("la contraseña solo se escribe en steamcommunity.com"), Custom Tab con dominio visible, botón oficial de Valve, nunca WebView |
| 2 | **Perfil privado** → import vacío → usuario cree que la app "no funciona" | `GetOwnedGames`: *"if their owned games/game details are visible to you"*; default "Friends Only" desde 2018; Exophase: causa #1 de fallos | Pantalla guiada paso a paso + deep link a instrucciones + reintentar + fallback manual; mensaje "no hace falta hacer público nada más" |
| 3 | **Incumplimiento de políticas Play** por login en WebView | Play: *"move away from using WebViews for authentication"* | Custom Tabs/Auth Tab + App Links; revisar SDK Console |
| 4 | **OpenID 2.0 es legacy** (sin OIDC de Steam) | Valve: *"as described in the OpenID 2.0 specification"*; industria migró a OIDC | Usar librería mantenida, aislar la integración tras una capa (`SteamAuthProvider`) para poder migrar; vigilancia de docs de Valve |
| 5 | **Duplicados / datos corruptos tras import** | Thread crítico en el foro de Trakt sobre importer con bugs de fechas | Import idempotente (upsert por appid), nunca borrar datos de usuario, resumen con "fusionados", QA amplio, feature flag |
| 6 | **Bibliotecas gigantes** → tiempos o errores | Slate promete "No limits, even if you've logged thousands"; Strava comunica límites | Job asíncrono, progreso no bloqueante, límites anunciados si existen, paginación UI |
| 7 | **Privacidad de datos** (Play Data safety + GDPR) | Play: privacy policy in-app + Data safety; CP disclosure best practices | Declarar recogida (biblioteca/horas), limitar a funcionalidad, borrado in-app, no exponer biblioteca por defecto sin opt-in |
| 8 | **Spam/abuso de re-sync** (coste API) | — | Throttle por usuario, cache, sync manual limitado/día |
| 9 | **Fricción del consentimiento excesivo** | NN/g: onboarding con coste de interacción; Play: opción de declinar siempre | 1 pantalla de consentimiento (3 bullets), skippable, progressive profiling tras conectar |

---

## Fuentes

**Guías autorizadas**
1. NN/g — Mobile-App Onboarding: An Analysis of Components and Techniques — https://www.nngroup.com/articles/mobile-app-onboarding/
2. NN/g — Progressive Disclosure — https://www.nngroup.com/articles/progressive-disclosure/
3. Material Design — Communication > Onboarding (archivo 2021) — https://web.archive.org/web/2021/https://material.io/design/communication/onboarding.html
4. Apple HIG — Onboarding (versión iOS archivada, 2021/2022) — https://web.archive.org/web/2021id_/https://developer.apple.com/design/human-interface-guidelines/ios/app-architecture/onboarding/
5. Google Play Console Help — Best practices for prominent disclosure and consent — https://support.google.com/googleplay/android-developer/answer/11150561
6. Google Play SDK Console Help — SDK versions with potential policy issues (Unsafe OAuth via WebView) — https://support.google.com/googleplay/android-developer/answer/14514531

**Steam (Valve, documentación primaria)**
7. Steam Web API Documentation (OpenID provider, botones de sign-in) — https://steamcommunity.com/dev
8. Steamworks — User Authentication and Ownership (OpenID 2.0, linking, ownership) — https://partner.steamgames.com/doc/features/auth
9. Steamworks Web API — IPlayerService (GetOwnedGames) — https://partner.steamgames.com/doc/webapi/IPlayerService
10. Steamworks Web API — ISteamUser (ResolveVanityURL) — https://partner.steamgames.com/doc/webapi/ISteamUser
11. Team Fortress Wiki — WebAPI/GetOwnedGames (campos: playtime_forever, etc.) — https://wiki.teamfortress.com/wiki/WebAPI/GetOwnedGames
12. Steam Support — Steam Profile Privacy (3 estados; subcategorías) — https://help.steampowered.com/ (FAQ "Steam Profile Privacy", acceso vía Steam Support search; cita de snippet)
13. ghacks — How to change Steam Privacy settings (defaults "Friends Only"; qué incluye "Game Details") — https://www.ghacks.net/2018/04/11/how-to-change-steam-privacy-settings/

**Android / Chrome (documentación primaria)**
14. Chrome for Developers — Overview of Android Custom Tabs — https://developer.chrome.com/docs/android/custom-tabs/overview
15. Chrome for Developers — Simplify authentication using Auth Tab — https://developer.chrome.com/docs/android/custom-tabs/guide-auth-tab
16. Android Developers — Verify Android App Links — https://developer.android.com/training/app-links/verify-android-applinks
17. AppAuth for Android (OpenID Foundation) — README (RFC 8252, Custom Tabs, "WebView explicitly not supported") — https://github.com/openid/AppAuth-Android

**Productos y casos**
18. Page Flows — Strava Onboarding Flow on Android (timeline de 20 pantallas, nov. 2024) — https://pageflows.com/post/android/onboarding/strava/
19. Mobbin — Strava iOS Onboarding Flow — https://mobbin.com/explore/flows/a1cc2697-2224-4e70-b255-75b38a1748d9
20. Mobbin — Strava Android Onboarding Flow — https://mobbin.com/explore/flows/1805e163-e80b-4bb3-9234-723e53a01c36
21. Letterboxd — Importing data (docs oficiales) — https://letterboxd.com/about/import/
22. Trakt Forums — "Import from IMDB, Letterboxd, TV Time, CSV, or JSON files!" (anuncio oficial del importer; welcome wizard) — https://forums.trakt.tv/t/import-from-imdb-letterboxd-tv-time-csv-or-json-files/32483
23. Trakt Forums — "Critical bugs in the new Letterboxd importer…" (riesgo correctitud) — https://forums.trakt.tv/t/critical-bugs-in-the-new-letterboxd-importer-that-corrupt-watch-dates-and-block-free-users-while-the-legacy-importer-is-broken-by-cloudflare/114734
24. Exophase — FAQ (tracking por perfiles públicos; sin contraseñas; causa de fallos = privacidad) — https://www.exophase.com/faq/
25. Backloggd — (demanda de import Steam, comentario de usuario, mayo 2025) — https://backloggd.com/
26. MyAnimeList — Import (requiere login; aviso anti-phishing) — https://myanimelist.net/import.php
27. Userpilot — Time to Value (definiciones TTV) — https://userpilot.com/blog/time-to-value/
28. Strava Help Center — Import Historical Data From Garmin Connect (límites de bulk upload) — https://support.strava.com/hc/en-us/articles/216917877
29. OpenID Foundation — (contexto migración OpenID 2.0 → OpenID Connect) — https://openid.net/

**Notas de fiabilidad:** las citas de páginas de Apple, Material y Steam Help fueron obtenidas de versiones archivadas o de snippets del buscador cuando la página live exige JavaScript/login; se indica en cada caso. Fuentes con Cloudflare que impidieron captura directa (Completionist.me, Letterboxd settings/import logueado, Steam Help FAQ concreta) se citan solo por lo verificable y se marcan con menor confianza en el texto.
