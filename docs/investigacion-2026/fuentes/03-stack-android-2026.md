## Dimension 03: Stack Android/Kotlin/Compose 2026

> **DOCUMENTO HISTÓRICO (marcado 30/09/2026):** refleja el stack en el momento de la
> investigación; el vigente está en `AGENTS.md` §1 y `gradle/libs.versions.toml`.

### Current State

GameVision declara hoy (2026-09-28) este stack:

| Componente | Versión declarada | Estado vs. referencia 2026 |
|---|---|---|
| Gradle | 9.6 | Coincide con el mínimo/por defecto de AGP 9.4 (9.6.0) ✅ |
| AGP | 9.4.1 | Cabeza de serie (AGP 9.4.0 publicado en septiembre 2026); soporta máx. API 37 ✅ |
| Kotlin | 2.2.10 (integrado) | **Desfasado ~2 ramas**: última estable 2.4.20 (2026-09-07) ⚠️ |
| KSP | KSP2 | No verificado en fuentes; requerido para Room/Hilt con Kotlin ≥2.x ⚠️ |
| Compose BOM | 2026.09 | Es la última BOM publicada (2026.09.00) ✅ |
| Material3 | 1.4 | Coincide con la estable 1.4.0 (2026-09-23) ✅ |
| compileSdk | 37 | Máximo que soporta AGP 9.4 (API 37) ✅ |
| targetSdk | 36 | Cumple el requisito de Play vigente desde 2026-08-31 (API 36) ✅ (justo) |
| minSdk | 33 | Sin requisito de plataforma; Hilt 2.60 exige mín. 23 ✅ |
| Hilt | 2.59.2 | Última es 2.60.1 (2026-07-06) ⚠️ |
| Room | 2.8.5 | No verificado en esta ronda ⚠️ |
| Navigation Compose | 2.10.2 | Es la última de **Nav2** (2026-09-23), pero **Nav3 1.2.0 ya es estable** ⚠️ |
| Coil | 3.3.0 | No verificado en esta ronda ⚠️ |

**Lectura general:** el proyecto está en la *frontera* de casi todo (Gradle, AGP, Compose BOM, Material3, compileSdk), con **dos rezagos claros**: (1) Kotlin 2.2.10 frente a 2.4.20 — arrastra lenguaje, tooling y compatibilidad de librerías; (2) arquitectura de navegación anclada a Nav2 cuando Nav3 ya es estable desde 2025-11-19. Dos ítems son **obligaciones de cumplimiento** (no opcionales): targetSdk ≥36 desde 2026-08-31 y edge-to-edge sin opt-out en targetSdk 36; un tercero tiene fecha futura (16 KB page size, 2027-02-01).

### Key Evidence

