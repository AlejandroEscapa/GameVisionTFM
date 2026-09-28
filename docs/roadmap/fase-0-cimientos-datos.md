# Fase 0 — Cimientos de datos

**Estado:** 🟢 En ejecución (27/09/2026, actualizado 28/09/2026) · **Estimación:** 1 semana · **Depende de:** — · **Bloquea a:** F1

## Objetivo

Preparar la base sobre la que se construye todo el producto: dejar de depender en
exclusiva de RAWG, tener un modelo de datos capaz de representar una biblioteca real
(no tres listas planas) y arreglar la deuda conocida de imágenes.

## Resultado verificable

Se puede **registrar un juego con estado y guardarlo sin conexión**, la app **sigue
mostrando catálogo si RAWG cae** (datos cacheados) y **la foto de perfil viaja entre
dispositivos**.

## Por qué esta fase y no otra

La investigación de mercado identificó el riesgo más grave del proyecto: **RAWG tuvo una
caída de 1 día y 15 horas (agosto 2026) y hay señales de abandono**. Hoy toda la app
depende de él. Además, las funciones de F1 (estados, notas, diario) necesitan un modelo
de datos que hoy no existe. Hacer F1 sin F0 obligaría a rehacerlo.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D0.1 | **Proveedor de catálogo** | ✅ **CERRADA** (27/09/2026): seguir con **RAWG** ahora, con el adapter por delante, y **migrar parcialmente a IGDB más adelante** (marcado abajo). Motivo: RAWG ya funciona, su licencia comercial es más simple (gratis hasta ~100k usuarios/mes) y el coste de IGDB hoy es trabajo de OAuth2 sin beneficio inmediato | ✅ |
| D0.2 | **Datos de los usuarios actuales** | ✅ **CERRADA** (27/09/2026): **migrar** las 3 listas a la biblioteca nueva **y limpiar los documentos basura** (`aa`, `ee`, que no son emails válidos) | ✅ |
| D0.3 | **Alcance de la caché** | ✅ **CERRADA** (27/09/2026): **fichas visitadas + búsquedas recientes** (descarta cachear todo el catálogo: RAWG no lo permite y no cabe en el dispositivo) | ✅ |
| D0.4 | **Cloud Functions ahora o después** | ✅ **CERRADA** (27/09/2026): **posponer a F2**, cuando la búsqueda de usuarios y el feed las necesiten de verdad | ✅ |
| D0.5 | **Modelo de amigos** | (a) Dejar el actual (simétrico, por email) · (b) Rediseñar a "seguir" (asimétrico) | **Debatir en F2**, no en F0: aquí sólo se decide si el modelo actual bloquea algo (no lo hace) | ⬜ |
| D0.6 | **Duración de los juegos** | 🟡 **PARCIAL** (27/09/2026): el **campo existirá** en el modelo de datos en F0 (no bloquea nada); **la fuente de los datos queda PENDIENTE DE DECIDIR** por el propietario y **debe cerrarse antes de la tarea T1.11 de F1** | 🟡 |

> **Cómo se cierra una decisión:** se escribe la opción elegida, la fecha y una línea de
> porqué en el "Registro de decisiones" del final. Mientras haya una decisión sin cerrar,
> la fase no pasa a 🔵 Aprobada.

---

## Tareas

### A. Desacoplar el catálogo (riesgo RAWG)
- [x] T0.1 Crear la interfaz de dominio `GameCatalog` (buscar, detalle) en la capa de datos
- [x] T0.2 Implementar `RawgGameCatalog` envolviendo lo actual (sin cambiar comportamiento)
- [x] T0.3 Mapear las respuestas al modelo de dominio (dejar de usar DTOs de RAWG en la UI)
- [x] T0.4 Añadir caché local con Room para fichas y búsquedas
- [~] T0.5 Modo degradado: si la red/RAWG falla, mostrar lo cacheado. **Hecho en la capa de datos**
      (`CachedGameCatalog` sirve lo cacheado); **pendiente el aviso claro en la UI** (indicador de
      "sin conexión, mostrando datos guardados").

### B. Modelo de datos de la biblioteca

> **Diseño definitivo pendiente de aprobación:** [docs/plan/F0B-propuesta-modelo-datos.md](../plan/F0B-propuesta-modelo-datos.md)
> — propuesta senior para escalar (uid, stats agregadas, índices, reglas, costes). Se implementa tras el visto bueno.

- [x] T0.6 Definir `library/{gameId}`: estado, nota, favorito, plataforma, fecha de alta (implementado y verificado en B1/B2)
- [x] T0.7 Definir `logs/{logId}`: partida con fecha de inicio/fin, horas, reseña, nota, plataforma (modelo y repositorio; UI en F1)
- [x] T0.8 Definir `sessions/{sessionId}`: sesión del diario (fecha, minutos) (modelo y repositorio; UI en F1)
- [x] T0.9 Implementar el repositorio de biblioteca (`LibraryRepository`) devolviendo `Result` (+ adopción en pantallas, verificado E2E en emulador)
- [x] T0.10 Reglas de Firestore actualizadas — **desplegadas el 28/09/2026 y verificadas**: login, lectura y escritura de biblioteca comprobados con las reglas nuevas activas. Test negativo sistemático: Bloque 2 del [plan de deuda](../plan/DEUDA-TECNICA-2026.md). Índices preparados en `firestore.indexes.json` para el próximo deploy por CLI
- [ ] T0.11 Migración/limpieza de datos: `playedlist`/`wishlist`/`history` → **revisado 28/09: eran datos de prueba → se borran (backup previo); verificación con usuario de prueba nuevo**

