# AGENTS.md — GameVision (es.androidtfm.gamevision)

Guía completa para agentes y desarrolladores que trabajen en este repositorio.
Estado descrito aquí: **post-upgrade-2026** (merge `572f732` en `master`).
El estado previo a la modernización está congelado en [`docs/previous-repo-state.md`](docs/previous-repo-state.md).
Todo lo que se hizo, commit a commit, está en [`docs/upgrade-2026-changelog.md`](docs/upgrade-2026-changelog.md).

## 1. Qué es esta app

App Android 100% Jetpack Compose (cero layouts XML) para consultar videojuegos
(RAWG API), noticias de videojuegos (NewsAPI) y gestión social entre usuarios
(Firebase Auth + Firestore). Originalmente un TFM (enero 2025), modernizada
íntegramente en septiembre 2026.

- **Paquete raíz:** `es.androidtfm.gamevision`
- **Idioma del código/comentarios:** español (mantener en nuevos cambios)
- **~6.900 líneas de Kotlin** en 36 archivos (main) + 4 de tests

## 2. Comandos esenciales (Git Bash / Windows)

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # JDK 21; NO usar ~/.jdks/ms-17.0.17 (corrupto)
./gradlew assembleDebug            # APK debug
./gradlew testDebugUnitTest        # tests unitarios (11 verdes)
./gradlew connectedDebugAndroidTest # tests UI (requiere emulador/dispositivo)
./gradlew assembleRelease          # APK release R8 (~7,5 MB; sin firmar si no hay keystore)
```

- `local.properties` (gitignored) contiene: `sdk.dir`, `newsApiKey`, `rawgApiKey`
  y opcionalmente `storeFile/storePassword/keyAlias/keyPassword`.
- `app/google-services.json` es un **placeholder** (gitignored). Para runtime
  (Auth/Firestore/Google Sign-In) hay que poner el real de la consola de Firebase.

## 3. Toolchain y restricciones de versiones (crítico)

Definido todo en `gradle/libs.versions.toml` (única fuente de verdad, sin versiones
hardcodeadas en build files).

| Componente | Versión |
|---|---|
| Gradle | 9.6.0 |
| AGP | 9.4.1 (Kotlin **integrado/built-in**, sin plugin kotlin-android) |
| Kotlin (compilador integrado) | 2.2.10 — plugins compose y serialization en 2.2.10 |
| KSP | 2.3.6 (KSP2) |
| Compose BOM | 2026.09.00 (Compose 1.12.1, Material3 1.4.0) |
| compileSdk / targetSdk / minSdk | 37 / 36 / 33 |
| Java | 17 (compilación con JDK 21 del JBR) |
| Hilt | 2.59.2 · Room 2.8.5 · Retrofit 2.12.0 · Coil 3.3.0 · credentials 1.6.0 · googleid 1.1.1 |

**Restricción dura que no se debe redescubrir por prueba y error:** el compilador
Kotlin integrado de AGP 9.4.1 lee metadatos solo hasta Kotlin **2.3.0**. Cualquier
librería compilada con Kotlin 2.4 rompe la compilación
("Incompatible classes were found in dependencies"). Por eso:
- `Coil` está en **3.3.0** (3.5+/3.6.x usan Kotlin 2.4).
- `googleid` está en **1.1.1** (1.2.1 usa Kotlin 2.4).
- `kotlinx-serialization-json` 1.11.0 **sí** es compatible.
- Cuando Google publique un AGP con Kotlin integrado ≥ 2.3/2.4, subir Coil y googleid.

**Dagger KSP** no acepta parámetros con valor por defecto en constructores `@Inject`
("should contain exactly one @Inject constructor"): los ViewModels usan constructor
primario `@Inject` + constructor secundario sin argumentos para previews.

## 4. Arquitectura

```
app/src/main/java/es/androidtfm/gamevision/
├── GameVisionApplication.kt        @HiltAndroidApp
├── MainActivity.kt                 @AndroidEntryPoint; edge-to-edge; MainScreen
├── di/
│   └── AppModule.kt                Singletons: Firestore, Auth, APIs Retrofit,
│                                   CredentialManager, webClientId, ThemeDataStore,
│                                   CoroutineScope de aplicación
├── data/repository/
│   └── UserRepository.kt           TODO el acceso a Firestore (344 líneas)
├── datastore/
│   └── DataStoreSettings.kt        ThemeDataStore (tema claro/oscuro, Preferences)
├── retrofit/
│   ├── RetrofitInstance.kt         2 Rettrofit singletons + Json config (leniente)
│   ├── GameApiService.kt           RAWG: searchGames, getGameDetails (key en BuildConfig)
│   ├── NewsApiService.kt           NewsAPI: getEverything
│   ├── GameDAO.kt                  DTOs RAWG @Serializable (Game, ApiResponse…)
│   └── NewDAO.kt                   DTOs NewsAPI @Serializable (New, Article, Source)
├── viewmodel/
│   ├── DDBBViewModel.kt            @HiltViewModel; estado usuario; delega en UserRepository
│   ├── UserViewModel.kt            @HiltViewModel; formulario registro/login, ProfileInfo
│   ├── GoogleViewModel.kt          VM plano (lo inicializa la Activity); Credential Manager
│   ├── NewsViewModel.kt            @HiltViewModel; filtrado/ordenado de noticias
│   ├── SearchViewModel.kt          @HiltViewModel; búsqueda y detalles de juegos
│   └── ThemeViewModel.kt           @HiltViewModel; tema claro/oscuro (DataStore + scope inyectable)
├── ui/navigation/
│   ├── NavHost.kt                  NavHost compose + grafo de 12 rutas
│   └── BottomBarNavigation.kt      NavigationBar M3 (ModernStyledNavigationBar)
├── ui/theme/Theme.kt               paletas M3 clara/oscura manual (toggle, no dinámico)
├── ui/views/composables/           12 pantallas, todas @Composable (ver §5)
├── User.kt                         modelo ligero de usuario
└── res/                            drawable (logos vectoriales), values, xml (backup rules)
```

**Flujo de datos:** Pantalla (Composable) → ViewModel (StateFlow/LiveData) →
UserRepository (Firestore) / RetrofitInstance (red). Sin use-cases ni Flow de
dominio: es una app de un solo módulo, la complejidad extra no está justificada.

## 5. Rutas de navegación (NavHost.kt)

`main` (home) · `login` · `news` (destino post-login) · `profile` · `gamelist` ·
`gameSearch` · `register` · `passrecover` · `editProfile` · `social` ·
`gameDetails/{gameId}` · `friendlist`.

Barra inferior (BottomBarNavigation) visible en guest con 2 items y autenticado
con 5 (Home, Search, gamelist, Social, Profile). Back hacia `main` con popUpTo.

## 6. Estado actual (lo verificado)

- ✅ `assembleDebug`, `testDebugUnitTest` (11 tests: News/Search/Theme),
  `assembleDebugAndroidTest` y `assembleRelease` (R8 + lintVital) pasan.
- ✅ Sin APIs deprecadas conocidas en el código (One Tap migrada a Credential Manager).
- ✅ R8 con reglas mínimas (`proguard-rules.pro`): solo atributos de crash traceability.
- ⚠️ Pendiente de validación **en dispositivo** (no hubo emulador disponible):
  smoke test de la app release, test de UI `BottomBarNavigationTest`, trazas de
  android-profiler, baseline profiles.
- ⚠️ Release sin firmar hasta que el usuario configure la keystore en local.properties.

## 7. Convenciones para futuros cambios

1. **Nuevas versiones → solo `libs.versions.toml`.** Nunca hardcodear en build files.
2. **Secrets → `local.properties` + BuildConfig** (`NEWS_API_KEY`, `RAWG_API_KEY`,
   keystore). Nunca en código fuente ni en el repo.
3. **Inyección:** dependencias nuevas → `AppModule`; ViewModels nuevos →
   `@HiltViewModel` + `@Inject` primario + secundario sin args (previews).
4. **Red:** DTOs nuevos → `@Serializable` + `@SerialName` + **default en todos los
   campos** (parseo tolerante). Nada de Gson (eliminado).
5. **UI:** Material 3 exclusivamente (Material 2 eliminado). Edge-to-edge: el
   fondo llega al borde; insets vía `windowInsetsPadding(WindowInsets.systemBars)`
   dentro del `Surface` de `MainScreen` — no reaplicar padding doble.
6. **Iconos de barras del sistema:** solo `SystemBarAppearance` (claro/oscuro).
   `window.statusBarColor` está deprecado — no reintroducirlo.
7. **Tests:** unit tests en JVM con fakes manuales (interfaces Retrofit /
   UserRepository); DataStore requiere scope inyectable + esperas en tiempo real
   (ver ThemeViewModelTest). Coroutines-test 1.11: `collect { }` lleva lambda.
8. **Backup:** reglas en `res/xml/data_extraction_rules.xml` + `backup_rules.xml`.
   Ojo: el dominio `database` no admite paths de directorio (lintVital lo marca fatal).
9. Commits convencionales (`build:`, `refactor:`, `feat:`, `test:`, `docs:`,
   `fix:`) en español o inglés, con cuerpo explicando el porqué.

## 8. Próxima frontera (acordado con el propietario)

Modernización de la UI (más profesional) trabajando sobre `ui/views/composables/`
y `ui/theme/`. Skills locales disponibles en `.zcode/skills/` (styles,
navigation-3, adaptive, migrate-xml-views-to-jetpack-compose, etc.).
Skills instaladas solo a nivel de proyecto, nunca global.
