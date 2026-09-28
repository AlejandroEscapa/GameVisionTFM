# Dimension 02: Costes, cuotas y agregación en Firestore

> Dimensionamiento económico y operativo de Google Cloud Firestore (modo nativo) para GameVision.
> Fecha de consulta de todas las fuentes: **2026-09-28**. Salida en español.
> Escenario base a dimensionar: 10.000 usuarios activos diarios, ~300 juegos por biblioteca, ~10 escrituras de sesión/semana y pantallas que listan biblioteca y diario (colecciones `users/{email}/library`, `logs`, `sessions`).

---

## Current State

**Modelo de facturación (estado actual del conocimiento).**
Firestore cobra por *operaciones sobre documentos* (lecturas, escrituras, borrados), por *almacenamiento* y por *transferencia de salida* (egress). No hay coste por consulta como tal: una consulta que devuelve N documentos genera N lecturas facturables; una escritura de documento = 1 escritura; un borrado = 1 borrado. El almacenamiento incluye el *overhead* de índices automáticos, compuestos y metadatos (no solo el payload). [^1][^2][^9]

**Capa gratuita.**
- Plan **Spark** (gratis, sin tarjeta) y el nivel gratuito de **Blaze** (pago por uso) comparten las mismas cifras diarias/gratuitas: **1 GiB de almacenamiento, 50.000 lecturas/día, 20.000 escrituras/día, 20.000 borrados/día y 10 GiB/mes de tráfico saliente**. [^3][^4]
- Solo se permite **una base de datos gratuita por proyecto** ("Firestore allows exactly one free database per project"). [^1]
- Las cuotas gratuitas son **diarias** (se reinician cada día) para operaciones y **mensuales** para egress/almacenamiento. [^4][^5]

**Precios por operación (dos niveles principales).**
La página oficial muestra tarifas que varían según **ubicación (multi-región vs. regional)**: la multi-región (*nam5* y equivalentes) cuesta aproximadamente el **doble** que una región individual. Cifras recurrentes y corroboradas en varias fuentes: multi-región ≈ **$0.06 / 100.000 lecturas**, **$0.18 / 100.000 escrituras**, **$0.02 / 100.000 borrados**, **$0.18 / GiB·mes** de almacenamiento; regional (p. ej. `us-central1`) ≈ la mitad: **$0.03 / 100.000 lecturas**, **$0.09 / 100.000 escrituras**, **$0.01 / 100.000 borrados**. [^1][^6][^7][^8]
> ⚠️ La propia página oficial se presentó en los resultados con columnas parcialmente ilegibles (`"$0.03 $0.024 $0.018"`), lo que sugiere **≥3 tramos de precio por región**. **Verificar la tabla viva antes de presupuestar**: los valores regionales exactos de almacenamiento no quedaron confirmados verbatim.

**Listeners en tiempo real.**
No se pudo obtener una **cita verbatim oficial** sobre el detalle de facturación de listeners en estas búsquedas (los resultados fueron ruidosos). El comportamiento documentado de Firestore es que el listener se factura como **lecturas**: se cobra por cada documento nuevo/añadido que entra en el resultado (y, en muchas configuraciones, una lectura mínima al adjuntar la query), mientras que los documentos que *salen* del resultado no se cobran. **Confianza baja/media — pendiente de verificación en la página oficial de billing.** [^1][^10]

**Consultas de agregación (`count()`, `sum()`, `avg()`).**
Firestore soporta agregaciones en el servidor. `count()` está disponible de forma general desde antes; `sum()` y `avg()` se anunciaron el **8 de noviembre de 2023**. La facturación es notablemente más barata que traer todos los documentos: **"se cobra una lectura de documento por cada lote de hasta 1.000 entradas de índice que coincidan con la consulta"**, con un **mínimo de una lectura** incluso con 0 coincidencias. [^11][^12]

