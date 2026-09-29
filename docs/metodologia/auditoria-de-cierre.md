# Auditoría de cierre de fase (protocolo)

> **Qué es.** El último paso **obligatorio** de cada fase: una revisión del **código y de la
> experiencia** antes de dar la fase por cerrada. Convierte «tareas marcadas» en «fase cerrada
> de verdad».
> **Cuándo:** al terminar las tareas de la fase y **antes** de pasar su estado a ✅ Completada.
> **Quién:** la ejecuta y firma el agente; la **valida el propietario**.
> **Por qué:** sin auditoría, la deuda y los detalles de calidad se acumulan hasta reaparecer
> como bugs en fases posteriores — o, peor, en la release.

Cada fase del [roadmap](../roadmap/README.md) termina con una sección
**«🔍 Auditoría de cierre de fase»** que aplica este protocolo, con un enfoque propio de la fase.

---

## 1. Los 6 bloques de la auditoría

| # | Bloque | Qué se revisa | Evidencia mínima |
|---|---|---|---|
| 1 | **Compilación y build** | `assembleDebug` y `assembleRelease` (R8) compilan **sin warnings nuevos** | Salida de Gradle |
| 2 | **Tests** | `testDebugUnitTest` + `connectedDebugAndroidTest` en **verde**; lo nuevo tiene test | Resumen de la suite |
| 3 | **Calidad estática** | Lint, warnings del compilador, **código muerto**, TODOs huérfanos, dependencias sin usar | Informe de lint |
| 4 | **Arquitectura y consistencia** | SSOT respetado, sin saltos de capa, patrones del design system, **sin DTOs de API en la UI** | Revisión del diff |
| 5 | **Documentación y trazabilidad** | Fase, decisiones, `AGENTS.md` y roadmap **sincronizados** con lo implementado | Diff de docs |
| 6 | **Experiencia y estados** | Recorrido real de las pantallas tocadas: **vacío / carga / error / offline**; si la fase toca UI, también **motion** | Capturas + recorrido |

> El bloque 6 es donde se comprueba que «funciona» no es lo mismo que «se siente bien».

## 2. Checklist (copiar en la fase)

- [ ] Debug y Release (R8) compilan sin warnings nuevos
- [ ] Unitarios en verde; instrumentados en verde (o justificado por qué no aplica)
- [ ] Tests nuevos para la lógica añadida en la fase
- [ ] Lint limpio; sin código muerto ni TODOs sin dueño
- [ ] Sin saltos de capa ni DTOs de API en la UI; SSOT intacto
- [ ] Todas las pantallas tocadas revisadas en sus estados vacío/carga/error/offline
- [ ] Estados de control (hover/press/disabled/active) y flujos sin callejones sin salida
- [ ] Accesibilidad básica: contraste, tamaño táctil, etiquetas para TalkBack
- [ ] Docs de la fase + ADRs + `AGENTS.md` sincronizados con lo implementado
- [ ] Criterios de aceptación (CA) de la fase con **evidencia** (captura, log o test)

## 3. Informe de auditoría (plantilla)

```
Fase: Fx — <título>          Fecha: dd/mm/aaaa
Resultado: ✅ Apta / 🟡 Apta con reservas / ⛔ No apta
Bloques:  1 ✅  2 ✅  3 🟡  4 ✅  5 ✅  6 ✅
Hallazgos y acciones:
  - <qué se encontró> → <acción / issue / decisión>
Reservas (si las hay):
  - <pendiente> → <quién / cuándo>
Firma del agente: ___        Validado por el propietario: ___
```

## 4. Regla de cierre

Una fase **no pasa a ✅ Completada** sin:
1. sus criterios de aceptación cumplidos **con evidencia**, y
2. la auditoría de cierre ejecutada y **validada por el propietario**.

Los hallazgos que no se resuelvan en la propia fase se convierten en **deuda** con disparador
(documentados en [DEUDA-TECNICA-2026.md](../plan/DEUDA-TECNICA-2026.md) o en el registro de la
fase).
