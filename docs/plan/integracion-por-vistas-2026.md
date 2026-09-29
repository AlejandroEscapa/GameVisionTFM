# Integración por vistas — mapa de producto (2026)

> **Qué es:** el diseño de DÓNDE y CÓMO se integra todo lo adoptado (P1–P4) en cada vista de la
> app, incluidas las vistas nuevas, el flujo completo de vinculación de Steam y el onboarding.
> **Fecha:** 29/09/2026 · **Estado:** propuesta para validación por secciones.
> **Habilitado por:** P1–P4 aceptados íntegramente + D1.5/D1.6/D1.7 cerrados
> (`fase-1-corazon-tracker.md` e `integraciones-apis-2026.md` §6).
> **Método:** análisis del código real (rutas de navegación y composables existentes) + razonamiento
> de producto + 3 investigaciones con evidencia verificada (`../investigacion-2026/fuentes-apis-views/`).

---

## 0. Resumen para el propietario

- Nada de lo adoptado "flota": **cada dato tiene una vista propietaria** y el resto solo referencia.
- Se crean **6 flujos/vistas nuevos**: Onboarding (1ª ejecución), Logros del juego (desde ficha),
  Reseñas (sección de ficha), Conexiones (desde Perfil), Diario y Estadísticas (ya planeadas en F1).
- **Steam se vincula desde Perfil → Conexiones** (y se ofrece en el onboarding, sin obligar):
  login en Steam (Chrome Custom Tab con OpenID 2.0), y desde ahí importación real de biblioteca+horas.
- Las horas reales de Steam viven en la **ficha** ("47 h jugadas · de ~60 h") y solo se muestran si
  existen; si no hay dato, el bloque **se oculta** (regla del propietario).
- El documento desglosa **20 micro-decisiones (D-V1…D-V20)** para ratificar cláusula a cláusula.

## 1. Los 6 principios de cohesión (gobiernan todas las vistas)

1. **La API enriquece, nunca manda.** Ningún bloque externo puede bloquear la vista: carga→skeleton,
   error→se omite, sin dato→no se pinta. La app es 100 % usable sin ninguna conexión.
2. **Lo tuyo primero.** Estado, valoración y horas del usuario SIEMPRE por encima de cualquier dato
   externo; los externos van debajo y etiquetados con su fuente.
3. **Un dato, un hogar.** Las horas viven en la ficha; los logros en Logros; las ofertas en
   Deseados/ficha; el valor de colección (futuro) en Colección. Ningún dato se duplica en tres sitios.
4. **Progressive disclosure.** La ficha es un resumen denso; el detalle completo (lista entera de
   logros, reseñas, ofertas del juego) vive en sub-vistas alcanzables con 1 tap.
5. **Real vs estimado, siempre etiquetado.** "47 h jugadas" (Steam, real) junto a "~60 h" (HLTB,
   estimado). El usuario jamás confunde un dato verificado con una estimación.
6. **Cero ruido.** Notificaciones opt-in por tipo, agrupadas (máx. 1 diaria de ofertas), sin
   re-engagement artificial. Los datos enriquecen; no acosan.

Estos principios traducen la evidencia transversal de las apps exitosas (dive 01): la anatomía común
es **catálogo → registro personal → social → stats/recap → ficha rica**; los estados cortos y
estables se repiten en todas las vistas con el mismo vocabulario; "explorar sin cuenta" es el mejor
onboarding que existe; y los dolores más castigados en reseñas de Play Store son los ads que abren
solos (Letterboxd, 240 "helpful"), los re-logins (Discogs 2,4★) y los paywalls sobre el core (Trakt).

## 2. El mapa: hoy → mañana

**Hoy (código real):** `login` → `main` con bottom bar de 5 tabs: `news` (Home), `gamesearch`,
`gamelist` (listas Jugando/Completado/Coleccionado/Deseado + Favoritos + histórico recientes + orden
alfabético/año/más jugados), `profile`, `social`; ficha `gameDetails/{gameId}` (portada 320 dp +
título/año + RatingBadge + Compartir/Añadir + tarjeta de metadatos RAWG); `editProfile`, `friendlist`.

