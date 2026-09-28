## Dimension 01: Publicación en Google Play Store (2026)

> Nota metodológica: esta dimensión se investigó con 14 búsquedas web independientes (documentación oficial de Google/Play Console como fuente prioritaria, después medios y blogs fiables). El proveedor de búsqueda devolvió en varias consultas resultados poco pertinentes; cuando un dato **no** pudo respaldarse con una fuente capturada en esta ronda, se indica explícitamente en lugar de inventar la cifra.

### Current State

- **Verificación de identidad obligatoria para todas las apps.** A partir de **septiembre de 2026**, Android exigirá que todas las apps estén registradas por desarrolladores verificados para poder instalarse en dispositivos Android certificados [^slashdot-verify]. Los datos del proceso de registro requeridos incluyen **nombre, dirección, correo electrónico y número de teléfono**, y **opcionalmente** una foto de identificación (photo ID) [^ltt-verify].
- **Testers obligatorios para cuentas personales nuevas: 12 testers / 14 días.** La ayuda oficial de Play Console lo formula así: "At least 12 testers must be opted in to your closed test continuously for the preceding 14 days when you apply for production access" [^play-testing-req]. El requisito se aplica a **cuentas de desarrollador personales** (no a organizaciones) [^ontesters][^primetestlab].
- **Cambio histórico de umbral: de 20 a 12 testers.** Fuentes de 2026 describen el paso del antiguo umbral de 20 testers al actual de 12 testers / 14 días continuos en la pista de prueba cerrada ("closed testing") [^primetestlab][^apphive]. Además, no basta con que 12 personas descarguen un APK: deben estar **opt-in de forma continua** durante los 14 días [^apphive].
- **Target API level obligatorio.** A partir del **31 de agosto de 2026**, las apps nuevas y las actualizaciones deben tener como target **Android 16 (API level 36)** o superior para enviarse a Google Play; ciertos tipos de app (p. ej. categorías específicas) deben apuntar a **Android 15 (API level 35)** o superior [^android-targetapi]. Antes, el hito previo fue **API 34 obligatorio desde el 31 de agosto de 2024** y **API 35 desde el 31 de agosto de 2025** [^fb-targetapi].
- **Política de actualización anual.** Google mantiene la regla de exigir un target API dentro de **un año** desde el lanzamiento de la última versión mayor de Android [^androidblog-targetapi].
- **D-U-N-S para cuentas de organización.** Un número **D-U-N-S** es necesario para verificar la legitimidad de una empresa u organización durante el registro de la cuenta de desarrollador de Google Play [^median-duns]. Las cuentas de organización necesitan **número D-U-N-S y documentos de empresa** [^ultrasys].
- **Data safety section obligatoria.** Google obliga a los desarrolladores a usar la sección de **Seguridad de datos (Data Safety Section)** para informar de cómo las apps recopilan, comparten y protegen los datos del usuario [^adgully][^alphaxiv]. Desde **noviembre de 2025** se debe declarar además el proceso de **eliminación de cuenta** en la sección de Data safety [^playacademy].
- **Política de privacidad obligatoria.** Un enlace a la política de privacidad es exigido para publicar apps; la ausencia de dicha política es una causa típica de violación "Policy Issue" que requiere acción correctiva [^termsfeed].
- **UGC / apps sociales.** Play exige que las apps **Anonymous Chat, Random Chat, Social y Dating** cumplan sus **Child Safety Standards** [^play-devpolicy]. En 2026 Google **amplía** su política de "Age-Restricted Content and Functionality" para aplicarla a apps de chat anónimo y chat aleatorio [^playpolicies]. Para UGC, la guía práctica indica **definir y prohibir** claramente el contenido, y disponer de mecanismos de moderación/reporte [^iubenda].
- **Contenido generado por IA (relevante para GameVision).** En 2025 Google actualizó su Developer Program Policy separando el **contenido generado por IA** como un área regulada específica, con obligación de que el contenido IA cumpla las políticas [^revera].
- **Permisos sensibles.** Play trata el acceso a **archivos y directorios del usuario como sensible y de alto riesgo**, y restringe el uso del permiso `MANAGE_EXTERNAL_STORAGE` [^perms]. Los avisos de Play Protect incluyen frecuentemente apps bloqueadas por **uso de permisos sensibles** [^playprotect].
- **Apelaciones.** El proceso de apelación se realiza desde Play Console: menú **"Policy Status"**, seleccionar la app y pulsar **"Appeal"** [^gplay-appeal].
- **Gaps declarados (no confirmados en esta ronda):** el **coste único de 25 USD**, la **obligatoriedad del Android App Bundle (AAB)**, los **límites de tamaño** concretos, **Play Asset Delivery**, los **umbrales de crash rate / ANR de Android vitals** y el **uso de Play Integrity API** NO quedaron respaldados por una cita verbatim capturada en estas búsquedas. Se marcan como "baja" y pendientes de verificación en una ronda adicional.

