# Re-auditoría — Fase 1, Bloque 5 (migración a `uid`, ADR-0008)

```
Fase: F1 — El corazón del tracker · Bloque 5 (ADR-0008)      Fecha: 29/09/2026
Resultado: ✅ APTA
Bloques:  1 ✅  2 ✅  3 ✅  4 ✅  5 ✅  6 ✅
Firma del agente: GameVision        Validado por el propietario: ✅ (29/09/2026)
```

> **Por qué existe esta auditoría.** La [`auditoria-fase-1-2026.md`](auditoria-fase-1-2026.md) es
> **anterior al Bloque 5**, que cambió 13 ficheros y reescribió `firebase/firestore.rules`. El
> cambio más grande de la fase no estaba auditado. Este documento lo cubre, y también los arreglos
> de verificación hechos el 29/09.

---

## 1. Compilación y build ✅

| Comprobación | Resultado |
|---|---|
| `./gradlew compileDebugKotlin` | ✅ sin errores |
| `./gradlew assembleDebug` | ✅ |
| `./gradlew assembleRelease` (R8 + `lintVitalRelease`) | ✅ |
| Warnings nuevos | Ninguno. Persiste el aviso esperado de `Deprecated 'org.jetbrains.kotlin.android'` (transición ADR-0004) |

## 2. Tests ✅

| Suite | Antes | Ahora |
|---|---|---|
| `testDebugUnitTest` | 70 | **88 en verde** (18 nuevos) |
| `firebase-tests/rules.test.mjs` | 17 checks | **35 checks en verde** |
| `connectedDebugAndroidTest` | 7 | Sin re-ejecutar en esta auditoría (ver reservas) |

**Tests añadidos:**

- `UserIndexPlanTest` (12) — el camino de escritura del perfil, que era el que no tenía **ninguna**
  prueba: normalización del email como clave de índice, creación de índices y, sobre todo, que **un
  rename retira el índice viejo** (índice fantasma) y que nunca quedan dos nombres apuntando al
  mismo uid.
- `LibraryCountersTest` (6) — los deltas de `ratingSum`/`ratingCount`. `LibraryRepository` solo
  puede ajustarlos por incrementos; si se desalinean, la nota media queda corrupta sin que ningún
  error lo delate.

**Prueba negativa de la suite de reglas (evidencia clave):** se reintrodujo deliberadamente el
bug de reglas descubierto en esta auditoría y el check correspondiente **falló**:
`[FALLO] NADIE secuestra el email_index de otro → Expected request to fail, but it succeeded`.
Con las reglas corregidas, 35/35 en verde. Queda demostrado que la suite detecta el defecto, no
solo que pasa.

## 3. Calidad estática ✅

`./gradlew lintDebug` → **0 errores**. Código muerto eliminado: `UserViewModel.currentEmail`, que
quedó como `StateFlow` público sin consumidores tras la migración (la segunda fuente de identidad
que el ADR-0008 vino a eliminar). Sin TODOs huérfanos.

## 4. Arquitectura y consistencia ✅

- **SSOT intacto e único:** identidad = `uid`, de `SessionRepository.sessionState` →
  `UserViewModel.currentUid`. El email es un campo del perfil.
- **Escrituras atómicas:** `createProfile` y `updateProfile` usan `WriteBatch`. Antes, un username
  ocupado dejaba los campos cambiados y la UI decía que no; ahora, o cambia todo o no cambia nada.
- **Un solo dueño del indexado:** `UserIndexPlan.kt` decide; `UserRepository` solo traduce a
  escrituras de lote. Antes `createProfile` delegaba y `updateProfile` lo hacía inline.
- **Sin DTOs de API en la UI**; modelos `Friend`/`ChatMessage` con `uid`, sin emails.

### 🔴 Defecto de seguridad encontrado y corregido en esta auditoría

Las reglas de `email_index` y `usernames` solo comprobaban
`request.resource.data.uid == request.auth.uid`, es decir **lo que se escribe**. Nunca miraban el
documento existente. Consecuencia:

> Cualquier usuario autenticado podía hacer `set(email_index/<email de otra persona>, {uid: su uid})`
> y **secuestrar la entrada**. Lo mismo con `usernames/{nombre de otro}`.

Impacto: redirigir el alta de amigos por email a un atacante, y en F2 la búsqueda por username
resolvía al impostor. El comentario de las reglas afirmaba «nadie puede reescribir el índice de
otro»: la regla no lo implementaba.

**Corregido** separando `create` y `update`: crear solo la propia entrada, reescribir solo si ya
era tuya. Cubierto por checks negativos en la suite.

## 5. Documentación y trazabilidad ✅

- `AGENTS.md` §4 regenerado desde el árbol de ficheros: la capa `data/` tiene **11 paquetes**, no 4.
- `AGENTS.md` §2 al día (88 tests) y nueva sección **"Verificación: lo que NO te dice el verde"**.
- `roadmap/README.md` y `PLAN-MAESTRO-2026.md`: F1 pasa a ✅ Completada, con la regla de que **el
  estado de una fase lo declara su fichero** y los resúmenes solo enlazan.
- `mapa-gamevision.md`: rama `master` única y KGP externo Kotlin 2.4.20 (ADR-0004).
- `docs/plan/CIERRE-DEUDA-HASTA-F2-2026.md` y
  `docs/metodologia/informe-calidad-2026-09-29.md` registrados en el repo.

## 6. Experiencia y estados 🟡

El Bloque 5 tocó `FriendsComposables` y `SocialScreen`. **Regresión encontrada y corregida:** los
literales `"Amigo"` y `"Autor"` habían sustituido al identificador real, y el avatar del mensaje
usaba el primer carácter del uid de Firebase (un carácter aleatorio). Ahora:

