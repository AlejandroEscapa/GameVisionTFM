# Auditoría de cierre — Fase 0 (Cimientos de datos)

> Aplica el [protocolo de auditoría de cierre](auditoria-de-cierre.md).
> **Ejecutada por:** agente (GameVision Agent) en modo autónomo · **Fecha:** 29/09/2026 (tarde/noche)
> **Validación del propietario:** ✅ **validada el 29/09/2026** (informe aprobado)

```
Fase: F0 — Cimientos de datos        Fecha: 29/09/2026
Resultado: ✅ APTA — validada por el propietario (29/09/2026)
Bloques:  1 ✅  2 ✅  3 ✅ (0 errores)  4 ✅  5 ✅  6 ✅
```

> **Actualización 29/09/2026 (noche, 3ª pasada):** **CA0.3 verificado** con la alternativa a Storage
> (ADR-0007: base64 en Firestore) → los **tres CA grandes (CA0.1, CA0.2, CA0.3) quedan cerrados**.
> **H1 corregido** (sin detalle técnico + «Reintentar»). Suite: **37 unitarios** + 7 instrumentados.

> **Actualización 29/09/2026 (noche, 2ª pasada):** CA0.2 **verificado en vivo** con el usuario QA
> (escritura offline sincronizada a Firestore). CA0.3 sigue bloqueado por el **coste de Storage**
> (requiere Blaze) → se investiga alternativa gratuita. Bloque 5 pasa a ✅.

---

## 1. Evidencia por bloque

| # | Bloque | Resultado | Evidencia |
|---|---|---|---|
| 1 | Compilación y build | ✅ | `assembleDebug` **y** `assembleRelease` (R8, minify + shrink) en verde. Sin warnings nuevos de código (solo los de toolchain ya conocidos: `android.builtInKotlin`/`newDsl` deprecados, propios de AGP 9). |
| 2 | Tests | ✅ | `testDebugUnitTest`: **37 unitarios, 0 fallos** (6 suites; +2 del copy de error H1). `connectedDebugAndroidTest` en emulador `Pixel_9` (API 36): **7 instrumentados, 0 fallos**. `assembleRelease` (R8) OK, APK ~7,5 MB. |
| 3 | Calidad estática | ✅ (con deuda anotada) | `lintDebug`: **0 errores** tras corregir los 4 detectados. Quedan **68 warnings**, triados (ver §3). |
| 4 | Arquitectura y consistencia | ✅ | Catálogo detrás de `GameCatalog` (SSOT); la UI no usa DTOs de RAWG; sin saltos de capa. `SearchViewModel` depende solo de la interfaz. |
| 5 | Documentación y trazabilidad | 🟡 | Docs sincronizadas salvo **dos discrepancias de recuento de tests** (corregidas, ver §3). |
| 6 | Experiencia y estados | 🟡 | Modo degradado verificado **en vivo** (CA0.1). Un hallazgo de estado de error en la ficha (ver §2, H1). |

## 2. Hallazgos

### H1 — La ficha sin caché y sin red mostraba el **error técnico crudo** al usuario ✅ (corregido)
- **Qué pasaba:** abrir una ficha **no cacheada** sin conexión mostraba
  *«Error al cargar detalles: Unable to resolve host "api.rawg.io"…»*.
- **Arreglo aplicado (29/09/2026, noche):**
  - `SearchViewModel` traduce los fallos con `toCatalogUserMessage()`: los de conectividad son
    «Sin conexión. Comprueba tu red e inténtalo de nuevo.» y el resto un mensaje genérico. **Nunca**
    se muestra el detalle técnico.
  - El estado de error de la ficha (`ErrorMessage`) ahora es un bloque con icono, mensaje y botón
    **«Reintentar»**.
- **Verificado:** con la red cortada y sin caché, la búsqueda muestra «Sin conexión…» (sin host);
  y 2 tests nuevos cubren el caso (`SearchViewModelTest`).
