# Fase 0 — Cimientos de datos

**Estado:** ⬜ Pendiente · **Estimación:** 1 semana · **Depende de:** — · **Bloquea a:** F1

## Objetivo

Preparar la base sobre la que se construye todo el producto: dejar de depender en
exclusiva de RAWG, tener un modelo de datos capaz de representar una biblioteca real
(no tres listas planas) y arreglar la deuda conocida de imágenes.

## Resultado verificable

Se puede **registrar un juego con estado y guardarlo sin conexión**, la app **sigue
mostrando catálogo si RAWG cae** (datos cacheados) y **la foto de perfil viaja entre
dispositivos**.

## Por qué esta fase y no otra

La investigación de mercado identificó el riesgo más grave del proyecto: **RAWG tuvo una
caída de 1 día y 15 horas (agosto 2026) y hay señales de abandono**. Hoy toda la app
depende de él. Además, las funciones de F1 (estados, notas, diario) necesitan un modelo
de datos que hoy no existe. Hacer F1 sin F0 obligaría a rehacerlo.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D0.1 | **Proveedor de catálogo** | (a) Sólo adapter con RAWG · (b) Adapter + IGDB como principal · (c) Adapter + ambos con conmutación | **(a) en F0, (b) cuando haya licencia/tiempo**: el adapter ya deja la puerta abierta y evita el trabajo de OAuth2 de Twitch ahora | ⬜ |
| D0.2 | **Datos de los usuarios actuales** | (a) Migrar las 3 listas a la biblioteca nueva · (b) Empezar limpio y borrar | **(a) migrar**: son 9 usuarios reales del TFM y demuestra migración de datos (bien para el tribunal) | ⬜ |
| D0.3 | **Alcance de la caché** | (a) Sólo fichas vistas · (b) Fichas + búsquedas populares · (c) Todo el catálogo | **(a) y luego (b)**: cachear fichas vistas arregla el 90 % del problema con coste mínimo | ⬜ |
| D0.4 | **Cloud Functions ahora o después** | (a) Añadir ya · (b) Posponer a F2/F3 | **(b) posponer**: Firebase gratis no las necesita para F0/F1 y añaden complejidad de despliegue | ⬜ |
| D0.5 | **Modelo de amigos** | (a) Dejar el actual (simétrico, por email) · (b) Rediseñar a "seguir" (asimétrico) | **Debatir en F2**, no en F0: aquí sólo se decide si el modelo actual bloquea algo (no lo hace) | ⬜ |
| D0.6 | **Duración de los juegos** | (a) Scraper de HowLongToBeat (sin API oficial) · (b) Campo propio en caché rellenado a mano/IGDB | **(b) en F0** (estructura), **(a) en F1** si el scraping resulta estable: es scraping de un tercero y puede romperse | ⬜ |

> **Cómo se cierra una decisión:** se escribe la opción elegida, la fecha y una línea de
> porqué en el "Registro de decisiones" del final. Mientras haya una decisión sin cerrar,
> la fase no pasa a 🔵 Aprobada.

---

## Tareas

### A. Desacoplar el catálogo (riesgo RAWG)
- [ ] T0.1 Crear la interfaz de dominio `GameCatalog` (buscar, detalle) en la capa de datos
- [ ] T0.2 Implementar `RawgGameCatalog` envolviendo lo actual (sin cambiar comportamiento)
- [ ] T0.3 Mapear las respuestas al modelo de dominio (dejar de usar DTOs de RAWG en la UI)
- [ ] T0.4 Añadir caché local con Room para fichas y búsquedas
- [ ] T0.5 Modo degradado: si la red/RAWG falla, mostrar lo cacheado con aviso claro

### B. Modelo de datos de la biblioteca
- [ ] T0.6 Definir `library/{gameId}`: estado, nota, favorito, plataforma, fecha de alta
- [ ] T0.7 Definir `logs/{logId}`: partida con fecha de inicio/fin, horas, reseña, nota, plataforma
- [ ] T0.8 Definir `sessions/{sessionId}`: sesión del diario (fecha, minutos)
- [ ] T0.9 Implementar el repositorio de biblioteca (`LibraryRepository`) devolviendo `Result`
- [ ] T0.10 Actualizar las reglas de Firestore para las colecciones nuevas
- [ ] T0.11 Migración de datos: `playedlist`/`wishlist`/`history` → `library` + `logs`

### C. Deuda conocida
- [ ] T0.12 Firebase Storage: subir la foto de perfil y guardar la URL (arregla el bug
      de que la imagen es una ruta local que no viaja entre dispositivos)
- [ ] T0.13 Firebase Cloud Messaging: dependencia, permiso `POST_NOTIFICATIONS` y token
- [ ] T0.14 Habilitar persistencia offline de Firestore

---

## Criterios de aceptación (con evidencia)

- [ ] CA0.1 **RAWG simulado caído** (bloquear el dominio en el emulador o forzar error):
      la app sigue mostrando las fichas ya visitadas y avisa de que está sin conexión.
- [ ] CA0.2 Un juego registrado desde el emulador **en modo avión** aparece en la biblioteca
      al recuperar la conexión (persistencia offline).
- [ ] CA0.3 La foto de perfil subida desde el **dispositivo A** se ve al entrar desde el
      **dispositivo B** (o tras borrar datos de la app).
- [ ] CA0.4 La migración conserva los juegos de los usuarios existentes: los 3 juegos de
      `alex@gmail.com` (historial) siguen ahí tras la migración.
- [ ] CA0.5 Cambiar de proveedor de catálogo no toca ni una pantalla (demostrable:
      sustituir la implementación en el módulo de Hilt compila sin tocar UI).
- [ ] CA0.6 Tests: los 17 unitarios + 6 instrumentados siguen verdes; nuevos tests para
      el repositorio de biblioteca y el modo degradado.

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Cambiar el modelo rompe lo que ya funciona | T0.2 y T0.11 van en commits separados; migración con copia de seguridad previa de los documentos |
| Room + Firestore duplican fuentes de verdad | Regla escrita: **Firestore es la verdad**, Room es sólo caché de catálogo (no de biblioteca) |
| El scraping de HowLongToBeat se rompe | Decisión D0.6: dato en caché, nunca dependencia crítica en vivo |

## Cómo se verifica

Emulador `Pixel_9` + tests (`testDebugUnitTest`, `connectedDebugAndroidTest`) + release
con R8. Capturas: modo degradado sin red, biblioteca offline, foto en segundo dispositivo.

---

## Registro de decisiones

> Se rellena conforme se debaten. Formato: `D0.x — Opción elegida (fecha) — porqué`.

_(vacío: pendiente de debate)_
