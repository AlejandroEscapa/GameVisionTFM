# Informe de calidad — GameVision (29/09/2026)

> Auditoría de dos ámbitos: el **workspace** (capa de agente y documentación) y el **producto**
> (el repo `GameVisionTFM-master/`). No es una auditoría de fase: no cierra ni bloquea F1.
> Es el mapa de lo que mejoraría antes de que la deuda crezca.
>
> **Autor:** GameVision (sesión Freebuff) · **Alcance:** lectura del código, ejecución de la
> suite JVM y verificación de enlaces. **No se modificó código de producto.**

---

## 0. Cómo se hizo (para que puedas discutirlo)

Todo lo de aquí sale de **ejecutar**, no de leer documentación:

| Comprobación | Cómo | Resultado |
|---|---|---|
| Compilación | `./gradlew testDebugUnitTest` | ✅ `BUILD SUCCESSFUL` |
| Tests unitarios | `app/build/test-results/**/*.xml` | ✅ **70/70**, 0 fallos (22:18) |
| Cobertura de la capa de datos | `grep` de repositorios/ViewModels en `src/test` | ⚠️ **0 de 5 repositorios** |
| Suite de reglas | lectura de `firebase-tests/rules.test.mjs` | ⚠️ 17 checks, **0 cubren las rutas nuevas** |
| Migración de datos | `git status --untracked-files=all` + `scripts/` | ⚠️ **no existía; apareció durante la auditoría** (ver §1.1) |
| Enlaces del workspace | `os.path.exists` sobre cada link | ✅ 14/14 |
| Estructura real vs. documentada | `find` sobre `data/` | ⚠️ el `AGENTS.md` del repo **miente** |

> **Advertencia metodológica:** durante la auditoría, otro agente estaba commiteando el cambio
> ADR-0008 en el mismo árbol (pasó de 10 a 13 ficheros modificados). Las cifras de "cambios en
> vuelo" son una foto de las 22:2x del 29/09.

---

## 1. Resumen ejecutivo

| Área | Nota | Veredicto en una línea |
|---|---|---|
| Cambio en vuelo (ADR-0008) | **6 / 10** | Re-keying impecable, pero **no migra datos**: el objetivo del ADR no se cumple en producción |
| Capa de datos | **4 / 10** | Los **5 repositorios** tienen **cero tests**. Todo el riesgo vive ahí |
| Suite de reglas | **3 / 10** | No es un framework. Puede quedar verde "probando" una versión que ya no existe |
| Higiene general | **7 / 10** | 2 TODO reales en 11.230 líneas. Deuda técnica **planificada y con disparadores** |
| Documentación | **5 / 10** | Mucha, buena y **con duplicación que ya ha mentido** dos veces |
| Design system | **5 / 10** | 119 líneas de `DESIGN.md` para governar 12+ pantallas |

**La tesis del informe:** el proyecto está muy bien planeado y casi no tiene deuda técnica
*escrita*. El problema no es la calidad del código: es que **la verificación no cubre la zona
donde está el riesgo**. Todo el testing verde prueba lógica pura; el 100 % del riesgo (Firestore,
identidad, reglas) está sin ejercitar.

## 1.1 · Nota de método: el informe quedó obsoleto solo

Escribí este informe contra el worktree a las 22:2x, mientras otro agente commiteaba. Cuando lo
terminé, el HEAD había pasado de `e12cdbb` a `cfbdc38` y **la migración de datos que yo daba por
inexistente ya estaba escrita y commiteada**: `scripts/migracion-uid/migrate-uid.js` (174 líneas).

Lo he actualizado en vez de publicarlo tal cual, y lo dejo dicho porque **es el mismo defecto que
este informe denuncia**: un dato de estado repetido en un sitio que no es su dueño, capturando el
estado de un instante y quedándose viejo. La lección no es "tuve mala información" —es que este
informe **es también un estado duplicado** y hay que releerlo antes de ejecutarlo.

Lo bueno: la migración corrige el defecto 🔴 nº 1 de B1. Lo malo: introduce dos problemas nuevos
(§B1-bis).

---

# PARTE A — El workspace y la capa de agente

## A1 · Documentación duplicada que ya ha mentido ⭐

Es el hallazgo estructural más importante del workspace, y no es teórico: **ya ha producido
dos falsosstatements verificables**.

