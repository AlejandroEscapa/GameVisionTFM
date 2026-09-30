# Fase 0 — Cimientos de datos

**Estado:** ✅ **Completada** (auditoría validada el 29/09/2026) · **Estimación:** 1 semana · **Depende de:** — · **Bloquea a:** F1

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
| D0.5 | **Modelo de amigos** | (a) Dejar el actual (simétrico, por email) · (b) Rediseño a "seguir" (asimétrico) | ✅ **CERRADA (30/09/2026, en F2/D2.1)**: **seguir asimétrico** (aristas `following`), migración T2.9 ejecutada | ✅ |
| D0.6 | **Duración de los juegos** | ✅ **CERRADA**: el campo existe en el modelo de datos de F0 y la **fuente quedó cerrada el 29/09 en la D1.4 de fase-1** (scraper HLTB + respaldo manual). T1.11 ya puede empezar | ✅ |

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
- [x] T0.5 Modo degradado completo (29/09/2026): la capa de datos (`CachedGameCatalog`) sirve lo
      cacheado y ahora **expone el estado** (`isServingFromCache()` en `GameCatalog`); la UI muestra
      un `OfflineBanner` del design system en Búsqueda y Ficha («Sin conexión · mostrando datos
      guardados»). Verificado con 3 tests unitarios nuevos del decorador + 2 del ViewModel.

### B. Modelo de datos de la biblioteca

> **Diseño definitivo pendiente de aprobación:** [docs/plan/F0B-propuesta-modelo-datos.md](../plan/F0B-propuesta-modelo-datos.md)
> — propuesta senior para escalar (uid, stats agregadas, índices, reglas, costes). Se implementa tras el visto bueno.

- [x] T0.6 Definir `library/{gameId}`: estado, nota, favorito, plataforma, fecha de alta (implementado y verificado en B1/B2)
- [x] T0.7 Definir `logs/{logId}`: partida con fecha de inicio/fin, horas, reseña, nota, plataforma (modelo y repositorio; UI en F1)
- [x] T0.8 Definir `sessions/{sessionId}`: sesión del diario (fecha, minutos) (modelo y repositorio; UI en F1)
- [x] T0.9 Implementar el repositorio de biblioteca (`LibraryRepository`) devolviendo `Result` (+ adopción en pantallas, verificado E2E en emulador)
- [x] T0.10 Reglas de Firestore actualizadas — **desplegadas el 28/09/2026 y verificadas**: login, lectura y escritura de biblioteca comprobados con las reglas nuevas activas. Test negativo sistemático: Bloque 2 del [plan de deuda](../plan/DEUDA-TECNICA-2026.md). Índices preparados en `firestore.indexes.json` para el próximo deploy por CLI
- [x] T0.11 **Limpieza ejecutada (29/09/2026)**: backup JSON + borrado de `playedlist`/`wishlist`/`history` (5 cuentas de prueba) y docs `aa`/`ee`, con verificación ✅. Herramienta: `scripts/b3-limpieza/`

### C. Deuda conocida
- [x] T0.12 Foto de perfil **sin** Cloud Storage (ADR-0007, 29/09/2026): `ProfileImageStorage`
      comprime la imagen (512 px, JPEG q80) y la guarda como base64 en `profile_images/{uid}`
      (Firestore); el perfil persiste el puntero `firestore://profile_images`. Aviso claro al usuario
      si supera **5 MB** (o 256 KB ya comprimida). Storage exige plan **Blaze** → descartado (reglas
      archivadas en `firebase/storage.rules`). Reglas de Firestore para `profile_images` **desplegadas**.
      **CA0.3 verificado.**
- [ ] T0.13 Firebase Cloud Messaging — **diferida con disparador (29/09/2026)**: sin criterio de
      aceptación en F0 y su UX se diseña en F2 (notificaciones sociales) / F4 (widget y avisos).
      Se abre al integrar las notificaciones de F2.
- [x] T0.14 Persistencia offline de Firestore (29/09/2026): caché persistente declarada
      **explícitamente** en `AppModule` (`PersistentCacheSettings`) — el default del SDK ya la
      activa, pero así un cambio de default no la apaga por debajo de la app. Verificación de
      CA0.2 (registro en modo avión) pendiente en emulador.
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
- [x] CA0.2 *(cerrado 29/09/2026, noche)*: **verificado en vivo** con el usuario QA — **Halo 3**
      añadido a la biblioteca en **modo avión** (estado «Jugando») y **sincronizado a Firestore**
      al recuperar la red (`users/{uid}/library/28589`, `status=jugando`).
- [x] CA0.3 *(cerrado 29/09/2026, noche)*: **verificado** — la foto se recupera al **borrar los datos
      de la app y volver a iniciar sesión** (simula el 2º dispositivo). Se guarda en
      `profile_images/{uid}` (Firestore) y se sirve como data URI (ADR-0007).
- [~] CA0.4 *(actualizado 29/09/2026)* Los datos antiguos eran **de prueba** y se han borrado con backup. Verificación: **parcial** — alta + listas verificadas E2E con la cuenta QA (28/09); el tramo de sesión/estadísticas llegará con F1 y se re-verificará entonces.
- [x] CA0.6 *(actualizado 29/09/2026, noche)* **35 unitarios + 7 instrumentados, 0 fallos** (medido en
      la auditoría de cierre; incl. los 5 nuevos del modo degradado). Los instrumentados ya corren en el
      emulador `Pixel_9`.
