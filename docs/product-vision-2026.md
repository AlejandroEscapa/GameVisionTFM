# GameVision — Visión de producto 2026

> Investigación de mercado + plan de producto para llevar GameVision de "app de TFM bien
> hecha" a **producto publicable de referencia en su nicho**.
>
> Metodología: skill `deep-research` (búsqueda multifuente + verificación adversarial).
> Nivel de ambición acordado con el propietario: **híbrido TFM excelente + publicable**
> (monetización diseñada, no implementada). Plataforma: **Android nativo primero sin
> cerrar puertas** (capa de datos preparada para Kotlin Multiplatform).
>
> Fecha: 27/09/2026. Cada afirmación lleva su nivel de confianza
> (`CONFIRMADO` / `PLAUSIBLE` / `CONTESTADO` / `NO VERIFICADO`).

---

## 1. Resumen ejecutivo

1. **Tenemos una ventaja que el líder del mercado no puede cubrir: somos una app nativa.**
   Backloggd es el referente del sector (~650.000 usuarios, duplicó en 2025) y
   **su petición número uno es una app móvil nativa, que no tiene** (`CONFIRMADO`).
   GG es el único competidor con apps nativas iOS+Android y su mayor queja pública es
   el muro de pago agresivo (4,99 $/mes) (`CONFIRMADO`). GameVision **ya es** una app
   Android nativa con diseño propio: ese es el ángulo estratégico.

2. **El mercado tiene dos huecos documentados y grandes**: (a) *casi ningún tracker usa
   gamificación* (rachas, XP, recaps) pese a estar probada en Duolingo/Strava/Spotify, y
   (b) *el abandono del seguimiento es el problema universal del sector*: la gente registra
   juegos con ilusión la primera semana y luego deja de hacerlo (`PLAUSIBLE`, informe
   sectorial). Los que sobreviven lo hacen por dos vías: **responsabilidad social** o
   **automatización** (importar de Steam/consolas).

3. **Riesgo crítico detectado: dependemos en exclusiva de RAWG, y RAWG está en declive.**
   Caída de **1 día y 15 horas** (2–4 agosto 2026), estado "no disponible" en Wikipedia,
   comunidades hablando de abandono por parte de sus dueños y desarrolladores migrando a
   IGDB (`CONFIRMADO`). Hoy, si RAWG cae, GameVision se queda sin catálogo entero.
   **Es el mayor riesgo técnico del producto y hay que mitigarlo sí o sí.**

4. **El producto ideal de este nicho se define por tres escuelas** que hay que fusionar:
   catálogo+valoraciones (IMDb), diario social (Letterboxd) y utilidad de seguimiento
   (HowLongToBeat/GameTrack). Hoy GameVision está en la primera y necesita las otras dos.

5. **Propuesta**: convertir GameVision en *"el tracker de videojuegos que sí es una app
   nativa, te dice qué jugar ahora y celebra lo que has jugado"*. Tres pilares:
   **biblioteca rica**, **decisión ("¿qué juego ahora?")** y **celebración compartible (Rewind)**.

---

## 2. Mapa del mercado (verificado)

### 2.1 Las tres escuelas

| Escuela | Referente | Qué la hace grande | Qué le falta |
|---|---|---|---|
| **Catálogo + valoraciones** | IMDb | Base de datos inmensa, Top 250 con media ponderada anti-manipulación, listas, ficha completísima | Comunidad fría; en 2026: publicidad agresiva que abre Amazon a la fuerza y exigir cuenta para leer reseñas (`CONFIRMADO`) |
| **Diario social** | Letterboxd | Diario cronológico, **watchlist** (su función más querida), listas hiperespecíficas que descubren cine, **"Top 4"** en el perfil, feed de actividad, tono de reseñas de una línea | Descubrimiento poco algorítmico; crítica de "gamificación del ocio" |
| **Utilidad de seguimiento** | HowLongToBeat, GameTrack | Responde preguntas concretas: ¿cuánto dura?, ¿lo terminé? | Sin capa social; dato seco |

**Lectura estratégica:** GameVision puede ocupar el cruce de las tres — tiene catálogo
(RAWG), tiene social (amigos + timeline) y puede tener la utilidad (duración, progreso,
decisión). **Nadie en videojuegos lo hace bien a la vez** (`PLAUSIBLE`).