| Hecho | Dónde vive | Coste |
|---|---|---|
| "F1 en ejecución (Bloque 1 hecho)" | `docs/roadmap/README.md` **y** `docs/plan/PLAN-MAESTRO-2026.md` | Los bloques 1, 2, 2b, 3 y 4 están hechos y auditados |
| Rama `ui-redesign-2026` | `mapa-gamevision.md` | Solo queda `master` |
| Kotlin integrado 2.2.10 | `mapa-gamevision.md` | KGP externo 2.4.20 desde ADR-0004 |
| `data/` con 4 paquetes | `AGENTS.md` del repo §4 | Hay **once** paquetes |
| "F0-F4" | `MEMORY.md` | El rango real es **F0-F6** (ocho fases) |

El patrón: **el mismo dato en dos sitios, y los dos se desactualizan a ritmos distintos**. El
fichero de fase (`fase-1-corazon-tracker.md`) sí está correcto; los resúmenes no.

**Mejora:** un solo dueño por dato.
1. Los resúmenes (`README.md`, `PLAN-MAESTRO`, `mapa`, `MEMORY.md`) **no declaran estado**: enlazan
   a la fase y listo. Si un resumen dice un estado, será por copia y mentirá.
2. `AGENTS.md` del repo §4: regenerar el árbol desde el sistema de ficheros, no a mano.
3. Un check barato en CI que falle si `mapa-gamevision.md` menciona una rama que no existe.

## A2 · La suite de reglas de Firestore no es un framework ⭐⭐

`firebase-tests/rules.test.mjs` son 127 líneas, 17 llamadas `check(...)` secuenciales sobre
`assertSucceeds` / `assertFails`. No hay `describe`/`it`, ni descubrimiento, ni parametrización.

**Consecuencia medida hoy:** tras reescribir `firestore.rules` para ADR-0008, el suite tiene
**cero referencias** a `email_index`, a `usernames` ni a la caída del fallback por email en
`isOwner`. Dos de los 17 checks (línea 103 `friends/bob@test.dev`, línea 119
`users/alice@test.dev/playedlist/1`) siguen probando formas legacy: **pasan, pero por el motivo
equivocado**. La memoria registra "17/17 OK → cierra D-S2 definitivamente". **Esa cierre ya no
sostiene nada.**

**Mejora (la de mayor retorno de todo el informe):** reescribir el suite como un runner con
descubrimiento automático a partir de una **tabla declarativa de casos**. Que toda regla escrita
en el fichero tenga que aparecer en un caso, o falle el build. Convierte "verde" en "probado".

## A3 · Lo que está bien (y hay que mantenerlo)

- El **protocolo de auditoría de cierre** existe, se ha aplicado dos veces (F0, F1) y ha encontrado
  hallazgos reales (H1). Es el activo de proceso más valioso del proyecto.
- El **plan de deuda técnica** tiene los 5 bloques cerrados y **6 diferidos con disparador
  explícito** (Nav3→F4, Baseline Profiles→F4, App Check→F5, "Me gusta"→F2…). Eso es deuda
  profesionalmente gestionada, no olvidada.
- `evolution-drafts/` convierte preferencias del usuario en reglas persistentes con aprobación
  humana. Es un patrón poco habitual y funciona.
- El code review de reglas incluye **negativos** desde F0-B, no solo positivos.

---

# PARTE B — El producto

## B1 · El cambio en vuelo: ADR-0008 (clave única `uid`)

Re-keying de 13 ficheros, +224/−162. **La calidad del refactor es alta**: las diez rutas, modelos,
ViewModels, UI y reglas migran a uid sin dejar ni una ruta por email; los índices inversos resuelven
`email → uid` sin exponer la colección; `UserViewModel.profile` se re-suscribe al cambiar de
usuario sin refetch manual.

### Defectos, por severidad

| # | Severidad | Defecto | Efecto observable |
|---|---|---|---|
| 1 | **🔴 Crítico** | **No había migración de datos.** `users/{email}` → `users/{uid}` no existía en ningún sitio; `scripts/b3-limpieza` solo borra y no había ficheros sin seguimiento | **Resuelto en `cfbdc38`** (§B1-bis). Mientras no corra, todo usuario existente queda con perfil **vacío** (`ProfileScreen` pinta `profile.email` = `""`), cero amigos, cero mensajes, y `findUidByEmail` devuelve `null`: **no se puede añadir a nadie** |
| 2 | **🟠 Alto** | `rules.test.mjs` sin actualizar (§A2) | Las reglas nuevas — la parte sensible de identidad — se despliegan sin una sola prueba |
| 3 | **🟠 Alto** | **Regresión de UI.** `FriendsComposables.kt` sustituye el email del amigo por el literal `"Amigo"`; `SocialCard` el del autor por `"Autor"`; los avatares caen a `ownerUid.take(1).uppercase()` — un carácter aleatorio del uid | Toda fila de la lista de amigos dice ahora **exactamente lo mismo**. `SocialScreen` ya tiene `currentFriends` (uid→username) a mano y no lo usa |
| 4 | **🟡 Medio** | **Fallo parcial reportado como total.** En `updateProfile` el `update()` del perfil se espera **antes** de escribir el índice. Si el username está ocupado, las reglas deniegan el `set` y la UI dice "No se pudo actualizar el perfil" — **pero los campos ya se escribieron** | El nombre visible cambia y la pantalla dice que no. Necesita `WriteBatch` |
| 5 | **🟡 Medio** | **Índice fantasma al renombrar.** Se escribe `usernames/{nuevo}` y nunca se borra `usernames/{viejo}`. El comentario lo admite ("el índice viejo se retira aparte") y no se retira; las reglas ya permiten ese borrado | Un usuario renombrado conserva una entrada viva bajo un nombre que ya no posee |
| 6 | **🟡 Medio** | **Perfil huérfano al registrarse.** `SessionRepository` compensa un fallo de `createProfile` borrando la cuenta de Auth, pero no borra el documento `users/{uid}` ya escrito | Reintentar genera otro uid y el huérfano se acumula |