| Dato | Valor/Versión | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Kotlin última versión | 2.4.20 | 2026-09-07 | "2.4.20 Released: September 7, 2026 Release on GitHub A tooling release with new experimental and stable features, performance improvements, bug fixes, and tooling updates." | Alta | [^kotlinrel] |
| Kotlin 2.4.0 (novedades) | 2.4.0 | 2026-06-03 (según listado) / doc dice 2026-07-14 | "Language: Stable context parameters, explicit backing fields, and multiple features for annotation use-site targets... Kotlin/JVM: Support for Java 26" | Alta (fecha en tensión) | [^k24] |
| Kotlin 2.3.0 | 2.3.0 | 2025-12-16 | "Kotlin 2.3.0 focuses on feature stabilization, introduces a new mechanism for detecting unused return values, and improves context-sensitive resolution." | Alta | [^k23] |
| Compose BOM más reciente | 2026.09.00 | 2026-09 | "Make a selection 2026.09.00 2026.08.00 2026.06.01 2026.06.00..." | Alta | [^bom] |
| Compose Material3 estable | 1.4.0 | 2026-09-23 | "September 23, 2026 1.4.0 - - 1.5.0-alpha29" | Alta | [^m3] |
| Material 3 Expressive (definición) | — | 2026 | "M3 Expressive is an expansion of Material Design 3, including research-backed updates to theming, components, motion, typography, and more" | Alta | [^m3doc] |
| Material3 1.5 (alpha) quita requisito SDK | compileSdk 37 | 2026 | "Remove requirement for compileSdk 37" (notas de material3 1.5.0-alpha) | Media-alta | [^m3] |
| AGP 9.4 · API máx. y Gradle | AGP 9.4.0 / Gradle 9.6.0 / JDK 17 | 2026-09 | "The maximum API level that Android Gradle plugin 9.4 supports is API level 37... Gradle 9.6.0 9.6.0... JDK 17 17" | Alta | [^agp94] |
| AGP 9: fin del plugin kotlin-android | AGP 9.0 | 2026-01 | "Android Gradle plugin 9.0 introduces built-in Kotlin support and enables it by default. That means you no longer have to apply the org.jetbrains.kotlin.android (or kotlin-android) plugin in your build files to compile Kotlin source files." | Alta | [^builtin] |
| Error de migración esperado | AGP 9.0+ | 2026 | "Failed to apply plugin 'org.jetbrains.kotlin.android'. > The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0." | Alta | [^builtin] |
| Fin del opt-out de DSL antiguo | AGP 10.0 | mid-2026 | "Caution: The ability to opt-out will be removed in AGP 10.0 (mid-2026)." | Alta | [^agp90] |
| targetSdk exigido por Play | API 36 | 2026-08-31 | "Starting August 31 2026: New apps and app updates must target Android 16 (API level 36) or higher to be submitted to Google Play" | Alta | [^targetsdk] |
| Edge-to-edge obligatorio | targetSdk 36 | 2026 | "For apps targeting Android 16 (API level 36), R.attr#windowOptOutEdgeToEdgeEnforcement is deprecated and disabled, and your app can't opt-out of going edge-to-edge." | Alta | [^a16] |
| Predictive back por defecto | targetSdk 36 | 2026 | "the predictive back system animations (back-to-home, cross-task, and cross-activity) are enabled by default. Additionally, onBackPressed is not called and KeyEvent.KEYCODE_BACK is not dispatched anymore." | Alta | [^a16] |
| 16 KB page size (deadline) | 2027-02-01 | 2027-02-01 | "all apps targeting Android 15 (API level 35) and higher must support 16 KB memory page sizes on 64-bit devices on Google Play. Starting February 1, 2027, if your app updates don't support 16 KB memory page sizes, you won't be able to release these updates." | Alta | [^pagesize] |
| Permisos de salud Android 16 | granular | 2026 | "For apps targeting Android 16 (API level 36) or higher, BODY_SENSORS permissions use more granular permissions under android.permissions.health" | Alta | [^a16] |
| Navigation 3 estable | 1.0.0 (2025-11-19); 1.2.0 (2026-09-23) | 2025-2026 | "The Navigation3 library is now stable! Navigation3 is the AndroidX Compose first approach to navigation." | Alta | [^nav3] |
| Nav3: propósito | — | 2026 | "Navigation 3 is a navigation library designed to work with Compose. With Navigation 3, you have full control over your back stack, and you navigate between destinations by adding and removing items from a list." | Alta | [^nav3guide] |
| Nav3 ListDetailSceneStrategy | — | 2026-03 | "ListDetailSceneStrategy in Nav3 handles that." | Media | [^jetc] |
| Navigation (Nav2) última | 2.10.2 | 2026-09-23 | "September 23, 2026 2.10.2 - - -" | Alta | [^nav2] |
| Baseline Profiles | +~30% | — | "Baseline Profiles improve code execution speed by about 30% from the first launch by avoiding interpretation and just-in-time (JIT) compilation steps for included code paths." | Alta | [^bp] |
| JankStats | — | — | "The JankStats library helps you track and analyze performance problems in your applications. Jank refers to application frames that take too long to render" | Alta | [^jank] |
| Arquitectura: UDF | — | — | "Follow Unidirectional Data Flow (UDF). Strongly recommended. Follow Unidirectional Data Flow (UDF) principles, where ViewModels expose UI state using the observer pattern and receive actions from the UI through method calls." | Alta | [^arch] |
| Hilt/Dagger última | 2.60.1 | 2026-07-06 | tag "dagger-2.60.1"; 2.60: "minSDK for Hilt is now 23, matching AndroidX" | Alta | [^dagger] |
| Compose Multiplatform | 1.12.1 estable; iOS estable desde 1.8.0 | 2026-09-22 | "Compose Multiplatform is stable for Android, iOS, and desktop, while the web target is currently in Beta." | Alta | [^cmp] |
| Android Studio estable | Quail 4 · 2026.1.4 Patch 1 | 2026-04-28 | "This page lists new features and improvements in the latest version in the stable channel, Android Studio Quail 4." | Alta | [^studio] |
| Android 17 | API 37 (beta) | 2026 | "Android 17 marks the start of our transition to an intelligence system, putting your apps at the center." | Alta | [^a17] |

**Detalle por tema (lo que GameVision tiene / le falta):**

