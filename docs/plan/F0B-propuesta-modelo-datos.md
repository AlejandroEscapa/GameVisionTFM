# F0-B — Propuesta: modelo de datos de la biblioteca, preparado para escalar

> **Estado:** ✅ APROBADA (28/09/2026) — ver §12 (registro de decisiones y plan de ejecución). Implementación en curso.
> **Base:** tres investigaciones técnicas hechas el 28/09/2026 sobre la documentación oficial de Firestore
> (modelado de datos · costes/cuotas/agregación · reglas/identidad/operación). Material crudo en
> [`docs/investigacion-2026/fuentes-tecnicas/`](../investigacion-2026/fuentes-tecnicas/).
> **Qué es:** el diseño definitivo de las tres capas de datos de F0-B (ficha · partida · sesión), más
> identidad, reglas, estrategia de escrituras/offline, costes y operación, resueltos con criterio senior
> para que el sistema aguante si el producto crece a miles o decenas de miles de usuarios sin "petar".

---

## 0. Resumen para decidir

Tres decisiones **cambian** respecto a lo que hablamos, cada una con su motivo; el resto queda afinado:

1. **Clave de usuario: de `email` a `uid`** de Firebase Auth (`users/{uid}`). El email pasa a ser un
   *campo*, no la ruta. Motivos: (a) saca PII de las rutas; (b) un cambio de email no obliga a migrar
   datos; (c) las reglas pasan a ser una comparación barata y segura (`request.auth.uid == userId`,
   sin lecturas extra). Coste de hacerlo ahora: bajo (hoy hay 3 usuarios). Coste de hacerlo después:
   crece con cada dato. Es *el* momento.
2. **El "Historial" (juegos vistos recientemente) sale de Firestore y pasa a local** en el dispositivo.
   Motivo: hoy es 1 escritura en Firestore por cada ficha abierta; no aporta nada que deba viajar
   entre dispositivos y a escala es puro ruido de escrituras y de coste.
3. **Se añade un documento de estadísticas agregadas** (`stats/summary`) mantenido con incrementos
   atómicos. Motivo: las estadísticas de F1 (horas, distribución de notas, contadores por estado) no
   deben obligar a leer cientos de documentos del diario; con esto pasan a **1 lectura**. Es el patrón
   oficial de contadores (`FieldValue.increment`), y encaja con escrituras offline.

**Se mantiene** (y queda afinado): las 3 capas, la nota y reseña **por juego**, la instantánea de
nombre/portada en la ficha, y el flujo partida→sesión.

---

## 1. Criterios de diseño (las reglas del juego)

1. **Modelar por consultas**: primero las pantallas y sus consultas, luego los campos.
2. **Una fuente de verdad por dato**; las copias solo existen para *display* (instantánea) o como
   *agregados derivables* (contadores), y siempre con un camino de reparación.
3. **Páginas pequeñas con cursores**; `offset` prohibido (se factura igualmente).
4. **Sin documentos calientes**: IDs dispersos (auto-ID o ID de juego), datos aislados por usuario.
5. **Escrituras offline-seguras**: lotes (*batches*) + `increment`; las transacciones **no** funcionan
   sin red y no se usan en caminos calientes.
6. **Los datos del usuario no caducan solos** (nada de TTL); el crecimiento se maneja **paginando**.
7. **El coste son las lecturas**: cada pantalla debe minimizar documentos leídos; la caché persistente
   de Android y los listeners de vida larga son parte del diseño, no un extra.
8. **Reglas simples y baratas**; App Check antes de abrir al público.
9. **Operar desde el día uno**: alertas de presupuesto, backups programados y tests de reglas.

---

## 2. Estructura final

```
users/{uid}
  ├─ (campos de perfil)
  ├─ library/{gameId}      → la ficha: una por juego
  ├─ logs/{logId}          → las partidas: una por run
  ├─ sessions/{sessionId}  → el diario: una por sesión
  └─ stats/summary         → agregados (documento único)
```

### 2.1 `users/{uid}` — perfil

| Campo | Tipo | Notas |
|---|---|---|
| `nameSurname`, `username`, `description`, `country` | String | como hoy |
| `email` | String | **dato**, no ruta; se conserva para mostrar y para F2 |
| `imageUrl` | String? | F0/T0.12 (Firebase Storage) |
| `createdAt`, `updatedAt` | Timestamp | reloj de servidor |

### 2.2 `library/{gameId}` — la ficha (una por juego)

