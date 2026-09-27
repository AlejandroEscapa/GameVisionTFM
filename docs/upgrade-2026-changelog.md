# Changelog de la modernización 2026 (upgrade-2026)

> Registro completo de **todo lo que se hizo** desde el snapshot original
> (ver [`previous-repo-state.md`](previous-repo-state.md)) hasta el estado actual.
> Objetivo: trazabilidad total. Si aparece un error tras cualquier cambio, este
> documento permite localizar el commit responsable y revertirlo de forma aislada.
>
> Trabajo realizado el 27/09/2026, en la rama `upgrade-2026` y fusionado
> (fast-forward) a `master`.

## Mapa de commits

| Commit | Fase | Título |
|---|---|---|
| `1c4522a` | F0 | Snapshot base del zip (+ `.gitignore` para local.properties/google-services.json/.zcode) |
| `c0b9f2d` | F0 | Ignorar `local.properties` y `google-services.json` |
| `026ffb9` | F1 | Toolchain 2026: Gradle 9.1, AGP 9.0.1, Kotlin integrado 2.2.10, KSP2, Hilt 2.59.2, Room 2.8.5, Java 17 |
| `62730ea` | F2 | Plataforma Android 16/17 + BOM 2026.09 + Material 3 puro + Coil 3 + edge-to-edge |
| `16d69a6` | F3 | Seguridad (API keys → BuildConfig, reglas de backup) + 11 tests unitarios |
| `dea737c` | F4 | R8 (minify + shrinkResources) + reglas mínimas + fix lintVital de backups |
| `a6fef9d` | M1 | Google Sign-In → Jetpack Credential Manager |
| `3b5b007` | M2 | Capa Repository + Hilt real (@HiltAndroidApp, AppModule, @HiltViewModel) |
| `f703965` | M3 | Signing config de release vía local.properties + versionCode 2 / versionName 2.0.0 |
| `572f732` | M4a | Gson/Jackson → kotlinx.serialization |

## Fase 0 — Línea base

- `git init`, conexión al remoto, rama `upgrade-2026` basada en `origin/master`.
- Creado `local.properties` (sdk.dir con barras normales — con backslashes AGP
  daba `IOException: Invalid file path` en SdkLocator).
- Placeholder de `app/google-services.json` (gitignored) para poder compilar.
- Verificado: `assembleDebug` del estado original compila antes de tocar nada.

## Fase 1 — Toolchain (commit `026ffb9`)

- `gradle-wrapper.properties`: 8.11.1 → 9.1.0.
- Catálogo: `agp 8.10.0 → 9.0.1`; **eliminado** el plugin `kotlin-android`
  (AGP 9 compila Kotlin de forma integrada); añadido
  `org.jetbrains.kotlin.plugin.compose` 2.2.10; `ksp → 2.3.6`; `hilt → 2.59.2`;
  `room → 2.8.5` (2.6.1 no es compatible con KSP2); `google-services → 4.5.0`.
- `app/build.gradle.kts`: eliminados los bloques `kotlinOptions {}` y
  `composeOptions {}`; `compileOptions` Java 1.8 → 17; plugins por alias.
- Build raíz: eliminadas las versiones hardcodeadas; todo por alias del catálogo.
- Verificado: assembleDebug + help + build --dry-run.

## Fase 2 — Plataforma y UI base (commit `62730ea`)

- `compileSdk 35 → 37` (Android 17; el BOM 2026.09 lo exige), `targetSdk 34 → 36`.
- AGP 9.0.1 → **9.4.1**, Gradle 9.1.0 → **9.6.0** (mínimo de AGP 9.4).
- Instalación manual de la plataforma `android-37.0` y cmdline-tools en el SDK
  local (el sdkmanager disponible era anterior a API 37).
- Compose BOM 2023.08.00 → **2026.09.00**; eliminados TODOS los pins manuales
  de Compose (el BOM gobierna); `material-icons-extended` fijado a 1.7.8
  (librería congelada).
