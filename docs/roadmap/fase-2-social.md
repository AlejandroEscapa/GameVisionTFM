# Fase 2 — Social

**Estado:** ✅ **Completada (01/10/2026)** — auditoría validada por el propietario · **Estimación:** 1 semana · **Depende de:** F1 · **Plan:** [FASE-2-SOCIAL-2026.md](../plan/FASE-2-SOCIAL-2026.md)

## Objetivo

Que GameVision deje de ser una app individual: **perfil público, encontrar gente por
nombre de usuario, feed de actividad, Top 4 y listas curadas**. La investigación es
explícita: los trackers que sobreviven al abandono lo hacen por **responsabilidad social**
o por automatización. Esta fase ataca la primera.

## Resultado verificable

Dos usuarios distintos pueden **encontrarse por nombre de usuario**, ver sus perfiles y
bibliotecas, seguirse y ver la actividad del otro en un feed. **Verificado en el emulador el
30/09/2026 con dos cuentas reales** (cuenta 1 y cuenta 2 QA; credenciales en `.secrets/qa-user.md`
y `.secrets/qa-user2.md`, fuera del repo).

## Por qué esta fase y no otra

Hoy sólo se puede añadir un amigo **escribiendo su email exacto** — una barrera enorme que
hace que la parte social esté prácticamente muerta aunque exista. Y el feed es lo que
convierte la app en algo que se abre a diario sin esfuerzo (es la razón por la que existe
Letterboxd). Va después de F1 porque **no hay nada interesante que mostrar en un feed
hasta que existe el diario y las notas**.

---

## Decisiones (cerradas el 30/09)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D2.1 | **Modelo social** | (a) Amigos simétricos · (b) Seguir asimétrico | **(b) seguir** | ✅ (b) (30/09) |
| D2.2 | **Privacidad por defecto** | (a) Público · (b) Privado por opción | **(a) público** con interruptor | ✅ (a) (30/09) |
| D2.3 | **Qué se publica** | (a) Todo · (b) Solo hitos | **(b) hitos** + **posts del usuario** (ampliación del propietario) | ✅ (30/09) |
| D2.4 | **Buscar usuarios** | (a) Cliente · (b) Cloud Function · (c) Índice + reglas | **(c)** índice `usernames` + reglas, sin Blaze | ✅ (c) (30/09) |
| D2.5 | **Moderación** | (a) Nada · (b) Reportar + bloquear | **(b) mínimo** | ✅ (b) (30/09) |
| D2.6 | **¿Comentarios o reacciones?** | (a) Comentarios · (b) Me gusta | **(b) me gusta en F2** | ✅ (b) (30/09) |
| D2.7 | **Chat existente** | — | Congelado: se oculta sin borrar código ni datos; su destino se decide en la auditoría de F2 | ✅ (30/09) |
| D2.8 | **Feed sin Cloud Functions y sin índice compuesto** | (a) Requerir el índice `feed(authorUid, createdAt)` · (b) Consulta sin `orderBy` + orden en cliente | **(b)**, coherente con D2.4: la SDK exige el índice compuesto para `whereIn` + `orderBy`; sin él la escucha falla con `FAILED_PRECONDITION` y la pestaña mostraba «No se pudo cargar el feed». El índice queda **declarado** en `firebase/firestore.indexes.json` y hay script de despliegue, pero la service account no tiene `datastore.indexes.create` (403) | ✅ (b) (30/09) + deuda |
| D2.9 | **Bloqueo y feed** | (a) Confiar solo en reglas · (b) Filtrar en cliente | **(b)**: el listado del feed es público *con sesión* por diseño (es la página pública del producto), así que el bloqueo se completa en cliente (`blockedUids` → `FeedQueryPlanner.filterBlocked`). Así se cumple lo que promete el diálogo («dejaréis de veros en el feed») | ✅ (b) (30/09) |
| D2.10 | **Alias de búsqueda para usuarios pre-F2** | (a) Nada · (b) Backfill | **(b)**: la búsqueda consulta el espacio de alias `usernames/~x`; los 9 usuarios anteriores a F2 no lo tenían y eran **invisibles** al buscar (`qad` encontraba a QADos pero `qa g` no encontraba a QA GameVision) | ✅ (b) (30/09) |
| D2.11 | **`createdAt` del feed** | (a) Solo `Number` · (b) `Number` + `Timestamp` | **(b)**: `serverTimestamp()` llega como `Timestamp`, no `Number`; leerlo solo como número dejaba `createdAt = 0` en todas las entradas y el feed se ordenaba por id de documento en vez de por recencia | ✅ (b) (30/09) |

---

## Tareas

- [x] T2.1 Nombre de usuario único + búsqueda por username (índice + reglas, sin Blaze — D2.4)
  — `SocialRepository.searchByUsername` (range query sobre alias) + backfill de alias (T2.10).
