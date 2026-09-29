# Auditoría de cierre — Fase 0 (Cimientos de datos)

> Aplica el [protocolo de auditoría de cierre](auditoria-de-cierre.md).
> **Ejecutada por:** agente (GameVision Agent) en modo autónomo · **Fecha:** 29/09/2026 (tarde/noche)
> **Validación del propietario:** ⬜ pendiente

```
Fase: F0 — Cimientos de datos        Fecha: 29/09/2026
Resultado: 🟡 Apta con reservas
Bloques:  1 ✅  2 ✅  3 ✅ (0 errores)  4 ✅  5 🟡  6 🟡
```

---

## 1. Evidencia por bloque

| # | Bloque | Resultado | Evidencia |
|---|---|---|---|
| 1 | Compilación y build | ✅ | `assembleDebug` **y** `assembleRelease` (R8, minify + shrink) en verde. Sin warnings nuevos de código (solo los de toolchain ya conocidos: `android.builtInKotlin`/`newDsl` deprecados, propios de AGP 9). |
| 2 | Tests | ✅ | `testDebugUnitTest`: **35 unitarios, 0 fallos** (6 suites). `connectedDebugAndroidTest` en emulador `Pixel_9` (API 36): **7 instrumentados, 0 fallos**. |
| 3 | Calidad estática | ✅ (con deuda anotada) | `lintDebug`: **0 errores** tras corregir los 4 detectados. Quedan **68 warnings**, triados (ver §3). |
| 4 | Arquitectura y consistencia | ✅ | Catálogo detrás de `GameCatalog` (SSOT); la UI no usa DTOs de RAWG; sin saltos de capa. `SearchViewModel` depende solo de la interfaz. |
| 5 | Documentación y trazabilidad | 🟡 | Docs sincronizadas salvo **dos discrepancias de recuento de tests** (corregidas, ver §3). |
| 6 | Experiencia y estados | 🟡 | Modo degradado verificado **en vivo** (CA0.1). Un hallazgo de estado de error en la ficha (ver §2, H1). |

## 2. Hallazgos

### H1 — La ficha sin caché y sin red muestra un **error técnico crudo** al usuario 🟠
- **Qué pasa:** abrir una ficha **no cacheada** con el dispositivo sin conexión muestra el texto
  *«Error al cargar detalles: Unable to resolve host "api.rawg.io": No address associated with hostname»*.
- **Por qué:** el `OfflineBanner` de `GameDetails` solo aparece cuando `fromCache == true` (había copia
  guardada). Si no hay copia, cae en el estado `ErrorMessage(error)` y este pinta **la excepción tal cual**
  (el `ViewModel` compone `"Error al cargar detalles: ${it.message}"`).
- **Impacto:** fuga de detalle técnico a la UI y experiencia pobre en el caso más común de primer uso sin red.
- **Arreglo propuesto (no aplicado — depende del criterio de diseño del propietario):**
  - **(a)** Mapa de errores: si el fallo es de conectividad (`UnknownHostException`/`IOException`), mostrar un
    mensaje humano reutilizando el tono del banner («Sin conexión · no hay copia guardada de esta ficha»).
  - **(b)** Reutilizar `ErrorMessage` con copy genérico («No se pudo cargar la ficha. Revisa tu conexión e inténtalo de nuevo») + botón «Reintentar».
  - Recomendación del auditor: **(a) + botón Reintentar**, y **no** concatenar `it.message` en la UI.
- **Estado:** ⬜ pendiente de decisión del propietario (es copy/UX: varias salidas razonables).

### H2 — Recuento de tests desactualizado en la documentación 🟡 (corregido)
- Los docs decían **«38 unitarios»** (AGENTS.md, fase-0) y **«17 unitarios + 6 instrumentados»** (CA0.6).
  La realidad medida: **35 unitarios** (6 suites) y **7 instrumentados** (3 suites).
- **Acción:** corregido en `AGENTS.md`, `docs/roadmap/fase-0-cimientos-datos.md` y esta auditoría.

### H3 — 4 errores de lint detectados y corregidos ✅
1. `local.properties` (*PropertyEscape* ×2): rutas escapadas (`C\:/…`), escrito **sin BOM** (con BOM Gradle no
   lee `sdk.dir`). Archivo local (gitignored); ajeno al repo.
2. `AndroidManifest.xml` (*CredManMissingDal*): **suprimido con justificación** — solo usamos Credential
   Manager para «Sign in with Google»; no hay login por contraseña ni passkeys, así que el *asset statements*
   (`assetlinks.json`) que exige la regla no aplica. **Revisar si algún día se añaden passkeys.**