### Además, sin tocar

- **`currentEmail` sobrevive** como `StateFlow<String?>` público con **cero consumidores** tras la
  migración. Quedan *dos* fuentes de identidad en la API, una muerta: exactamente el estado que el
  ADR vino a eliminar.
- **El indexado tiene dos dueños**: `createProfile` delega en `writeIndexes`; `updateProfile` lo
  hace inline. Una preocupación en dos sitios, con comportamientos distintos.
- **`getFriends` hace N+1 lecturas secuenciales** y el índice `usernames` que acaban de introducir
  es justo la solución, sin usarse.
- **Escrituras no atómicas**: `set()` y `writeIndexes()` son 2-3 viajes red separados, sin
  compensación.

## B1-bis · La migración que apareció durante la auditoría (`cfbdc38`)

`scripts/migracion-uid/migrate-uid.js` (174 líneas) está **bien conceived**: migra perfil, amigos
(resolviendo cada email a su uid) y mensajes; crea `email_index` y `usernames`; borra el documento
antiguo; y trae un readme que explica el procedimiento. Cierra el defecto más grave de B1.

Pero introduce dos problemas de seguridad, comparados **con el precedente que el propio repo
estableció** en `b3-limpieza`:

| | `b3-limpieza` (precedente) | `migracion-uid` (nuevo) |
|---|---|---|
| Modo por defecto | **dry-run**; el borrado exige `--commit` | **escribe de verdad**; el dry-run exige `--dry-run` |
| Backup | JSON **antes** de borrar | `summary.json` se escribe **al final** (línea 171) |

**D-1 · El seguro está invertido.** `const DRY = process.argv.includes('--dry-run')` (línea 23):
olvidarse del flag no evita nada, ejecuta la migración y el borrado. En una herramienta que borra
datos de producción, el default tiene que ser el contrario. Es un `if (process.env.PROD)` que se
salta con un despiste.

**D-2 · El backup puede perderse justo cuando hace falta.** La línea 171 escribe el backup
**después** del bucle; el borrado de `users/{email}` ocurre dentro. Si el proceso muere a mitad —
red caída, Ctrl-C, cuota agotada— **se han borrado documentos y el fichero de rescate no existe**.
Con la forma de `b3-limpieza` el backup ya estaría en disco.

Ningún otro cambio del repo invierte un seguro con el que ya se venía trabajando. Son dos líneas y
un `fs.writeFileSync` movido de sitio.

## B2 · La capa de datos no tiene un solo test ⭐⭐

| Componente | Tests |
|---|---|
| `UserRepository` | **0** |
| `SessionRepository` | **0** |
| `LibraryRepository` | **0** |
| `HltbRepository` | **0** |
| `ProfileImageStorage` | **0** |
| `DDBBViewModel` | **0** |
| `UserViewModel` | **0** |
| `GoogleViewModel` | **0** |

Los 70 unitarios cubren `RatingUtils`, `DiaryUtils`, `HltbUtils`, `LibraryFilters`,
`StatisticsUtils`, `SearchViewModel`, `NewsViewModel`, `ThemeViewModel`, `CachedGameCatalog` y
`UserProfile.fromMap`. **Son legítimos y bien hechos** — pero son lógica pura.

La consecuencia es medible: **los seis defectos de B1 viven todos en la zona sin ejercitar.** Los
encontré leyendo, no ejecutando. El mismo week-end en que se commiteó la migración, la suite daba
70/70 verde.

**Mejora:** no hace falta cubrirlo todo. Empezar por el **camino de escritura del perfil**
(`createProfile` / `updateProfile` / `writeIndexes`) con un fake de Firestore, que es donde ya hay
tres defectos. Es la mayor densidad de bugs por línea de test de todo el repo.