- **Material 2 eliminado**: `BottomNavigation`→`NavigationBar`,
  `BottomNavigationItem`→`NavigationBarItem` (y `backgroundColor`→`containerColor`),
  `Divider`→`HorizontalDivider`, `Surface`/`Text`→M3 (LoginScreen, SocialScreen,
  BottomBarNavigation).
- **Coil 2.3 → 3.3.0**: imports `coil.compose.*` → `coil3.compose.*` (5 archivos),
  añadido `coil-network-okhttp`. Coil 3.5+ descartado (metadatos Kotlin 2.4).
- **Edge-to-edge** (skill `edge-to-edge`): `enableEdgeToEdge()` en `onCreate`;
  `SystemUiController` (accompanist + `statusBarColor`) sustituido por
  `SystemBarAppearance` (solo apariencia de iconos); `windowInsetsPadding`
  movido dentro del `Surface` para que el fondo llegue al borde; manifest con
  `windowSoftInputMode="adjustResize"`.
- Logout reescrito: `GoogleSignIn` (eliminado en play-services-auth 22) →
  `SignInClient.signOut()` (luego M1 lo volvió a migrar).

## Fase 3 — Seguridad y tests (commit `16d69a6`)

- **API keys fuera del código**: `NEWS_API_KEY` y `RAWG_API_KEY` se leen de
  `local.properties` vía `buildConfigField` (en `buildTypes.all`); valores reales
  movidos a local.properties. Touchpoints: `NewsViewModel`, `GameApiService`.
- **Backups**: `data_extraction_rules.xml` + `backup_rules.xml` excluyen caché de
  Firestore y DataStore (cloud-backup y device-transfer).
- **Testabilidad por inyección con defaults**: `NewsViewModel(newsApi)`,
  `SearchViewModel(gamesApi)`, `ThemeViewModel(application, themeDataStore,
  externalScope)`, `ThemeDataStore(context, dataStoreOverride)`.
- **11 tests unitarios** (JUnit4 + coroutines-test 1.11 + turbine):
  `NewsViewModelTest` (filtro `[Removed]`/sin imagen, orden, límite 25, fechas),
  `SearchViewModelTest` (éxito/error/limpieza de query, detalles),
  `ThemeViewModelTest` (DataStore real en fichero temporal).
- Test de UI Compose `BottomBarNavigationTest` (androidTest; requiere dispositivo).
- Eliminados los tests plantilla `Example*Test`.
- Aprendizajes embebidos en los tests: scope inyectable en ThemeViewModel (si no,
  las corrutinas sobreviven al test y contaminan los siguientes), esperas en
  tiempo real (`Dispatchers.Default.limitedParallelism(1)`) porque el tiempo de
  `runTest` es virtual, y no borrar el fichero de DataStore en caliente.

## Fase 4 — R8 (commit `dea737c`)

- `isMinifyEnabled = true` + `isShrinkResources = true` en release.
- `proguard-rules.pro`: reglas mínimas (DTOs para reflexión de Gson — luego
  eliminada en M4a — y `SourceFile,LineNumberTable` para deobfuscación).
- `lintVitalRelease` destapó 2 errores fatales de las reglas de backup: el dominio
  `database` no admite paths de directorio (sin barra final). Corregidos.
- Verificado: `assembleRelease` (~5,9 MB) + `mapping.txt` generado.

## M1 — Credential Manager (commit `a6fef9d`)

- `GoogleViewModel` reescrito sobre `androidx.credentials` + `googleid`
  (`GetCredentialRequest` + `GoogleIdTokenCredential`; `signIn(context)` suspendido;
  `GetCredentialCancellationException` → estado Idle, no error).
- `logout()` ahora suspende y llama a `clearCredentialState()`.
- Eliminado todo el andamiaje del launcher (`googleSignInLauncher`) de
  MainActivity/NavHost/LoginScreen; `play-services-auth` fuera del classpath.