### C. Deuda conocida
- [ ] T0.12 Firebase Storage: subir la foto de perfil y guardar la URL (arregla el bug
      de que la imagen es una ruta local que no viaja entre dispositivos)
- [ ] T0.13 Firebase Cloud Messaging: dependencia, permiso `POST_NOTIFICATIONS` y token
- [ ] T0.14 Habilitar persistencia offline de Firestore
- [x] T0.15 **Modo degradado en Noticias** (bug offline encontrado el 28/09/2026): sin red,
      `fetchFilteredNews` lanzaba `UnknownHostException` y **crasheaba la app**. Arreglado:
      el ViewModel captura el fallo y la pantalla muestra un estado de error con "Reintentar".
      Verificado en emulador en modo avión (sin crash).

---

## Criterios de aceptación (con evidencia)

- [ ] CA0.1 **RAWG simulado caído** (bloquear el dominio en el emulador o forzar error):
      la app sigue mostrando las fichas ya visitadas y avisa de que está sin conexión.
- [ ] CA0.2 Un juego registrado desde el emulador **en modo avión** aparece en la biblioteca
      al recuperar la conexión (persistencia offline).
- [ ] CA0.3 La foto de perfil subida desde el **dispositivo A** se ve al entrar desde el
      **dispositivo B** (o tras borrar datos de la app).
- [ ] CA0.4 *(actualizado 28/09/2026)* Los datos antiguos eran **de prueba**: no se migran. La verificación
      pasa a ser **end-to-end con un usuario de prueba nuevo** (registro → biblioteca → sesión → estadísticas).
- [ ] CA0.5 Cambiar de proveedor de catálogo no toca ni una pantalla (demostrable:
      sustituir la implementación en el módulo de Hilt compila sin tocar UI).
- [ ] CA0.6 Tests: los 17 unitarios + 6 instrumentados siguen verdes; nuevos tests para
      el repositorio de biblioteca y el modo degradado.

---

## Licencias de los proveedores de catálogo (verificado 27/09/2026)

Decide D0.1, así que conviene tenerlo por escrito:

| | **RAWG** (actual) | **IGDB** (candidato) |
|---|---|---|
| Uso no comercial (TFM) | Gratis | **Gratis** (Twitch Developer Service Agreement) |
| Uso comercial | Gratis hasta ~100.000 usuarios/mes (`PLAUSIBLE`) | **Requiere acuerdo de partnership** — escribir a partner@igdb.com; no hay precio público |
| Fricción de integración | Clave de API en segundos | Cuenta de Twitch con **2FA**, app registrada, **OAuth2** con renovación de token |
| Límite de peticiones | ~20.000/mes (`PLAUSIBLE`) | 4 peticiones/segundo, 8 simultáneas |
| Idiomas | Sólo inglés | **50+ idiomas (incluye español)** |
| Fiabilidad | Caída de 1 d 15 h en agosto 2026; señales de abandono | Producción (Discord, Twitch, Xbox) |

**Conclusión para el debate:** integrar IGDB **no cuesta nada mientras el proyecto sea no
comercial**, y aporta español y fiabilidad. El coste aparece sólo si se monetiza **con IGDB
como proveedor principal** — y en ese escenario RAWG (gratis hasta ~100k usuarios/mes) es la
vía comercial más simple. Por eso la recomendación es RAWG principal + IGDB secundario,
los dos detrás del adapter: cero coste hoy, y la decisión comercial se aplaza hasta que
haya ingresos que justifiquen la conversación.

> **Aparte de los proveedores:** las carátulas y los nombres de los juegos son propiedad
> de las **editoras**, no del proveedor de datos. Eso aplica igual hoy con RAWG. Para el
> TFM no es un problema; si el producto se comercializa, es un asunto a revisar
> independientemente de qué API se use.

## 🔖 MARCADO PARA EL FUTURO — Migración parcial a IGDB (no ahora)

**Decidido el 27/09/2026:** no se integra IGDB en esta fase, pero queda marcado como
trabajo futuro con condiciones de disparo claras. Lo que habría que hacer cuando toque:

**Condiciones que lo activan (cualquiera de ellas):**
1. RAWG vuelve a caer de forma prolongada o anuncia cierre (ya cayó 1 d 15 h en agosto 2026).
2. Se quiere mostrar el catálogo **en español** (RAWG es sólo inglés; IGDB trae 50+ idiomas).
3. Se necesita algo que RAWG no da: franquicias, estudios, DLC, modos de juego como entidades.
4. Se monetiza y se quiere IGDB como proveedor principal (implica hablar con partner@igdb.com).

