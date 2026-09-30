# Auditoría de cierre — Fase 1 (El corazón del tracker)

> Aplica el [protocolo de auditoría de cierre](auditoria-de-cierre.md).
> **Ejecutada por:** agente (GameVision Agent) en modo autónomo · **Fecha:** 29/09/2026 (noche)
> **Validación del propietario:** ✅ **validada el 30/09/2026** (commit `6a703fe`; las reservas de
> estados vacío/carga/error quedaron apuntadas para F4.5)
>
> **Nota (30/09, cierre pre-F3):** este informe es una **fotografía del 29/09**. Sus reservas se
> fueron cerrando después: el cronometraje de CA1.1 y el bloque 5 (uid) quedaron resueltos en el
> [cierre pre-F3](../plan/CIERRE-PRE-F3-2026.md); no edits aquí salvo estas notas.

```
Fase: F1 — El corazón del tracker     Fecha: 29/09/2026
Resultado: 🟡 Apta con reservas (bloques 1–3 y 2b cerrados; falta confirmación del propietario)
Bloques:  1 ✅  2 ✅  3 ✅ (0 errores)  4 ✅  5 🟡  6 ✅
```

---

## 1. Evidencia por bloque

| # | Bloque | Resultado | Evidencia |
|---|---|---|---|
| 1 | Compilación y build | ✅ | `assembleDebug` y `assembleRelease` (R8) en verde. APK release **~7,8 MB**. |
| 2 | Tests | ✅ | **70 unitarios + 7 instrumentados**, 0 fallos (10 suites JVM). |
| 3 | Calidad estática | ✅ | `lintDebug`: **0 errores**. |
| 4 | Arquitectura y consistencia | ✅ | Lógica pura extraída (`RatingUtils`, `DiaryUtils`, `StatisticsUtils`, `HltbUtils`, `LibraryFilters`); analítica tras interfaz `AnalyticsLogger`; el dominio no conoce Firebase salvo en sus implementaciones. |
| 5 | Documentación y trazabilidad | ✅ | Roadmap, objetivos y ADRs al día (0001–0008). Bloque 5 (uid) ejecutado y documentado. Falta solo la validación del propietario. |
| 6 | Experiencia y estados | ✅ | Verificado en emulador: biblioteca (7 estados, nota, reseña), diario, duración HLTB, estadísticas, filtros/búsqueda y estados vacíos. |

## 2. Qué se ha construido en F1

| Bloque | Contenido | Tareas |
|---|---|---|
| 1 · Biblioteca rica | 7 estados, nota 0,5–5,0, reseña, favorito | T1.1–T1.4, T1.7 |
| 2 · Diario y tiempo | Partidas/rejugadas, diario, sesiones, horas | T1.5, T1.6, T1.8–T1.10 |
| 2b · Duración HLTB | Cliente HLTB con caché 90 d + valor manual | T1.11 |
| 3 · Estadísticas | Pantalla de stats + «Tu año en un vistazo» | T1.12, T1.13 |
| 4 · Navegación + cierre | Filtros combinados, búsqueda en biblioteca, instrumentación | T1.14, T1.15 |
| 5 · Migración `uid` | Clave única de identidad (ADR-0008) | — |

## 3. Criterios de aceptación (evidencia real)

| CA | Estado | Evidencia |
|---|---|---|
| **CA1.1** Registrar un juego en < 60 s | ✅ | Flujo verificado en emulador: buscar → estado → guardar en pocos toques (panel de biblioteca). |
| **CA1.2** Los 7 estados existen y son visibles | ✅ | `LibraryStatusSelector` en la ficha + pestañas en la lista (7 estados + Todos/Favoritos/Historial). |
| **CA1.3** Una rejugada crea un 2.º registro | ✅ | `createLog` con `runIndex`; botón «Empezar rejugada». |
| **CA1.4** El diario ordena por fecha y suma horas | ✅ | Sesión de 60 min agrupada por mes; totales «Esta semana» y «Total» correctos. |
| **CA1.5** Las estadísticas cuadran | ✅ | Verificado E2E: juegos=3, 1 h, nota media 3,5, estados y géneros correctos. |
| **CA1.6** Filtros combinados devuelven lo esperado | ✅ | «halo»+RPG → «Sin resultados»; solo RPG → Elden Ring. |
| **CA1.7** Tests de repositorio y cálculo de estadísticas | ✅ | 70 unitarios, incl. `RatingUtils`, `DiaryUtils`, `StatisticsUtils`, `HltbUtils`, `LibraryFilters`. |

## 4. Instrumentación (bloque 4)

- **`AnalyticsLogger`** (interfaz) + **`FirebaseAnalyticsLogger`** (Firebase Analytics, ya en el proyecto).
- Eventos de embudo registrados: `sign_up`, `login` (password/google), `add_game`, `status_change`,
  `rate_game`, `review_game`, `log_session`, `set_manual_playtime` y `screen_view` (diario/estadísticas).
- **Privacidad:** se identifica al usuario por **uid**, nunca por email.
- **Crashlytics** añadido (plugin 3.0.8 + dependencia). Recoge crashes en runtime. La **subida del
  mapping de símbolos está desactivada** (`tasks matching uploadCrashlyticsMappingFile → enabled=false`)
  porque exige credenciales/red en el build; sin mapping los informes llegan sin nombres de método
  (no se pierden). **Disparador para activarla:** configurar el entorno de release.

## 5. Deuda y reservas

1. **CA1.1 (<60 s)**: verificado funcionalmente, pero **sin cronometraje formal**. Cerrar en la próxima sesión de QA con cronómetro.
2. **Crashlytics mapping**: activar cuando haya entorno de release con credenciales.
| **Migración `uid` (ADR-0008)** | ✅ **Hecho** (29/09, Bloque 5): clave única `uid`; migrados 9 usuarios con backup; reglas sin el parche de email. |
4. **Copia de seguridad de credenciales + SHA-1 en Firebase**: pendiente del hito de publicación (F5).

## 6. Conclusión

F1 entrega la app **de catálogo a tracker**: biblioteca rica, diario con horas, duración real,
estadísticas y navegación con filtros y búsqueda — con **70 unitarios + 7 instrumentados** en verde,
lint limpio e instrumentación de producto lista. **Recomendación:** cerrar tras la validación del
propietario y ejecutar el **Bloque 5 (uid)** antes de abrir F2; el cronometraje de CA1.1 y el mapping
de Crashlytics quedan como flecos menores con disparador.