`gameId` = id de RAWG en string (documento con ID conocido, estable y no secuencial en la práctica).

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | String | redundante a propósito: facilita export y consultas de grupo futuras |
| `status` | String | `jugando` · `completado` · `dominado` · `en_pausa` · `retirado` · `abandonado` · `deseado` |
| `rating` | Number? | **0,5–5,0 en pasos de 0,5** (medias estrellas); `null` = sin nota |
| `review` | String? | reseña por juego; **excluida de índices** (texto largo) |
| `favorite` | Bool | para el Top 4 de F2 |
| `lastPlatform` | String? | copia de la plataforma de la última partida (permite filtros sin joins) |
| `addedAt` | Timestamp | fecha de alta |
| `startedAt` / `finishedAt` | Timestamp? | hitos (primera vez jugando / al completar) |
| `name`, `coverUrl`, `released`, `genres` | String?/String?/String?/List | **instantánea** del catálogo: permite pintar la biblioteca sin red y sin pedir cada juego a RAWG (evita el N+1 actual) |
| `minutesTotal` | Int | minutos jugados (contador agregado; ver §5) |
| `updatedAt` | Timestamp | reloj de servidor |

### 2.3 `logs/{logId}` — las partidas (auto-ID)

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | String | |
| `runIndex` | Int | 1ª, 2ª, 3ª partida del juego |
| `startedOn` / `finishedOn` | Timestamp? | fechas de la partida |
| `platform` | String? | T1.6: plataforma jugada por partida |
| `minutes` | Int? | horas "de bolsillo" si el usuario no apunta sesiones |
| `note` | String? | nota corta de esa run; **excluida de índices** |
| `createdAt` | Timestamp | reloj de servidor |

### 2.4 `sessions/{sessionId}` — el diario (auto-ID)

| Campo | Tipo | Notas |
|---|---|---|
| `gameId` | String | |
| `logId` | String? | partida activa en el momento de apuntar |
| `date` | Timestamp | **día jugado** (editable; por defecto hoy). Es el campo de orden del diario |
| `minutes` | Int | |
| `note` | String? | detalle corto opcional |
| `createdAt` | Timestamp | reloj de servidor (desempata dentro del día) |

### 2.5 `stats/summary` — agregados (documento único por usuario)

| Campo | Tipo | Notas |
|---|---|---|
| `gamesTotal` | Int | nº de fichas |
| `gamesByStatus` | Map<String, Int> | contador por estado (se ajusta con ±1 por transición) |
| `minutesTotal` | Int | suma de minutos (sesiones + partidas con `minutes`) |
| `sessionsTotal` | Int | nº de sesiones apuntadas |
| `ratingSum`, `ratingCount` | Number/Int | media exacta de notas (suma en pasos de 0,5) |
| `lastActiveAt`, `updatedAt` | Timestamp | |

**Camino de reparación:** si un contador desviara (p. ej. un cambio hecho a mano), se recalcula con
**consultas de agregación** (`count()`, `sum()`) — 1 lectura por cada 1.000 entradas de índice.

---

## 3. Índices

- Los índices de **campo único son automáticos**; no hace falta hacer nada para ordenar por un campo.
- Índices **compuestos** necesarios (pocos, y versionados en `firebase/firestore.indexes.json`):

| # | Colección | Índice | Para qué |
|---|---|---|---|
| 1 | `library` | `status` (asc) + `addedAt` (desc) | lista filtrada por estado, orden por fecha de alta |
| 2 | `library` | `status` (asc) + `rating` (desc) | ordenar por nota dentro de un estado (si se aprueba esa vista) |
| 3 | `logs` | `gameId` (asc) + `startedOn` (desc) | partidas de un juego |
| 4 | `sessions` | `gameId` (asc) + `date` (desc) | diario de un juego (añadir solo si la pantalla lo pide) |

- **Exenciones de índice**: `review` y `note` (textos largos) — ahorra entradas de índice
  (límite: 40.000/documento) y evita el truncado de indexado a 1.500 bytes.

---

