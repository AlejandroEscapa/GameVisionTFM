# Fase 3 — El "wow": decidir y celebrar

**Estado:** 🟢 En ejecución — D3.1–D3.8 cerradas · T3.1–T3.11 hechas · auditoría de cierre ejecutada 02/10 (apta, pendiente de validación del propietario) · **Estimación:** 1 semana · **Depende de:** F1 · **No depende de:** F2

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
| D3.7 | **¿Cómo se avisa de que el Rewind está listo?** (T3.11) | (a) **WorkManager** periódico con `HiltWorker` · (b) `AlarmManager` anual + `BroadcastReceiver` contra una caché local · (c) aviso en la app al abrirla, sin notificación | **(a)**, pero **el alcance de T3.11 hay que partirlo**: ver abajo | ✅ **Cerrada (02/10): (a)** — comprobación **diaria idempotente** (`RewindAvisoWorker`), aviso del **año cerrado** cualquier día desde el 1 de enero, **sin datos no se avisa** (D3.6) y «ya avisado de {año}» persistido en prefs |
| D3.8 | **¿Dónde vive el feedback aceptar/descartar?** (T3.5) | (a) DataStore local por uid · (b) Firestore (perfil o subcolección) | **(a)**: el motor corre en cliente con datos locales (D3.3); es lo "ligero" que pide T3.5; no toca `firestore.rules` (checks explícitos + despliegue a producción); funciona en invitado | ✅ **Cerrada (02/10): (a)** — `RecoFeedbackStore`; migrable a Firestore si el feedback debe cruzar dispositivos |

> **Cómo se cerró T3.11/D3.7 (02/10).** La regla 4 de este roadmap obligó a debatir
> antes de tocar código; estas fueron las respuestas que fijaron la implementación:
>
> 1. **¿Cuándo es «fin de año»?** No hay un día y una hora: el trabajo es **diario** y el
>    Rewind del año que cierra se avisa **cualquier día desde el 1 de enero** (si el móvil
>    estuvo apagado, sale el día que pueda). Avisar en diciembre contaría un año a medias.
> 2. **¿Cómo se llega al usuario?** Opción **(a): WorkManager** periódico (1 día, `KEEP`) con
>    `HiltWorker` (`androidx.work:work-runtime-ktx` 2.12.0 + `androidx.hilt:hilt-work` 1.4.0,
>    mismas generaciones que el stack). `AlarmManager` se descartó: no sobrevive al reinicio
>    sin receptor `BOOT_COMPLETED`, y un `OneTimeWork` con meses de retardo se pierde igual.
> 3. **¿Y si no hay actividad?** D3.6 prohíbe culpabilizar: **sin datos no se notifica**, y
>    «ya avisado de {año}» se persiste para no repetir.
>
> **Parte independiente, hecha junto con el programador:** canal de notificación, permiso
> `POST_NOTIFICATIONS` (API 33+, pedido al abrir el Rewind, sin insistir) y deep link
> `gamevision://rewind` que abre el Rewind al tocar la notificación.

---

## Tareas

### A. Decidir: "¿Qué juego ahora?"
- [x] T3.1 Motor de recomendación (lógica pura y testeable) con factores y pesos de D3.1 —
  `data/library/RecommendationEngine.kt`, **13 tests** (`RecommendationEngineTest`), commit `ac0baae`
  (*evidencia:* 132 unitarios en verde; regla CA3.1 con el matiz anti-caja-vacía)
