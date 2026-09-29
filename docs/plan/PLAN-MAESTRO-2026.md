# Plan Maestro 2026 - GameVision

> **Qué es.** El plan único que ordena **todos los frentes** de GameVision: producto, diseño,
> datos, comercial, publicación, crecimiento, proceso y filosofía del negocio. Es el documento
> que da contexto a cualquier sesión futura: *dónde estamos, qué viene y por qué*.
>
> **Cómo se relaciona con lo demás.** Este plan **no sustituye** al roadmap de fases
> ([`docs/roadmap/`](../roadmap/README.md)); lo **envuelve** y le añade las vertientes que
> faltaban (comercial, publicación, crecimiento). El método de trabajo está en
> [`docs/metodologia/`](../metodologia/README.md). La evidencia competitiva está en
> [`docs/investigacion-2026/`](../investigacion-2026/).
>
> Fecha: **28/09/2026** · Estado: **propuesta para aprobación**.

---

## 1. Las 8 vertientes

Todo el trabajo de GameVision cae en una de estas ocho vertientes. Cada tarea o fase declara
a cuáles toca.

| # | Vertiente | Qué cubre | Documento de referencia |
|---|---|---|---|
| **V1** | **Producto / funcional** | Biblioteca, diario, estados, stats, social, decisión, Rewind | [`roadmap/`](../roadmap/README.md) |
| **V2** | **Diseño / UX** | Design system, patrones, Wrapped, accesibilidad, adaptativo | [`DESIGN.md`](../../DESIGN.md) |
| **V3** | **Datos / arquitectura** | Adapter de catálogo, caché, modelo, offline, sync | [`AGENTS.md`](../../AGENTS.md) · ADR-0001 |
| **V4** | **Comercial / monetización** | Modelo gratis vs. premium, anuncios, precios | [`investigacion-2026/sintesis-2026.md`](../investigacion-2026/sintesis-2026.md) §3 |
| **V5** | **Publicación / distribución** | Cuenta, ficha, Data Safety, privacidad, rollout | mismo doc, §2 |
| **V6** | **Crecimiento / marketing** | ASO, comunidad, creadores, import/export, Rewind viral | mismo doc, §6 |
| **V7** | **Proceso / calidad** | GDF, ADRs, CI/CD, métricas, SLO | [`metodologia/`](../metodologia/README.md) |
| **V8** | **Filosofía / estrategia** | ¿Tiene sentido el negocio? Posicionamiento y foco | este documento, §3 |

---

## 2. Estado de partida (28/09/2026)

| Vertiente | Estado | Evidencia |
|---|---|---|
| V1 Producto | 🟢 F0 en ejecución | Adapter `GameCatalog` escrito (sin commitear) |
| V2 Diseño | ✅ Base sólida | DESIGN.md + `ui/designsystem/` completos |
| V3 Datos | 🟢 En curso | `data/catalog/` creado; falta caché y modelo de biblioteca |
| V4 Comercial | ✅ Decidido (29/09) | [ADR-0005](../metodologia/adr/0005-monetizacion-por-etapas.md) (D-C6/D-E2/D-E3) · [ADR-0006](../metodologia/adr/0006-cuenta-play-personal.md) (D-E1) |
| V5 Publicación | 📄 Investigado, sin empezar | Requisitos de Play mapeados |
| V6 Crecimiento | 📄 Investigado, sin empezar | ASO y canales documentados |
| V7 Proceso | ✅ GDF creado | `docs/metodologia/` (esta sesión) |
| V8 Filosofía | 🟡 Revisada aquí | §3 de este documento |

**Activos que ya tenemos:** app Android nativa funcional, design system propio, arquitectura
limpia con SSOT, 11 tests verdes, documentación de fases, **emulador `Pixel_9` (API 36)
operativo**, y una investigación de mercado/competencia profunda.

**Deuda conocida:** Kotlin 2.2.10 (vs 2.4.20), Nav2 (vs Nav3), release sin firmar, cambios de
F0 sin commitear.

---

## 3. Filosofía del negocio (¿tiene sentido?)