### Key Evidence

| Dato | Valor | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Testers mínimos (cuenta personal) | 12 testers opt-in/14 días | (help doc, vigente) | "At least 12 testers must be opted in to your closed test continuously for the preceding 14 days when you apply for production access." | alta | [^play-testing-req] |
| Testers 2026 | 12 testers, 14 días continuos, pista cerrada, solo cuentas personales | 2026-09-14 | "In 2026 the Google Play tester requirement is 12 testers, 14 continuous days, closed testing track, and it applies to personal developer [accounts]" | media | [^ontesters] |
| Cambio 20→12 testers | 12 testers / 14 días | 2026-08-17 | "Google Play requires a closed test with at least 12 testers opted in continuously for at least 14 days before you can apply for production [access]" | media | [^primetestlab] |
| Testers: opt-in continuo | no basta descargar APK | 2026-05-25 | "Google requires you to run a closed test with at least 12 testers for 14 days consecutively. And no, just having 12 friends download the APK [no sirve]" | media | [^apphive] |
| Target API (nuevas apps/updates) | Android 16 / API 36 desde 2026-08-31 | 2026 (vigente) | "自 2026 年8 月31 日起: 新应用和应用更新必须以 Android 16( API 级别36)或更高版本为目标平台" | alta | [^android-targetapi] |
| Target API previos | API 34 (2024-08-31), API 35 (2025-08-31) | — | "API 34 by August 31, 2024, with API 35 required starting August 31, 2025" | baja | [^fb-targetapi] |
| Regla de un año | target dentro de 1 año de la última versión mayor | 2022-04-06 | "We currently require new apps and app updates to target an Android API level within one year of the latest major Android OS version release." | alta | [^androidblog-targetapi] |
| Verificación de identidad (todas las apps) | registro de dev verificado desde sep-2026 | 2025-08-25 | "Starting in September 2026, Android will require all apps to be registered by verified developers in order to be installed on certified Android [devices]" | media | [^slashdot-verify] |
| Datos de registro | nombre, dirección, email, teléfono, photo ID opcional | 2025-08-26 | "The required data in registration process consists of name, address, email address, telephone number and optionally a photo ID." | baja | [^ltt-verify] |
| D-U-N-S | requerido para organizaciones | 2025-02-15 | "A D‐U‐N‐S number is required to verify the legitimacy of a business or organization during Google Play developer account registration." | media | [^median-duns] |
| Cuentas de organización | D-U-N-S + documentos de empresa | — | "Organization accounts need a D-U-N-S Number and business documents." | baja | [^ultrasys] |
| Data safety | obligatoria, informa recogida/uso/protección | 2022-04-27 | "Data safety section, where developers will be required to give people more information about how apps collect, share and secure users' data." | media | [^adgully] |
| Data safety: borrado de cuenta | declarar proceso de eliminación de cuenta | 2025-11-11 | "Developers will need to disclose information regarding their account deletion process in the Data safety section on Google Play" | media | [^playacademy] |
| Data Safety Section (DSS) | obligatoria por mandato | — | "Google has mandated developers to use Data Safety Sections (DSS) to increase transparency in data collection and sharing practices." | media | [^alphaxiv] |
| Privacy policy | enlace obligatorio; su falta = violación | 2026-05-20 | "A Privacy Policy can fix the 'Policy Issue' violation. The required action, as suggested by Google, is to either provide a link to your Privacy [Policy]" | media | [^termsfeed] |
| UGC / social + Child Safety | Social/Anonymous/Random Chat/Dating deben cumplir | 2026-08-26 | "Google Play requires Anonymous Chat, Random Chat, Social, and Dating apps to comply with our Child Safety Standards policy." | alta | [^play-devpolicy] |
| UGC moderación | definir y prohibir contenido | 2026-09-09 | "User-generated content (UGC) is any content users create and share inside your app that other users can see. Clearly define and ban [content]" | media | [^iubenda] |
| Política de contenido con restricción de edad | ampliada a chat anónimo/aleatorio (2026) | 2026 | "2026 We're expanding our Age-Restricted Content and Functionality policy to apply to anonymous chat and random chat apps." | media | [^playpolicies] |
| Contenido IA | área regulada específica desde 2025 | — | "In 2025, Google Play updated its Developer Program Policy, setting AI-generated content apart as a separate regulated area." | media | [^revera] |
| Permisos sensibles | MANAGE_EXTERNAL_STORAGE restringido | (vigente) | "Google Play policy treats access to user files and directories as sensitive and high risk access, so we restrict use of the MANAGE_EXTERNAL_STORAGE permission" | alta | [^perms] |
| Play Protect: bloqueos | apps bloqueadas por permisos sensibles | 2026-08-18 | "Common Play Protect warnings include apps blocked due to sensitive permission use, apps identified as harmful, recommended app scans" | media | [^playprotect] |
| Apelaciones | Policy Status → app → "Appeal" | 2026-07-18 | "On the left menu, choose 'Policy Status'. Select the app that you want to appeal. Click the 'Appeal' button." | media | [^gplay-appeal] |
| Coste 25 USD (único) | NO confirmado en esta ronda | — | (sin cita verbatim capturada) | baja | — |
| AAB obligatorio / límites de tamaño / Play Asset Delivery | NO confirmado en esta ronda | — | (sin cita verbatim capturada) | baja | — |
| Crash rate / ANR (Android vitals) / Play Integrity API | NO confirmado en esta ronda | — | (sin cita verbatim capturada) | baja | — |