- **Kotlin.** Última estable = **2.4.20** (2026-09-07). GameVision usa **2.2.10** → se pierde: `context parameters` y `explicit backing fields` **estables** (2.4.0), `nested type aliases` y `data-flow-based exhaustiveness checks` **estables** (2.3.0), el `unused return value checker` (2.3.0), soporte de **Java 25** (2.3.0) y **Java 26** (2.4.0), y collection literals experimentales (2.4.0). **K2** es el compilador del frontend por defecto desde Kotlin 2.0 (afirmación de contexto establecida; no la re-verifiqué en fuente en esta ronda → confianza media). Con AGP 9 el compilador Kotlin va "integrado": la versión efectiva la fija el proyecto/AGP, así que subir Kotlin implica revisar cómo se declara la versión con *built-in Kotlin* (ver riesgo R4).
- **Compose.** BOM **2026.09.00** (última) y Material3 **1.4.0** estable = mismos números que declara GameVision. **Material 3 Expressive** ya está implementado en Compose y "complements the Android 16 visual style and system UI" [^m3doc]; las piezas Expressive graduadas a **no-experimental** (list items, FlexibleTopAppBar, etc.) siguen llegando por la rama **1.5.0-alpha** que, además, **relaja el requisito de compileSdk 37**. Adopción de Expressive: disponible desde Material3 1.4 estable, pero las APIs más nuevas aún en alpha.
- **AGP 9.x.** AGP 9.0 (2026-01) introduce **Kotlin integrado** y el **nuevo DSL** por defecto; el plugin `org.jetbrains.kotlin.android` es incompatible con el nuevo DSL. AGP **9.4** (2026-09) soporta **API 37**, exige **Gradle 9.6.0** y **JDK 17**. El opt-out del DSL antiguo (`android.newDsl=false`) **desaparece en AGP 10.0 (mid-2026)**. Para KMP, el plugin `org.jetbrains.kotlin.multiplatform` ya no puede combinarse con `com.android.library`/`com.android.application` con built-in Kotlin activo [^builtin].
- **Plataforma.** targetSdk **36** es el mínimo para publicar desde **2026-08-31**; **edge-to-edge** ya no se puede desactivar a partir de targetSdk 36; **predictive back** está on por defecto y `onBackPressed`/`KEYCODE_BACK` dejan de emitirse (opt-out temporal vía `android:enableOnBackInvokedCallback="false"`); **16 KB page size** obligatorio para actualizaciones desde **2027-02-01** (solo afecta si hay código nativo/NDK, directo o vía SDK); Android 16 añade granularidad en permisos de salud.
- **Navigation.** Nav2 (Navigation Compose) última = **2.10.2**, igual que GameVision. Pero **Nav3 1.0.0 fue estable el 2025-11-19** y **1.2.0 el 2026-09-23** (con 1.3.0-alpha01 en curso). Nav3 modela el back stack como una **lista propia**, da control total y permite leer **varios destinos a la vez** (layouts adaptativos), con `ListDetailSceneStrategy` para patrón lista-detalle automático.
- **Rendimiento/calidad.** Baseline Profiles documentadas (≈**+30%** de velocidad de ejecución desde el primer arranque); **Startup Profiles** y **Macrobenchmark** documentados en el mismo árbol de docs; **R8** sigue siendo el optimizador de release (comportamiento por defecto); **JankStats** para medir jank en producción.
- **Testing.** `createComposeRule()`/`createAndroidComposeRule` + `androidx.compose.ui:ui-test-junit4` para UI Compose; **Robolectric** para JVM; **screenshot testing** con **Roborazzi** (sobre Robolectric) o **Paparazzi** (JVM sin emulador) — el estándar de facto en 2026 para snapshots deterministas.
- **Arquitectura 2026.** Google recomienda **arquitectura por capas** + **UDF** ("Strongly recommended") con AAC `ViewModel`; **Now in Android** es el app de referencia "built entirely with Kotlin and Jetpack Compose". DI: **Hilt** es la recomendación oficiosa de Google (integrado con ViewModel/WorkManager); última versión **2.60.1**. (Nota: 2.60 **sube minSDK de Hilt a 23** y elimina multidex; 2.59.2 ya traía el fix de builds incrementales lentos con AGP 9.)
- **KMP / Compose Multiplatform.** CMP es **estable en Android, iOS y desktop**; **web sigue en Beta**; iOS estable desde **CMP 1.8.0 (2025-05-06)**; última estable **1.12.1** (2026-09-22).
- **Build/IDE.** Android Studio estable = **Quail 4 | 2026.1.4 Patch 1**; AGP 9.4 + Gradle 9.6.

