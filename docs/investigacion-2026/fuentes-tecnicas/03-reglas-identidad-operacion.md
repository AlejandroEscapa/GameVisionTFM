## Dimension 03: Reglas, identidad, cliente Android y operación

### Current State

GameVision es una app Android (Kotlin + Compose) con Firestore como backend. El modelo F0-B vigente define:

- `users/{email}` como **documento raíz de usuario**, con subcolecciones `library`, `logs` y `sessions`.
- La clave de documento es el **email** del usuario, no el `uid` de Firebase Auth.
- El equipo ya valora migrar a `users/{uid}` para desacoplar identidad de dato personal.

Estado de la cuestión en este punto del diseño:

- **Identidad:** la documentación oficial de Firebase es explícita en que Firestore debe colgar de `user.uid` (identificador estable), no de PII como el email. Usar el email como ID de documento arrastra PII al *path*, complica el cambio de email, y hace que cualquier cambio de email obligue a migrar el documento (renombrar/recrear) con impacto en reglas y referencias.
- **Reglas:** ya existe consciencia de que "las reglas no son filtros" y de que `get()`/`exists()` dentro de reglas tienen coste. Falta cerrar la política: evitar `get()` en rutas calientes, o aceptarlo asumiendo su facturación y latencia.
- **Cliente Android:** la persistencia offline de Firestore está **activada por defecto** en Android; hay que gestionar correctamente el ciclo de vida de los `SnapshotListener` (registrar/quitar) para no dejar listeners colgando ni quemar lecturas, y usar `limit`/`startAfter` para listados grandes (biblioteca, logs históricos).
- **Operación:** no consta todavía un plan de backups programados, export a BigQuery, TTL/retención ni alertas de presupuesto/uso.

Este documento cubre reglas de seguridad, identidad, App Check, Emulator Suite, cliente Android, consistencia/concurrencia y operación, con foco en escalar a decenas de miles de usuarios sin agujeros ni facturas sorpresa.

### Key Evidence