### 2.2 Competencia directa: quién es quién

| Producto | Datos clave | Fuerza | Debilidad explotable |
|---|---|---|---|
| **Backloggd** | ~650K usuarios (fin 2025), +86 % en un año; 1 dev a tiempo completo; Patreon 1 $/3 $ (`CONFIRMADO`) | Mejor comunidad y cultura de reseñas; estados ricos (Completado, Dominado, Retirado, En pausa, Abandonado) | **Sin app nativa** (su petición nº1); sin importación de Steam; herramientas masivas pobres |
| **GG (ggapp.io)** | Única con iOS+Android nativos; nota ~4,2 iOS / 3,8 Android (`CONFIRMADO`) | Registro en 10 s; separa "Beaten" de "Completed" | Muro de pago de 4,99 $/mes que expulsa usuarios; Android con quejas de pulido |
| **Stash** | 1M+ instalaciones, 4,62★, ~2M estimadas (`CONFIRMADO`) | El mejor diseño según usuarios; bibliotecas personalizadas | Poca capa social y de datos |
| **GameTrack** | iOS/iPadOS solo (`CONFIRMADO`) | Minimalista, recuento anual, escaneo de código de barras | Sin Android; sin social |
| **HowLongToBeat** | Sin API oficial; sólo scrapers de terceros (`CONFIRMADO`) | Dato de duración (historia / +extras / completista) | Sin tracking ni social |

**Combinación habitual del usuario real:** Backloggd (social) + HowLongToBeat (decidir)
(`CONFIRMADO`). **Es exactamente el hueco que GameVision puede cerrar en una sola app.**

### 2.3 Los huecos que la investigación señala como oportunidades

1. **Casi ningún tracker usa gamificación**: sin XP, rachas ni insignias — mecánicas
   probadas en Duolingo, Strava o Spotify (`PLAUSIBLE`, informe sectorial 2026).
2. **El seguimiento se abandona**: hábito que muere en semanas; sobreviven los que
   aportan **responsabilidad social** (comunidad) o **automatización** (importar) (`PLAUSIBLE`).
3. **Nadie une videojuegos con otros hobbies** (mesa, cine) (`PLAUSIBLE`).
4. **Decisión del siguiente juego**: Backloggd no responde "¿qué juego ahora?";
   herramientas como SavePoint (con un "Backlog Score" 0–100) sí, y por ahí se diferencian (`PLAUSIBLE`).

### 2.4 Qué funciona para retener (evidencia de otras industrias)

Los "Year in Review" son la mecánica de retención mejor documentada
(`PLAUSIBLE`, fuentes secundarias de marketing):

| Caso | Resultado |
|---|---|
| Strava Year in Sport | +12 M de tarjetas compartidas; engagement 4× superior |
| Nintendo Year in Review | +35 % de retención al trimestre siguiente |
| Duolingo Year in Language | +51 % de descargas atribuidas |
| Tinder Year in Swipe | +15 % de reactivaciones |

**Advertencia de la propia investigación:** la gamificación clásica (rachas, rankings)
puede volverse "tóxica" y castigar al que no puede seguir el ritmo. La versión moderna
apuesta por **identidad y significado** (Spotify Wrapped no tiene rankings ni rachas y es
el fenómeno cultural del sector) (`PLAUSIBLE`). **Diseño: celebrar, no culpar.**

---

## 3. Dónde está GameVision hoy

| Activo | Estado |
|---|---|
| Catálogo RAWG (buscar + ficha) | ✅ funcionando, con hero cinematográfico |
| Noticias (NewsAPI) | ✅ funcionando |
| Autenticación (Firebase + Google + email) | ✅ recién arreglada (SSOT) |
| Perfil + edición | ✅ con datos en vivo |
| Amigos + timeline de mensajes | ✅ básico |
| Listas (jugados / deseos / historial) | ⚠️ listas planas sin fecha, nota ni reseña |
| Diseño (design system propio, tema, navegación adaptativa) | ✅ nivel profesional |
| **Nada de**: valoraciones, reseñas, diario, estadísticas, notificaciones, importación, offline | ❌ el salto está aquí |

**Conclusión:** la *fontanería* ya está a nivel de producto (arquitectura, auth, SSOT,
design system). Lo que falta es **producto**: las funciones que hacen que alguien vuelva.