**`FieldValue.increment`.**
Es un operador de escritura atómico pensado para contadores: incrementa/decrementa un campo numérico *en el servidor*. No se localizó una cita verbatim en estas búsquedas; el comportamiento documentado es atómico y funciona con la caché offline (la operación se encola y se reaplica), facturándose como una escritura normal. **Confianza media — verificar en la doc oficial.** [^13]

**Escrituras por lote vs. transacciones.**
- Límite común: **máximo 500 operaciones por lote/transacción**; una transacción **no puede modificar más de 500 documentos** y debe completarse en **270 segundos**. [^14][^15][^16]
- Semántica: los **batches** son "todo o nada" pero *no* permiten read-then-write; las **transacciones** sí (lectura-consistente + escritura). [^15]
- Offline: los *batched writes* se encolan localmente y se ejecutan al recuperar conexión; las **transacciones fallan si el cliente está offline** (requieren conectividad para su read-then-write consistente). *Confianza media — patrón conocido, sin verbatim capturado.* [^16]
- Tamaño de request: **límite de 10 MiB por petición API**. [^16]

**Cuotas y límites de servicio (por base de datos).**
- Tamaño máximo de documento: **1 MiB (1 MB)**. [^17]
- Límite práctico de escritura sostenida sobre **un mismo documento**: ~**1 escritura/segundo**; por encima aparecen hotspots → usar contadores distribuidos / sharding. [^18]
- Tamaño máx. de request API: **10 MiB**; timeout de transacción **270 s**. [^16]
- Existe un límite de **configuraciones de campos/índices** por base de datos (una exención de indexado de un campo y una política TTL sobre el mismo campo cuentan como una sola configuración). [^4]

**TTL (time-to-live).**
Políticas que borran automáticamente documentos caducados (p. ej. sesiones/logs antiguos). **Reduce coste de almacenamiento**, pero **cada borrado TTL se factura como un borrado de documento** (mismo precio que un delete). El borrado no es instantáneo: se ejecuta en segundo plano tras expirar. [^19][^2]

**Alertas de presupuesto y monitorización.**
Se recomienda **configurar alertas de presupuesto antes de pasar a Blaze** y monitorizar lecturas/escrituras/borrados/listeners. Importante: las *budget alerts* de GCP **notifican pero NO cortan el gasto** por defecto; para un tope duro hay que combinar la alerta con una acción programática (p. ej. Pub/Sub + Cloud Function que deshabilite el servicio). [^10][^20]

**Ejemplo de facturación oficial.**
La propia doc de Firebase publica un caso resuelto: "For 50,000 app installs (5,000 Daily Active Users): **$12.14/month**". [^21]

**Metodología de estimación (ilustrativa; tarifas citadas arriba).**
`Coste = (lecturas_facturables/100.000 × tarifa_lectura) + (escrituras/100.000 × tarifa_escritura) + (borrados/100.000 × tarifa_borrado) + (GiB × tarifa_almacenamiento) + egress`. Calculando con el escenario GameVision (multi-región):
- Lecturas: 10.000 usuarios × ~350 lecturas/día (listar 300 juegos + diario) ≈ **3,5 M lecturas/día** ≈ **105 M/mes**.
- Escrituras de sesión: 10.000 × 10/semana ≈ 100.000/semana ≈ **~430.000/mes**.
- Almacenamiento: 10.000 × 300 docs × ~1 KB ≈ **~3 GiB** (más overhead de índices).
- Crédito gratuito mensual: 50.000 lecturas/día ≈ 1,5 M/mes → **~103,5 M lecturas facturables**; 20.000 escrituras/día ≈ 600.000/mes → **escrituras cubiertas por el nivel gratuito**.
- **~$62/mes en lecturas + ~$0,5/mes de almacenamiento ≈ ~$63/mes (multi-región)**, o **~$32/mes (regional)**. **Las lecturas de pantallas de listado/real-time dominan el coste.**

---

## Key Evidence