### Tensions & Counter-arguments

1. **"Ya está en la última" — pero Kotlin no.** Gradle/AGP/BOM/Material3/compileSdk están en la frontera, lo que da falsa sensación de "todo al día"; sin embargo Kotlin 2.2.10 está **dos ramas por detrás** (2.3.x y 2.4.x). Con *built-in Kotlin*, la versión de Kotlin deja de ser un simple plugin y pasa a ser un ajuste del proyecto/AGP: subirla es barato en build files pero **puede arrastrar ABI de librerías** (Room/Hilt/Compose compiler) y exige KSP alineado. *Contra-argumento:* si no se usan `context parameters` ni se compila a Java 25/26, el coste-beneficio de subir Kotlin puede ser bajo a corto plazo — pero el desfase se agrava cada 6 meses (cadencia de releases) y bloquea features que las librerías empiezan a pedir.

2. **targetSdk 36: "cumple" pero por los pelos.** La regla de Play es de **2026-08-31** y GameVision está exactamente en 36. El siguiente escalón (API 37 / Android 17) llegará y obligará a subir targetSdk otra vez; ya tienen compileSdk 37, así que la deuda es mínima. Riesgo real: **edge-to-edge y predictive back no son opcionales a targetSdk 36**, y a menudo rompen UI/gestos en apps legacy. *Contra-argumento:* el opt-out `android:enableOnBackInvokedCallback="false"` permite posponer predictive back temporalmente, pero es deuda técnica.

3. **16 KB page size: cuidado con las fechas de los blogs.** Muchos artículos de 2025 afirman que el requisito entró en vigor el **2025-11-01**; la página oficial **actual** dice que **las actualizaciones que no soporten 16 KB no se podrán publicar a partir del 2027-02-01** [^pagesize]. Diferencia material de 15 meses: **verificar siempre en fuente oficial** antes de planificar un *freeze*. Además, solo afecta a apps con código nativo (NDK directo o SDK); una app 100% Kotlin/Java ya lo soporta por construcción (recomiendan probar igualmente).

4. **Nav2 vs Nav3: "2.10.2 es la última" ≠ "es lo recomendado".** GameVision tiene la **última de Nav2**, pero **Nav3 es estable desde 2025-11** y es el enfoque *Compose-first* con más capacidad adaptativa (`ListDetailSceneStrategy`). *Contra-argumento:* Nav3 cambia el modelo mental (back stack como lista propia, keys serializables, SceneStrategy) y **no hay migración automática** desde `navigation-compose`; para una app ya madura, el ROI de migrar solo llega si se necesitan layouts adaptativos/tablet o se quiere dejar atrás Nav2. Mantener Nav2 2.10.2 es perfectamente válido a corto plazo. (Nota: `ListDetailSceneStrategy` lo confirmé vía newsletter/snippet, no en la página de referencia de la API → **confianza media**, a re-verificar.)

5. **Hilt vs Koin: "oficial" vs "práctico".** Hilt es la recomendación de facto de Google y encaja con ViewModel/WorkManager; Koin tiene defensores por su menor ceremonia y mejor encaje en módulos SDK/KMP. Hay testimonios de migraciones **en ambos sentidos** y de fricción real (una migración Koin→Hilt reportada: "took two weeks, broke several tests"). *Contra-argumento:* para GameVision, ya en Hilt, lo sensato es **quedarse** y subir a 2.60.x; cambiar de DI sin motivo es riesgo puro. La pega inmediata no es Hilt vs Koin, sino que Hilt 2.59.2 está una versión menor por detrás (2.60.x) — y 2.60 cambia minSDK de Hilt a 23 (sin impacto con minSdk 33).

6. **Material 3 Expressive: ¿adoptar ya?** "Expressive" = expansión de M3 (theming, componentes, motion, tipografía) alineada con el estilo visual de Android 16. La versión **estable (1.4.0)** ya lo soporta, pero el flujo de novedades (list items, ToggleButton, TimePicker, `material3-ripple`) sigue en **1.5.0-alpha**. *Contra-argumento:* adoptar Expressive es sobre todo **decisión de producto**, y usar APIs alpha (aunque "no experimental") añade churn; M3 clásico sigue siendo válido.

7. **KMP: potencia, pero no gratis.** CMP es **estable en Android/iOS/desktop** (web en Beta). *Contra-argumento:* GameVision es (aparentemente) Android-only; introducir KMP multiplica la superficie de build (built-in Kotlin **prohíbe** combinar `kotlin.multiplatform` con `com.android.library/application`; hay que usar `com.android.kotlin.multiplatform.library`). Solo justificable si hay objetivo multi-plataforma real.

