# Fase 4 — Nativo y pulido

**Estado:** ⬜ Pendiente · **Estimación:** 1 semana · **Depende de:** F1

## Objetivo

Explotar la ventaja que **los competidores web no pueden copiar**: ser una app Android
nativa de verdad (widget en la pantalla de inicio, notificaciones útiles, funcionar sin
conexión, tablet y plegable bien resueltos). Es el argumento que el líder del mercado no
puede igualar (Backloggd no tiene app; su petición nº1 es tenerla).

## Resultado verificable

Hay un **widget en la pantalla de inicio** que muestra tu juego actual y tu progreso, las
**notificaciones llegan** con acciones útiles, y la app **abre y funciona sin conexión**
con lo ya visitado.

## Por qué esta fase y no otra

Es el cierre que convierte "buena app" en "app que se queda instalada": el widget y las
notificaciones son lo que mantiene el hábito sin abrir la app. Va al final porque necesita
que exista contenido que mostrar (biblioteca, diario y estadísticas de F1).

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D4.1 | **Qué muestra el widget** | (a) Juego actual + progreso · (b) Estadísticas de la semana · (c) Ambos, configurable | **(a) por defecto, (c) como opción**: el juego actual es lo más útil y lo más vistoso | ⬜ |
| D4.2 | **Tecnología del widget** | (a) Glance (Compose) · (b) Vistas XML remotas | **(a) Glance**: ya somos 100 % Compose y reutiliza el design system | ⬜ |
| D4.3 | **Qué notificaciones** | (a) Sociales (seguidores, me gusta) · (b) Estrenos y ofertas de tus deseados · (c) Recordatorio de tu backlog · (d) Todas | **(a) y (c) al principio**, configurables en ajustes: avisar de todo es la vía rápida a que se desactiven | ⬜ |
| D4.4 | **Alcance offline** | (a) Catálogo visitado + biblioteca · (b) Todo el catálogo | **(a)**: la biblioteca siempre offline (es tuya) y el catálogo ya visto; el resto necesita red | ⬜ |
| D4.5 | **¿Wear OS / TV?** | (a) No por ahora · (b) Wear como compañero (registrar desde el reloj) · (c) TV | **(a)**: no aporta al objetivo y multiplica el mantenimiento; la tablet ya está cubierta por la navegación adaptativa | ⬜ |
| D4.6 | **Estantería visual** | (a) Sí, en esta fase · (b) Descartar | **(a)**: es altísimo valor percibido para un coleccionista y barato de hacer con las portadas ya cacheadas | ⬜ |
| D4.7 | **Superficie de descubrimiento en Home** | (a) **Sí**: rail de trending + grid de pósters · (b) Diferir | **(a)**: hoy Home no muestra catálogo (0 `LazyRow`/`LazyVerticalGrid`); es el mayor retorno visual por línea del proyecto | ⬜ |

---

## Tareas

- [ ] T4.1 Widget con Glance: juego actual, portada, tiempo jugado y acceso directo
- [ ] T4.2 Configuración de notificaciones en ajustes (por tipo, con interruptores)
- [ ] T4.3 Notificaciones sociales (nuevo seguidor, me gusta) vía FCM + Cloud Function
- [ ] T4.4 Notificación útil de backlog ("llevas 3 semanas sin tocar X", nunca culpabilizante)
- [ ] T4.5 Modo offline afinado: indicador de estado, sin pantallas en blanco, sin errores técnicos
- [ ] T4.6 Estantería visual: vista de colección ordenable con carátulas
- [ ] T4.7 Repaso de tablet/plegable: lista-detalle real con Navigation 3 (pendiente de la fase de UI)
- [ ] T4.8 Accesibilidad: contraste, tamaños táctiles, TalkBack en los flujos principales
- [ ] T4.9 Icono temático y pantalla de arranque acordes a la marca
- [ ] T4.10 Superficie de descubrimiento en Home: rail de *trending* (`LazyRow`) + grid de pósters (`LazyVerticalGrid`)
- [ ] T4.11 Rutas de navegación tipadas (`@Serializable`) y `hiltViewModel()` por destino (Nav Compose 2.8+, sin migrar a Nav3)
- [ ] T4.12 Paginación del catálogo (Paging 3) en búsqueda y lista de juegos

---

## Criterios de aceptación (con evidencia)

- [ ] CA4.1 El widget aparece en el lanzador del emulador y **se actualiza** al cambiar el
      estado de un juego (captura antes/después).
- [ ] CA4.2 Las notificaciones llegan con la app cerrada y respetan los interruptores de
      ajustes (probado desactivando una categoría).
- [ ] CA4.3 Con el emulador en **modo avión**: la app abre, muestra la biblioteca completa
      y avisa del estado sin conexión; al recuperar la red se sincroniza.
- [ ] CA4.4 TalkBack recorre los flujos principales sin elementos sin etiqueta
      (revisión manual de registro, biblioteca y ficha).
- [ ] CA4.5 En ventana de tablet se ve lista + detalle a la vez.
- [ ] CA4.6 Sin regresiones: **todos** los tests (unitarios + instrumentados) en verde.
- [ ] CA4.7 Home muestra un rail de trending y un grid de pósters navegables (captura en móvil y tablet).

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Los widgets consumen batería si se actualizan demasiado | Actualización sólo al cambiar datos relevantes, no por temporizador agresivo |
| Las notificaciones saturan y el usuario las desactiva | D4.3: por defecto las mínimas y configurables |
| El repaso de accesibilidad destapa problemas de contraste del design system | Ya hay tokens de color centralizados: se corrige en un sitio (DESIGN.md) |

## Cómo se verifica

Emulador (widget en el lanzador, modo avión, ventana de tablet) + TalkBack + suite completa
de tests + captura de la app en tablet con lista-detalle.

---

## 🔍 Auditoría de cierre de fase

> Aplica el [protocolo de auditoría de cierre](../metodologia/auditoria-de-cierre.md).
> **Particularidad de F4:** todo es nativo (widget, notificaciones, offline, adaptativo), así que
> el bloque 1 se centra en que **no haya regresiones de rendimiento** y el bloque 6 en el
> comportamiento real del widget y del modo avión.

- [ ] Bloque 1: build debug y release (R8) sin warnings; sin regresión de rendimiento/batería
- [ ] Bloque 2: unitarios + instrumentados en verde (CA4.6: **todos** en verde)
- [ ] Bloque 3: lint limpio; sin manifest/permissions sin usar
- [ ] Bloque 4: widget (Glance) reutiliza el design system; sin lógica duplicada
- [ ] Bloque 5: docs de la fase + decisiones D4.x registradas
- [ ] Bloque 6: widget en el lanzador, notificaciones con app cerrada y modo avión (CA4.1–CA4.3)
- [ ] Auditoría firmada y validada por el propietario

---

## Registro de decisiones

_(vacío: pendiente de debate)_