Esta sección responde a la pregunta que pediste: **darle una vuelta a la filosofía y ver si el
negocio tiene sentido.** Sin adornos.

### 3.1 Qué es realmente GameVision

No es una app de catálogo (eso ya lo dan RAWG/IGDB gratis). Es **un hábito**: el lugar donde el
jugador **registra lo que juega y descubre qué jugar después**. El producto es la **costumbre**,
no la base de datos.

### 3.2 Dónde está el valor defendible (el "foso")

| Candidato a foso | ¿Sirve? | Por qué |
|---|---|---|
| El catálogo de juegos | ❌ No | Commoditizado vía RAWG/IGDB; cualquiera lo replica |
| La app nativa Android | ✅ **Sí (parcial)** | El líder (Backloggd) no la tiene; es ventaja real pero **copiable** a medio plazo |
| El hábito de registro diario | ✅ **Sí** | El coste de cambiar de tracker (perder tu historial) protege al que gana |
| La red social / comunidad | ✅ **Sí (el fuerte)** | Efecto red: nadie lo ha logrado en juegos todavía |
| El motor de recap viral (Rewind) | ✅ **Sí** | Crecimiento de coste cero; nadie lo aplica bien al gaming |

**Conclusión:** el foso se construye con **hábito + comunidad + recap**, y se apuntala con la
ventaja **nativa Android**. El catálogo es solo la entrada.

### 3.3 Los tres riesgos honestos

1. **Techo de mercado bajo.** Es un nicho de aficionados, con ARPU bajo y usuarios muy
   sensibles al precio (el caso Trakt lo demuestra). No es un negocio que "escale a
   millones" por sí solo: es un producto **sostenible y querido**, no una startup de
   hipercrecimiento.
2. **Problema del arranque en frío social.** Una app social vacía es peor que una individual.
   → **Mitigación:** arrancar **individual-first** (diario, stats, backlog que ya aportan valor
   solo) y sembrar la capa social con clubes, retos y listas descubribles.
3. **Riesgo de mimetismo.** Copiar tanto a Letterboxd que pierda identidad. → **Regla:** copiar
   mecánica, reinterpretar estética.

### 3.4 Veredicto

**Sí tiene sentido**, con una expectativa bien puesta:

- Como **producto**: excelente. Resuelve un dolor real (abundancia/backlog), en un nicho sin
  dueño, con una ventaja clara (nativo) y un motor viral probado (Rewind).
- Como **negocio**: **sostenible, no hiperescalable**. La meta realista es un producto
  **gratuito con premium barato (15-25 $/año) + micromecenazgo**, capaz de cubrirse solo y de
  crecer por comunidad. Quien te prometa que esto factura como un AAA te está mintiendo.
- Como **escaparate profesional**: sobresaliente. Un producto publicado, con usuarios y con un
  trabajo de investigación y diseño detrás, vale más que cualquier portfolio.

**La apuesta correcta:** ganar el nicho (ser *el* tracker de videojuegos con app nativa), no
maximizar ingresos. Si se gana el nicho, la monetización viene sola; al revés, no funciona.

### 3.5 Principios de producto (la "constitución")

1. **Registrar primero, monetizar después.** El núcleo (registrar, puntuar, reseñar, listas,
   feed) es **gratis para siempre**.
2. **Nunca cobrar lo que antes era gratis.** (Lección Trakt.)
3. **Regalar la salida.** Export/API abiertos: ganar la entrada facilitando la marcha.
4. **Anti-culpa.** El backlog no es una deuda; la app no regaña.
5. **Celebrar, no castigar.** El Rewind espeja lo que hiciste, nunca presiona.
6. **El usuario es el héroe.** La app desaparece; el protagonista es su biblioteca.

---

## 4. Fases del plan

Ocho fases (F4.5 incluida). Las **F0-F4 son el roadmap de producto** ya existente; se añaden **F5
(Publicación)** y **F6 (Crecimiento)**, y se refuerza lo transversal.