[^play-testing-req]: Google Play Console Help — "App testing requirements for new personal developer accounts". (help doc, vigente). https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB
[^ontesters]: OnTesters — "Google Play Tester Requirements 2026: 12 Testers, 14 Days". 2026-09-14. https://ontesters.com/
[^primetestlab]: PrimeTestLab — "Google Play 20 to 12 Testers: 2026 Closed Testing Rule". 2026-08-17. https://primetestlab.com/
[^apphive]: App Hive — "The Ultimate Guide to Google Play Closed Testing". 2026-05-25. https://getapphive.com/
[^android-targetapi]: Android Developers — "符合 Google Play 的目标 API 级别要求". 2026 (vigente). https://developer.android.com/
[^fb-targetapi]: Facebook (resultado agregado) — "Google Play Target API Level Requirements Android 2026". —. https://www.facebook.com/
[^androidblog-targetapi]: Android Developers Blog — "Expanding Play's Target Level API Requirements". 2022-04-06. https://android-developers.googleblog.com/
[^slashdot-verify]: Slashdot (Tech) — "Google To Require Identity Verification for All Android App [developers]". 2025-08-25. https://tech.slashdot.org/
[^ltt-verify]: Linus Tech Tips (foro) — "Google requires developers to register before sideloading". 2025-08-26. https://linustechtips.com/
[^median-duns]: Median.co — "How to set up your Google Play developer account: A guide". 2025-02-15. https://median.co/
[^ultrasys]: Ultra Systems (Kuwait) — "Android App Development Kuwait — Google Play Apps". —. https://ultrasystemsq8.com/
[^adgully]: Adgully — "Get data safety information about your apps in Google Play". 2022-04-27. https://www.adgully.com/
[^playacademy]: Google Play Academy (ExceedLMS) — "Disclosing user data deletion on the Data safety details page". 2025-11-11. https://playacademy.exceedlms.com/
[^alphaxiv]: alphaXiv (paper) — "Unpacking Privacy Labels: A Measurement and Developer [study]". —. https://www.alphaxiv.org/
[^termsfeed]: TermsFeed — "How-to: Google Play Store Violation 'Policy Issue'". 2026-05-20. https://www.termsfeed.com/
[^play-devpolicy]: Google Play Console Help — "Developer Program Policy". 2026-08-26. https://support.google.com/googleplay/android-developer/answer/17517561
[^iubenda]: iubenda — "An overview of Google Play's requirements and restrictions". 2026-09-09. https://www.iubenda.com/
[^playpolicies]: Android Developers — "Google Play Policies". 2026. https://developer.android.com/
[^revera]: Revera Legal — "AI in Mobile Applications: Google Play Requirements". —. https://www.revera.legal/
[^perms]: Google Play Console Help — "Permissions and APIs that Access Sensitive Information". (vigente). https://support.google.com/googleplay/android-developer/answer/16558241
[^playprotect]: Google Developers — "Developer Guidance for Google Play Protect Warnings". 2026-08-18. https://developers.google.com/android/play-protect/warning-dev-guidance
[^gplay-appeal]: Google Play Console Help (hilo de comunidad) — "Requesting Manual Review for Mistaken 'Associated account' termination". 2026-07-18. https://support.google.com/googleplay/android-developer/thread/452340256
[^transparency]: Google Transparency Center — "Google Play Policies and Guidelines". https://transparency.google/

