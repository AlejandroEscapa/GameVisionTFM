# Fase 1 — El corazón del tracker

**Estado:** ⬜ Pendiente · **Estimación:** 2 semanas · **Depende de:** F0 · **Bloquea a:** F2, F3, F4

## Objetivo

Convertir las tres listas planas actuales en una **biblioteca rica**: estados reales,
valoración con estrellas, reseña, diario con fechas y horas, rejugadas, duración del
juego y estadísticas de perfil. Es lo que separa "app de catálogo" de "tracker".

## Resultado verificable

Un usuario nuevo puede **registrar un juego en menos de 60 segundos** eligiendo estado y
nota, **añadir una sesión de juego al diario** y **ver sus estadísticas** (horas, notas,
géneros) sin que nada sea de pago.

## Por qué esta fase y no otra

Es el **mínimo del mercado**: Backloggd distingue Completado/Dominado/Retirado/En pausa/
Abandonado, Letterboxd tiene diario y estadísticas, y HowLongToBeat da la duración. Sin
esto no competimos. Además, el diario es el **dato crudo del Rewind** (F3): sin F1 no hay
nada que celebrar. Y las estadísticas, que en los líderes son **de pago**, aquí se regalan
como diferenciación directa.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D1.1 | **Definición exacta de los estados** | (a) Completado = ver créditos, Dominado = 100 % · (b) Nuestra propia definición | **(a)**: el sector ya usa ese vocabulario y evita discusión con el usuario | ⬜ |
| D1.2 | **¿Nota y reseña obligatorias al registrar?** | (a) Todo opcional · (b) Nota obligatoria · (c) Nota obligatoria sólo al completar | **(a) opcional al registrar, (c) sugerida al completar**: no bloquear nunca el registro rápido (GG gana por registrar en 10 s) | ⬜ |
| D1.3 | **Escala de nota** | (a) 5 estrellas con medias · (b) 10 puntos · (c) Ambas | ✅ **CERRADA (28/09/2026; revisada el mismo día): escala 0,5–5,0 con medias estrellas**, alineada con el formato de RAWG. Ver ADR-0003 (supersede al ADR-0002) | ✅ |
| D1.4 | **Duración del juego: fuente** | (a) Scraper HowLongToBeat · (b) Sólo campo propio · (c) No incluirla | **(a) con caché y fallback a (b)**: es *la* pregunta práctica y hoy obliga al usuario a abrir otra app | ⬜ |
| D1.5 | **Estadísticas: ¿cliente o servidor?** | (a) Calcular en el cliente · (b) Cloud Function | **(a) en F1**: con los datos del propio usuario es rápido y evita dependencia de Functions; (b) cuando F3 necesite agregados | ⬜ |
| D1.6 | **¿El diario es automático o manual?** | (a) Manual (el usuario apunta) · (b) Automático (al cambiar de estado) | **(a) manual, con atajos**: el diario es un acto de voluntad (como Letterboxd) y su valor está en que el usuario lo escribe | ⬜ |
| D1.7 | **Importación de Steam/PSN/Xbox** | (a) En F1 · (b) Fase posterior · (c) Descartar | **(b)**: la investigación la marca como clave contra el abandono, pero es un bloque grande; F1 primero debe tener algo que importar | ⬜ |

---

## Tareas

### A. Biblioteca
- [ ] T1.1 UI de estado: selector con los 7 estados (Jugando, Completado, Dominado, En pausa, Retirado, Abandonado, Deseado)
- [ ] T1.2 Cambio de estado rápido desde la ficha del juego y desde la lista
- [ ] T1.3 Valoración con medias estrellas (crear, editar, borrar)
- [ ] T1.4 Reseña escrita por juego, con formato corto destacado (cultura "una línea" de Letterboxd)
- [ ] T1.5 Múltiples partidas por juego (rejugada = nuevo `log`)
- [ ] T1.6 Plataforma jugada por partida (PC, PS5, Switch…)
- [ ] T1.7 Favorito (para el Top 4 de F2)

### B. Diario y tiempo
- [ ] T1.8 Vista de diario cronológico (por mes, con carátulas)
- [ ] T1.9 Apuntar sesión: "hoy jugué X minutos" (con atajo rápido)
- [ ] T1.10 Horas acumuladas por juego y totales
- [ ] T1.11 Duración estimada del juego (historia / +extras / completista) desde la caché de F0

### C. Estadísticas
- [ ] T1.12 Pantalla de estadísticas: horas totales, distribución de notas, géneros y plataformas favoritas, juegos por año
- [ ] T1.13 "Tu año en un vistazo" (semilla del Rewind de F3)

### D. Navegación de la biblioteca
- [ ] T1.14 Filtros y orden: por estado, nota, género, plataforma, fecha
- [ ] T1.15 Búsqueda dentro de tu biblioteca (distinta del catálogo)

---

## Criterios de aceptación (con evidencia)

- [ ] CA1.1 Registrar un juego nuevo (buscar → estado → guardar) en **menos de 60 s**
      cronometrado en el emulador.
- [ ] CA1.2 Los 7 estados existen y son visibles en la ficha y en la lista.
- [ ] CA1.3 Una rejugada crea un segundo registro sin borrar el primero.
- [ ] CA1.4 El diario muestra las sesiones ordenadas por fecha y suma las horas.
- [ ] CA1.5 Las estadísticas cuadran con los datos introducidos (verificación manual con
      un usuario de prueba: 3 juegos, 2 completados, 1 abandonado → los números coinciden).
- [ ] CA1.6 Los filtros combinados (estado + género) devuelven lo esperado.
- [ ] CA1.7 Tests nuevos para el repositorio de biblioteca y el cálculo de estadísticas
      (lógica pura, testeable en JVM sin Firebase).

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| Sobrecargar el registro y perder la rapidez | Decisión D1.2: nada obligatorio salvo el estado; medir CA1.1 en cada iteración |
| Las estadísticas se vuelven lentas | Se calculan sobre datos ya cargados del usuario; si crece, pasar a Cloud Function |
| Duplicar el vocabulario del sector y confundir | D1.1: usar las definiciones ya establecidas (Completado vs Dominado) |

## Cómo se verifica

Emulador + tests. Capturas: selector de estado, diario con sesiones, estadísticas de un
usuario de prueba con datos variados.

---

## Registro de decisiones

- **D1.3 — Escala de nota: 0,5–5,0 con medias estrellas (28/09/2026; revisión)** — Tras contrastar
  con el formato de RAWG (nota sobre 5 con decimales), la escala queda en **medias estrellas
  (0,5–5,0 en pasos de 0,5)**: congruencia visual con los datos de RAWG y 10 niveles de granularidad.
  Sustituye a la decisión previa del 1–10. Ver **ADR-0003** (`docs/metodologia/adr/0003-escala-medias-estrellas.md`),
  que supersede al ADR-0002.