8. **Rendimiento medible, no asumido.** Baseline Profiles ≈ +30% y JankStats/Macrobenchmark/Startup Profiles existen, pero **no aportan nada sin medición en release + R8 + dispositivo real**; muchas guías insisten en medir release (no debug, no `CompilationMode.None`). *Contra-argumento:* el coste de mantener Baseline Profiles en CI es real; para apps pequeñas puede no compensar.

9. **Incertidumbre declarada.** No pude confirmar en fuente en esta ronda: la **última versión de Room (2.8.5)** y de **Coil (3.3.0)**, el estado real de **KSP2** en el proyecto, ni el **nombre exacto del ajuste** para fijar la versión de Kotlin bajo *built-in Kotlin* de AGP 9.4. Tampoco re-verifiqué que K2 sea el frontend por defecto (hecho establecido desde Kotlin 2.0). Se marcan como **no confirmados**, no como falsos.

#### Fuentes

[^kotlinrel]: Kotlin Documentation (JetBrains) — *Releases*. Consultado 2026-09-28. https://kotlinlang.org/docs/releases.html
[^k24]: Kotlin Documentation (JetBrains) — *What's new in Kotlin 2.4.0*. 2026. https://kotlinlang.org/docs/whatsnew24.html
[^k23]: Kotlin Documentation (JetBrains) — *What's new in Kotlin 2.3.0*. 2025-12-16. https://kotlinlang.org/docs/whatsnew23.html
[^bom]: Android Developers — *Compose BOM mapping*. 2026. https://developer.android.com/develop/ui/compose/bom/bom-mapping
[^m3]: Android Developers — *Compose Material3 releases*. 2026-09-23. https://developer.android.com/jetpack/androidx/releases/compose-material3
[^m3doc]: Android Developers — *Material Design 3 in Compose (M3 Expressive)*. 2026. https://developer.android.com/develop/ui/compose/designsystems/material3
[^agp94]: Android Developers — *Android Gradle plugin 9.4.0 (September 2026)*. 2026-09. https://developer.android.com/build/releases/gradle-plugin
[^agp90]: Android Developers — *Android Gradle plugin 9.0.1 (January 2026)*. 2026-01. https://developer.android.com/build/releases/agp-9-0-0-release-notes
[^builtin]: Android Developers — *Migrate to built-in Kotlin*. 2026. https://developer.android.com/build/migrate-to-built-in-kotlin
[^targetsdk]: Android Developers — *Target API level requirements for Google Play apps*. 2026. https://developer.android.com/google/play/requirements/target-sdk
[^a16]: Android Developers — *Behavior changes: apps targeting Android 16 (API level 36)*. 2026. https://developer.android.com/about/versions/16/behavior-changes-16
[^pagesize]: Android Developers — *Support 16 KB page sizes*. 2026. https://developer.android.com/guide/practices/page-sizes
[^nav3]: Android Developers — *Navigation3 releases*. 2026-09-23. https://developer.android.com/jetpack/androidx/releases/navigation3
[^nav3guide]: Android Developers — *Navigation 3*. 2026. https://developer.android.com/guide/navigation/navigation-3
[^nav2]: Android Developers — *Navigation releases (Nav2)*. 2026-09-23. https://developer.android.com/jetpack/androidx/releases/navigation
[^jetc]: jetc.dev — *Newsletter Issue #305*. 2026-03-10. https://jetc.dev/
[^bp]: Android Developers — *Baseline Profiles overview*. https://developer.android.com/topic/performance/baselineprofiles/overview
[^jank]: Android Developers — *JankStats Library*. https://developer.android.com/topic/performance/jankstats
[^arch]: Android Developers — *Architecture recommendations*. https://developer.android.com/topic/architecture/recommendations
[^dagger]: GitHub — *google/dagger releases (Dagger/Hilt 2.60.1, 2.60, 2.59.2)*. 2026-07-06. https://github.com/google/dagger/releases
[^cmp]: Kotlin Multiplatform / JetBrains — *Kotlin Multiplatform – Build Cross-Platform Apps* + Compose Multiplatform releases (v1.12.1). 2026-09-22. https://kotlinlang.org/ · https://github.com/JetBrains/compose-multiplatform/releases
[^studio]: Android Developers — *Android Studio Quail 4 (2026.1.4 Patch 1)*. 2026-04-28. https://developer.android.com/studio/releases
[^a17]: Android Developers — *Android 17*. 2026. https://developer.android.com/about/versions/17