- La fila del amigo muestra **nombre visible + `username`**, y la inicial sale del `username`.
- El muro resuelve `ownerUid → nombre` con los datos ya cargados (`currentFriends` + perfil propio).

### Recorrido en emulador (Pixel_9, API 36) — cerrado el 30/09

Re-ejecutados los **7 tests instrumentados** (0 fallos) y recorrida la app real con el usuario QA:

| Pantalla | Qué se vio |
|---|---|
| Login | Formulario correcto; sesión con el usuario QA ✅ |
| Noticias | Feed con datos reales de NewsAPI ✅ |
| **Social** | Muro con autores reales (**«Javi»**, inicial **«J»**), mensajes, «Me gusta», campo de comentario con contador `0 / 280`. Sin literal `«Autor»` ✅ |
| **Amigos** | Fila con **nombre + handle** («Javi» / «Javi12»), inicial del nombre. Sin literal `«Amigo»`, sin `«Sin nombre»` ✅ |

Capturas: `.openclaw/tmp/social-tras-fix.png` y `.openclaw/tmp/amigos-tras-fix.png`.

#### 🔴 Hallazgo: la pestaña Social crashea al abrirse (preexistente, ya corregido)

El recorrido encontró un crash que ninguna prueba detectaba:

```
E ComposeInternal: java.lang.RuntimeException: Cannot create an instance of
                   class es.androidtfm.gamevision.viewmodel.DDBBViewModel
    at SocialScreenKt.SocialScreen(SocialScreen.kt:510)
```

**Causa:** `SocialScreen` declara `ddbbViewModel: DDBBViewModel = viewModel()`. Dentro del bloque
`composable() { … }` el `LocalViewModelStoreOwner` es el **`NavBackStackEntry`**, cuya
`defaultViewModelProviderFactory` **no es la de Hilt**, así que `viewModel()` no puede construir un
`@HiltViewModel` y la pantalla se cae al entrar.

**No era mi regresión**: el mismo código estaba en la versión commiteada antes de mis cambios.

**Por qué nadie lo vio:** las rutas `stats`, `diary`, `friendlist` y `news` **sí** pasan
`ddbbViewModel` desde `NavHost`, que ya lo recibe de `MainActivity`. La de `social` era la única que
no lo pasaba, así que era la única que caía en el default roto. Las verificaciones anteriores se
hicieron por `friendlist`, no por `social`.

**Corregido** en `NavHost.kt` pasando el parámetro, igual que las rutas hermanas. Verificado en
emulador: la pantalla abre y renderiza sin `FATAL` en logcat.

> Nota: `SocialScreen` sigue siendo **la única pantalla con defaults `= viewModel()`**. Ese default
> es una mina porque crashea si alguien vuelve a olvidar el parámetro. El resto de pantallas los
> exige explícitos. Retirarlo rompería el `@Preview`; queda registrado como deuda menor.

---

## Reservas

Ninguna abierta. Cerradas las tres:

| Reserva | Cómo se cerró |
|---|---|
| Tests instrumentados tras los cambios de UI y de `UserRepository` | `connectedDebugAndroidTest` → **7/7 en verde** (Pixel_9, API 36) |
| Recorrido visual de amigos y muro con sus estados | Recorrido real con el usuario QA: Social y Amigos verificados; hallazgo del crash de `Social` encontrado y corregido |
| Validación del propietario | ✅ dada el 29/09/2026 al aprobar la ejecución de los cambios urgentes |

**Fuera de este alcance (no bloquea F1):** los estados **vacío/carga/error/offline** de estas dos
pantallas no se ejercitaron, porque el usuario QA ya tiene datos y no se forzó un fallo de red.
Queda como trabajo de F4.5 (auditoría de experiencia), que es su fase.

## ✅ Despliegue de las reglas (29/09/2026, 21:21 UTC)

La corrección de seguridad **ya está en producción**, con permiso explícito del propietario.

| | |
|---|---|
| Proyecto | `gamevision-tfm-b1b4d` |
| Release activo | `projects/gamevision-tfm-b1b4d/releases/cloud.firestore` |
| Ruleset activo | `projects/gamevision-tfm-b1b4d/rulesets/efc02946-866a-4923-acd4-9d3eb08e9590` |
| Ruleset anterior (rollback) | `projects/gamevision-tfm-b1b4d/rulesets/f1c53dd3-7cdd-4270-8a3a-8fd630becbb6` |
| Verificación | **4/4** comprobaciones sobre el contenido servido, idéntico byte a byte al local (3704) |

**Cómo se desplegó** y por qué no con `firebase deploy`: la service key no tiene permiso sobre
`serviceusage.googleapis.com`, así que el pre-flight de firebase-tools falla con 403 **antes** de
tocar las reglas. Nuevo script [`scripts/deploy-rules/`](../../scripts/deploy-rules/deploy-rules.js),
que usa la Firebase Rules API directamente, con **dry-run por defecto**.

Dos detalles no evidentes que costaron dos intentos fallidos:

1. **El release de Firestore se llama `cloud.firestore`.** Crear un release con otro nombre
   *parece* un despliegue válido y **no lo es**: Firestore sigue usando las viejas.
2. **El PATCH exige el cuerpo envuelto** en `{ release: { name, rulesetName } }` y **sin**
   `updateMask`. Un Release a secas devuelve `400 Unknown name "rulesetName"`.

## Acciones

1. ~~Desplegar las reglas corregidas~~ → **hecho y verificado** (ver arriba).
2. Cerrar las dos reservas de arriba: tests instrumentados y recorrido visual.
3. Firma del propietario. Tras eso, F1 pasa a ✅ Completada sin reservas y **F2 se abre**.