---

## 4. La visión: tres pilares

> **GameVision — tu biblioteca de videojuegos, en el bolsillo, que además te dice qué jugar y celebra lo que has vivido.**

### Pilar 1 — BIBLIOTECA RICA (ser el mejor tracker)
Que registrar un juego sea tan satisfactorio como en Backloggd y tan rápido como en GG.

### Pilar 2 — DECISIÓN (resolver "¿qué juego ahora?")
El hueco que Backloggd no cubre y por el que la gente usa dos apps a la vez.

### Pilar 3 — CELEBRACIÓN COMPARTIBLE (Rewind)
La mecánica de retención probada que **nadie aplica bien al gaming**.

---

## 5. Plan de funciones por niveles

### Nivel 1 — Lo que el mercado espera y no tenemos (imprescindible)

| # | Función | Referente | Por qué |
|---|---|---|---|
| 1 | **Estados ricos**: Jugando · Completado · **Dominado (100 %)** · En pausa · Retirado · Abandonado · Deseado | Backloggd, GG | Hoy son tres listas planas; esto es el vocabulario mínimo: separa "lo acabé" de "lo exprimí" |
| 2 | **Valoración por estrellas** (medias estrellas) | Backloggd, Letterboxd | Sin nota propia no hay perfil, ni estadísticas, ni comparación |
| 3 | **Reseña escrita por juego** (con formato corto destacado) | Letterboxd | La cultura de la frase corta es lo que hizo viral a Letterboxd |
| 4 | **Diario con fechas y sesiones** ("hoy jugué 1 h 20 min") | Letterboxd Diary + Backloggd | Convierte el historial en un diario; genera el dato para el Rewind |
| 5 | **Múltiples partidas por juego** (rejugarlo cuenta otra vez) | Backloggd | Un juego no se juega una vez en la vida |
| 6 | **Estadísticas de perfil**: horas, distribución de notas, géneros y plataformas favoritas | Letterboxd Pro / Backloggd Patreon | Hoy son función de pago en los líderes → **regalarlo es diferenciación directa** |
| 7 | **Perfil público + buscar amigos por nombre de usuario** | Todos | Hoy sólo se puede añadir por email exacto: barrera brutal |
| 8 | **Duración del juego** (historia / +extras / completista) | HowLongToBeat | Es *la* pregunta práctica; hoy la gente abre otra app |
| 9 | **Notificaciones** (seguidores, comentarios, estrenos de tus juegos deseados) | Todos | Sin avisos no hay retorno |

### Nivel 2 — Diferenciadores (por lo que nos elegirían)

| # | Función | Por qué nos eligen |
|---|---|---|
| 10 | **"¿Qué juego ahora?" — Backlog Score**, explicado: cruza duración pendiente (HLTB), tu tiempo disponible, tu género favorito del momento y tu backlog | Cierra el hueco documentado del sector; es *el* momento "wow" de la demo |
| 11 | **GameVision Rewind** (recap anual con tarjetas compartibles) | Mecánica de retención probada (Strava, Nintendo, Duolingo) y **ausente en gaming** |
| 12 | **Top 4 de tu vida** en el perfil | Identity marker; en Letterboxd se volvió fenómeno cultural |
| 13 | **Listas curadas y rankeables** ("Los 10 juegos que más me marcaron", públicas o privadas) | La máquina de descubrimiento de Letterboxd; contenido generado por el usuario gratis |
| 14 | **Importación de Steam / PSN / Xbox** | La "automatización" que la investigación marca como superviviente nº1 del abandono |
| 15 | **Nativo de verdad como ventaja**: widget de Android, notificaciones ricas, modo offline | Backloggd no tiene app; GG tiene muro de pago. **Nosotros ya somos nativos** |

### Nivel 3 — Excelencia (nivel "genio digital")