- [x] T2.2 Perfil público: biblioteca, estadísticas, Top 4, listas visibles — `PublicProfileScreen`.
- [x] T2.3 Seguir / dejar de seguir (asimétrico) + contadores (aggregate `count()`).
- [x] T2.4 Feed de actividad (hitos + posts) con listener en vivo y chunks `whereIn`.
- [x] T2.5 Top 4 editable y visible en el perfil — `TopGamesEditor` (persiste `topGameIds`).
- [x] T2.6 Listas curadas: crear, públicas o privadas, con progreso — `gamelist` / `gamelist_private`.
- [x] T2.7 Me gusta en entradas del feed (`feed/{id}/likes/{uid}` + `likesCount`).
- [x] T2.8 Reportar y bloquear usuario — `reports/{id}`, `blocks/{me}/people/{uid}`.
- [x] T2.9 Migrar amigos simétricos → aristas de seguimiento (8 amistades → 16 aristas, 0 fallos).
- [x] T2.10 Alias de búsqueda `usernames/~x` para los usuarios pre-F2 (9 alias) — D2.10.

---

## Criterios de aceptación (con evidencia)

- [x] **CA2.1** Dos usuarios de prueba se encuentran buscando por **nombre de usuario** y ven sus
  perfiles. → `qad` → QADos (`Siguiendo`), `qa g` → QA GameVision; perfil público abierto con
  biblioteca y contadores (1 seguidor / 1 seguido). Tras D2.10.
- [x] **CA2.2** El feed muestra la actividad del otro usuario y **se actualiza sin recargar**.
  → Con el feed de la cuenta 2 abierto, un hito publicado por la cuenta 1 (con **su token de
  usuario**, reglas reales) apareció como tarjeta «Completó — The Legend of Zelda: The Minish Cap 🎉»
  sin tocar la app; también aparecieron sin recargar el post propio y el cambio de contador de
  me gusta. Orden por recencia verificado tras D2.11.
- [x] **CA2.3** El Top 4 aparece en el perfil y se puede cambiar. → selección en `TopGamesEditor`
  persistida en `users/{c1}.topGameIds` (`["326243","27418"]`) y **vista desde c2** en el perfil
  público, con el Top 4 poblado: «Top 4» → Elden Ring + The Legend of Zelda: The Minish Cap
  (biblioteca 3, listas 0).
- [x] **CA2.4** Una lista privada **no** es visible desde otro usuario. → `gamelist_private`
  **Reserva retirada (30/09, cierre pre-F3):** verificado también **por UI** — creada desde
  *Editar perfil → Crear una lista* una lista privada («QA privada F2») y una pública
  («QA publica F2») con c1; la admin confirma `users/{c1}/gamelist_private` vs `users/{c1}/gamelist`
  y desde c2 el perfil de c1 muestra «Listas (1)» solo con la pública.
  deniega el listado sin sesión (403 verificado por REST); el perfil público solo pinta listas
  públicas (`loadLists(..., onlyPublic = true)`). *Reserva:* la lista privada se verificó por
  reglas + código, no con una lista creada a mano desde la UI (ver deuda 4).
- [x] **CA2.5** Los amigos migrados del modelo antiguo siguen apareciendo. → T2.9: 16 aristas
  creadas y `users/{x}/friends` vaciado; contadores del perfil y feed de seguidos coherentes.
- [x] **CA2.6** La búsqueda ya **no expone** el listado completo de perfiles. → REST sin
  autenticar: `users`, `feed`, `following` y `gamelist_private` responden **403**;
  `users/{uid}` también exige sesión (el perfil raíz no es anónimo).
- [x] **CA2.7** Tests de seguimiento y visibilidad → `SocialLogicTest` (30 tests; 118 en total).
- [x] **CA2.8** Un post con texto se publica y recibe me gusta de la otra cuenta. → post de la
  cuenta 1 con `likesCount = 1` y `feed/{id}/likes/{uid2}` verificado por service key; el hito de
  estado y las listas también alimentan el feed.
- [x] **CA2.9** El bloqueado no ve el perfil ni interactúa. → `blocks/{c2}/people/{c1}` creado;
  la lectura del perfil del bloqueador falla (tarjeta del feed con autor sin resolver) y el post
  del bloqueado **desaparece del feed** tras D2.9; reglas cortan `get` y los me gusta.
- [x] **CA2.10** El reporte queda almacenado. → `reports/{id}` con `reason = spam`,
  `reporterUid = c2`, `targetUid = c1` y `createdAt` de servidor, leído con la service key.

---

## Riesgos (revisados al cierre)

