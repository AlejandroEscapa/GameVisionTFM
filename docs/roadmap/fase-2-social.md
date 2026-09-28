# Fase 2 — Social

**Estado:** ⬜ Pendiente · **Estimación:** 1 semana · **Depende de:** F1

## Objetivo

Que GameVision deje de ser una app individual: **perfil público, encontrar gente por
nombre de usuario, feed de actividad, Top 4 y listas curadas**. La investigación es
explícita: los trackers que sobreviven al abandono lo hacen por **responsabilidad social**
o por automatización. Esta fase ataca la primera.

## Resultado verificable

Dos usuarios distintos pueden **encontrarse por nombre de usuario**, ver sus perfiles y
bibliotecas, seguirse y ver la actividad del otro en un feed.

## Por qué esta fase y no otra

Hoy sólo se puede añadir un amigo **escribiendo su email exacto** — una barrera enorme que
hace que la parte social esté prácticamente muerta aunque exista. Y el feed es lo que
convierte la app en algo que se abre a diario sin esfuerzo (es la razón por la que existe
Letterboxd). Va después de F1 porque **no hay nada interesante que mostrar en un feed
hasta que existe el diario y las notas**.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D2.1 | **Modelo social** | (a) Amigos simétricos (actual) · (b) Seguir asimétrico (Letterboxd) | **(b) seguir**: el asimétrico no exige permiso mutuo, crece solo y es el modelo que funciona en el sector | ⬜ |
| D2.2 | **Privacidad por defecto** | (a) Perfil público · (b) Privado y abrir por opción | **(a) público** con interruptor para hacerlo privado: el descubrimiento es el motor del producto | ⬜ |
| D2.3 | **Qué se publica en el feed** | (a) Todo (estados, notas, reseñas, sesiones) · (b) Solo hitos (completado, reseña, lista) | **(b) solo hitos**: las sesiones diarias saturan el feed | ⬜ |
| D2.4 | **Buscar usuarios: dónde se hace** | (a) En el cliente (como hoy) · (b) Cloud Function | **(b)**: hoy para buscar un amigo se puede leer cualquier perfil (privacidad); con Function se expone solo `username → email` al buscar | ⬜ |
| D2.5 | **Moderación de reseñas** | (a) Nada · (b) Reportar + bloqueo de usuario · (c) Filtro de palabras | **(b) mínimo**: reportar y bloquear es suficiente para el alcance actual y demuestra criterio | ⬜ |
| D2.6 | **¿Comentarios o solo reacciones?** | (a) Comentarios · (b) Me gusta · (c) Ambos | **(b) me gusta en F2, comentarios después**: los comentarios implican moderación y notificaciones | ⬜ |

---

## Tareas

- [ ] T2.1 Nombre de usuario único + búsqueda por username (Cloud Function)
- [ ] T2.2 Perfil público: biblioteca, estadísticas, Top 4, listas visibles
- [ ] T2.3 Seguir / dejar de seguir (modelo asimétrico) + contadores
- [ ] T2.4 Feed de actividad con los hitos de la gente que sigues
- [ ] T2.5 Top 4 editable y visible en el perfil (identidad, como en Letterboxd)
- [ ] T2.6 Listas curadas: crear, ordenar, públicas o privadas, con progreso
- [ ] T2.7 Me gusta en reseñas y en entradas del feed
- [ ] T2.8 Reportar y bloquear usuario
- [ ] T2.9 Migrar los amigos actuales (simétricos) al modelo nuevo sin perder relaciones

---

## Criterios de aceptación (con evidencia)

- [ ] CA2.1 Dos usuarios de prueba se encuentran buscando por **nombre de usuario**
      (no por email) y ven sus perfiles.
- [ ] CA2.2 El feed muestra los hitos del otro usuario y se actualiza sin recargar la app.
- [ ] CA2.3 El Top 4 aparece en el perfil y se puede cambiar.
- [ ] CA2.4 Una lista privada **no** es visible desde otro usuario (verificado con dos cuentas).
- [ ] CA2.5 Los amigos migrados del modelo antiguo siguen apareciendo.
- [ ] CA2.6 La búsqueda de usuarios ya **no expone** el listado completo de perfiles
      (verificar con la API REST: sin autenticar no se puede listar `users`).
- [ ] CA2.7 Tests: lógica de seguimiento y de visibilidad de listas.

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| La búsqueda por username sin Function obliga a leer todos los perfiles | D2.4: Cloud Function desde el principio de la fase |
| Modelo social nuevo rompe amigos existentes | T2.9 con migración y CA2.5 verificable |
| Contenido inapropiado en reseñas | D2.5: reportar + bloquear como mínimo viable |

## Cómo se verifica

Dos cuentas reales en el emulador (y una en segundo dispositivo si hay) + comprobación
REST de que las lecturas públicas ya no exponen la colección completa.

---

## Registro de decisiones

_(vacío: pendiente de debate)_

## 💡 Ideas registradas (28/09/2026)

- **Comunidad de logros (estilo foro)**: feed de logros conseguidos, secciones de «coleccionados»,
  y **porcentaje de jugadores por logro**.
  ⚠️ Requiere **fuente de datos de logros por plataforma (Steam/PSN/Xbox)** — RAWG/IGDB no la ofrecen;
  análisis pendiente antes de planificar tareas. Vinculado a D2.x (feed/moderación) y a la vitrina de
  «Coleccionados» de F1.
