# Fase 3 — El "wow": decidir y celebrar

**Estado:** ⬜ Pendiente · **Estimación:** 1 semana · **Depende de:** F1 · **No depende de:** F2

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
| D3.1 | **Factores del "¿qué juego ahora?"** | (a) Sólo tiempo disponible vs duración · (b) + género favorito del momento · (c) + estado de ánimo elegido por el usuario · (d) + nota histórica del usuario | **(b) más (c) opcional**: con tiempo+duración+gusto se acierta mucho y se explica fácil; el estado de ánimo como filtro manual da control | ⬜ |
| D3.2 | **¿Se explica la recomendación?** | (a) Sí, con el porqué · (b) No, solo el resultado | **(a) siempre**: la investigación del sector critica las cajas negras; explicar es parte del valor y da confianza | ⬜ |
| D3.3 | **¿Dónde se calcula el Rewind?** | (a) En el cliente con los datos locales · (b) Cloud Function | **(a)**: el dato ya está en el dispositivo y evita dependencia de Functions; (b) si hiciera falta histórico largo | ⬜ |
| D3.4 | **Formato de compartir** | (a) Imagen generada (tarjeta) · (b) Texto · (c) Ambos | **(a) imagen**, con las tarjetas del design system: es lo que se comparte de verdad (el formato de Strava/Spotify) | ⬜ |
| D3.5 | **Periodicidad del Rewind** | (a) Sólo anual · (b) Anual + mensual + "tu historia con este juego" | **(b) anual como plato fuerte y "por juego" como relleno**: multiplica las ocasiones de compartir con poco trabajo extra | ⬜ |
| D3.6 | **Tono del Rewind** | (a) Celebrar siempre (también si jugaste poco) · (b) Mostrar también lo no jugado | **(a) celebrar**: la investigación avisa de que la gamificación puede volverse tóxica; nunca culpabilizar | ⬜ |

---

## Tareas

### A. Decidir: "¿Qué juego ahora?"
- [ ] T3.1 Motor de recomendación (lógica pura y testeable) con factores y pesos de D3.1
- [ ] T3.2 Pantalla "¿Qué juego ahora?" con el tiempo disponible como entrada rápida (30 min / 2 h / una tarde)
- [ ] T3.3 Explicación de cada recomendación en lenguaje natural
- [ ] T3.4 Acciones directas desde la recomendación (empezar, marcar como jugando, descartar)
- [ ] T3.5 Aprendizaje ligero: registrar si el usuario aceptó o descartó para mejorar el orden

### B. Celebrar: GameVision Rewind
- [ ] T3.6 Cálculo del recap: horas totales, juego más jugado, géneros dominantes, nota media, racha más larga, mes más activo, plataforma principal
- [ ] T3.7 Pantalla del Rewind con animación y las tarjetas del design system
- [ ] T3.8 Generación de imagen compartible (con marca de la app)
- [ ] T3.9 Compartir nativo de Android (hoja de compartir del sistema)
- [ ] T3.10 "Tu historia con este juego" (mini-recap por juego)
- [ ] T3.11 Notificación de fin de año cuando el Rewind está listo

---

## Criterios de aceptación (con evidencia)

- [ ] CA3.1 Con un usuario de prueba que tiene **3 juegos de duración conocida** y elige
      "30 minutos", la recomendación **excluye** el juego de 40 h y **explica por qué**.
- [ ] CA3.2 La recomendación cambia de forma coherente al cambiar el tiempo disponible
      (probado con los 3 valores: 30 min / 2 h / una tarde).
- [ ] CA3.3 El Rewind cuadra exactamente con los datos introducidos (horas, juego más
      jugado, nota media) — verificación manual contra el diario.
- [ ] CA3.4 La imagen compartible se genera y se comparte correctamente (probado con la
      hoja de compartir de Android en el emulador y guardada en el dispositivo).
- [ ] CA3.5 Un usuario con **muy poca actividad** ve un Rewind que celebra igualmente
      (no mensajes negativos ni vacíos).
- [ ] CA3.6 Tests JVM del motor de recomendación (casos: biblioteca vacía, un solo juego,
      duraciones extremas, sin tiempo indicado) y del cálculo del Rewind.

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

_(vacío: pendiente de debate)_