```
F0 CIMIENTOS ──> F1 TRACKER ──┬──> F2 SOCIAL ──> F3 WOW ──> F4 NATIVO
   (datos)         (corazón)   │
                               └──> F5 PUBLICACIÓN (transversal, arranca al cerrar F1)
                                        │
                                        └──> F6 CRECIMIENTO (tras publicar)
```

### F0 · Cimientos de datos - ✅ Completada (29/09/2026)
- **Vertientes:** V3, V1
- **Objetivo:** dejar de depender en exclusiva de RAWG, tener modelo de biblioteca real, arreglar la deuda de imágenes y **añadir import/export** (D-C3).
- **Entregable:** app que registra un juego offline, muestra catálogo cacheado si RAWG cae, y la foto de perfil viaja entre dispositivos.
- **Extra de este análisis:** **import/export universal** (Steam/HLTB/Backloggd/CSV) sube a requisito de F0/F1.

### F1 · El corazón del tracker - 🟢 En ejecución (Bloque 1 hecho)
- **Vertientes:** V1, V2, V3
- **Objetivo:** biblioteca rica (estados, notas, reseñas, diario, sesiones, stats).
- **Añadido por el análisis:** cerrar la **escala de nota** (D-C1), **avisos de contenido** (S11), y **estimaciones personalizadas** de duración.
- **Bloqueo activo:** fuente de la duración de los juegos.

### F2 · Social - ⬜ Pendiente
- **Vertientes:** V1, V6
- **Objetivo:** perfil público, buscar por username, seguir, feed de hitos, Top 4, listas.
- **Añadido:** **reacciones**, **listas colaborativas**, **roadmap público con votación** (D-C10), **import desde otras apps**.

### F3 · El "wow": decidir y celebrar - ⬜ Pendiente
- **Vertientes:** V1, V2
- **Objetivo:** "¿Qué juego ahora?" explicado + GameVision Rewind compartible.
- **Añadido (la combinación que nadie tiene):** **mood tags** (S10), **Rewind story vertical** con export 9:16 (S9), **duelos ELO** (S15).
- **Regla:** el Rewind básico **nunca se cobra** (D-C4).

### F4 · Nativo y pulido - ⬜ Pendiente
- **Vertientes:** V1, V2
- **Objetivo:** widget, notificaciones, offline afinado, estantería visual, list-detail adaptativo (Nav3).
- **Añadido:** **gamificación ética** (S18), **accesibilidad** auditada.

### F4.5 · Diseño, animaciones y auditoría de experiencia - ⬜ Nueva (antes de la comercial)
- **Vertientes:** V2, V1
- **Objetivo:** auditar y pulir la experiencia **pantalla a pantalla** (diseño, estados y **animaciones**) antes de abrir la monetización. Ver [fase-4-5](../roadmap/fase-4-5-diseno-animaciones.md).
- **Por qué antes de F5:** monetizar una experiencia sin pulir es el peor orden posible; el cuidado del detalle es parte del argumento del producto.

### Auditoría de cierre de cada fase (transversal)
Cada fase (F0-F6) cierra con una **auditoría de código y experiencia** validada por el propietario, según el [protocolo](../metodologia/auditoria-de-cierre.md). Una fase no pasa a ✅ Completada sin ella.

### F5 · Publicación en Play Store - 📄 Nueva (transversal)
- **Vertientes:** V4, V5, V7
- **Objetivo:** app **publicada, monetizada y estable** en Google Play.
- **Contenido:**
  1. **Cuenta de desarrollador** y verificación (cuenta **personal** → [ADR-0006](../metodologia/adr/0006-cuenta-play-personal.md); revalidar el requisito de testers vigente en Play Console al arrancar F5).
  2. **Ficha de tienda** (ASO: título, descripciones, capturas, icono, vídeo).
  3. **Data Safety + política de privacidad** (obligatorios; somos red social con UGC → moderación, reportar/bloquear, Child Safety).
  4. **Integración de monetización**: AdMob discreto (banner/native/rewarded) + UMP (consentimiento) + "quitar anuncios" con Play Billing v8.
  5. **Beta cerrada** (12 testers/14 días si cuenta personal) y **staged rollout** 5 %→10 %→50 %→100 %.
  6. **SLO de calidad**: crash-free ≥ 99,7 % con Crashlytics.