- [x] T3.2 Pantalla "¿Qué juego ahora?" con el tiempo disponible como entrada rápida (30 min / 2 h / una tarde) — **hecha dentro de la Home «Para ti»** (bloque F de la iteración): chips de tiempo + ánimo en `ParaTiScreen`
- [x] T3.3 Explicación de cada recomendación en lenguaje natural — el motor ya la devuelve (D3.2) y la Home la pinta en cada tarjeta
- [x] T3.4 Acciones directas desde la recomendación (empezar, marcar como jugando, descartar) — cada tarjeta trae **«Jugar»** (registra aceptado en T3.5; si estaba en pausa pasa a jugando; abre la ficha) y **«Descartar»** (registra y oculta la tarjeta). *Evidencia en emulador:* `.verificacion/15-recomendacion-acciones.png` (tarjetas con botones) y «Jugar» abre la ficha del juego
- [x] T3.5 Aprendizaje ligero: registrar si el usuario aceptó o descartó para mejorar el orden — `RecoFeedbackStore` (DataStore local por uid, D3.8): lo descartado se excluye del motor, lo aceptado sube (+15 con motivo `ELEGIDO_ANTES` explicado). Si todo está descartado se ofrece «Mostrar de nuevo» (descartar no es para siempre). *Evidencia:* 4 tests del motor + 5 del store; ciclo completo en emulador (descartar ×2 → aviso + recuperar → tarjetas de vuelta, `.verificacion/14-recomendacion-descartada.png`)

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
- [x] T3.11 Notificación de fin de año cuando el Rewind está listo — **D3.7 cerrada con la
  opción (a)**. `data/notifications/`: `RewindAviso` (lógica pura del aviso: año que cierra,
  `debeAvisar`; **5 tests** en `RewindAvisoTest`), `RewindAvisoWorker` (`@HiltWorker`,
  comprobación diaria idempotente: sin sesión no hay aviso, sin datos no se notifica),
  `RewindNotifier` (canal «Tu Rewind», permiso API 33+, deep link `gamevision://rewind`).
  `GameVisionApplication` implementa `Configuration.Provider` con `HiltWorkerFactory`
  (inicializador automático quitado del manifest); `MainActivity`/`NavHost` consumen el deep
  link con el mismo patrón que el de Steam (incluido el intent inicial); `RewindScreen` pide
  el permiso al abrirse, sin insistir si se deniega. *Evidencia:* **189 unitarios verdes**
  (18 clases, 0 fallos), `assembleDebug` OK, `lintDebug` limpio, check-docs sin deriva.
  *En emulador (Pixel_9, API 36):* deep link `gamevision://rewind` entregado a la instancia en
  marcha abre el Rewind con datos reales (`.verificacion/13-deep-link-rewind.png`); el diálogo
  del permiso aparece al entrar; el trabajo periódico queda programado en el JobScheduler.

---

## Criterios de aceptación (con evidencia)

- [x] CA3.1 Con un usuario de prueba que tiene **3 juegos de duración conocida** y elige
      "30 minutos", la recomendación **excluye** el juego de 40 h y **explica por qué** —
      `RecommendationEngineTest.con_30_min_excluye_el_juego_de_40h_y_explica_por_que` (verde).
- [x] CA3.2 La recomendación cambia de forma coherente al cambiar el tiempo disponible
      (probado con los 3 valores: 30 min / 2 h / una tarde) —
      `RecommendationEngineTest.el_tiempo_disponible_cambia_la_recomendacion_de_forma_coherente`
      (verde); además las explicaciones se ven en el dispositivo (dumps de T3.4).
- [x] CA3.3 El Rewind cuadra exactamente con los datos introducidos (horas, juego más
      jugado, nota media) — verificación manual contra el diario — **02/10 (cuenta QA en
      Pixel_9)**: el diario muestra Minish Cap 29-sep-2026 1 h (total 1 h) y el Rewind 2026
      muestra 1 h jugadas, juego del año Minish Cap, 1 juego 1 terminado y Septiembre 1 h
      (`.verificacion/18-diario-qa.png` contra `13-deep-link-rewind.png`). Cuadra.
- [x] CA3.4 La imagen compartible se genera y se comparte correctamente (probado con la
      hoja de compartir de Android en el emulador y guardada en el dispositivo) — **02/10**: PNG de
      996×1118 extraído de la caché de la app (`.verificacion/10-imagen-compartida.png`) y hoja de
      compartir del sistema abierta con `image/png` (`.verificacion/09-compartir.png`). Se interpreta
      «guardada en el dispositivo» como **escrita en el almacenamiento de la app** (caché); no se
      publica en la galería, que sería otro flujo.