### Tensions & Counter-arguments

- **Nº de testers (12 vs 20):** el requisito clásico difundido era **20 testers / 14 días**; en 2026 las fuentes apuntan a **12 testers**, presumiblemente por relajación de la política. La fuente oficial [^play-testing-req] confirma los "12 testers / 14 días" como cifra vigente, pero conviene revalidar en Play Console porque Google ha ajustado este umbral repetidamente. Si se publica con **cuenta de organización**, el requisito de testing de cuentas personales **no aplica** (queda exenta) — esto es clave para decidir el tipo de cuenta.
- **Target API: API 35 vs 36.** Hay mezcla de mensajes: una fuente (agregada, baja fiabilidad) menciona "API 35 desde 31-ago-2025" [^fb-targetapi], mientras la documentación de Android indica **API 36 (Android 16) desde 31-ago-2026** [^android-targetapi]. Lo más probable: **API 35 fue el hito de 2025 y API 36 el de 2026**. Verificar la fecha exacta según la fecha real de publicación de GameVision (si se publica antes del 31-ago-2026, API 35 sería suficiente).
- **Cambio grande de verificación (sep-2026):** el requisito de que **todos los desarrolladores estén verificados** para instalar en dispositivos certificados (incluso por sideloading) es un cambio estructural [^slashdot-verify][^ltt-verify]. La fiabilidad de estas fuentes es media/baja (foros y agregadores); debe confirmarse en la fuente oficial de Google ("Android Developer Verification").
- **Riesgo específico para GameVision (UGC + social):** al ser una red social de videojuegos con contenido de usuarios, cae en el ámbito de las políticas **UGC + Child Safety** [^play-devpolicy][^iubenda] y de la ampliación 2026 de la política de contenido con restricción de edad [^playpolicies]. Necesitará moderación, reporte/bloqueo de usuarios, términos y posiblemente flujo de edad. Además, si la app usa **RAWG/NewsAPI** con contenido textual/imágenes de terceros, y si se monetiza con **anuncios**, debe reflejarse todo en la **Data Safety form** y en la **política de privacidad** [^adgully][^termsfeed].
- **Monetización con anuncios:** no se localizó en esta ronda una cita verbatim sobre la "declaración de anuncios" (ads declaration) en Play Console; es un campo obligatorio conocido del formulario de contenido de la app, pero **queda pendiente de verificación** con fuente oficial.
- **Datos no verificados:** el **coste de 25 USD**, la **obligatoriedad del AAB**, sus **límites de tamaño**, **Play Asset Delivery**, los **umbrales de Android vitals (crash/ANR)** y **Play Integrity** no obtuvieron respaldo verbatim en estas 14 búsquedas. No deben citarse como hechos confirmados sin una segunda ronda dirigida a `developer.android.com` y `play.google/developer-content-policy`.

[^play-testing-req]: Google Play Console Help — "App testing requirements for new personal developer accounts". https://support.google.com/googleplay/android-developer/answer/14151465?hl=en-GB
[^android-targetapi]: Android Developers — "符合 Google Play 的目标 API 级别要求". https://developer.android.com/
[^play-devpolicy]: Google Play Console Help — "Developer Program Policy". 2026-08-26. https://support.google.com/googleplay/android-developer/answer/17517561
[^perms]: Google Play Console Help — "Permissions and APIs that Access Sensitive Information". https://support.google.com/googleplay/android-developer/answer/16558241