- **Arranca:** al cerrar F1 (en paralelo a F2-F4).

### F6 · Crecimiento - 📄 Nueva (tras publicar)
- **Vertientes:** V6, V8
- **Objetivo:** tracción orgánica sostenible.
- **Contenido:** ASO continuo, comunidad (Reddit/Discord/creadores), **Rewind viral**, import/export como captación, roadmap público, y análisis de datos reales de uso.

---

## 5. Vertiente comercial en detalle (V4)

**Modelo adoptado (D-C6 → [ADR-0005](../metodologia/adr/0005-monetizacion-por-etapas.md)), por etapas con disparadores medibles:**

| Etapa | Modelo | Detalle |
|---|---|---|
| **Lanzamiento** | Gratis + **micromecenazgo** opcional | Core completo gratis; Patreon/donaciones para quien quiera apoyar |
| **Crecimiento** | **+ "Quitar anuncios"** (pago único) | AdMob discreto en la capa gratis; el pago único lo elimina |
| **Escala** | **+ Premium anual 15-25 $** | Tipo Letterboxd Pro: stats avanzadas, temas, multi-colección, export extra |
| **Nunca** | - | Cobrar el registro, las listas, el feed o el Rewind básico |

**Reglas de oro:** UMP desde el día uno; anuncios de prueba en desarrollo; nada de
interstitial/app-open agresivos; precio estable (no "bait-and-switch").

---

## 6. Cómo se gobierna este plan

| Artefacto | Para qué | Cuándo se toca |
|---|---|---|
| **Este plan** | Contexto global de vertientes y fases | Al cerrar una fase o cambiar la estrategia |
| **[roadmap/](../roadmap/README.md)** | Detalle de tareas y decisiones por fase | Al debatir/cerrar cada fase |
| **[metodologia/](../metodologia/README.md)** | Cómo se trabaja (GDF) | Al cambiar convenciones |
| **[investigacion-2026/](../investigacion-2026/)** | Evidencia de mercado y tecnología | Referencia |
| **[ADR](../metodologia/adr/README.md)** | Decisiones caras de revertir | Al decidir |
| **[objetivos-2026.md](../metodologia/objetivos-2026.md)** | Horizonte inmediato | Semanal |

**Regla:** una fase no se cierra sin cumplir sus criterios de aceptación **con evidencia**
(tests, emulador, captura) y sin actualizar este plan.

---

## 7. Próximos pasos (lo que toca ahora)

1. **Decisiones bloqueantes cerradas (29/09):** escala de nota **ratificada** (0,5-5,0 →
   [ADR-0003](../metodologia/adr/0003-escala-medias-estrellas.md)), monetización **por etapas con
   disparadores** ([ADR-0005](../metodologia/adr/0005-monetizacion-por-etapas.md)) y cuenta Play
   **personal** ([ADR-0006](../metodologia/adr/0006-cuenta-play-personal.md)).
2. **Terminar F0**: caché Room, modelo de biblioteca, Storage, import/export. → ✅ **cerrado (29/09)**; la foto va a Firestore (ADR-0007), no a Storage.
3. **Abrir F5 en borrador**: cuenta de desarrollador + política de privacidad + Data Safety.
4. **Instrumentar** (Analytics + Crashlytics) al cerrar F1.
5. **✅ Mapa de integraciones por vistas ratificado (29/09):**
   `integracion-por-vistas-2026.md` - las 20 micro-decisiones D-V1...D-V20 adoptadas en bloque.

---

## Apéndices

- [Análisis competitivo y de mercado (cierre)](../investigacion-2026/analisis-competitivo-2026.md)
- [Investigación técnica y de publicación 2026](../investigacion-2026/sintesis-2026.md)
- [Integraciones y APIs aceptadas (P1-P4)](../investigacion-2026/integraciones-apis-2026.md)
- [Mapa de integración por vistas (D-V1...D-V20)](integracion-por-vistas-2026.md)
- [Framework de desarrollo (GDF)](../metodologia/README.md)
- [Visión de producto](../product-vision-2026.md)