- **googleid fijado en 1.1.1** (la 1.2.1 viene compilada con Kotlin 2.4).
- La máquina de estados `SignInState` no cambió → UI intacta.

## M2 — Repository + Hilt (commit `3b5b007`)

- Nuevo: `GameVisionApplication` (`@HiltAndroidApp`, registrado en el manifest),
  `di/AppModule.kt` (Firestore, FirebaseAuth, APIs Retrofit, CredentialManager,
  `@Named("webClientId")`, ThemeDataStore, CoroutineScope de aplicación),
  `data/repository/UserRepository.kt` (todo el acceso a Firestore, extraído de
  DDBBViewModel verbatim).
- `MainActivity` con `@AndroidEntryPoint`; `SearchViewModel` pasa a `viewModel()`.
- ViewModels: `@HiltViewModel` + `@Inject` primario **+ constructor secundario sin
  argumentos** (Dagger KSP rechaza defaults en `@Inject`; los previews siguen
  funcionando con `UserViewModel()`, `DDBBViewModel()`, etc.).
- `GoogleViewModel` sigue siendo VM **plano** (no `@HiltViewModel`): necesita
  contexto de Activity y lo inicializa `MainActivity` con
  `initializeGoogleSignIn(this, webClientId)` — comportamiento idéntico al original,
  ahora sobre Credential Manager.
- Estado/flows públicos de todos los ViewModels: sin cambios.

## M3 — Firma y versión (commit `f703965`)

- `signingConfigs.create("release")` solo si `local.properties` define
  `storeFile`; plantilla de claves documentada al final de local.properties.
- `versionCode 2`, `versionName "2.0.0"`.

## M4a — kotlinx.serialization (commit `572f732`)

- Plugin `org.jetbrains.kotlin.plugin.serialization` 2.2.10 (root + app).
- `retrofit 2.11 → 2.12` (incluye el converter oficial) +
  `kotlinx-serialization-json 1.11.0`; **eliminados** `converter-gson` y
  `jackson-module-kotlin`.
- DTOs (`GameDAO.kt`, `NewDAO.kt`): `@SerializedName` → `@SerialName`,
  `@Serializable` en todas las clases y **defaults en todos los campos**
  (tolerancia equivalente a Gson ante claves ausentes).
- `RetrofitInstance`: `Json { ignoreUnknownKeys = true; coerceInputValues = true }`
  + `asConverterFactory("application/json".toMediaType())`.
- R8: eliminada la keep rule de DTOs (serializadores generados en compilación).
- Verificado: assembleDebug, 11 tests, assembleRelease (R8), androidTest compila.

## Qué NO se hizo (y por qué)

- **M4b Navigation 3**: saltada deliberadamente; la navegación actual (2.10.2)
  funciona y no se puede validar en runtime sin dispositivo.
- **M5 perfilado / smoke test / baseline profiles / test de UI en runtime**:
  requieren dispositivo o emulador conectado (no había ninguno).
- **Firmar el release**: requiere la keystore del propietario (instrucciones en
  `local.properties`). El flujo de firma ya está montado y probado sin keystore.

## Cómo depurar si algo falla

```bash
git log --oneline                     # localizar el commit sospechoso
git revert <commit>                   # deshacer UN hito de forma aislada (recomendado)
git checkout <commit> -- app/ gradle/ # restaurar solo ficheros concretos
```

Dependencias críticas entre hitos: F1→F2 (toolchain), F3→F4 (reglas de backup
corregidas por lintVital), M2 depende de F1 (Hilt/KSP2), M4a depende de M2
(UserRepository referencia DTOs). `googleid 1.1.1` y `Coil 3.3.0` son límites
duros hasta que AGP integre Kotlin ≥ 2.3 — ver `AGENTS.md` §3.