| Dato/Práctica | Descripción | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Estructura de reglas (docs oficial) | La guía oficial de estructuración de reglas documenta cómo hacer *match* por rutas y datos jerárquicos; la página de buenas prácticas enlaza "Structure Security Rules", "Fix insecure rules", "Test your Security Rules" y "Securely query data" | 2024 (doc viva) | "Structure Security Rules · Writing conditions for Security Rules · Fix insecure rules · Test your Security Rules · Securely query data · Control access per ..." | Alta | [^1] |
| Las reglas NO son filtros | Guía oficial de condiciones: las reglas no filtran consultas; una query que viola reglas se rechaza entera | 2024 (doc viva) | "Rules are not filters Once you secure your data and begin to write queries, keep in mind that security rules are not filters. Queries must follow the ..." | Alta | [^2] |
| "All or nothing" | Doc cloud equivalente: las consultas son todo-o-nada respecto a reglas | 2024 (doc viva) | "Rules are not filters When writing queries to retrieve documents, keep in mind that security rules are not filters — queries are all or nothing." | Alta | [^3] |
| `get()`/`exists()` se facturan como lecturas | Blog oficial de Firebase (cross-service Security Rules): cada llamada `firestore.get()`/`firestore.exists()` genera una operación de lectura facturable | 2022-09-28 | "Both firestore.get() and firestore.exists() calls result in a read operation on your Firestore database. You will be billed for the Firestore ..." | Alta | [^4] |
| `get()` añade latencia y consume límites | Análisis de terceros: las llamadas `get()` en reglas suman latencia, cuentan como lecturas y consumen límites por petición; encadenar varias es un antipatrón | 2026-02-17 | "Document access calls such as get() can add latency, count as reads, and hit per-request limits. If you find yourself chaining multiple get() ..." | Media | [^5] |
| Límite de expresiones por petición | Nota técnica (no doc oficial) sobre evaluación de reglas: se evalúan hasta ~1000 expresiones por petición. **No confirmado en doc oficial** | n/d | "Firestore evaluates at most 1000 expressions per request" | Baja | [^6] |
| UID como clave de documento (Auth↔Firestore) | Guía oficial de reglas + Auth: puente entre identidad y datos | 2024 (doc viva) | "A guide to using Firebase Authentication with Firebase Security Rules, covering how to identify users, leverage user information, and define custom claims ..." | Alta | [^7] |
| UID es el puente canónico | Guía práctica: Auth sabe *quién* es el usuario; Firestore sabe *qué* le pertenece; el puente es `user.uid` | 2026-03-25 | "Firebase Auth handles who a user is. Firestore handles what belongs to them. The bridge between the two is user.uid — the unique ID Firebase ..." | Media | [^8] |
| UID-match evita volcados de colección | Análisis: atar el acceso al UID que coincide con el ID de documento evita el *dump* de colección posible con reglas laxas | 2026-09-08 | "This ties access to the authenticated user's UID matching the document ID, preventing the collection-wide data dump possible with looser rules." | Media | [^9] |
| Custom claims = RBAC | Doc oficial: el Admin SDK permite definir atributos/roles en el token, aplicados en reglas | 2024 (doc viva) | "The Firebase Admin SDK supports defining custom attributes on user accounts. This provides the ability to implement various access control strategies, including role-based access control, in Firebase apps." | Alta | [^10] |
| Claims vs documento Firestore | Debatido: almacenar roles en Firestore también funciona, pero puede requerir `get()` en reglas; los claims evitan esa lectura | 2024 (doc/terceros) | "Using Firebase Authentication Custom Claims While storing roles in Firestore works, Firebase Custom Claims provide better security, efficiency, and real-time access control." | Media | [^11] |
| Sincronía claims↔Firestore | Problema conocido: mantener roles en Firestore y claims en Auth sincronizados; los claims requieren refresco de token | n/d | "A common question I often encounter, is how to maintain consistency between custom claims in Firebase Auth and role assignments stored in Firestore." | Media | [^12] |
| App Check protege de abuso | Terceros: App Check protege los backends Firebase impidiendo clientes no autorizados | 2026-02-10 | "Google has a feature called Firebase App Check which protects your firebase backends from abuse by preventing unauthorised clients from ..." | Media | [^13] |
| App Check y cuotas/planes | Terceros: los límites de App Check existen para prevenir abuso; Spark tiene cuotas más estrictas que Blaze | n/d | "App Check : Limits are set to prevent abuse and ensure fair resource distribution. For example, the Spark Plan has stricter quotas compared to the Blaze Plan." | Baja | [^14] |
| Offline persistence ON por defecto (Android) | Doc oficial: en Android y Apple la persistencia offline está activada por defecto | 2024 (doc viva) | "For Android and Apple platforms, offline persistence is enabled by default . To disable persistence, set the PersistenceEnabled option to false ." | Alta | [^15] |
| Listeners offline reciben cambios de caché | Doc cloud: estando offline con persistencia activada, los listeners reciben eventos cuando cambia la caché local | 2024 (doc viva) | "While the device is offline , if you have enabled offline persistence , your listeners will receive listen events when the locally cached data changes." | Alta | [^16] |
| Listener es persistente; quitar en ciclo de vida | Práctica: el listener de Firestore es persistente y debe quitarse según el ciclo de vida de la Activity | n/d | "Please note that this is a persistent listener and should be removed according to the life cycle of your activity. The addSnapshotListener() ..." | Media | [^17] |
| Errores de listener son terminales | Doc oficial: tras un error el listener no recibe más eventos (hay que re-registrar/reintentar) | 2024 (doc viva) | "After an error, the listener will not receive any more events, and there is ..." | Alta | [^18] |
| Paginación con cursores oficial | Doc oficial: usar `startAt()`/`startAfter()` para paginar | 2024 (doc viva) | "Use the startAt() or startAfter () methods to … startAfter , ..." | Alta | [^19] |
| Paginación con `limit` + `startAfter` | Terceros: implementar paginación con `limit` y `startAfter`/`startAt` para traer menos datos | 2026-05-11 | "Pagination: You can also implement pagination in your queries using Firestore's limit and startAfter or startAt methods. This fetches fewer ..." | Media | [^20] |
| `serverTimestamp()` = reloj del servidor | Terceros: `serverTimestamp()` da consistencia entre dispositivos usando el reloj del servidor | 2026-06-10 | "serverTimestamp() ensures consistent timestamps across devices by using the Firestore server clock rather than the client clock." | Media | [^21] |
| Conflictos offline: Last-Write-Wins | Terceros: patrón LWW — el último cambio por timestamp gana (Firestore resuelve por último-write en el servidor si no se usa transacción) | 2026-04-08 | "Implement conflict resolution strategies. Key patterns include: Last-write-wins (LWW): The latest change by timestamp overrides others." | Media | [^22] |
| Tests de reglas en Emulator | Terceros: los tests de reglas corren contra el emulador con el harness oficial `@firebase/rules-unit-testing` | n/d | "security rules and money: Firestore security-rules tests run against the emulator with the official @firebase/rules-unit-testing harness." | Media | [^23] |
| Emulador en integración (Android) | Terceros: levantar Emulator Suite local `firebase emulators:start --only auth,firestore` y apuntar la app a `http://10.0.2.2:9099` | n/d | "For integration tests, spin up a local Firebase Emulator Suite ( firebase emulators:start --only auth,firestore ) and point the app to http://10.0.2.2:9099 ..." | Media | [^24] |
| Simulador de reglas en consola | Doc oficial: el simulador de reglas permite simular lecturas/escrituras/borrados autenticados y no autenticados | 2024 (doc viva) | "The rules simulator lets you simulate authenticated and unauthenticated reads , writes, and deletes." | Alta | [^25] |
| Backups programados (oficial) | Doc oficial: función de *scheduled backups* de Firestore para proteger datos (corrupción a nivel de app) | 2024 (doc viva) | "This page describes how to use the Cloud Firestore scheduled backups feature . Use backups to protect your data from application-level data corruption or ..." | Alta | [^26] |
| Backups: frecuencia diaria/semanal | Blog oficial: los backups programados respaldan toda la BD con frecuencia diaria y/o semanal a almacenamiento frío | 2023-09-08 | "Scheduled backups lets you backup your entire database on a chosen frequency of either daily and/or weekly onto cold storage." | Alta | [^27] |
| Export a BigQuery (extensión) | Extensión oficial: exporta una colección de Firestore a BigQuery, de forma *realtime* e incremental | n/d | "Use this extension to export the documents in a Cloud Firestore collection to BigQuery . Exports are realtime and incremental," | Alta | [^28] |
| Carga de exports de Firestore en BigQuery | Doc oficial GCP: se puede cargar datos desde el fichero de metadatos de un export de Firestore | 2024 (doc viva) | "You can load data from a Firestore export metadata file by using the Google Cloud console, bq command-line tool, or API." | Alta | [^29] |
| Alertas de presupuesto no cortan gasto | Terceros (GCP): las alertas de presupuesto avisan pero no detienen el gasto automáticamente; usar Cloud Monitoring para síntomas de disponibilidad | n/d | "Budgets alert ; they do not automatically stop project spending. Monitor user-visible success and latency, not only resource utilization ." | Media | [^30] |
| Patrones de coste-amplificación | Terceros: las facturas sorpresa de Firestore vienen de listeners sin límite, re-lecturas, fan-out de reescrituras y conteos en tiempo de render | n/d | "The six patterns behind most surprise Firestore bills — unbounded listeners, re-reads, fan-out rewrites, render-time counts — and their fixes." | Media | [^31] |
| Colecciones grandes: paginación por periodos | Síntesis: para colecciones que crecen años, la práctica estándar es paginar por cursor de campo ordenado (p. ej. fecha) y/o subcolecciones por año/mes; **convergencia de fuentes moderada, sin cita verbatim única fuerte** | n/d | (sin cita verbatim concluyente; recomendación sintetizada) | Baja | [^19][^20] |

