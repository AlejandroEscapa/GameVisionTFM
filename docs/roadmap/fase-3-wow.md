# Fase 3 — El "wow": decidir y celebrar

**Estado:** 🟢 En ejecución — D3.1–D3.6 cerradas y **D3.7 abierta** (bloquea solo T3.11) · T3.1–T3.3 hechas, **T3.6–T3.10 hechas** · **Estimación:** 1 semana · **Depende de:** F1 · **No depende de:** F2

## Objetivo

Dos funciones que **ningún competidor cubre bien** y que son la razón para elegir
GameVision: **"¿Qué juego ahora?"** (decidir) y **GameVision Rewind** (celebrar y
compartir). Esta es la fase que da la demo que impresiona.

## Resultado verificable

La app responde **"¿qué juego ahora?"** con una recomendación **explicada** ("te quedan
12 h por delante y hoy tienes 30 minutos: esto te encaja") y genera un **recap anual
compartible como imagen** listo para enseñar y publicar.

## Por qué esta fase y no otra

- **Decidir**: la investigación documenta que el usuario real **usa dos apps a la vez**
  (Backloggd para lo social, HowLongToBeat para decidir). Ese hueco se cierra aquí, y es
  el hueco que da sentido a tener la duración y el diario de F1.
- **Celebrar**: el "Year in Review" es la **mecánica de retención mejor documentada**
  (Strava: 12 M de tarjetas compartidas y 4× engagement; Nintendo: +35 % de retención;
  Duolingo: +51 % de descargas) y **casi ningún tracker de videojuegos la usa**. Es
  diferenciación con evidencia detrás, no una corazonada.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D3.1 | **Factores del "¿qué juego ahora?"** | (a) Sólo tiempo disponible vs duración · (b) + género favorito del momento · (c) + estado de ánimo elegido por el usuario · (d) + nota histórica del usuario | **(b) más (c) opcional**: con tiempo+duración+gusto se acierta mucho y se explica fácil; el estado de ánimo como filtro manual da control | ✅ **(b) + (c) opcional (01/10)** |
| D3.2 | **¿Se explica la recomendación?** | (a) Sí, con el porqué · (b) No, solo el resultado | **(a) siempre**: la investigación del sector critica las cajas negras; explicar es parte del valor y da confianza | ✅ (a) (01/10) |
| D3.3 | **¿Dónde se calcula el Rewind?** | (a) En el cliente con los datos locales · (b) Cloud Function | **(a)**: el dato ya está en el dispositivo y evita dependencia de Functions; (b) si hiciera falta histórico largo | ✅ (a) (01/10) |
| D3.4 | **Formato de compartir** | (a) Imagen generada (tarjeta) · (b) Texto · (c) Ambos | **(a) imagen**, con las tarjetas del design system: es lo que se comparte de verdad (el formato de Strava/Spotify) | ✅ (a) (01/10) |
| D3.5 | **Periodicidad del Rewind** | (a) Sólo anual · (b) Anual + mensual + "tu historia con este juego" | **(b) anual como plato fuerte y "por juego" como relleno**: multiplica las ocasiones de compartir con poco trabajo extra | ✅ (b) anual + por juego (01/10) |
| D3.6 | **Tono del Rewind** | (a) Celebrar siempre (también si jugaste poco) · (b) Mostrar también lo no jugado | **(a) celebrar**: la investigación avisa de que la gamificación puede volverse tóxica; nunca culpabilizar | ✅ (a) (01/10) |
| D3.7 | **¿Cómo se avisa de que el Rewind está listo?** (T3.11) | (a) **WorkManager** periódico con `HiltWorker` · (b) `AlarmManager` anual + `BroadcastReceiver` contra una caché local · (c) aviso en la app al abrirla, sin notificación | **(a)**, pero **el alcance de T3.11 hay que partirlo**: ver abajo | ⬜ **Abierta (02/10)** |

> **Por qué T3.11 no se ha implementado todavía (decisión, no dejadez).** La regla 4 de este roadmap
> dice: *si aparece una decisión nueva durante la ejecución, se añade a la fase **antes** de
> improvisar*. T3.11 introduce **la primera dependencia nueva de la fase** y tiene tres preguntas sin
> cerrar:
>
> 1. **¿Cuándo es «fin de año»?** El Rewind del año que cierra solo tiene sentido **en enero**; lanzarlo
>    en diciembre cuenta un año a medias. Hay que fijar el día y la hora.
> 2. **¿Cómo se llega al usuario?** `WorkManager` es lo estándar, pero exige `androidx.work` **más
>    `androidx.hilt:hilt-work`**, un `HiltWorkerFactory`, que `GameVisionApplication` implemente
>    `Configuration.Provider` y quitar el inicializador por defecto del manifest. La alternativa
>    (`AlarmManager` + receptor) evita dependencias pero obliga a mantener una caché local del año, y
>    el receptor no puede leer Firestore sin sesión. **La opción (a) es la recomendada**, y es trabajo
>    real: no se improvisa en una ronda.
> 3. **¿Y si no hay actividad?** D3.6 prohíbe culpabilizar: **sin datos no se notifica**. Un aviso de
>    «tu Rewind está listo» a quien no jugó es exactamente el mensaje tóxico que la investigación
>    desaconseja. Hay que persistir «ya avisado de {año}» para no repetir.
>
> **Parte que sí es independiente y no bloquea:** el canal de notificación, el permiso
> `POST_NOTIFICATIONS` (API 33+) y el enlace para que tocar la notificación abra el Rewind. Se pueden
> hacer cuando se cierre D3.7, junto con el programador.

---

## Tareas

### A. Decidir: "¿Qué juego ahora?"
- [x] T3.1 Motor de recomendación (lógica pura y testeable) con factores y pesos de D3.1 —
  `data/library/RecommendationEngine.kt`, **13 tests** (`RecommendationEngineTest`), commit `ac0baae`
  (*evidencia:* 132 unitarios en verde; regla CA3.1 con el matiz anti-caja-vacía)
- [x] T3.2 Pantalla "¿Qué juego ahora?" con el tiempo disponible como entrada rápida (30 min / 2 h / una tarde) — **hecha dentro de la Home «Para ti»** (bloque F de la iteración): chips de tiempo + ánimo en `ParaTiScreen`
- [x] T3.3 Explicación de cada recomendación en lenguaje natural — el motor ya la devuelve (D3.2) y la Home la pinta en cada tarjeta
- [ ] T3.4 Acciones directas desde la recomendación (empezar, marcar como jugando, descartar) — *parcial:* la tarjeta abre la ficha (donde ya se cambia de estado); faltan las acciones en línea
- [ ] T3.5 Aprendizaje ligero: registrar si el usuario aceptó o descartó para mejorar el orden

### B. Celebrar: GameVision Rewind
- [x] T3.6 Cálculo del recap: horas totales, juego más jugado, géneros dominantes, nota media, racha más larga, mes más activo, plataforma principal — `data/library/RewindUtils.kt`, **21 tests** (`RewindUtilsTest`). *Evidencia:* **178 unitarios en verde** (17 clases, 0 fallos) y `testDebugUnitTest` BUILD SUCCESSFUL. Cubre CA3.5 (poca actividad y biblioteca vacía siguen teniendo recap, nunca vacío) y CA3.6. La racha usa `java.time` y el ordinal de día, así que es correcta en cambios de mes, de año y de hora de verano (documentado en la cabecera del fichero)
- [x] T3.7 Pantalla del Rewind con animación y las tarjetas del design system —
  `ui/views/composables/RewindScreen.kt`, ruta `rewind` en el NavHost y entrada desde **Home**
  (tarjeta «Tu Rewind» en `ParaTiScreen`, la primera de la pantalla porque esto es lo que se comparte).
  *Evidencia en emulador (Pixel_9, API 36):* `.verificacion/01-arranque.png` (la tarjeta en Home),
  `.verificacion/02-rewind.png` (el recap con datos reales: 1 h, 1 juego, 1 terminado, 3,5 de nota,
  juego del año, género, mes y plataforma) y `.verificacion/03-rewind-sin-animaciones.png` (**con las
  animaciones del sistema desactivadas la pantalla se pinta completa e idéntica** — el gate de
  movimiento reducido funciona, CA4.5.4). *Build:* 178 unitarios verdes, `lintDebug` limpio,
  `assembleDebug` OK
- [x] T3.8 Generación de imagen compartible (con marca de la app) — el **póster** de
  `RewindScreen` se dibuja en una `GraphicsLayer` (`rememberGraphicsLayer` +
  `Modifier.drawWithContent`) y se convierte en PNG con `toImageBitmap()`; se escribe en la caché
  con `data/storage/ShareImageStorage.kt`. **Se captura lo que está en pantalla**, así que no hay
  una plantilla paralela que se desincronice del diseño.
  *Evidencia:* `.verificacion/10-imagen-compartida.png`, **PNG real extraído de la caché de la app**
  (996×1118, cabecera válida, 251.807 bytes) con marca, año, horas, portada y juego del año.
  *Dos defectos encontrados y corregidos en la propia verificación:* el pie se recortaba con
  `aspectRatio(1f)` + `SpaceBetween` (ahora la altura la dicta el contenido) y salía
  «1 juegos, 1 terminados» (ahora `RewindUtils.resumenDeJuegos`, **con test de regresión**)
- [x] T3.9 Compartir nativo de Android (hoja de compartir del sistema) — `Intent.ACTION_SEND` con
  `type = "image/png"`, `EXTRA_STREAM` y `FLAG_GRANT_READ_URI_PERMISSION`, más `EXTRA_TEXT` con el
  resumen. Requirió **añadir el `FileProvider`** al manifest (`res/xml/file_paths.xml`, solo la
  carpeta `rewind/` de la caché): pasar un `file://` habría lanzado `FileUriExposedException`.
  *Evidencia:* `.verificacion/09-compartir.png` — la hoja del sistema muestra «Sharing image» con el
  texto del Rewind y destinos (Quick Share, Drive, Mensajes). **Precisión:** la imagen queda en la
  caché de la app, **no** en la galería; «guardar en el dispositivo» sería otro flujo (MediaStore) y
  no lo pide T3.8
- [x] T3.10 "Tu historia con este juego" (mini-recap por juego) — lógica **pura y testeada** en
  `RewindUtils.computeForGame` → `GameStory` (horas, sesiones, recorrido «De 1 de marzo a 28 de
  septiembre», racha del juego y nota; **5 tests**), y tarjeta `HistoriaDelJuego` en la ficha del
  juego (`GameDetails`), visible solo si el juego está en la biblioteca. *Evidencia:* `.verificacion/12-historia-juego.png`
  (la tarjeta en la ficha de Elden Ring, con el caso suelo «Todavía sin partidas apuntadas» porque
  ese juego no tiene diario); el caso con datos lo cubren los tests. 184 unitarios verdes, lint limpio
- [ ] T3.11 Notificación de fin de año cuando el Rewind está listo — **bloqueada por la decisión
  D3.7** (cuándo, cómo se llega al usuario y con qué dependencia). No se implementa a medias: ver el
  bloque de D3.7 para las tres preguntas abiertas y la parte que sí es independiente

---

## Criterios de aceptación (con evidencia)

- [ ] CA3.1 Con un usuario de prueba que tiene **3 juegos de duración conocida** y elige
      "30 minutos", la recomendación **excluye** el juego de 40 h y **explica por qué**.
- [ ] CA3.2 La recomendación cambia de forma coherente al cambiar el tiempo disponible
      (probado con los 3 valores: 30 min / 2 h / una tarde).
- [ ] CA3.3 El Rewind cuadra exactamente con los datos introducidos (horas, juego más
      jugado, nota media) — verificación manual contra el diario.
- [x] CA3.4 La imagen compartible se genera y se comparte correctamente (probado con la
      hoja de compartir de Android en el emulador y guardada en el dispositivo) — **02/10**: PNG de
      996×1118 extraído de la caché de la app (`.verificacion/10-imagen-compartida.png`) y hoja de
      compartir del sistema abierta con `image/png` (`.verificacion/09-compartir.png`). Se interpreta
      «guardada en el dispositivo» como **escrita en el almacenamiento de la app** (caché); no se
      publica en la galería, que sería otro flujo.
- [ ] CA3.5 Un usuario con **muy poca actividad** ve un Rewind que celebra igualmente
      (no mensajes negativos ni vacíos).
- [x] CA3.6 Tests JVM del motor de recomendación (casos: biblioteca vacía, un solo juego,
      duraciones extremas, sin tiempo indicado) y del cálculo del Rewind — **recomendación:** 13 tests
      (`RecommendationEngineTest`); **Rewind:** 21 tests (`RewindUtilsTest`). Verificado el 02/10:
      178 unitarios en verde, 0 fallos.

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| La recomendación parece aleatoria y pierde credibilidad | D3.2: explicar siempre el porqué; test de aceptación con usuarios reales |
| El Rewind sin datos suficientes se siente vacío | CA3.5: diseñar el caso "poca actividad" desde el principio |
| Generar imágenes con muchas portadas va lento | Reutilizar la caché de F0 y limitar la tarjeta a 4–6 juegos |

## Cómo se verifica

Emulador + tests JVM de la lógica pura (motor y cálculos) + capturas del Rewind y de las
recomendaciones explicadas + imagen compartida.

---

## 🔍 Auditoría de cierre de fase

> Aplica el [protocolo de auditoría de cierre](../metodologia/auditoria-de-cierre.md).
> **Particularidad de F3:** la fase es de **celebración y movimiento**, así que el bloque 6
> revisa explícitamente las **animaciones del Rewind** y de la recomendación, y el caso de
> «poca actividad» (CA3.5).

- [ ] Bloque 1: build debug y release (R8) sin warnings nuevos
- [ ] Bloque 2: unitarios en verde; motor de recomendación y cálculo del Rewind testeados
- [ ] Bloque 3: lint limpio; sin recursos de compartir sin usar
- [ ] Bloque 4: el motor de recomendación es lógica pura (testeable en JVM sin Firebase)
- [ ] Bloque 5: docs + decisiones D3.x registradas
- [ ] Bloque 6: Rewind y recomendación revisados en movimiento, y el caso «poca actividad» con tono positivo
- [ ] Auditoría firmada y validada por el propietario

---

## Registro de decisiones

- **D3.1–D3.6 — Adoptadas según recomendación (01/10/2026)** — decisión del propietario en bloque.
  Factores: tiempo + duración + géneros favoritos, con estado de ánimo como filtro opcional (D3.1);
  recomendación siempre explicada (D3.2); Rewind calculado en cliente (D3.3); imagen como formato
  de compartir (D3.4); anual + "tu historia con este juego" (D3.5); tono celebratorio sin culpa
  (D3.6). **Nota visual:** los componentes nuevos del Rewind nacen en `ui/designsystem/` según la
  regla de convivencia de [ADR-0009](../metodologia/adr/0009-reanclaje-design-system.md) — ver el
  debate de arranque en el registro de la sesión.
- **Estado de la fase:** 🔵 Aprobada — lista para ejecutar.

## Hallazgos abiertos (detectados al verificar T3.7 en emulador)

- **Los géneros llegan en inglés.** La captura `02-rewind.png` muestra «Género dominante: **Action**».
  El dato viene de RAWG y se pinta tal cual, así que incumple `DESIGN.md` §8 («cero textos en inglés»).
  **No es un problema del Rewind:** afecta también a Estadísticas (`topGenres`), a los filtros de la
  biblioteca y a cualquier sitio que muestre géneros. Dos salidas razonables: (a) tabla de traducción
  en la capa de presentación (barato, y el género original se conserva para comparar con RAWG), o
  (b) traducir en la ingesta del catálogo (hay que migrar lo ya guardado). **Decisión pendiente del
  propietario**; no bloquea T3.7 y se anota aquí para que no se pierda.
- **El barrido del acento (ADR-0012) seguía incompleto.** Al tocar `GameDetails` para T3.10
  apareció `DurationCard` pintando **«Llevas jugado: N h»** con `colorScheme.primary`: es un dato, no
  una acción, y ADR-0012 lo prohíbe explícitamente. Corregido a tinta el 02/10/2026 (commit de T3.10).
  Confirma lo que la auditoría ya avisaba: los **50 `colorScheme.primary` de 23 ficheros** siguen
  necesitando el barrido completo que DX-T4 deja pendiente. Se deja anotado aquí.
