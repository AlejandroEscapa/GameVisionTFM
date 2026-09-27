# Estado previo del repositorio ("previous-repo-state")

> Congelado a fecha **27/09/2026**. Este documento describe **cómo estaba la app
> ANTES de la modernización**, es decir, el estado del zip exportado de
> `master` en `0fa36fc` (snapshot de trabajo en `1c4522a`).
> Sirve de referencia histórica: si algo se rompe, aquí está el punto de partida
> original y qué se ha cambiado desde entonces (ver también
> [`upgrade-2026-changelog.md`](upgrade-2026-changelog.md)).

## 1. Origen

- TFM de **Alejandro Olivares Escapa**, fechado entre enero y febrero de 2025.
- El proyecto llegó como **zip** del repositorio `AlejandroEscapa/GameVisionTFM`
  (sin `.git`), con las carpetas de skills locales (`.zcode/`) añadidas después.
- Repositorio remoto: `https://github.com/AlejandroEscapa/GameVisionTFM.git`,
  rama `master` en `0fa36fc`.

## 2. Toolchain congelado (principios de 2025)

| Componente | Versión entonces |
|---|---|
| Gradle | 8.11.1 |
| AGP | 8.10.0 |
| Kotlin | 1.9.0 (K1) con plugin `org.jetbrains.kotlin.android` |
| KSP | 1.9.0-1.0.13 (KSP1) |
| Compose BOM | **2023.08.00** con pins manuales contradictorios a Compose 1.7.6 |
| Compose compiler | 1.5.0 (vía `composeOptions.kotlinCompilerExtensionVersion`) |
| compileSdk / targetSdk | 35 / **34** |
| minSdk | 33 |
| Java | 1.8 (source/target/jvmTarget) |
| Hilt | 2.53.1 en catálogo **y** 2.51.1 hardcodeado en el build raíz (duplicado) |
| Room | 2.6.1 · Retrofit 2.11 + converter Gson 2.9 · Jackson 2.13 · Coil 2.3.0 |

## 3. Código y arquitectura entonces

- **31 archivos Kotlin**, ~6.300 líneas, 100% Jetpack Compose (sin XML de layouts).
- Sin Application class, sin Hilt activo (plugin aplicado pero ningún ViewModel
  inyectado), sin capa Repository: los ViewModels llamaban a
  `FirebaseFirestore.getInstance()` / `FirebaseAuth.getInstance()` directamente.
- `MainActivity` con `ActivityResultLauncher` manual para Google Sign-In vía
  **APIs "One Tap" de Identity** (`BeginSignInRequest`, `SignInClient`) — hoy deprecadas.
- `MainActivity.kt` pintaba la status bar con `window.statusBarColor`
  (deprecado) + `accompanist-systemuicontroller` 0.32.0 (deprecado).
- Mezcla **Material 2 + Material 3** (`BottomNavigation`, `Divider`, `Surface` y
  `Text` de M2 en LoginScreen, SocialScreen y BottomBarNavigation).
- Mezcla **LiveData + StateFlow**.
- **Dos API keys hardcodeadas en el código fuente**: NewsAPI en
  `NewsViewModel.kt` (`860f15b6…`) y RAWG dos veces en `GameApiService.kt`
  (`f1d385d0…`).
- **Cero tests reales**: solo `ExampleUnitTest` / `ExampleInstrumentedTest`
  de la plantilla.
- Release **sin minify** (`isMinifyEnabled = false`), `proguard-rules.pro` vacío
  (plantilla).
- Reglas de backup: plantillas vacías (TODO), con `allowBackup=true`.
- `versionCode 1`, `versionName "1.0"`. Sin signing config.
- Jackson declarado pero sin uso real (solo un import huérfano en `GameDAO.kt`).

## 4. Navegación y pantallas (no han cambiado de forma estructural)

Mismas 12 rutas: `main`, `login`, `news`, `profile`, `gamelist`, `gameSearch`,
`register`, `passrecover`, `editProfile`, `social`, `gameDetails/{gameId}`,
`friendlist`. El manifiesto ya tenía `enableOnBackInvokedCallback=true`.

## 5. Problemas del entorno detectados al empezar (no del repo, pero relevantes)

- Caché de Gradle 8.11.1 corrupta (`trove4j` faltante) → re-descarga.
- `JAVA_HOME` de `~/.jdks/ms-17.0.17` **corrupto** (`sun.util.calendar.ZoneInfoFile`)
  → se usa el JBR de Android Studio (`/c/Program Files/Android/Android Studio/jbr`).
- `local.properties` no existía; el SDK tenía plataformas 33/35/36 (la 37 se
  instaló a mano más tarde, junto con cmdline-tools 13114758).
- `google-services.json` **no venía** en el zip (secreto nunca commiteado) →
  se creó un placeholder gitignored; el real debe aportarlo el propietario.

## 6. Cómo volver a este estado (si fuera necesario)

```bash
git log --oneline                       # localizar commits
git checkout 1c4522a                    # snapshot exacto del zip original (+ .gitignore)
git checkout 0fa36fc -- app/ gradle/    # restaurar solo código/build del original
```

La historia completa posterior está en `docs/upgrade-2026-changelog.md`.