| # | Función | Idea |
|---|---|---|
| 16 | **Duelos 1 contra 1** entre dos juegos de tu biblioteca, votados, con resultado global | Genera datos propios únicos y conversación; nadie lo tiene |
| 17 | **Coach de backlog**: "te quedan 12 h y tienes 30 min → prueba este" | Recomendación explicada, no caja negra |
| 18 | **Wrapped por juego**: tu historia con un juego (horas, días, notas que le pusiste) | Reutiliza el dato del diario |
| 19 | **Estantería visual** con carátulas propias y lomos ordenables | Puro deleite coleccionista; altísimo valor percibido |
| 20 | **Integración con estrenos y ofertas**: "tu deseado está de rebajas / en Game Pass" | Uso comercial futuro y utilidad inmediata (JustWatch demuestra el patrón) |
| 21 | **Búsqueda de dónde jugarlo** (suscripciones/tiendas) | Equivalente al "dónde verlo" de JustWatch/Letterboxd |
| 22 | **Logros** sincronizados por juego | GG lo hace; profundiza el seguimiento |

---

## 6. Arquitectura para sostenerlo

Todo esto se apoya en piezas que ya existen en el proyecto; el trabajo es de producto,
no de rehacer cimientos.

### 6.1 Mitigación del riesgo RAWG (prioridad máxima)

- **Adapter de proveedor de datos**: interfaz propia (`GameCatalog`) con implementación
  RAWG hoy e IGDB mañana. Cambiar de proveedor = 1 línea de inyección de dependencias.
  Ya tenemos Hilt y una interfaz `GameApiService` — es el sitio natural.
- **Caché local** (Room ya está en el proyecto) de fichas y búsquedas: la app deja de
  depender de la red para lo ya visto (y habilita el modo offline).
- **IGDB como proveedor principal futuro**: multilingüe (50+ idiomas → **español**, que
  RAWG no tiene), datos relacionales (franquicias, estudios, DLC), usado por Discord,
  Twitch y Xbox (`PLAUSIBLE`). Coste: OAuth2 vía Twitch y licencia comercial si hay ingresos.

### 6.2 Modelo de datos (nuevo)

```
users/{email}
  ├── profile (nameSurname, username, description, country, imageUri)
  ├── library/{gameId}        ← estado + nota + favorito + plataforma
  ├── logs/{logId}            ← una entrada por partida: fecha inicio/fin, horas, reseña, nota, plataforma
  ├── sessions/{sessionId}    ← sesiones del diario (fecha, minutos)
  ├── lists/{listId}          ← listas curadas, con orden y visibilidad
  ├── top4                    ← 4 juegos favoritos (identidad pública)
  ├── friends, messages       ← ya existen
  └── stats                   ← agregados calculados (horas, distribuciones)
games/{gameId}                ← caché de catálogo (nombre, portada, géneros, duración)
```

### 6.3 Servicios

- **Firebase Storage**: subir fotos de perfil y de colección — arregla el bug actual
  (hoy la foto es una ruta local que no viaja entre dispositivos).
- **Cloud Functions**: cálculo del Rewind, estadísticas agregadas, búsqueda de usuarios
  por username y feed social. Resuelve de paso los dos problemas de diseño detectados:
  el **N+1** al cargar amigos/mensajes y la **privacidad** (dejar de exponer los datos
  de todos los usuarios al cliente).
- **Firebase Cloud Messaging**: notificaciones (nuevo seguidor, comentario, estreno,
  "vuelve a tu backlog").
- **Persistencia offline de Firestore** + caché RAWG: la app funciona en el metro.

### 6.4 Preparación multiplataforma (sin coste hoy)

Mantener dominio y datos libre de dependencias de UI Android (ya lo están tras el SSOT):
eso es el 80 % del trabajo de una futura migración a Kotlin Multiplatform para iOS.

### 6.5 Monetización (diseñada, no implementada)

Según lo aprendido del sector: **no cobrar por lo esencial** (la queja que hundió a GG).
Modelo recomendado, al estilo Letterboxd:
- Gratis: todo el tracking, social, listas, Rewind, estadísticas.
- **Premium ~2–3 €/mes**: temas y carátulas propias, estadísticas avanzadas, más listas,
  exportación de datos, sin publicidad, widgets exclusivos.
- Nunca: límites al número de juegos registrados ni a las funciones sociales básicas.

---

## 7. Métricas que hay que medir desde el día uno