## 4. Reglas de seguridad (borrador)

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /library/{gameId}    { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /logs/{logId}        { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /sessions/{sessionId}{ allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /stats/{docId}       { allow read, write: if request.auth != null && request.auth.uid == userId; }
    }
  }
}
```

- **Sin `get()`/`exists()`** en el camino caliente: cada uno es una **lectura facturable** y suma latencia.
- Recuerda: **las reglas no filtran** consultas (son "todo o nada") → por eso todo cuelga del usuario.
- El social (F2) se resolverá con colecciones públicas / Cloud Functions; **no se abre nada hoy**.
- Tests de reglas con el **Emulator Suite** (CI), antes de tocar producción.

---

## 5. Escrituras, offline y consistencia

- Apuntar una sesión = **lote de 2 escrituras**: `sessions` + `stats/summary` (increment). Los lotes
  **funcionan offline** (se encolan y se aplican al volver la red); las **transacciones, no**.
- Cambio de estado = 1 escritura + ajuste de contadores (decremento del estado viejo, incremento del
  nuevo) en el mismo lote.
- `FieldValue.increment` es **atómico** y compatible con la cola offline.
- `serverTimestamp()` para fechas de sistema; la fecha del diario es del usuario (editable).
- Multi-dispositivo: **last-write-wins** del servidor (suficiente: los datos son de un solo usuario).
- `minutesTotal` puede recibir de dos fuentes (sesiones y `minutes` de partida): la reparación con
  agregaciones está definida en §2.5 para que un desfase nunca sea un problema real.

---

## 6. Costes (orden de magnitud, con lo verificado)

- **Cuota gratuita**: 50.000 lecturas/día · 20.000 escrituras/día · 1 GiB (por proyecto; se resetea a diario).
  Un arranque modesto (decenas–cientos de usuarios activos) cabe **gratis**.
- **Precio** (a validar en la tabla viva el día del cálculo): regional ≈ **0,03 $/100k lecturas** ·
  **0,09 $/100k escrituras**; multi-región ≈ **el doble**.
- **Escenario 10.000 usuarios activos** (según la investigación): si se lee sin cuidado, ~100M
  lecturas/mes → **~30–65 $/mes** según región. Con las palancas de abajo, baja notablemente.
- **Palancas**: ubicación **regional** (≠ multi-región), **paginación** (nunca offset), **listeners de
  vida larga** (evitar re-attach por pantalla), **stats desde el resumen**, **caché persistente** (Android).
- **Verificar en consola**: la **ubicación actual** de la base (no se puede cambiar después) y la tabla
  de precios vigente (la página oficial mostraba tramos).
- **Alertas de presupuesto**: configurar sí o sí — pero ojo: **avisan, no cortan el gasto**. El "freno
  duro" (acción automática al superar umbral) se añade antes de abrir al público.

---

## 7. Operación — "que no pete" (riesgos → mitigación ya incluida)

| Riesgo a escala | Mitigación en este diseño |
|---|---|
| Hotspots de escritura | IDs dispersos (auto-ID/ID de juego), datos aislados por usuario; rampa 500/50/5 al estrenar colecciones con picos |
| Colección con campo secuencial indexado (500 esc/s) | Irrelevante al ser colecciones **por usuario**; regla: no crear campos secuenciales indexados en colecciones compartidas |
| Lecturas que inflan la factura | Cursores, listeners largos, resumen de stats, caché persistente, nada de contar en el render |
| Colecciones que crecen años (diario) | Paginación por `date` desde el día 1; partición por año/archivo **solo si** llega a hacer falta (camino marcado) |
| Abuso externo (bots, cuotas) | **App Check** antes del lanzamiento público; reglas estrictas por usuario |
| Borrados masivos / subcolecciones | No hay operaciones masivas previstas; TTL no aplica (datos de usuario); los borrados de subcolección se hacen por lotes |
| Factura sorpresa | Alertas de presupuesto + patrones prohibidos documentados + freno duro pre-lanzamiento |
| Pérdida de datos | **Backups programados** (diario) antes de la migración y en producción |
| Reglas rotas sin darse cuenta | **Tests de reglas** con Emulator Suite en CI; test de volumen opcional (sembrar 10k documentos en el emulador) |

---

## 8. Migración (T0.11)

1. **Backup previo** (export de la base) — sin excepciones.
2. Mapa `email → uid` (consola de Auth / Admin SDK). Revisar documentos huérfanos sin cuenta Auth.
3. Por cada usuario real migrar:
   - `playedlist` → `library` con estado inicial **(a decidir contigo — es la única pregunta abierta)**,
   - `wishlist` → `library` con `status: deseado`,
   - `favorites` → `favorite: true`,
   - `history` → **se descarta** (pasa a ser local en el dispositivo),
   - `friends`/`messages` → migrados a la clave `uid`.
4. Borrar los documentos basura `aa` y `ee` (decisión D0.2 ya cerrada).
5. Verificar con el criterio CA0.4 (los juegos de `alex@gmail.com` siguen ahí).
6. Herramienta: script one-shot con Admin SDK (queda en `scripts/`, sin secretos) — o edición manual en
   consola si preferimos cero código para 3 usuarios. **Con backup previo en ambos casos.**

---

## 9. Plan por etapas (para no pagar hoy lo de mañana)

- **Etapa A — ahora (→ ~10.000 usuarios):** todo lo de esta propuesta. Coste esperado: gratis/decenas de € al mes.
- **Etapa B — tracción (10k–100k):** App Check en producción, re-agregados programados (Cloud Function),
  auditoría de índices, export a BigQuery para analítica de producto, freno duro de presupuesto,
  partición del diario por año **solo si hiciera falta**.
- **Etapa C — social/escala (>100k):** Cloud Functions para feed/búsqueda (ya previstas en F2), perfiles
  públicos denormalizados, contadores con shards si aparecen métricas globales, multi-región solo si
  la disponibilidad lo exigiera.

---

## 10. YAGNI — lo que NO haremos ahora (a propósito)

Contadores distribuidos · TTL · BigQuery · Cloud Functions · partición del diario por años ·
colección `games` en Firestore (la caché de catálogo sigue en Room, local) · multi-región.

---

## 11. Lo que necesito de ti para cerrar F0-B

1. ¿Aprobamos **`users/{uid}` ahora**? (recomendación: sí — es el momento más barato).
2. ¿El **"Historial" pasa a local** en el dispositivo? (recomendación: sí).
3. **Migración**: los juegos de tu vieja lista "Juegos jugados" → ¿con qué estado entran?
   (Completado / Jugando / Otro — con tu respuesta queda cerrado).
4. ¿Algo que quieras cambiar de **campos o índices** antes del visto bueno?

Con tu OK: implemento (T0.6–T0.10), ejecuto la migración (T0.11) y registramos los ADRs que toca
(escala 0,5–5 con medias estrellas, clave `uid`, contadores de stats).

---

## Anexo — confianza de la investigación y huecos declarados

- **Confianza alta**: estructura con subcolecciones, IDs dispersos (scatter), índices compuestos para
  igualdad+rango, cursores (offset prohibido), agregaciones `count/sum`, cuotas gratuitas diarias,
  `get()` en reglas facturado como lectura, persistencia offline activada por defecto en Android.
- **Confianza media**: tarifa exacta regional (la tabla oficial mostraba tramos parcialmente ilegibles),
  límite de 500 documentos por lote (ya no aparece en la página de límites oficial), comportamiento
  offline exacto de batches/transacciones (documentado, pero sin cita verbatim capturada).
- **Pendiente/baja**: detalle verbatim de la facturación de listeners en tiempo real; validar ubicación
  y precios **el día del cálculo** en la consola y en la página oficial de precios.

---

## 12. Registro de aprobación (28/09/2026)

| # | Decisión | Resultado |
|---|---|---|
| **D-F0B-1** | Clave de usuario `users/{uid}` (email como campo) | ✅ Aprobado |
| **D-F0B-2** | El "Historial" pasa a local en el dispositivo | ✅ Aprobado |
| **D-F0B-3** | Migración: `playedlist` → estado `jugando`; `wishlist` → `deseado`; `favorites` → `favorite: true`; `history` → descartado | ✅ Aprobado (revisable antes de ejecutar) |
| **D-F0B-4** | Campos e índices | ✅ Delegado al agente: §2–§3 tal cual; los índices compuestos se añaden a `firestore.indexes.json` junto con las pantallas que los usan |

**Escala de nota:** confirmada **0,5–5,0 en pasos de 0,5** (medias estrellas). Registrada en
[ADR-0003](../metodologia/adr/0003-escala-medias-estrellas.md), que supersede al ADR-0002.

### Plan de ejecución

| Bloque | Contenido | Estado |
|---|---|---|
| **B1** | Capa de datos: modelos + `LibraryRepository` (Result, lotes, incrementos) + tests de lógica pura | 🟢 En curso (código en `data/library/`; 29 tests verdes) |
| **B2** | Adopción: sesión `uid` + ViewModels/pantallas (listas leídas del modelo nuevo; menú "Añadir" escribe la ficha) + reglas de seguridad (en el mismo paso, para no romper la app actual) + índices | ⬜ |
| **B3** | Migración: backup → mapa email→uid → migración de listas → limpieza `aa`/`ee` → verificación CA0.4 | ⬜ |