- [x] CA0.1 *(cerrado 29/09/2026, noche)*: **verificado en vivo** en emulador con **modo avión real** —
      la búsqueda «Halo» sirve los resultados cacheados y muestra el banner «Sin conexión · mostrando
      resultados guardados».
- [ ] CA0.5 Cambiar de proveedor de catálogo no toca ni una pantalla (demostrable:
      sustituir la implementación en el módulo de Hilt compila sin tocar UI).
- [x] CA0.6 Tests: **35 unitarios + 7 instrumentados** en verde (auditoría 29/09/2026); tests nuevos
      para el repositorio de biblioteca y el modo degradado.

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
| D0.5 | Modelo social: ✅ cerrado en F2 (seguir asimétrico, D2.1) |
| D0.6 | Estructura de duración sí (F0); **fuente cerrada (29/09/2026)**: scraper HLTB con caché + respaldo manual — [D1.4 de fase-1](fase-1-corazon-tracker.md) e [investigación](../investigacion-2026/hltb-verificacion-2026.md) |

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

## 🔍 Auditoría de cierre de fase

> Aplica el [protocolo de auditoría de cierre](../metodologia/auditoria-de-cierre.md).
> **Particularidad de F0:** los datos y el offline son la base de todo, así que el bloque 6
> (experiencia) se centra en los **modos degradado y sin conexión**, y el bloque 1 en que la
> **release firmada con R8** sigue compilando tras la modernización.

- [ ] Bloque 1: `assembleDebug` y `assembleRelease` (R8) sin warnings nuevos
- [x] Bloque 1: `assembleDebug` y `assembleRelease` (R8) sin warnings nuevos
- [x] Bloque 2: **35 unitarios + 7 instrumentados en verde** (sesión de emulador 29/09/2026)
- [x] Bloque 3: lint **0 errores** (4 corregidos); 68 warnings triados como deuda de estilo para F4.5
- [x] Bloque 4: `GameCatalog` como SSOT del catálogo; sin DTOs de RAWG en la UI
- [ ] Bloque 5: docs/ADRs sincronizados (recuento de tests corregido; pendiente validación)
- [ ] Bloque 6: **CA0.1, CA0.2 y CA0.3 verificados** en vivo
- [x] **Reglas de Firestore desplegadas** (colección `profile_images` incluida)
- [x] Hallazgo **H1** corregido: el error de la ficha ya no expone el detalle técnico (mensaje claro + «Reintentar»)
- [ ] Auditoría firmada y validada por el propietario

> **Informe completo:** [auditoría de cierre de F0](../metodologia/auditoria-fase-0-2026.md) —
> resultado **🟡 Apta con reservas** (29/09/2026). Hallazgo abierto **H1**: la ficha sin caché y sin red
> muestra el error técnico crudo al usuario (decisión de copy del propietario).

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
- **D0.5 — Diferida a F2 (27/09/2026) · CERRADA (30/09/2026)** — el modelo de amigos se decidió
  en la fase social: **seguir asimétrico** (D2.1 de fase-2), con migración T2.9 ejecutada.
- **D0.6 — Estructura sí; fuente cerrada (29/09/2026)** — el campo de duración se crea en
  el modelo; la fuente de los datos quedó decidida ese mismo día en la **D1.4 de fase-1**:
  scraper ligero de HowLongToBeat con caché + respaldo manual (verificación en
  [hltb-verificacion-2026](../investigacion-2026/hltb-verificacion-2026.md)). IGDB queda como
  candidato futuro. **T1.11 ya puede empezar.**
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
- **B3 ejecutado (29/09/2026, madrugada)** — backup automático en `scripts/b3-limpieza/backups/`;
  borradas 12 subcolecciones antiguas de 5 cuentas de prueba y los documentos `aa`/`ee`; verificación ✅.
  Perfiles y social intactos (llegan a su rediseño en F2).
- **Cierre de código de F0 (29/09/2026, tarde)** — T0.5 (modo degradado visible con
  `OfflineBanner`), T0.12 (foto en Storage + reglas escritas, pendiente publicarlas) y T0.14
  (caché persistente explícita). T0.13 (FCM) **diferida a F2** con disparador. Compilación y
  **35 unitarios + 7 instrumentados en verde**; CA0.2 y CA0.3 quedan para la sesión de emulador (requieren el
  `google-services.json` real, claves API en `local.properties` y reglas de Storage publicadas).
- **Auditoría de cierre de F0 (29/09/2026, noche)** — ejecutada en modo autónomo. Resultado
  **🟡 Apta con reservas** ([informe](../metodologia/auditoria-fase-0-2026.md)): build debug+release OK,
  **35 unitarios + 7 instrumentados**, lint **0 errores**, **CA0.1 verificado en vivo** (modo avión real).
  Quedan como reservas: publicar `storage.rules`, CA0.2/CA0.3 en emulador autenticado, y el hallazgo **H1**
  (la ficha sin caché y sin red muestra el error técnico crudo → decisión de copy del propietario).