| Riesgo | Resolución real |
|---|---|
| La búsqueda sin Function obliga a leer todos los perfiles | Se implementó el índice de alias + range query; el coste es 1 lectura por resultado (límite 20) |
| El modelo nuevo rompe los amigos existentes | T2.9 ejecutada con backup; 16 aristas, 0 fallos, re-ejecución idempotente limpia |
| Contenido inapropiado | D2.5: reportar + bloquear implementados y verificados (CA2.9/CA2.10) |

## Cómo se verifica

Dos cuentas reales en el emulador + comprobación REST sin autenticar. **Hecho**: Pixel 9 API 36,
`adb` por UI (uiautomator) + REST con la service key; resultados arriba CA por CA.

---

## 🔍 Auditoría de cierre de fase

> Protocolo: [auditoria-de-cierre.md](../metodologia/auditoria-de-cierre.md).
> Particularidad de F2: contenido de terceros y datos nuevos (username, seguidores, feed), por lo
> que el bloque 4 revisa las reglas y el bloque 6 los estados de feed vacío y perfil privado.

**Informe**

```
Fase: F2 — Social             Fecha: 30/09/2026
Resultado: 🟡 Apta con reservas (pendiente validación del propietario)
Bloques:  1 ✅  2 ✅  3 ✅  4 ✅  5 ✅  6 🟡
```

- **1 Compilación y build** ✅ — `assembleDebug` y `assembleRelease` (R8 + lintVital) verdes.
- **2 Tests** ✅ — `testDebugUnitTest` **118/118** (30 de lógica social: seguimiento, visibilidad,
  merge/orden, filtro de bloqueados, hitos, listas y compositor de posts). Instrumentados: no se
  ejecutaron en esta sesión (deuda 5).
- **3 Calidad estática** ✅ — `lintDebug` limpio; el fix de lint previo (uid como estado composable)
  sigue en pie.
- **4 Arquitectura y consistencia** ✅ — SSOT respetado (`SessionRepository` → `UserViewModel` →
  `SocialViewModel`); la UI no toca Firestore; las reglas se despliegan solo con la suite verde
  (**94 checks OK**). Sin DTOs de API en la UI.
- **5 Documentación y trazabilidad** ✅ — este documento, `AGENTS.md` §4/§6, `firebase/firestore.indexes.json`,
  scripts de migración/backfill/índices y el registro de deuda actualizados.
- **6 Experiencia y estados** 🟡 — revisados: **feed vacío** (estado con llamada a la acción),
  **feed en vivo**, **error de feed** (se reprodujo el fallo real y su mensaje antes del fix),
  **perfil público propio y ajeno**, **perfil privado** (lectura denegada → autor sin resolver),
  diálogos de **reporte** y **bloqueo**. Revisados además en el cierre pre-F3 (30/09):
  **offline** (banner + caché + like que sincroniza) y **aviso de perfil no disponible**
  (deudas 3 y 6 cerradas).

**Hallazgos y acciones**

- El feed **no cargaba en producción**: faltaba el índice compuesto `feed(authorUid, createdAt)` y
  la service account no puede crearlo (403). → Fallback en cliente (D2.8) + índice declarado y
  script de despliegue listo; **crear el índice desde consola** (enlace que da el propio logcat).
- Guardar en *Editar perfil* **vaciaba el perfil** si antes se había usado la pantalla de login
  (los `formFields` del login hacían que el prefill no se ejecutara). → `clearFormFields()` al
  iniciar sesión + datos de la cuenta 2 reparados. **Bug de pérdida de datos, corregido**.
- El bloqueo **no quitaba los posts del bloqueado** del feed pese a lo que prometía el diálogo. →
  `FeedQueryPlanner.filterBlocked` + refresco al bloquear (D2.9).
- Los usuarios pre-F2 **no aparecían en la búsqueda** (sin alias). → Backfill de 9 alias (D2.10).
- El feed **no ordenaba por recencia** (`createdAt` leído solo como `Number`). → D2.11.
- Espera del handoff incorrecta: `users/{uid}` **no** es legible sin autenticar; las reglas exigen
  sesión para el perfil raíz. → Documentado con el resultado real (403).

**Reservas** → deuda con disparador, abajo.

Firma del agente: **GameVision (agente)** · Validado por el propietario: **✅ validada (01/10/2026)**

---

## Registro de decisiones

D2.1–D2.7 cerradas el 30/09 con el propietario. **D2.8–D2.11** se añadieron durante la
verificación E2E del mismo día (fallos reales encontrados al usar la app con dos cuentas).
Detalle y modelo de datos en el [plan de F2](../plan/FASE-2-SOCIAL-2026.md).

## Deuda técnica (con disparador)