**Mañana (mapa de navegación):**

```
Login ──► Onboarding (NUEVO, 1ª ejecución, skippable)
              └─ "Continuar como invitado" (ya existe) ──► main
main (bottom bar 5 tabs)
├─ Inicio (hoy "news")
│   ├─ Sigue jugando (tus "Jugando" + recently played de Steam)   [F2]
│   ├─ Ofertas de tus deseados (1 fila compacta si hay deals)     [F2]
│   └─ Noticias del sector (NewsAPI, ya existe)
├─ Búsqueda (RAWG + chips HLTB)                                   [F1]
├─ Biblioteca (Game List: estados + horas reales + sorts)         [F1/F2]
│   └─ Lista "Deseado" con chips "en oferta" + filtro "solo en oferta" [F2]
├─ Perfil
│   ├─ Vitrina de logros (top rarezas, patrón Steam)              [F2]
│   ├─ Conexiones y datos (NUEVA sub-vista)                       [F2]
│   └─ Editar perfil / Seguidos / Cerrar sesión (ya existen)
└─ Social (feed con logros reales de amigos)                       [F2]
Ficha del juego (gameDetails)
├─ Tu partida (estado, valoración, horas tuyas vs ~HLTB)          [F1/F2]
├─ Duración HLTB (ya adoptado)                                    [F1]
├─ Comunidad y reseñas (badge Steam + sección Reseñas)            [F2]
├─ Logros del juego (NUEVA sub-vista: rareza %, tu progreso)      [F2]
├─ Ofertas del juego (chip si hay deal → detalle)                 [F2]
├─ Datos del juego (dev/pub, PEGI/ESRB, enlaces Steam)            [F1]
└─ Relacionados y chips/tags (ya adoptado)                        [F1]
Diario (NUEVA, F1) · Estadísticas (NUEVA, F1) · Rewind (F3) · Widget (F4)
```

**Nota sobre los estados (dive 01, sin acción en F1):** el modelo actual ya cubre el patrón de la
industria (Jugando/Completado/Coleccionado/En pausa/Retirado/Abandonado/Deseado). La evidencia solo
sugiere vigilancia de nomenclatura ("Pendiente" como backlog vs "En pausa") en el rediseño de la
biblioteca; no se proponen cambios de esquema.

## 3. Vista a vista: qué se integra y con qué cláusulas

Formato: **[Hoy]** · **[Añadido]** · **[Cláusulas D-V]** · **[Estados vacío/carga/error]** · **[Fase]**

### 3.1 Onboarding (NUEVO — primera ejecución) — D-V1
- [Hoy] Login con 3 caminos: iniciar sesión / registrarse / continuar como invitado.
- [Añadido] Basado en el blueprint del dive 02 (NN/g + Material Self-Select + Apple HIG + Strava +
  Trakt importer-in-wizard), **6 pantallas máx**:
  1. **Bienvenida** (1 pantalla, sin carrusel; NN/g desaconseja la promoción de features al primer
     launch): claim "Todos tus juegos. Todas tus horas. Un solo lugar."
  2. **Cuenta en 1 toque**: Google Sign-In primario + email alternativo.
  3. **Self-select corto** (skippable): "¿Dónde juegas?" (chips: Steam · PlayStation · Xbox · Switch
     · Retro) + 1 pregunta opcional "¿Qué quieres seguir primero?" — con microcopy que explica el
     para qué ("Lo usamos para ordenar tu Inicio y tus sugerencias. Puedes cambiarlo luego.").
  4. **Conectar Steam** (el acelerador de valor, con consentimiento prominente estilo Play):
     "¿Importamos tu biblioteca de Steam? En 1 minuto tendrás tus juegos y tus horas. Solo lectura,
     sin contraseñas. Puedes desconectar cuando quieras." Botones: [Conectar Steam] (botón oficial
     de Valve) · [Añadir mis juegos a mano] · "Ahora no".
  5. **Time-to-value inmediato**: si conectó → "¡Listo! Encontramos 412 juegos y 3.924 h de juego."
     + [Ver mi biblioteca]; si no → "Añade 3 juegos que hayas jugado" (search rápido) para que el
     Inicio nunca esté vacío.
  6. **Inicio + checklist de activación** (patrón Strava "onboarding tasks", máx. 4 ítems,
     dismissible, no reaparece): importa Steam · puntúa 1 juego · añade 1 a "Jugando" · sigue a
     2 jugadores.
  - **Qué NO se pide en onboarding** (dive 02): permiso de notificaciones (se pide tras el primer
    logro con contexto), contactos, fecha de nacimiento, tema visual, tour de UI.