3. `SearchScreen.kt` (*ViewModelConstructorInComposable*): era el **preview** (`SearchViewModel()` usa su
   constructor sin args declarado a propósito para previews) → `@Suppress` con comentario explicativo.

## 3. Deuda de lint triada (68 warnings — no bloqueante)

| Categoría | Nº | Veredicto |
|---|---|---|
| `Typos` | 35 | **Falsos positivos**: cadenas base64 en `font_certs.xml`. Ignorar. |
| `UnusedResources` | 14 | Mixto: iconos de launcher usados por el manifiesto (falso positivo) + recursos realmente huérfanos (`gamevision.xml`, `gvloadingscreen.gif`, colores `gray_*`). → **limpieza en F4.5**. |
| `PrivateResource` | 5 | **Intencional**: override de los certs de fuentes de Google. |
| `VectorRaster` / `VectorPath` | 6 | Real: los vectores de marca son grandes (14 208 chars de path). → **optimizar iconografía en F4.5**. |
| `ObsoleteSdkInt` | 2 | Menor: checks de SDK ya siempre ciertos (minSdk 33). → limpieza en F4.5. |
| `NewerVersionAvailable` / `GradleDependency` | 4 | Frescura de dependencias. → rutina. |
| `UseKtx`, `OldTargetApi` | 2 | Menor. |

**Decisión:** no se corrigen ahora (sería refactor fuera de alcance de F0). Quedan como **tarea de
limpieza en F4.5 (diseño y pulido)** para que la app llegue a la fase comercial con lint ejemplar.

## 4. Criterios de aceptación (evidencia real de esta sesión)

| CA | Estado | Evidencia |
|---|---|---|
| **CA0.1** RAWG caído → sirve caché y avisa | ✅ **Verificado en vivo** | Emulador con **modo avión real** (`airplane_mode_on=1`): la búsqueda «Halo» devuelve los resultados cacheados y muestra el banner **«Sin conexión · mostrando resultados guardados»**. (Antes, con red: búsqueda real de RAWG OK.) |
| **CA0.2** Registro en modo avión → aparece en biblioteca al volver la red | ⛔ **No verificable ahora** | Requiere sesión autenticada (Firestore). Sin credenciales de una cuenta de prueba en este entorno. **Bloquea el cierre de la fase** hasta probarlo con la cuenta QA. |
| **CA0.3** Foto de perfil viaja entre dispositivos | ⛔ **No verificable ahora** | Requiere **segundo dispositivo**/borrado de datos y las **reglas de Storage publicadas**. Pendiente del propietario. |
| **CA0.5** Cambiar de proveedor de catálogo no toca la UI | 🟡 Parcial | Estructuralmente cumplido (adapter `GameCatalog` + inyección en `AppModule`; `SearchViewModel` solo conoce la interfaz). Falta la demostración explícita (swap de binding que compile sin tocar UI). |
| **CA0.6** Tests en verde | ✅ | **35 unitarios + 7 instrumentados**, 0 fallos. |

**Extra verificado (no listado como CA):** la app arranca en emulador, el login muestra «Continuar como
invitado», el **modo invitado entra y carga Noticias reales** (RAWG/news online OK).

## 5. Reservas y bloqueos para cerrar F0

1. **Publicar `firebase/storage.rules`** en Firebase Console (acción del propietario) → desbloquea CA0.3.
2. **Sesión de emulador autenticada** para **CA0.2** (y el tramo de biblioteca offline).
3. **Decisión sobre H1** (copy del estado de error de la ficha).
4. **Demostración de CA0.5** (swap de binding). Opcional para cerrar, recomendable.

## 6. Conclusión

El **código de F0 está sano**: compila (debug y release con R8), la suite completa pasa
(42 tests), lint sin errores y la arquitectura respeta el adapter. El **modo degradado funciona de
verdad** (probado con la red cortada). No se cierra la fase al 100 % porque **CA0.2 y CA0.3 dependen de
credenciales/dispositivo** y queda un **hallazgo de UX (H1)** que es decisión de diseño del propietario.

**Recomendación del auditor:** cerrar F0 en cuanto se resuelvan H1 (decisión), se publique
`storage.rules` y se pase CA0.2/CA0.3 en la sesión de emulador; el resto (lint de estilo) puede
arrastrarse como deuda a F4.5 sin bloquear F1.