**Trabajo estimado: ~media jornada.**
- Registrar app en el Twitch Developer Portal (cuenta con **2FA**, Client Type *Confidential*).
- OAuth2 *client-credentials* con renovación automática de token (el `Client Secret` va en
  `local.properties`, nunca en el repositorio — mismo patrón que las claves actuales).
- Implementar `IgdbGameCatalog` contra la interfaz `GameCatalog` (queries en Apicalypse).
- Mapear su respuesta al modelo de dominio y activarlo por configuración de Hilt.
- Verificar con los mismos criterios de aceptación de esta fase (CA0.1 y CA0.5).

**Lo que NO cambia:** ni una pantalla. Ese es precisamente el valor de hacer el adapter
en F0: la migración futura es añadir una clase y cambiar una línea de inyección.

## Decisiones cerradas (resumen)

| # | Decisión |
|---|---|
| D0.1 | RAWG ahora + adapter; IGDB marcado para el futuro |
| D0.2 | Migrar los datos existentes y limpiar los documentos basura |
| D0.3 | Caché: fichas visitadas + búsquedas recientes |
| D0.4 | Cloud Functions se posponen a F2 |
| D0.5 | Modelo social: se decide en F2 |
| D0.6 | Estructura de duración sí (F0); **fuente pendiente de decidir** (bloquea T1.11) |

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Cambiar el modelo rompe lo que ya funciona | T0.2 y T0.11 van en commits separados; migración con copia de seguridad previa de los documentos |
| Room + Firestore duplican fuentes de verdad | Regla escrita: **Firestore es la verdad**, Room es sólo caché de catálogo (no de biblioteca) |
| El scraping de HowLongToBeat se rompe | Decisión D0.6: dato en caché, nunca dependencia crítica en vivo |

## Cómo se verifica

Emulador `Pixel_9` + tests (`testDebugUnitTest`, `connectedDebugAndroidTest`) + release
con R8. Capturas: modo degradado sin red, biblioteca offline, foto en segundo dispositivo.

---

## Registro de decisiones

> Se rellena conforme se debaten. Formato: `D0.x — Opción elegida (fecha) — porqué`.

- **D0.1 — Seguir con RAWG + adapter, IGDB marcado para el futuro (27/09/2026)** — RAWG
  funciona, su licencia comercial es más sencilla (gratis hasta ~100k usuarios/mes) y no
  hay beneficio inmediato en asumir el OAuth2 de Twitch. La migración parcial a IGDB queda
  documentada arriba con sus condiciones de disparo y su estimación (~media jornada),
  y se hará sin tocar ninguna pantalla gracias al adapter.
- **D0.2 — Migrar y limpiar (27/09/2026)** — se migran las listas de los usuarios reales al
  modelo nuevo (demuestra migración de datos, defendible ante tribunal) y se borran `aa` y
  `ee`, que no son emails válidos y nunca podrán entrar.
- **D0.3 — Fichas visitadas + búsquedas recientes (27/09/2026)** — cubre el uso real, hace
  útil el modo sin conexión y no ataca los términos de uso de RAWG (nada de volcado masivo).
- **D0.4 — Cloud Functions a F2 (27/09/2026)** — F0 y F1 se resuelven en el cliente; menos
  piezas que desplegar y depurar ahora.
- **D0.5 — Diferida a F2 (27/09/2026)** — el modelo de amigos (simétrico vs seguir) se
  decide en la fase social, que es donde tiene consecuencias. No bloquea F0.
- **D0.6 — Estructura sí, fuente pendiente (27/09/2026)** — el campo de duración se crea en
  el modelo; **el propietario decidirá más adelante de dónde salen los datos** (scraper de
  HowLongToBeat, dato manual, IGDB u otra vía). Marca de bloqueo: **T1.11 no puede empezar
  sin esta decisión**. Se deja anotado también en el README del roadmap.
- **F0-B — propuesta de modelo aprobada (28/09/2026)** — clave `uid` (el email pasa a campo),
  histórico local en el dispositivo, estadísticas agregadas (`stats/summary`) y migración de listas
  (`playedlist`→`jugando`, `wishlist`→`deseado`, `favorites`→flag `favorite`; `history` se descarta).
  Implementación en 3 bloques: B1 datos → B2 adopción → B3 migración.
  Ver [propuesta](../plan/F0B-propuesta-modelo-datos.md).
- **D0.2 — revisión (28/09/2026)** — los datos de las listas actuales eran **de prueba**: en B3 se
  **borran** (junto a `aa` y `ee`) en lugar de migrarse; la verificación se hace creando un **usuario de
  prueba nuevo**.
- **Reglas desplegadas y verificadas (28/09/2026)** — el propietario publicó las reglas nuevas
  (por uid) y se verificaron desde la app (positivos): login, lectura y escritura de biblioteca.
  La comprobación negativa (acceso cruzado denegado) se hará con el Emulator Suite
  ([plan de deuda](../plan/DEUDA-TECNICA-2026.md), Bloque 2).