- [Cláusulas] D-V1.
- [Estados] El paso Steam con perfil privado → pantalla guiada (§4.8). El resto no depende de red.
- [Fase] Con P1 (F2); hasta entonces el login actual se mantiene tal cual.

### 3.2 Inicio (hoy `news`) — D-V2
- [Hoy] Noticias del sector con ArticleCard + estados de carga/error/retry correctos.
- [Añadido] El tab "Home" pasa a ser **"Inicio"** con 2 secciones sobre las noticias:
  (a) **Sigue jugando**: tarjetas horizontales con tus "Jugando" (y recently played de Steam si hay
  sync) → tap = ficha; (b) **Ofertas de tus deseados**: 1 fila compacta SOLO si hay deals activos
  (tarjeta patrón Deku Deals: precio tachado + precio + % + chip "mínimo histórico"). El resto del
  descubrimiento (trending/estrenos) se evalúa en F3 con Rewind y datos reales de uso — no se añade
  ruido antes de tiempo (regla 6).
- [Cláusulas] D-V2 (renombrar tabs al español + convertir Home en Inicio con secciones).
- [Estados] Secciones con skeletons propios; si una fuente falla, esa sección desaparece sin romper
  la página; sin "Deseados" con ofertas, no se muestra la sección.
- [Fase] F2 (ofertas y recently played necesitan P1); el renombrado puede ir en F1 (cosmético).

### 3.3 Búsqueda — (sin cambios de fondo)
- [Hoy] SearchBar RAWG + tarjetas con chip de rating + orden por nombre/rating/lanzamiento.
- [Añadido] Solo lo ya adoptado en F1: chips de duración HLTB en resultados. Nada más: la búsqueda
  debe seguir siendo rápida y limpia (regla 4). La lección de Untappd ("The search is dreadful")
  pesa más que cualquier chip extra.
- [Fase] F1.

### 3.4 Ficha del juego (`gameDetails/{gameId}`) — el corazón — D-V3, D-V4, D-V7, D-V17, D-V18
- [Hoy] Portada 320 dp + gradiente + título/año + RatingBadge + Compartir/Añadir (menú de estados) +
  tarjeta de metadatos (veces recomendado, Metacritic, RAWG rating, lanzamiento, género).