1. ~~Índice compuesto `feed(authorUid ASC, createdAt DESC)` en producción~~ **CERRADO (01/10)** —
   creado desde consola por el propietario y **verificado por REST**: la consulta
   `whereIn(authorUid)` + `orderBy(createdAt DESC)` devuelve 200 ordenado por recencia (antes:
   `FAILED_PRECONDITION`). El fallback cliente de D2.8 **se conserva** como resiliencia.
2. **`reports` visibles solo en consola** — no hay pantalla de moderación. *Disparador:* cuando
   lleguen reportes reales (o en F4).
3. ~~Aviso visual de perfil privado~~ **CERRADO (30/09, cierre pre-F3)** — `PublicProfileScreen`
   muestra ahora un estado «Perfil no disponible» cuando la lectura se deniega (privacidad o
   bloqueo); antes quedaba en «Cargando perfil…» indefinido e incluso **crasheaba** la app al
   abrir el perfil del bloqueador (`PERMISSION_DENIED` no controlado). La búsqueda ya no se tumba
   si un perfil individual está denegado.
4. **Lista privada creada a mano desde la UI** para CA2.4 (hoy verificado por reglas + código), y
   **hito de reseña desde la UI** (el panel de biblioteca no responde a `input tap`;
   ver deuda 5).
5. ~~Instrumentados de Social~~ **CERRADO (30/09, cierre pre-F3)** — `SocialIntegrationTest`
   (`app/src/androidTest`): like, bloqueo que filtra el feed e hito de reseña contra Firestore real
   y reglas reales; **10/10 conectados** en verde. El flujo de UI (ficha→reseña→tarjeta) sigue
   verificado a mano.
6. ~~Offline y errores de red en Social~~ **CERRADO (30/09, cierre pre-F3)** — verificado E2E en
   modo avión real con dos cuentas: el feed sirve caché sin crash, el like sincroniza al
   reconectar, el post offline falla con mensaje claro y **conserva el texto** en el compositor
   (no se encola: se reintenta a mano). Añadido banner «Sin conexión · mostrando datos guardados»
   al feed (observa conectividad real con `NetworkCallback`; exige red **validada**).

## Errores acumulados por las interrupciones (30/09)

Errores encontrados al retomar la fase tras la pausa, todos corregidos y con test o evidencia:

1. **Crash Hilt de la pestaña Social** (defaults `viewModel()` en rutas) — ya estaba corregido; se
   **re-verificó** hoy: la pestaña abre sin `FATAL` y sin defaults en la firma.
2. **Feed que no cargaba** (`FAILED_PRECONDITION` por índice compuesto ausente) → D2.8.
3. **Bug propio durante el fix**: el refactor dejó `fun listen()` **sin invocar**, así que el flow
   quedaba en `awaitClose` y el feed en carga infinita. Cazado comparando UI y logcat; corregido.
4. **Pérdida de datos al guardar el perfil** (prefill saltado por `formFields` del login) → fix +
   reparación de los datos de la cuenta 2.
5. **Búsqueda ciega para usuarios pre-F2** (sin alias) → backfill (D2.10).
6. **Bloqueo incompleto en el feed** → filtro cliente (D2.9).
7. **Orden del feed roto** (`createdAt` como `Timestamp`) → D2.11.
8. **Expectativa errónea del handoff** sobre REST anónimo del perfil → docs corregidos.
9. **Metodológicos de la sesión** (para no repetirlos):
   - Los ficheros del repo tienen **finales de línea mixtos por fichero** (unos LF, otros CRLF, y
     algunos **mezclados dentro del mismo fichero**): editar con `node -e` + detección de `\r\n`
     por bloque y **verificar con `grep`** tras cada edición. `git` normaliza al comitear
     (`core.autocrlf=true`), así que los diffs salen limpios.
   - Scripts de parche con **comillas invertidas** deben ir por *heredoc citado* (`<<'EOF'`): la
     shell las interpreta dentro de un `node -e "..."`.
   - `input text` de `adb` **no traga paréntesis** (shell del dispositivo); los espacios van como `%s`.
   - Imprimir ids **truncados** llevó a borrar un documento inexistente (el DELETE devolvió 200 por
     idempotencia): usar el id completo en operaciones destructivas.
   - El teclado del emulador se come el primer `tap` de la barra inferior: cerrar con `BACK` antes.

## 💡 Ideas registradas (28/09/2026)

- **Comunidad de logros (estilo foro)**: feed de logros conseguidos, secciones de «coleccionados»,
  y **porcentaje de jugadores por logro**.
  ⚠️ Requiere **fuente de datos de logros por plataforma (Steam/PSN/Xbox)** — RAWG/IGDB no la ofrecen;
  análisis pendiente antes de planificar tareas. Vinculado a D2.x (feed/moderación) y a la vitrina de
  «Coleccionados» de F1.