- [x] CA3.5 Un usuario con **muy poca actividad** ve un Rewind que celebra igualmente
      (no mensajes negativos ni vacíos) — garantía por construcción: `buildHighlights`
      siempre devuelve al menos una línea (suelo «Tu biblioteca te está esperando») y el titular
      flojo tiene frase propia («Tu {año}, a tu ritmo»); cubierto por tests de biblioteca vacía
      (`RewindUtilsTest`) y copy revisado («Un año tranquilo también cuenta»). Reserva: captura
      en dispositivo con cuenta vacía, pendiente (ver informe de auditoría).
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

- [x] Bloque 1: build debug y release (R8) sin warnings nuevos — `assembleDebug`,
  `assembleRelease` (con lintVital) en verde 02/10; warnings solo preexistentes (`Locale`,
  `!!`, `TabRow`)
- [x] Bloque 2: unitarios en verde; motor de recomendación y cálculo del Rewind testeados —
  **198 unitarios** (19 clases, 0 fallos) + **10 instrumentados** (0 fallos, Pixel_9); la
  auditoría encontró 2 instrumentados desactualizados (★ y textos en inglés) y los corrigió
- [x] Bloque 3: lint limpio; sin recursos de compartir sin usar — `lintDebug` limpio;
  `FileProvider`/`file_paths.xml`/`EXTRA_TEXT` en uso; 1 TODO con dueño (`RewindNotifier`:
  icono monocromo → reserva F5); sin código muerto en lo añadido por F3
- [x] Bloque 4: el motor de recomendación es lógica pura (testeable en JVM sin Firebase) —
  `RecommendationEngine`, `RewindUtils` y `RewindAviso` sin imports Android; SSOT (uid,
  `SessionRepository`, `UserViewModel.profile`) y capas respetados; `RecoFeedbackStore` en
  el scope del ViewModel
- [x] Bloque 5: docs + decisiones D3.x registradas — D3.1–D3.8 en la fase, check-docs sin
  deriva, roadmap sincronizado
- [x] Bloque 6: Rewind y recomendación revisados en movimiento, y el caso «poca actividad» con tono positivo — deep link, permiso, Jugar/Descartar y recuperar recorridos en Pixel_9 (capturas 13–18); Rewind byte-idéntico con/sin animaciones (SHA-256); copy de poca actividad celebratorio con suelo
- [ ] Auditoría firmada y validada por el propietario

### Informe de auditoría

```
Fase: F3 — El "wow": decidir y celebrar          Fecha: 02/10/2026
Resultado: ✅ Apta
Bloques:  1 ✅  2 ✅  3 ✅  4 ✅  5 ✅  6 ✅
Hallazgos y acciones:
  - 2 instrumentados desactualizados (★ en RatingBadge, inglés en SmokeTest) → corregidos
    en la propia auditoría (solo tests, sin tocar producto)
  - La ronda 3 de T3.11 dejó el árbol sin compilar (import de LaunchedEffect) → corregido
    al retomar, verificado con build + tests
  - TODO icono monocromo de notificación (RewindNotifier) → reserva: antes de F5
Reservas:
  - Captura en dispositivo del Rewind con cuenta vacía → cuando haya cuenta sin actividad
  - Disparo estacional real del aviso (enero) sin evidencia directa → cubierto por tests
    de debeAvisar + worker idempotente
Firma del agente: GameVision, 02/10/2026        Validado por el propietario: (pendiente)
```

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
- **D3.7 — Cerrada con la opción (a) (02/10/2026).** WorkManager periódico diario e idempotente
  con `HiltWorker` (`work-runtime-ktx` 2.12.0 + `hilt-work` 1.4.0). Cuándo: cualquier día desde
  el 1 de enero, del año que acaba de cerrar (si el móvil estuvo apagado, sale el día que
  pueda). Sin actividad no se avisa (D3.6) y «ya avisado de {año}» se persiste en prefs. La
  alternativa `AlarmManager` se descartó (no sobrevive al reinicio sin `BOOT_COMPLETED`).
- **D3.8 — Cerrada con la opción (a) (02/10/2026).** Feedback aceptar/descartar en DataStore
  local por uid (`RecoFeedbackStore`); el motor excluye descartados y empuja aceptados.

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