**Fuentes (Dimension 03):**

[^1]: Best practices for Cloud Firestore (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/best-practices
[^2]: Writing conditions for Cloud Firestore Security Rules (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/security/rules-conditions
[^3]: Securely querying data — Firestore in Native mode (Google Cloud docs). Doc viva. https://docs.cloud.google.com/firestore/native/docs/security/rules-query
[^4]: Announcing cross-service Security Rules — The Firebase Blog (Google). 2022-09-28. https://firebase.blog/
[^5]: How to Write Firestore Security Rules for User-Based access (OneUptime). 2026-02-17. https://oneuptime.com/
[^6]: CLAUDE.md — Svartifoss (GitHub, nota técnica de proyecto). n/d. https://github.com/ (confianza baja, no doc oficial)
[^7]: Security Rules and Firebase Authentication (Firebase / Google). Doc viva. https://firebase.google.com/docs/rules/rules-and-auth
[^8]: Firebase Auth — Next Steps: Firestore, Security Rules, Email (nfarhaan.com). 2026-03-25. https://nfarhaan.com/
[^9]: Insecure Firestore Security Rules & PII Exposure (daily.dev). 2026-09-08. https://daily.dev/
[^10]: Control Access with Custom Claims and Security Rules (Firebase / Google). Doc viva. https://firebase.google.com/docs/auth/admin/custom-claims
[^11]: Migrating to Firebase Custom Claims for Role-Based Access Control (Medium / Chaitanya Yendru). n/d. https://medium.com/@chaitanyayendru/migrating-to-firebase-custom-claims-for-role-based-access-control-26c08f852795
[^12]: How to Keep Your Custom Claims in Sync with Roles Stored in Firestore (dev.to / oddbit). n/d. https://dev.to/oddbit/how-to-keep-your-custom-claims-in-sync-with-roles-stored-in-firestore-93l
[^13]: How to secure your firebase app (dev.to). 2026-02-10. https://dev.to/
[^14]: Google Cloud Firebase Pricing 2025: Plans & Costs (Tekpon). n/d. https://tekpon.com/
[^15]: Access data offline | Firestore (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/manage-data/enable-offline
[^16]: Enable offline data — Firestore in Native mode (Google Cloud docs). Doc viva. https://docs.cloud.google.com/firestore/native/docs/manage-data/enable-offline
[^17]: How to effortlessly get real-time updates from Firestore on Android (Medium / Firebase Tips & Tricks). n/d. https://medium.com/firebase-tips-tricks/how-to-effortlessly-get-real-time-updates-from-firestore-on-android-bcb823f45f20
[^18]: Get realtime updates with Cloud Firestore (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/query-data/listen
[^19]: Paginate data with query cursors | Firestore (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/query-data/query-cursors
[^20]: Firestore Query & Record Limitations (Estuary). 2026-05-11. https://estuary.dev/
[^21]: Flutter and Firebase in 2026 (sharpskill.dev). 2026-06-10. https://sharpskill.dev/
[^22]: Offline sync & conflict resolution patterns — Crash Course (studio-ak.fr). 2026-04-08. https://studio-ak.fr/
[^23]: SurePact | Abhilash Shrivastava (geniusjackass.com). n/d. https://www.geniusjackass.com/
[^24]: How to Test Registration Flow on Flutter (SUSATest). n/d. https://www.susatest.com/
[^25]: Get started with Cloud Firestore Security Rules (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/security/get-started
[^26]: Back up and restore data | Firestore (Firebase / Google). Doc viva. https://firebase.google.com/docs/firestore/backups
[^27]: Firestore adds point-in-time recovery and scheduled backups (Google Cloud Blog). 2023-09-08. https://cloud.google.com/blog/products/databases/firestore-adds-point-in-time-recovery-and-scheduled-backups
[^28]: Stream Firestore to BigQuery (extensions.dev — Firebase Extensions). n/d. https://extensions.dev/
[^29]: Load data from Firestore exports | BigQuery (Google Cloud docs). Doc viva. https://docs.cloud.google.com/bigquery/docs/loading-data-cloud-firestore
[^30]: Google Cloud Fundamentals: Projects, Resources, and APIs (tutorialslogic.com). n/d. https://www.tutorialslogic.com/
[^31]: Six Cost-Amplification Patterns Hiding in Your Firestore bill (FlameRangers). n/d. https://flamerangers.com/

### Tensions & Counter-arguments

**1. Email como clave de documento vs UID (`users/{email}` → `users/{uid}`).**
- *A favor de migrar a UID:* evita PII en rutas, sobrevive a cambios de email, alinea con `request.auth.uid` en reglas (una comprobación barata y sin `get()`), y evita "dump" de colección si las reglas atan el ID al UID autenticado ([^9]). La doc oficial encuadra el UID como el puente identidad↔datos ([^7][^8]).
- *Contra / coste:* migrar `users/{email}` a `users/{uid}` obliga a renombrar documentos y reescribir referencias (subcolecciones `library/logs/sessions`), reglas e índices. Es un *breaking change*; conviene hacerlo ANTES de crecer (hoy, con pocas cuentas) para que el coste sea bajo.
- *Matiz:* el email bien usado puede ser conveniente para búsquedas de admin, pero **no** como ID estructural. Mantén el email como *campo* (dato), no como *path*.

**2. Custom claims vs documento/rol en Firestore.**
- *Claims:* cero lecturas extra en reglas, RBAC limpio ([^10][^11]).
- *Contra:* los claims requieren usar el Admin SDK (entorno confiable, p. ej. Cloud Functions) y **refrescar el token** para propagarse; mantener sincronía claims↔Firestore es un problema real ([^12]). Para GameVision (app de usuario final, sin panel admin complejo todavía), los claims aportan poco; **leer un documento de rol con `get()` es aceptable si es raro y no está en el camino caliente**, pero ten presente su coste ([^4][^5]).

**3. `get()`/`exists()` en reglas: comodidad vs coste.**
- *Evidencia dura:* cada `get()`/`exists()` es una **lectura facturable** ([^4]) y suma latencia ([^5]).
- *Contra-argumento:* prohibir `get()` siempre es excesivo; para "roles" o "pertenencia" poco frecuentes puede ser correcto. La regla práctica: **no usar `get()` en reglas que se evalúan en cada lectura/escritura caliente** (p. ej. cada entrada de `sessions`). Prefiere derivar de `request.auth.uid` y de campos del propio documento.
- *No confirmado:* el límite "~1000 expresiones por petición" proviene de una nota de proyecto, no de doc oficial → trátalo como orientativo, no como hecho ([^6]).

**4. Las reglas no filtran: impacto en el modelo.**
- Como las reglas son **todo-o-nada** y no filtran consultas ([^2][^3]), un listado de `sessions` que "deje fuera" filas ajenas fallará entero. La solución profesional es **estructurar por usuario** (`users/{uid}/sessions/...`) y/o añadir el `where` de pertenencia en la query, no confiar en reglas para recortar resultados.

**5. Persistencia offline y listeners en Android.**
- La persistencia está ON por defecto ([^15]) y los listeners offline emiten con datos de caché ([^16]) → datos potencialmente "viejos" (usar metadatos `fromCache`/`hasPendingWrites` para UI). Los listeners son **persistentes** y hay que quitarlos según ciclo de vida ([^17]); tras un error dejan de emitir ([^18]). En Compose, ata el registro/desregistro a `DisposableEffect`/`LaunchedEffect` con clave estable y colecciona con paginación ([^19][^20]).

**6. Consistencia/concurrencia.**
- `serverTimestamp()` da consistencia entre dispositivos ([^21]); en escrituras offline la cola se vacía al reconectar y el servidor resuelve con **last-write-wins** por defecto salvo transacciones ([^22]). Para `sessions`/`logs` (datos append por un solo usuario) esto suele bastar; para datos compartidos multi-dispositivo, valora transacciones.

**7. Operación: coste y abuso.**
- Los patrones que rompen la factura son listeners sin límite, re-lecturas, fan-out y conteos en render ([^31]). App Check reduce abuso de clientes no autorizados ([^13]) pero las alertas de presupuesto **no cortan el gasto** ([^30]). Complementa con backups programados ([^26][^27]), export a BigQuery **solo cuando necesites analítica** (es realtime/incremental, [^28][^29]), y TTL/retención para `logs` antiguos (no se pudo confirmar cita oficial de TTL en esta ronda).

**8. Colecciones que crecen años (`logs`, `sessions`).**
- Recomendación sintetizada (confianza baja, sin cita verbatim única): paginar por cursor de fecha (`startAfter`) y/o particionar en subcolecciones por año/mes, archivando lo histórico fuera del camino de lectura activo ([^19][^20]). Es un patrón de diseño, no un hecho documental único.

**Limitaciones de esta investigación (declaración honesta):**
- No dispongo de herramienta de *fetch* directo de páginas; las citas verbatim provienen de fragmentos de resultados de búsqueda, por lo que pueden estar truncados ("..."). Verifícalas abriendo la URL oficial antes de fijar política.
- Las afirmaciones sobre **App Check**, **TTL** y **patrones de colecciones grandes** quedaron con **confianza baja/media** en esta ronda: la doc oficial existe, pero los fragmentos recuperados fueron débiles. Se recomienda verificación directa contra `firebase.google.com/docs/app-check` y la doc de TTL (`time-to-live`) antes de decidir.