| Métrica | Objetivo | Por qué |
|---|---|---|
| Tiempo hasta el primer registro | < 60 s desde abrir la app | El onboarding decide todo: GG gana por "10 segundos" |
| % de usuarios con ≥ 1 registro en 7 días | > 40 % | Mide el abandono que la investigación señala como problema universal |
| Retención D1 / D7 / D30 | D7 > 25 % | Estándar de producto |
| Amigos por usuario | > 1 | La investigación: sobreviven los sociales o los automáticos |
| Registros por usuario activo / semana | > 2 | Mide si el hábito existe |
| Compartidos del Rewind | crecimiento | El efecto viral medido en Strava/Duolingo |

---

## 8. Roadmap propuesto

Diseñado sobre el estado actual del código (arquitectura, auth y design system ya hechos).

| Fase | Duración | Contenido | Resultado |
|---|---|---|---|
| **F0 · Cimientos de datos** | 1 semana | Adapter de catálogo + caché Room + modelo `library`/`logs` + Storage para imágenes + FCM | La app deja de depender de RAWG en exclusiva y arregla la foto de perfil |
| **F1 · El corazón del tracker** | 2 semanas | Estados ricos, estrellas, reseñas, diario con sesiones, múltiples partidas, duración HLTB, estadísticas de perfil | GameVision ya es un tracker de verdad, comparable a los líderes |
| **F2 · Social** | 1 semana | Perfil público, buscar por username, feed de actividad, Top 4, listas curadas | Deja de ser una app individual: aparece la retención social |
| **F3 · El "wow"** | 1 semana | "¿Qué juego ahora?" + GameVision Rewind con tarjetas compartibles | La demo que impresiona y la mecánica de retorno |
| **F4 · Nativo y pulido** | 1 semana | Widget, notificaciones ricas, offline afinado, estantería visual | Ventaja que los competidores web no pueden copiar rápido |

**Total: ~6 semanas de trabajo** para un producto presentable y publicable.
Cada fase deja la app compilando, con tests verdes y verificada en emulador (como venimos
haciendo).

---

## 9. Riesgos y cómo se cubren

| Riesgo | Gravedad | Mitigación |
|---|---|---|
| **RAWG cae o cierra** (ya pasó: 1 d 15 h en agosto 2026) | 🔴 Crítica | Adapter + IGDB + caché Room (F0) |
| Datos personales legibles públicamente | 🟠 Alta | Cloud Functions para búsqueda de usuarios y feeds |
| Carga lenta con muchos amigos (N+1) | 🟠 Media | Agregados en Functions o consultas por lotes |
| Coste de Firebase al crecer | 🟡 Media | Free tier holgado para el nicho; Functions sólo para agregados |
| Licencia comercial de IGDB si hay ingresos | 🟡 Media | Mantener RAWG como respaldo y valorar licencia al monetizar |
| Gamificación mal diseñada (rachas tóxicas) | 🟡 Media | Celebrar logros (Rewind), nunca castigar ausencias |

---

## 10. Fuentes y confianza

Consultadas vía búsqueda web multifuente (27/09/2026). Se indica el nivel de confianza de
cada bloque; las fuentes primarias citadas son los propios productos, más el informe
sectorial "State of Game Tracking 2026" (Two Average Gamers) y agregadores de analítica
móvil (Sensor Tower/androidrank).

- **Productos (primarias)**: [Backloggd](https://backloggd.com/) · [Letterboxd](https://letterboxd.com/) ·
  [GG](https://ggapp.io/) · [HowLongToBeat](https://howlongtobeat.com/) · [IGDB](https://api-docs.igdb.com/) ·
  [JustWatch Partner API](https://apis.justwatch.com/docs/api/)
- **Cifras de crecimiento de Backloggd** (650K usuarios, sin app nativa): `CONFIRMADO`
- **RAWG: caída de 1 d 15 h (2–4 ago 2026) y abandono**: `CONFIRMADO`
- **Muro de pago de GG y migración de usuarios**: `CONFIRMADO`
- **Problema universal de abandono del seguimiento**: `PLAUSIBLE` (informe sectorial único)
- **Ausencia de gamificación en trackers**: `PLAUSIBLE`
- **Métricas de "Year in Review"**: `PLAUSIBLE` (fuentes secundarias de marketing)
- **IGDB multilingüe y uso en producción**: `PLAUSIBLE`
- **Tamaño de mercado en dinero**: `NO VERIFICADO` — no existe una cifra fiable publicada;
  el segmento es nicho y mayoritariamente gratuito. La magnitud se mide en usuarios, no en facturación.