- [Añadido — reorganización en 5 secciones ordenadas] (estructura basada en el patrón verificado de
  las fichas de IMDb/Letterboxd/Goodreads/Backloggd, adaptado a juegos):
  1. **Tu partida**: estado (chips), valoración en medias estrellas, y **"47 h jugadas · de ~60 h
     (main)" con barra de progreso** — horas reales de Steam cuando existan (D-V7: si no hay
     vinculación o el juego no está en tu Steam, el bloque de horas tuyas SE OCULTA; nunca "0 h").
  2. **Duración** (HLTB, ya adoptado en F1): main/extras/completista + mediana + chips de tags.
  3. **Comunidad y reseñas**: conteos HLTB (ya adoptado) + **badge de reseñas Steam** ("Muy
     positivas · 5,2 M reseñas") que abre la sección Reseñas (D-V4). Cuando la comunidad GameVision
     crezca (F2), su nota media con histograma va encima de la externa (patrón IMDb/Letterboxd).
  4. **Logros**: resumen de 3–4 logros + "Ver los 42" → sub-vista Logros (D-V5). Sin datos de
     logros, la sección no existe.
  5. **Datos del juego**: dev/publisher, PEGI/ESRB, géneros, plataformas, **botón "Ver en Steam"**
     (appid), badge ProtonDB (D-V18) y arte SteamGridDB como fallback de portada (D-V17).
- [Cláusulas] D-V3 (reorden), D-V4 (badge + sección reseñas), D-V7 (ocultar horas si no hay dato),
  D-V17 (arte fallback), D-V18 (chip Deck best-effort).
- [Estados] Cada sección es independiente: skeleton propio, y si su fuente falla se omite. La ficha
  base funciona SIEMPRE, incluso en modo avión con caché.
- [Fase] F1: secciones 1 (sin horas Steam), 2, 5 y reorden. F2: horas Steam, reseñas, logros, ofertas.

### 3.5 Logros del juego (NUEVA sub-vista desde ficha) — D-V5, D-V6
- [Añadido] Basado en los patrones verificados (dive 03):
  - Cabecera con **progreso global** ("18/42 · 43 %") + tu progreso si hay conexión Steam/RA.
  - Cada logro: icono, nombre, descripción, **% global con barra** (patrón Steam) y **etiqueta de
    rareza** cuando aplique.
  - **Umbrales de rareza GameVision** (alineados con los estándares verificados): **Ultra Raro <5 %**
    (banda "Ultra Rare" de PSN observada; PSNProfiles) con el color ácido #C8F135 · **Muy Raro 5–10 %**
    · **Raro 10–20 %** · **Común ≥20 %**. Xbox marca "rare" <10 % (se respeta su etiqueta de origen);
    Steam/RA muestran % crudo y aplicamos nuestras bandas etiquetadas "según GameVision".
  - **Logros ocultos**: "Logro oculto — toca para revelar" (semántica PSN: protege spoilers por
    defecto).
  - Filtros: Todos · Pendientes · Conseguidos · **Ocultar conseguidos** (lo piden usuarios reales de
    apps de logros) · Raros primero.
- [Cláusulas] D-V5 (sub-vista, no tabla interminable en ficha), D-V6 (umbrales y etiquetas).
- [Estados] Sin conexión de logros → entrada oculta. Carga → skeleton de filas.
- [Fase] F2 (Steam) / F2 tardío (RA).

### 3.6 Reseñas (sección de ficha) — D-V4
- [Añadido] Dos niveles: (a) el badge Steam en "Comunidad"; (b) sección Reseñas con las **2–3 reseñas
  de Steam más útiles** (appreviews, caché) + "Ver todas en Steam" + (F2 social) reseñas de usuarios
  GameVision con su valoración propia — la comunidad propia SIEMPRE primero (regla 2).
- [Estados] Sin reseñas → sección con las de GameVision si existen; si no, no se pinta.
- [Fase] F2.

### 3.7 Ofertas — D-V8, D-V9
- [Añadido] Hogar de las ofertas: **lista "Deseado" de la Biblioteca** (chips "en oferta" en cada
  ítem + filtro "solo en oferta") + **"Ofertas" en la ficha** (chip con % si hay deal activo → detalle
  con el deal por tienda, precio, `cheapestPriceEver` como "mínimo histórico" y botón "Ir a la
  tienda" SIEMPRE vía redirect de CheapShark, requisito del servicio). En **Inicio**, solo la fila
  compacta ya descrita. El bottom bar NO gana tab (ya está en su tope de 5).
- [Cláusulas] D-V8 (hogar: Deseados + ficha + fila Inicio; sin tab nueva), D-V9 (alerta push opt-in:
  1 diaria agrupada, umbral por defecto 30 % — Steam usa ≥20 % oficial — o nuevo mínimo histórico;
  umbral ajustable por juego, patrón Deku Deals/ITAD).
- [Estados] Juego sin deals → chip oculto. Deals caducados → purga con el TTL de caché.
- [Fase] F2 (CheapShark). F3: ITAD para histórico real y alertas con webhook.

### 3.8 Biblioteca (`gamelist`) — D-V10, D-V11
- [Hoy] Listas Jugando/Completado/Coleccionado/Deseado (+ Favoritos + recientes) + orden
  (alfabético, año asc/desc, más jugados).
- [Añadido] (a) **Horas reales** en la tarjeta ("47 h") cuando existan (mismo D-V7: sin dato, sin
  chip); (b) sort nuevo **"Por jugar (más corto)"** (ya adoptado en F1 con HLTB); (c) tras importar
  Steam: pantalla de **revisión de importación** (D-V10) — los juegos que SOLO están en Steam se
  ofrecen como lote ("142 juegos detectados → ¿añadir a tu biblioteca?") con selección y estado
  propuesto por defecto (no jugado nunca → Deseado/Pendiente; con horas históricas → Completado si
  tiene +X h y sin sesiones recientes, Jugando si tiene `playtime_2weeks` > 0); los ya existentes NO
  se tocan (D-V11: el estado local manda; solo se actualizan horas y `last_played`, que son datos
  factuales; la fecha de primera sesión NO existe en la API → el diario nunca se inventa).
- [Cláusulas] D-V10, D-V11.
- [Estados] La importación muestra progreso real y resultado ("142 juegos · 1.234 h · 87 ya en tu
  lista, fusionados sin duplicados" — patrón Trakt/Letterboxd: import idempotente).
- [Fase] F2.

### 3.9 Diario (NUEVA, F1) — D-V19
- [Añadido] Diario manual (D1.6) con entradas: juego, fecha, horas/notas. Atajos: "+ sesión de hoy"
  desde la ficha y "continuar registro" en Inicio. **Asistente Steam** (F2): botón "rellenar con mis
  últimas 2 semanas de Steam" que crea un BORRADOR editable (juego + horas jugadas en el periodo),
  nunca una entrada automática — el diario sigue siendo un acto de voluntad (patrón Letterboxd).
- [Cláusulas] D-V19 (asistente = borrador editable).
- [Fase] F1 (diario) / F2 (asistente Steam).

### 3.10 Estadísticas (NUEVA, F1) — D-V20
- [Añadido] Cálculo en cliente (D1.5a): tiempo total, backlog en horas ("Por jugar: ~X h" con HLTB),
  valoración media, distribución por género/plataforma. Con Steam (F2): separación explícita
  **"Horas verificadas (Steam)" vs "Horas estimadas"** (regla 5) y "top logros raros" enlazado a la
  vitrina. Sin vanity metrics.
- [Cláusulas] D-V20.
- [Fase] F1 / F2 (parte Steam).

### 3.11 Perfil + Conexiones + Vitrina — D-V12, D-V13
- [Hoy] Header + tarjeta de detalles (descripción, país, email) + acciones (Seguidos, Editar perfil,
  Cerrar sesión).
- [Añadido] (a) **Vitrina de logros** (patrón "Rarest Achievement Showcase" de Steam, verificado):
  top 3–6 rarezas auto-seleccionadas por % + contadores (logros totales, juegos completados; los
  100 % destacados, patrón Xbox 2024) — pública solo opt-in (D-V13); (b) **"Conexiones y datos"**
  (NUEVA sub-vista, D-V12): tarjeta por servicio — Steam (Conectado · hace 2 h · Sincronizar/
  Desvincular), RetroAchievements (D-V14), Xbox (piloto, D-V15) — cada tarjeta con "qué leemos y
  qué no" en texto claro y revocación; PSN no se ofrece en la UI (D-V16); (c) resumen de estadísticas
  (6 números clave → tap = Estadísticas).
- [Fase] F2.

### 3.12 Social (F2) —
- [Hoy] Amigos + chat + comentarios (feed básico).
- [Añadido] El feed gana **eventos reales**: "Álex consiguió «Logro ultra raro» (3,1 %)" — los %
  globales públicos de Steam/RA alimentan el foro de logros registrado en fase-2 sin inventar nada.
  Esto responde a la advertencia "⚠️ Requiere fuente de datos de logros por plataforma" del doc de
  F2: la fuente existe y es pública (Steam % + RA).
- [Fase] F2.

### 3.13 Parking documentado (no se diseñan hoy)
- **Rewind (F3):** "tu año" con horas verificadas, logros del año y top rarezas (patrón Letterboxd
  Wrapped — la palanca de crecimiento estacional mejor documentada del sector, dive 01).
- **Widget (F4):** juego en curso con barra de progreso + oferta estrella del día.
- **Valor de colección (P3):** disparador = ingresos; diseño patrón CLZ/Discogs (valor por condición,
  disclaimer honesto, privado por defecto) cuando toque.
- **Crítica agregada (P4):** contactar a OpenCritic; sin integración hasta respuesta.

## 4. Vinculación de Steam — flujo completo (el corazón de P1)

**Dónde se dispara:** Onboarding paso 4 (opcional) · Perfil → Conexiones · Card en Inicio para quien
lo omitió · Empty state de Biblioteca ("¿La importamos desde Steam?") — los 4 entry points del dive 02.

**Flujo técnico-UX (8 pasos, todo verificado contra Valve/Chrome/Play/NN/g):**
1. **Pantalla de consentimiento** (en la app, justo antes de salir — política Play): título "Conecta
   tu cuenta de Steam" + 3 bullets (✅ "Leeremos: perfil público, lista de juegos y horas jugadas" ·
   🚫 "Nunca veremos tu contraseña: el inicio de sesión ocurre en steamcommunity.com, la web oficial
   de Steam" · 🔁 "Solo lectura. No publicamos nada. Puedes desconectar cuando quieras"). Botones:
   [Conectar Steam] (asset oficial de Valve, requisito de su ToS) · [Ahora no].
2. **Chrome Custom Tab / Auth Tab** (NUNCA WebView — Play lo desaconseja formalmente y AppAuth lo
   considera prohibido por seguridad): lanzar `steamcommunity.com/openid` con `return_to` =
   `https://api.gamevision.app/auth/steam/callback`. El usuario pone su contraseña SOLO en Steam.
3. **Retorno por App Link verificado** (`assetlinks.json` + `autoVerify`): el backend valida la
   assertion OpenID 2.0 (librería mantenida, con `return_to` firmado/nonce) y extrae el **SteamID64**
   del `claimed_id`. Fallback: página puente con botón "Volver a GameVision" si el App Link no está
   verificado en ese dispositivo.
4. **Pantalla "Conectado"**: "Conectado como [nickname] ✔" + "Analizando tu biblioteca…".
5. **Import server-side** (la clave de Steam NUNCA en la app): Cloud Function → `GetOwnedGames`
   (`include_appinfo=1`, `include_played_free_games=1`) → 142 juegos con `playtime_forever` (minutos)
   + `rtime_last_played` → mapeo appid↔RAWG/HLTB → pantalla de revisión por lote (§3.8). No
   bloqueante: "Puedes seguir usando la app; te avisamos al terminar". Errores parciales honestos:
   "42 juegos no se pudieron emparejar con el catálogo; se añaden igualmente sin datos extra".
6. **Resumen post-import (el momento "wow")**: "412 juegos importados · 3.924 h · 87 ya en tu lista
   (fusionados, sin duplicados)". Acciones: ocultar juegos concretos, marcar backlog, ordenar por
   horas. Idempotente por diseño (upsert por appid; re-sincronizar no duplica jamás — lección Trakt).
7. **Sync incremental** (no polling): al abrir si hace >6 h, manual desde Conexiones, y tras push.
   `GetRecentlyPlayedGames` alimenta "Sigue jugando".
8. **Desvincular**: borra steamId64; pregunta si conservar las horas ya importadas (dato factual
   histórico) o limpiarlas. Refleja la promesa del consentimiento.

**El caso #1 de fallo — perfil privado (verificado):** desde 2018 Steam pone "Detalles de los
juegos" en "Friends Only" por defecto y `GetOwnedGames` solo devuelve datos si son visibles. Exophase
documenta esta como la causa #1 de fallos de tracking. Pantalla guiada: título "Tu perfil de Steam
está en privado" + pasos numerados (steamcommunity.com → tu perfil → Editar perfil → pestaña
Privacidad → "Detalles de los juegos" → Público) + [Ya lo he cambiado — Reintentar] + [Importar a
mano] + nota "no hace falta hacer público nada más".

**Qué NO hace la vinculación:** no publica nada, no toca estados, no escribe en Steam (imposible:
solo lectura), no requiere email/contraseña de Steam en nuestra UI (jamás; Valve lo prohíbe).

**Arquitectura mínima:** clave Steam SOLO en Cloud Functions (proxy + caché compartida + rate limit);
en el dispositivo: steamId64 y horas en el perfil del usuario; Room cachea catálogo+HLTB+SteamInfo.

## 5. Arquitectura de datos (lo mínimo para que exista)

- **Firestore (perfil):** `users/{uid}.connections.steam {steamId64, visible, lastSyncAt}` +
  subcolección `playtime` por juego (appid, minutes, lastPlayed).
- **Room (catálogo local):** `GameEntity` (ya existe) + `HltbInfo` (F1) + `SteamInfo {appid,
  playtimeMinutes, lastPlayed, updated}` + `AchievementSummary {total, unlocked, rarest}`.
- **Cloud Functions:** `steamProxy`, `raProxy`, `dealsProxy` (CheapShark) + job de mapeo de
  identidades (RAWG↔HLTB↔appid; HLTB ya expone `steamAppIds` — verificado — y el storefront resuelve
  el resto por búsqueda).
- **Reglas de red:** TTL HLTB 90 d · SteamSpy 24 h · deals 6–24 h · logros 24 h; `Retry-After`
  respetado; degradación silenciosa en TODAS las fuentes.

## 6. Fases: qué entra cuándo (resumen)

| Fase | De este documento |
|---|---|
| **F1 (en curso)** | Reorden de ficha en 5 secciones (sin horas Steam) · duración/datos/relacionados HLTB · Diario · Estadísticas cliente · chips búsqueda · rename tabs español |
| **F2** | Steam completo (onboarding, Conexiones, import, horas reales, sync) · Logros + rareza · Reseñas · Ofertas (CheapShark + alertas) · Arte fallback · RA · Vitrina · Social con logros reales |
| **F3** | ITAD histórico/alertas · Xbox piloto · Rewind · descubrimiento trending (con datos de uso) |
| **F4** | Widget (progreso + oferta) |
| **F5** | Gates de monetización (IGDB partner, PriceCharting) — modelo "Pro anual barato" estilo Letterboxd (~19 $/año, free sin castraciones) |

## 7. Micro-decisiones D-V (para ratificar)

| # | Cláusula | Recomendación |
|---|---|---|
| D-V1 | Onboarding: 6 pantallas máx (Self-Select + importer en wizard), skippable, Steam opcional, checklist post-onboarding | Sí |
| D-V2 | Tabs al español; "Home"→"Inicio" con Sigue jugando + fila Ofertas + Noticias | Sí |
| D-V3 | Ficha reordenada: Tu partida / Duración / Comunidad / Logros / Datos | Sí |
| D-V4 | Reseñas: badge Steam + máx 3 reseñas + "ver en Steam"; comunidad propia primero cuando exista | Sí |
| D-V5 | Logros: sub-vista propia desde ficha (resumen 3–4 en ficha) | Sí |
| D-V6 | Rareza: Ultra Raro <5 % (ácido #C8F135) · Muy Raro 5–10 % · Raro 10–20 % · Común ≥20 %; Xbox respeta su etiqueta de origen (<10 %) | Sí |
| D-V7 | Horas Steam solo si existen; si no, bloque oculto (nunca "0 h") | Sí (regla del propietario) |
| D-V8 | Ofertas viven en Deseados + ficha + fila compacta en Inicio; sin tab nueva | Sí |
| D-V9 | Alerta de ofertas: opt-in, 1/día agrupada, umbral 30 % o mínimo histórico, ajustable por juego | Sí |
| D-V10 | Import nunca crea estados sin confirmación; propuesta inteligente por lote | Sí |
| D-V11 | Merge no destructivo: estado local manda; horas y last_played se actualizan | Sí |
| D-V12 | Conexiones: sub-vista con transparencia ("qué leemos") y desvinculación | Sí |
| D-V13 | Vitrina de logros en Perfil (top rarezas, patrón Steam); pública solo opt-in | Sí |
| D-V14 | RA: usuario + API key propios del usuario | Sí |
| D-V15 | Xbox: piloto con OpenXBL cuando llegue su fase | Sí |
| D-V16 | PSN: no aparece en la UI (sin vía segura) | Sí |
| D-V17 | SteamGridDB como fallback de arte (no sustituye portadas RAWG) | Sí |
| D-V18 | ProtonDB: chip Deck best-effort, eliminable si se rompe | Sí |
| D-V19 | Asistente de diario Steam = borrador editable, nunca auto-diario | Sí |
| D-V20 | Estadísticas: separar horas verificadas vs estimadas; sin vanity metrics | Sí |

## 8. Evidencia y fuentes (verificadas en los 3 dives)

**Dive 01 — apps exitosas (IMDb, Letterboxd, Trakt, MAL/AniList, Goodreads, Untappd, Discogs,
Backloggd):** anatomía común y orden de ficha verificados en páginas reales; notas de Google Play
verificadas el 29/09/2026 (Untappd 4,7★ · IMDb 4,6★ · Goodreads 4,5★ · Trakt 4,3★ · Letterboxd 3,7★
· MAL 3,5★ · Discogs 2,4★); dolores documentados con reseñas citadas (ads que abren solos, re-logins,
paywalls, app recortada vs web); lecciones TV Time (portabilidad) y Trakt (sync como killer feature).

**Dive 02 — onboarding y Steam:** NN/g (onboarding breve/skippable, progressive disclosure), Material
Self-Select, Apple HIG, Play (consentimiento prominente, anti-WebView), Strava (flujo completo de 20
pantallas con onboarding tasks), Trakt (importer en welcome wizard), Letterboxd (import de 3 formas),
docs de Valve (OpenID 2.0, `GetOwnedGames` con `playtime_forever`, botón oficial), Chrome Custom
Tabs/Auth Tab + App Links, Exophase (perfiles públicos; privacidad = causa #1 de fallo).

**Dive 03 — logros, ofertas, colección:** bandas de rareza (PSN API: Ultra/Very/Rare/Common;
PSNProfiles 5 bandas <5/5–10/10–20 %; Xbox rare <10 % con diamante; Steam % crudo con barra; RA
points/TrueRatio), trofeos ocultos (semántica oficial PSN), Xbox 2024 (100 % destacados, ocultar
juegos), Steam wishlist (notificación ≥20 % y >8 h), Deku Deals (tarjeta con "Lowest price ever",
price history 6m/1y/2y/All, wishlist alerts; app Android + iOS 4,6★), ITAD (flags N/H/S, tabs
Prices/History/Stats, botones Wait/Collect/Ignore), CheapShark (`cheapestPriceEver`), CLZ Games
(valores PriceCharting diarios Loose/CIB/New; suscripción 19,99 $/año; 4,11★), Discogs (valor
estimado sobre últimas 30 ventas, privado por defecto; iOS 4,8★ vs Android 2,4★).

Los informes completos están en `../investigacion-2026/fuentes-apis-views/` (dive_01, dive_02,
dive_03) con todas las URLs.