- **Estado:** ✅ cerrado.

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
| **CA0.2** Registro en modo avión → aparece en biblioteca al volver la red | ✅ **Verificado en vivo** | Con el **usuario QA** autenticado: se añadió **Halo 3** a la biblioteca **en modo avión** (estado «Jugando»); al recuperar la red, el juego **persistió y se sincronizó a Firestore** (`users/{uid}/library/28589`, `status=jugando`). La lista offline lo mostró ya sin conexión (escritura local) y siguió tras el sync. |
| **CA0.3** Foto de perfil viaja entre dispositivos | ✅ **Verificado en vivo** | **ADR-0007**: la foto se comprime y se guarda en `profile_images/{uid}` (Firestore). Verificado: se sube desde la app, se **borran los datos** (simula 2º dispositivo) y al volver a iniciar sesión **la foto aparece**. En Firestore: `profile_images/{uid}` con base64 de ~164 KB (cabecera JPEG válida) y `imageUri=firestore://profile_images`. |
| **CA0.5** Cambiar de proveedor de catálogo no toca la UI | 🟡 Parcial | Estructuralmente cumplido (adapter `GameCatalog` + inyección en `AppModule`; `SearchViewModel` solo conoce la interfaz). Falta la demostración explícita (swap de binding que compile sin tocar UI). |
| **CA0.6** Tests en verde | ✅ | **35 unitarios + 7 instrumentados**, 0 fallos. |

**Extra verificado (no listado como CA):** la app arranca en emulador, el login muestra «Continuar como
invitado», el **modo invitado entra y carga Noticias reales** (RAWG/news online OK).
También: **login con email/contraseña con el usuario QA** funciona y carga la navegación completa
(Perfil, Social, Lista, Búsqueda).

## 4.b Usuario QA creado (29/09/2026)

Se ha dado de alta un **usuario de pruebas fijo** para poder verificar E2E sin crear cuentas nuevas:
ver [entorno de pruebas](../metodologia/entorno-pruebas.md).

| Campo | Valor |
|---|---|
| Email | `gamevision@gmail.com` |
| Nombre / usuario | QA GameVision |
| UID | `gKfQYvAdiBOezcGnrWo3rH0nUmY2` |
| Documento perfil | `users/gamevision@gmail.com` |
| Credenciales | `.secrets/qa-user.md` (local, fuera del repo) |

> **Nota de arquitectura detectada:** el **perfil** se guarda en `users/{email}` (UserRepository)
mientras la **biblioteca** se guarda en `users/{uid}/…` (LibraryRepository, modelo F0-B). Es
intencional (clave por uid en la biblioteca), pero **conviene unificar el criterio** antes de F2:
que el perfil también use `uid`, o documentar por qué el email sigue siendo la clave del perfil.

## 5. Reservas para cerrar F0

1. ~~CA0.3~~ → **hecho** (ADR-0007, verificado en vivo).
2. ~~Decisión H1~~ → **hecho** (corregido y verificado).
3. **Demostración de CA0.5** (swap de binding que compile sin tocar UI). Opcional para cerrar, recomendable.
4. ~~Validación del propietario~~ → ✅ **validada (29/09/2026)**.

> **Nota de arquitectura (ADR-0008, propuesto):** hoy el **perfil** vive en `users/{email}` y la
> **biblioteca** en `users/{uid}/…`. Conviene unificar la identidad en **`uid`** antes de F2; propuesta
> detallada en [ADR-0008](../metodologia/adr/0008-clave-unica-uid.md).

## 6. Conclusión

El **código de F0 está sano**: compila (debug y release con R8), la suite completa pasa
(**37 unitarios + 7 instrumentados**), lint sin errores y la arquitectura respeta el adapter. Los
**tres criterios grandes (CA0.1, CA0.2, CA0.3) están verificados en vivo** con el emulador y una
cuenta real, incluida la foto que viaja entre dispositivos tras un borrado de datos.

**Recomendación del auditor:** F0 puede darse por cerrada una vez el propietario valide este informe
y (opcional) se demuestre CA0.5. La **unificación de la clave de identidad (ADR-0008)** debe cerrarse
antes de F2; el lint de estilo puede arrastrarse a F4.5.