## B3 · Higiene: mejor de lo que sugiere la nota

- **78 ficheros Kotlin, 11.230 líneas en `main`, y solo 2 marcas TODO/FIXME reales** (los otros dos
  aciertos del grep eran la palabra "TODOS" en comentarios). Eso es excepcionalmente limpio.
- La capa de datos respeta la regla `Result<T>` sin tragar errores, de forma consistente.
- `CatalogDatabase` está en `version = 2` con `fallbackToDestructiveMigration(dropAllTables = true)`
  y sin `Migration` explícita. **Para una base de caché es la decisión correcta** (se regenera desde
  red), pero conviene **dejarlo escrito**: el coste es que cada bump de versión tira la caché HLTB
  de 90 días. Que sea una decisión documentada, no un descuido.
- Sin APIs deprecadas conocidas; One Tap migrada a Credential Manager; R8 con reglas mínimas.

## B4 · Design system: la fuente de verdad es fina

`DESIGN.md` tiene **119 líneas** para gobernar una app con 12+ pantallas, siete componentes de marca
y una dirección declarada ("design-system-first cinematográfico con identidad editorial": monocromo
cálido + spot `#C8F135`, Space Grotesk, muelles, portadas como protagonista).

El contenido que sí está —atmósfera, color, tipografía, forma, motion— está bien y es específico.
Lo que falta es lo que hace útil un design system a escala: **tokens con nombre** (no solo prosa),
**estados de cada componente** (vacío, carga, error, deshabilitado), **reglas de densidad y
responsive**, y **criterios de accesibilidad** (contraste medido, objetivo táctil, foco).

Relacionado: el AGENTS.md del repo apunta a un skill `navigation-3` y a `DESIGN.md` como fuente
única, y la fase F4.5 (diseño y animaciones) sigue sin empezar. **Ahí es donde este trabajo encaja,
no en un refactor del design system actual.**

---

# 2. Plan de mejoras, priorizado

## P0 — Antes de desplegar las reglas de ADR-0008

1. ~~**Script de migración**~~ → **hecho** en `cfbdc38`, pero con los dos fallos de §B1-bis:
   invertir el default a dry-run y **escribir el backup antes de borrar**. Es bloqueante: desplegar
   reglas solo-uid sin una migración ejecutada deja los datos inalcanzables e irrecuperables.
2. **Tests de reglas** para `email_index`, `usernames` y "un usuario **no** puede escribir en
   `users/{otroEmail}`". Sin esto, el paso 1 se ejecuta a ciegas.
3. **Arreglar la regresión de UI** (defecto 3): autor y amigo deben mostrarse por `username`.

## P1 — Antes de F2 (Social)

4. **`WriteBatch`** en `createProfile` y `updateProfile`; borrar `usernames/{viejo}`; compensar el
   documento huérfano.
5. **Eliminar `currentEmail`** y unificar el indexado en un solo sitio.
6. **Tests de la ruta de escritura del perfil** con fake de Firestore.

## P2 — Salud del proyecto

7. **Suite de reglas con descubrimiento automático** (§A2) — el mayor retorno de todo el informe.
8. **Dejar de duplicar el estado**: los resúmenes enlazan, no declaran.
9. **Ampliar `DESIGN.md`** con tokens y estados, en la fase F4.5.
10. **Suite de integración Firestore** reutilizando `@firebase/rules-unit-testing`, que ya está
    instalado y solo se usa para reglas.

---

## 3. Lo que NO haría

- **No reescribiría `rules.test.mjs` antes de añadirle los casos que faltan.** Primero cobertura,
  después refactor del runner. Al revés se quedan los casos fuera.
- **No tocaría `fallbackToDestructiveMigration`.** Para una caché es lo correcto; documentarlo basta.
- **No abriría el Nav3 ni los Baseline Profiles.** Tienen disparador (F4) y están bien planeados.
- **No metería i18n.** Diferido a decisión de multiidioma, y con razón.

---

## 4. Una observación sobre el proceso

La secuencia de las últimas 48 h es: 70 tests verdes → CI verde → 17/17 reglas verdes → y aun así
seis defectos en el cambio de identidad. **No es un fallo de disciplina, es un fallo de cobertura**:
la suite mide lógica pura y el riesgo está en otra capa. La deuda más rentable de este repo no es
de código, es **de verificación**.

Cuando esta migración esté cerrada y auditada, el debate que conviene abrir es de una sola
pregunta: **¿qué se ejecuta automáticamente antes de que un humano pueda decir "verde"?** Todo lo
demás de este informe se ordena solo detrás de esa respuesta.