| Dato | Valor | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Nivel gratuito Firestore (Spark/Blaze) | 1 GiB, 50.000 lecturas/día, 20.000 escrituras/día, 20.000 borrados/día | 2026 (consulta) | "Stored data, 1 GiB ; Document reads, 50,000 per day ; Document writes, 20,000 per day ; Document deletes, 20,000 per day." | Alta | [^3] |
| Cuotas gratuitas + egress | +10 GiB/mes de salida | 2026 (consulta) | "Free quota ; Document reads, 50,000 per day ; Document writes, 20,000 per day ; Document deletes, 20,000 per day ; Outbound data transfer, 10 GiB per month" | Alta | [^4] |
| Una sola BD gratuita por proyecto | 1 | 2026 (consulta) | "Firestore allows exactly one free database per project." | Alta | [^1] |
| Precio operaciones (nivel mostrado) | Reads $0.03 / Writes $0.09 / Deletes $0.01 por 100k | 2026 (consulta) | "Pricing overview ; Document Reads. 50,000. $0.03 ; Document Writes. 20,000. $0.09 ; Document Deletes. 20,000. $0.01 ; TTL Deletes. Not supported. $0.01 ..." | Alta (regional) | [^1] |
| Tramo multi-región | $0.06/100k reads, $0.18/100k writes | 2026 (consulta) | "A multi-region like nam5 costs $0.06 per 100,000 reads and $0.18 per 100,000 writes; the regional us-central1 is exactly half that." | Media-Alta | [^7] |
| Almacenamiento | $0.18/GiB·mes (multi-región) | 2026 (consulta) | "$0.06 per 100k reads, $0.18 per 100k writes, $0.18/GiB/month storage." | Media | [^8] |
| Multitramo de lecturas (página oficial) | reads $0.03 / $0.024 / $0.018 por 100k | 2026 (consulta) | "Document Reads 50,000 $0.03 $0.024 $0.018 per 100,000 documents" | Baja (columna ilegible) | [^1] |
| Facturación de agregaciones | 1 lectura por lote de hasta 1.000 entradas de índice | 2023-11-08 | "You are charged one document read for each batch of up to 1000 index entries matched by the query. For aggregation queries that match 0 index ..." | Alta | [^11] |
| Facturación de `count()` (oficial GCP) | 1 lectura / 1.000 entradas de índice | 2026 (consulta) | "For aggregation queries such as count(), you are charged one document read for each batch of up to 1,000 index entries matched by the query." | Alta | [^12] |
| Mínimo de cobro agregación | 1 lectura | 2023-11-08 | "there is a minimum charge of one document read" | Alta | [^11] |
| Ventaja de agregaciones | Ahorra lecturas y bytes | 2026 (consulta) | "Compared to executing a full query and calculating the aggregation in your app, aggregation queries save on both billed document reads and bytes transferred." | Alta | [^12] |
| Lanzamiento SUM/AVG | 2023-11-08 | 2023-11-08 | (Título/artículo) "Aggregate with SUM and AVG in Firestore" | Media-Alta | [^11] |
| TTL y coste | Borrados TTL se facturan como deletes | 2026 (consulta) | "With TTL, you can decrease storage costs by cleaning out obsolete data. Pricing TTL delete operations count towards your document delete costs." | Alta | [^19] |
| TTL deletes en tarifa | No incluidos en gratis; $0.01/100k | 2026 (consulta) | "TTL Deletes. Not supported. $0.01 ..." | Alta | [^1] |
| Límite de transacción | ≤500 documentos, 270 s | 2023-10-31 | "A single transaction cannot modify more than 500 documents. A transaction operation must be completed within 270 seconds. A transaction cannot ..." | Alta | [^14] |
| Límite batch/transacción | 500 operaciones | 2026 (consulta) | "Max 500 operations per batch/transaction." | Alta | [^15] |
| Semántica batch vs transacción | batch = todo-o-nada; transacción = read-then-write | 2026 (consulta) | "Batches: Multiple writes that all succeed or all fail. Transactions: Read-then-write operations with consistency." | Alta | [^15] |
| Request de transacción | ≤10 MiB, 270 s | 2026-02-17 | "The request must fit within Firestore's 10 MiB API request size limit, and the transaction must complete within 270 seconds with no more than 60 ..." | Media-Alta | [^16] |
| Tamaño máximo de documento | 1 MB / 1 MiB | 2026 (consulta) | "Document Size : The maximum size for a single document is 1 MB." | Alta | [^17] |
| Escritura sostenida por documento | ~1 escritura/s | 2026-03-30 | "Firestore limits a single document to ~1 write/second. Use distributed counters or sharded aggregation instead." | Media | [^18] |
| Overhead de almacenamiento | Incluye índices y metadata | 2026 (consulta) | "Firestore charges the amount of data that is stored and storage overhead. Storage overhead includes composite indexes, metadata, and automatic indexes." | Media-Alta | [^9] |
| Ejemplo oficial de facturación | 5.000 DAU → $12,14/mes | 2026 (consulta) | "For 50,000 app installs (5,000 Daily Active Users): $12.14/month" | Alta | [^21] |
| Recomendación de alertas | Configurar budgets antes de Blaze | 2026-08-18 | "Set budget alerts before moving a production project to Blaze. Monitor Firestore reads, writes, deletes, listeners," | Media | [^10] |
| `FieldValue.increment` | Operador atómico de contador | — | (Sin verbatim capturado) | Baja-Media | [^13] |
| Listeners en tiempo real | Se facturan como lecturas por documentos recibidos | — | (Sin verbatim capturado) | Baja-Media | [^1][^10] |

