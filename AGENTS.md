# AGENTS.md — GameVision (es.androidtfm.gamevision)

Guía completa para agentes y desarrolladores que trabajen en este repositorio.
Estado descrito aquí: **post-upgrade-2026** (merge `572f732` en `master`).
El estado previo a la modernización está congelado en [`docs/previous-repo-state.md`](docs/previous-repo-state.md).
Todo lo que se hizo, commit a commit, está en [`docs/upgrade-2026-changelog.md`](docs/upgrade-2026-changelog.md).

## 0. Propietario y forma de trabajar

- **Propietario:** Alejandro Olivares Escapa (León, España). Llamarlo "My G".
- **Idioma y tono:** siempre en español; los tecnicismos se quedan en inglés. Tono serio y
técnico, directo pero explicativo; jocoso solo si él abre la puerta.
- **Rigor sobre halagos:** sin adulaciones ni asentimientos por cortesía; si algo está mal o es
mejorable, decirlo con datos.
- **Excelencia:** buscar la solución correcta y bien hecha, no solo que funcione; señalar causas
raíz, riesgos y trade-offs que él no haya mencionado.
- **Alcance y autonomía:** proyecto personal en modo alta autonomía — ejecutar y explicar después.
Preguntar solo ante decisiones de diseño con varias salidas razonables. Nada de refactors fuera
de alcance ni comentarios existentes reformulados.
- **Acciones irreversibles** (`push --force`, `reset --hard`, borrados de ramas/datos): confirmar
siempre, incluso con orden explícita.

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
./gradlew testDebugUnitTest        # tests unitarios (88 verdes, 29/09/2026)
./gradlew connectedDebugAndroidTest # tests UI (requiere emulador/dispositivo)
./gradlew assembleRelease          # APK release R8 (~7,5 MB; sin firmar si no hay keystore)
```

- `local.properties` (gitignored) contiene: `sdk.dir`, `newsApiKey`, `rawgApiKey`
  y opcionalmente `storeFile/storePassword/keyAlias/keyPassword`.
- `app/google-services.json` es un **placeholder** (gitignored). Para runtime
  (Auth/Firestore/Google Sign-In) hay que poner el real de la consola de Firebase.

### Verificación: lo que NO te dice el verde

- **`testDebugUnitTest` puede salir `UP-TO-DATE`** si otra sesión ya compiló y nada cambió. Los
  números reales están en `app/build/test-results/testDebugUnitTest/*.xml`; no te fíes del resumen.
- **El verde de los unitarios no cubre Firestore.** Son lógica pura (Utils y ViewModels con fakes);
  los repositorios siguen sin tests de integración. Compilar y ver verde no prueba el modelo de
  datos: verifica en emulador (`connectedDebugAndroidTest` o a mano con el usuario QA).
- **`firebase-tests/rules.test.mjs` NO descubre reglas nuevas.** Es un script lineal con checks
  explícitos. Si tocas `firebase/firestore.rules` **sin añadir su check**, la suite sigue pasando
  probando una versión que ya no es la que corre. Es la trampa más cara del repo: ya hizo que se
  diera por cerrado D-S2 con una regla abierta.
- **Los emuladores necesitan `java` en PATH**: `export PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH"`
  además de `JAVA_HOME`. Si no, `firebase emulators:exec` falla al arrancar.
- **Las reglas de Firestore no se despliegan sin suite verde.** Ya corre contra datos reales.

## 3. Toolchain y restricciones de versiones (crítico)

Definido todo en `gradle/libs.versions.toml` (única fuente de verdad, sin versiones
hardcodeadas en build files).

| Componente | Versión |
|---|---|
| Gradle | 9.6.0 |
| AGP | 9.4.1 (con **KGP externo** + palancas de transición — ver nota) |
| Kotlin | **2.4.20** (KGP externo; plugins compose y serialization 2.4.20) |
| KSP | 2.3.12 (KSP2) |
| Compose BOM | 2026.09.00 (Compose 1.12.1, Material3 1.4.0) |
| compileSdk / targetSdk / minSdk | 37 / 36 / 33 |
| Java | 17 (compilación con JDK 21 del JBR) |
| Hilt | 2.60.1 · Room 2.8.5 · Retrofit 2.12.0 · Coil 3.6.3 · credentials 1.6.0 · googleid 1.2.1 |

**Transición de toolchain (ADR-0004, 29/09/2026):** el proyecto compila con **KGP externo
(Kotlin 2.4.20)** y las palancas `android.builtInKotlin=false` + `android.newDsl=false`
(modo DSL legacy; ambas se ELIMINAN en AGP 10). Coil y googleid ya están desbloqueados.
- El aviso `Deprecated 'org.jetbrains.kotlin.android' plugin usage` es esperado en transición.
- Los avisos `DEPRECATED_DSL` se silencian con `android.sync.suppressAgpWarnings=DEPRECATED_DSL`.
- **Retorno a built-in:** cuando haya AGP estable con Kotlin integrado ≥2.4, quitar las dos
  palancas + el plugin KGP y re-verificar (checklist en ADR-0004).

**Dagger KSP** no acepta parámetros con valor por defecto en constructores `@Inject`
("should contain exactly one @Inject constructor"): los ViewModels usan constructor
primario `@Inject` + constructor secundario sin argumentos para previews.

## 4. Arquitectura

```
app/src/main/java/es/androidtfm/gamevision/
├── GameVisionApplication.kt        @HiltAndroidApp
├── MainActivity.kt                 @AndroidEntryPoint; edge-to-edge; MainScreen
├── di/
│   └── AppModule.kt                Singletons: Firestore (caché offline explícita), Auth,
│                                   Storage, APIs Retrofit, CredentialManager, webClientId,
│                                   ThemeDataStore, SessionPreferences, CoroutineScope de aplicación
├── data/                           11 paquetes (verificados 29/09/2026)
│   ├── model/                      UserProfile · Social.kt (Friend, ChatMessage)
│   ├── session/                    SessionState (Anonymous|Guest|LoggedIn(uid, email)) ·
│   │                               SessionRepository (SSOT de sesión + Auth + CredentialManager) ·
│   │                               SavedPassword
│   ├── repository/                 UserRepository (perfil, amigos, muro; Result) ·
│   │                               UserIndexPlan (lógia pura de los índices inversos)
│   ├── library/                    LibraryRepository · LibraryModels · LibraryFilters ·
│   │                               RatingUtils · DiaryUtils · StatisticsUtils · Platforms
│   ├── hltb/                       HltbClient (aíslado, reintentos) · HltbModels · HltbRepository
│   ├── storage/                    ProfileImageStorage (foto en profile_images/{uid})
│   ├── analytics/                  AnalyticsLogger · FirebaseAnalyticsLogger
│   ├── catalog/                    GameCatalog (interfaz) · CatalogGame · CachedGameCatalog
│   │   ├── rawg/                   RawgGameCatalog ← implementación actual
│   │   └── local/                  CatalogDatabase (Room, v2) · GameDao · GameEntity ·
│   │                               SearchCacheEntity · HltbCacheEntity · Converters
│   └── (modelos de biblioteca en LibraryModels.kt)
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

**Flujo de datos (SSOT):** Pantalla (Composable) → ViewModel (StateFlow) →
repositorios. La identidad tiene UNA sola fuente y es el **uid** (ADR-0008):
`SessionRepository.sessionState` → `UserViewModel.currentUid`. El email es solo un campo del
perfil: `email_index/{email}` y `usernames/{username}` son índices inversos, no identidad.
Cualquier código que use el email como id de documento falla con `PERMISSION_DENIED`.
El perfil tiene UNA sola fuente: `UserViewModel.profile`, un flujo EN VIVO del
documento de Firestore (`addSnapshotListener`) que se re-suscribe al cambiar de
usuario y actualiza todas las pantallas sin refetch manual.

Reglas de la capa de datos:
- Todos los métodos del repositorio devuelven `Result`: nunca se traga un error.
- Las operaciones de sesión/perfil corren en `viewModelScope`, NUNCA en el
  scope de la pantalla (al abrirse la sesión la pantalla sale de composición y
  una corrutina suya se cancelaría a mitad de operación).
- `formFields` es SOLO estado de entrada del formulario, jamás identidad.

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

## 8. Design system (fase UI 2026)

**`DESIGN.md` es la fuente única de verdad visual** (leerlo antes de tocar UI).
Dirección elegida y registrada en `docs/ui-redesign-2026-proposals.md`:
*design-system-first cinematográfico con identidad editorial* — monocromo cálido
+ spot verde ácido (#C8F135), Space Grotesk variable (Google Fonts provider,
certs en `res/values/font_certs.xml`), física de muelles, portadas como
protagonista.

- **`ui/designsystem/`**: `GameVisionTheme` (marca + Material You opt-in),
  `GVTypography`, `GVShapes`, `GVMotion`, `GVSharedTransition`
  (shared elements por CompositionLocals, no-op seguro en previews) y
  componentes: GameCard, GameCover, NewsCard, RatingBadge, FriendAvatar,
  GVSkeleton, EmptyState, GVButton, GVChip — todos con `@Preview`.
- **Reglas UI**: cero spinners (solo skeletons), cero Material 2, cero
  gradientes decorativos, elevación por tono de superficie, un solo spot de
  color por pantalla. `SharedTransitionLayout` envuelve el NavHost en
  `MainActivity`; claves de shared element: `"cover-{gameId}"`.
- **Pendiente**: Fase 4 restante: list-detail con Navigation 3 (`ListDetailSceneStrategy`)
  — nav3 1.2.0 estable existe y su metadato es compatible en principio; migración
  atómica de las 12 rutas a NavKeys (ver skill `navigation-3`). El área de
  navegación adaptativa (barra↔rail, `AppScaffold`) ya está implementada y
  validada en emulador.

## 8bis. Validación en emulador (27/09/2026)

Emulador `Pixel_9` (API 36). La app corre sin crashes; NewsAPI y RAWG fluyen con
datos reales; tests instrumentados en verde. **Hallazgos corregidos en runtime:**
- Los roles `secondaryContainer` y `surfaceContainer*` que no se definían en
  `GVTheme` heredaban el tono LAVANDA del baseline M3 (barra de navegación y
  campos de texto). Regla: al tocar el tema, definir SIEMPRE la familia de
  superficies completa.
- La API de `NavigationSuiteScaffold` cambió en 1.4.0: el contenido ya no
  recibe `PaddingValues`; `AppScaffold` usa doble camino (Scaffold+barra en
  compacto, suite+rail en ancho medio/expandido).
- Arranque del emulador: la imagen android-36 venía sin `encryptionkey.img`
  (se copió de android-36.1); si el AVD falla con "failed to create encrypt
  partition", revisar eso.

## 9. Próxima frontera (acordado con el propietario)

La capa de datos y el design system están listos; el trabajo de UI continúa
sobre `ui/views/composables/` consumiendo el design system (pendiente
descomponer a fondo SearchScreen y SocialScreen). Skills locales en
`.zcode/skills/` (styles, navigation-3, adaptive, etc.).
Skills instaladas solo a nivel de proyecto, nunca global.