Fuentes:
[^1]: Firestore pricing (Google Cloud). 2026 (consulta 2026-09-28). https://cloud.google.com/firestore/pricing
[^2]: Understand Cloud Firestore billing (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/pricing
[^3]: Understand Cloud Firestore billing (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/pricing
[^4]: Usage and limits (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/quotas
[^5]: Usage and limits (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/quotas
[^6]: Firestore pricing (Google Cloud). 2026 (consulta 2026-09-28). https://cloud.google.com/firestore/pricing
[^7]: What It Costs to Run a Multi-Vendor Delivery App: Firebase (devsnack). 2026 (consulta 2026-09-28). https://devsnack.dev/
[^8]: Supabase vs Firebase: The 2026 Backend Decision Guide (vibetown). 2026 (consulta 2026-09-28). https://vibetown.pro/
[^9]: Indonesian Journal of Electrical Engineering and Computer Science (IJEECS) — Firestore storage overhead. 2026 (consulta 2026-09-28). https://ijeecs.iaescore.com/
[^10]: What Is Google Firebase? Services, Pricing, Security (itechguides). 2026-08-18. https://www.itechguides.com/
[^11]: Aggregate with SUM and AVG in Firestore (Google Cloud Blog). 2023-11-08. https://cloud.google.com/blog/products/databases/aggregate-with-sum-and-avg-in-firestore
[^12]: Billing example | Firestore in Native mode (Google Cloud docs). 2026 (consulta 2026-09-28). https://docs.cloud.google.com/firestore/native/docs/billing-example
[^13]: Summarize data with aggregation queries / Firestore docs (Google Cloud docs). 2026 (consulta 2026-09-28). https://docs.cloud.google.com/firestore/native/docs/query-data/aggregation-queries
[^14]: Best Practices for Dealing with Firestore Limitations (DhiWise). 2023-10-31. https://www.dhiwise.com/
[^15]: Firestore data modeling patterns — references/detailed-guide (GitHub skill). 2026 (consulta 2026-09-28). https://github.com/
[^16]: How to Use Firestore Transactions to Ensure Atomic Read-Write (OneUptime). 2026-02-17. https://oneuptime.com/
[^17]: Google Cloud Firestore: Build Real-time Apps That Scale (cloudtechgyani). 2026 (consulta 2026-09-28). https://www.cloudtechgyani.com/
[^18]: Firestore Pricing Explained: How to Estimate Costs (firemap.dev). 2026-03-30. https://firemap.dev/
[^19]: Manage data retention with TTL policies (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/ttl
[^20]: Firestore pricing / Cloud Billing budgets (Google Cloud). 2026 (consulta 2026-09-28). https://cloud.google.com/firestore/pricing
[^21]: See a Cloud Firestore pricing example (Firebase). 2026 (consulta 2026-09-28). https://firebase.google.com/docs/firestore/billing-example

---

## Tensions & Counter-arguments

1. **Las lecturas son el enemigo, no las escrituras.** Tanto la experiencia de la comunidad (Reddit: "writes in Firestore costs $1686 vs $80 in Spanner; document reads $3615 vs $80") como el ejemplo oficial (5.000 DAU → $12,14/mes) apuntan a que el coste está dominado por **patrones de lectura ineficientes** — listar 300 documentos de biblioteca sin paginación ni caché puede multiplicar la factura. **Contramedida arquitectónica: agregaciones `count()`/`sum()` para contadores, paginación, caché de SDK y listeners acotados.** [^11][^21]

2. **Multi-región vs. regional = 2× en todo.** Elegir `nam5`/multi-región (alta disponibilidad) duplica lecturas/escrituras/almacenamiento frente a una región única. Para una app de tracking, una **región única** (p. ej. `europe-west1`) puede ser suficiente y recortar a la mitad la factura — a cambio de menor tolerancia a fallos regionales. [^1][^7]

3. **El nivel gratuito es generoso para arrancar, pero frágil.** 50.000 lecturas/día se agotan rápido con usuarios reales (un hilo de Facebook reporta superar la cuota en 7-8 h con la app en uso). Además, la cuota gratuita **es por proyecto y solo hay una BD gratis**: no escala con "más bases". [^4][^5]

4. **Las agregaciones NO son gratis ni ilimitadas.** El cobro por lotes de 1.000 entradas de índice es barato comparado con leer todo, pero **depende de índices**: una `count()` sobre un conjunto con 300.000 entradas de índice → ~300 lecturas facturables, no 1. La ventaja real es frente a traer los documentos a la app. [^11][^12]

5. **Las transacciones online vs. offline.** Los batches funcionan offline (se encolan), pero las transacciones **no**; si GameVision usa transacciones para, p. ej., "incrementar sesiones y actualizar último login" en móvil sin conexión, **fallarán**. Los contadores con `FieldValue.increment` son la alternativa correcta para operaciones offline. Sin verbatim oficial capturado: **verificar el comportamiento exacto offline de transacciones en la doc de Android.** [^13][^14][^16]

6. **TTL ahorra almacenamiento pero sigue costando borrados.** Cada documento expirado por TTL **se factura como borrado** ($0.01–$0.02 / 100k), no como gratis. Para `sessions`/`logs` con alta rotación, conviene valorar el coste de borrado TTL vs. almacenar indefinidamente. [^1][^19]

7. **Las budget alerts NO son un tope duro.** Un error de configuración de alertas deja creer que "está cubierto" cuando en realidad el gasto sigue. Para un límite real hay que añadir una acción automática (Pub/Sub + Cloud Function que deshabilite Firestore/App Check). **Punto de riesgo operativo alto para una app de consumo.** [^10][^20]

8. **Precios oficiales ambiguos / dinámicos.** La página oficial devolvió una columna multitramo (`$0.03 $0.024 $0.018`) sin encabezados legibles en los resultados de búsqueda, y las tarifas cambian con el tiempo y por región. **No se deben presupuestar cifras "de memoria": validar la tabla viva en `cloud.google.com/firestore/pricing` el mismo día del cálculo.** [^1]

9. **Datos no confirmados (declararlo explícitamente):** (a) facturación **verbatim de listeners en tiempo real**; (b) semántica exacta de `FieldValue.increment` **offline**; (c) comportamiento offline de transacciones; (d) tarifa regional exacta de almacenamiento. Estos puntos se marcan **Baja-Media** y requieren verificación directa en la documentación oficial antes de decisiones de producción.
