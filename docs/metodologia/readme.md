# GameVision Development Framework (GDF)

> Sistema de trabajo de GameVision: cómo se decide, se construye, se verifica y se
> documenta cada avance. Nace de la unión de metodologías probadas y reales
> (Shape Up, ADR, Conventional Commits, SemVer, Keep a Changelog, trunk-based
> development, Definition of Done, entrega escalonada) adaptadas a **un
> desarrollador principal asistido por agentes de IA**.
>
> Este documento es la **capa de proceso**. El "qué" está en
> [`../../docs/product-vision-2026.md`](../product-vision-2026.md); el "cuándo" en
> [`../roadmap/README.md`](../roadmap/README.md); el "cómo se ve" en
> [`../../DESIGN.md`](../../DESIGN.md).

---

## 1. Principios (las reglas que no se negocian)

| # | Principio | De dónde viene | Qué significa aquí |
|---|---|---|---|
| 1 | **Decidir antes de construir** | Shape Up | Ninguna fase empieza con decisiones abiertas. Primero se debate y se cierra; luego se toca código. |
| 2 | **Una decisión importante = un ADR** | Architecture Decision Records | Toda decisión que sea cara de revertir queda escrita con su porqué y sus alternativas. |
| 3 | **Trabajo en fases con forma cerrada** | Shape Up | Cada fase tiene objetivo, alcance, criterios de aceptación y fecha de cierre. Si no cabe, se recorta alcance, no calidad. |
| 4 | **Trunk-based + ramas cortas** | Trunk-based development | Rama por fase, vida corta, merge cuando compila y los tests están verdes. Nada de ramas eternas. |
| 5 | **Verificar con evidencia** | Cultura de ingeniería | "Hecho" = compila + tests verdes + verificado en emulador con captura o log. Sin evidencia, no está hecho. |
| 6 | **Todo cambio es trazable** | Conventional Commits + Keep a Changelog | Cada cambio se explica (el *porqué*, no solo el *qué*) y aparece en el changelog. |
| 7 | **Automatizar lo repetible** | CI/CD | Lo que se hace dos veces a mano se convierte en script o en tarea de CI. |
| 8 | **Medir antes de opinar** | Lean / producto | Las decisiones de producto se apoyan en métricas, no en corazonadas. |
| 9 | **Documentar para el yo-del-futuro** | Práctica personal | Si una sesión nueva (o un agente) no puede retomar el trabajo en 5 minutos leyendo los docs, la documentación ha fallado. |
| 10 | **Celebrar, no culpar** | Diseño de producto | Vale para el producto (gamificación sana) y para el proceso: los fallos se documentan como aprendizaje, no como castigo. |

---

## 2. El ciclo de una fase

```
 ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌───────────┐   ┌────────┐
 │ 1.DEBATIR│ → │ 2.APROBAR│ → │3.EJECUTAR│ → │4.VERIFICAR│ → │5.CERRAR│
 └──────────┘   └──────────┘   └──────────┘   └───────────┘   └────────┘
   decisiones     sin          tareas [x]     criterios de      commit +
   cerradas       decisiones   + commits      aceptación con    changelog +
                  abiertas     pequeños       evidencia         ADR si toca
```

| Paso | Entrada | Salida | Puerta de salida |
|---|---|---|---|
| **1. Debatir** | Fichero de fase en `docs/roadmap/` | Decisiones cerradas (opción + fecha + porqué) | No quedan decisiones abiertas relevantes |
| **2. Aprobar** | Decisiones cerradas | Estado 🔵 Aprobada | Alcance y criterios de aceptación escritos |
| **3. Ejecutar** | Lista de tareas | Commits convencionales + tareas `[x]` | Todas las tareas hechas o recortadas a conciencia |
| **4. Verificar** | Criterios de aceptación | Evidencia (tests, captura de emulador, log) | **Todos** los criterios cumplidos con evidencia |
| **5. Auditar** | Todo lo anterior | Informe de auditoría (código + experiencia) validado | Bloques del [protocolo de auditoría](auditoria-de-cierre.md) en verde |
| **6. Cerrar** | Todo lo anterior | Commit de cierre + changelog + ADR + registro de decisiones | La app compila, tests verdes, doc actualizada |

**Regla de oro:** una fase no se cierra "casi". O cumple sus criterios con evidencia, o sigue abierta.

---

## 3. Artefactos y dónde viven

| Artefacto | Ubicación | Para qué | Cuándo se actualiza |
|---|---|---|---|
| **Visión de producto** | `docs/product-vision-2026.md` | El norte a largo plazo | Cuando cambia la estrategia |
| **Roadmap** | `docs/roadmap/` | El "cuándo": fases, decisiones, criterios | Al debatir/cerrar cada fase |
| **ADRs** | `docs/metodologia/adr/` | Decisiones de arquitectura y producto | Al tomar una decisión cara de revertir |
| **Changelog del producto** | `CHANGELOG.md` | Qué cambió entre versiones, para usuarios | En cada release |
| **Changelog técnico** | `docs/upgrade-2026-changelog.md` | Trazabilidad commit a commit de la modernización | Al cerrar un bloque de trabajo grande |
| **Design system** | `DESIGN.md` | Fuente única de verdad visual | Antes de tocar UI |
| **Guía de agentes** | `AGENTS.md` | Cómo trabajar en este repo | Al cambiar convenciones |
| **Objetivos próximos** | `docs/metodologia/objetivos-2026.md` | El horizonte inmediato, priorizado | Revisión periódica |
| **Memoria de sesión** | `memory/YYYY-MM-DD.md` | Continuidad entre sesiones/agentes | Al final de cada sesión con hallazgos |
| **Auditoría de cierre de fase** | `docs/metodologia/auditoria-de-cierre.md` | Revisión de código y experiencia antes de cerrar una fase | Al cerrar cada fase |

**Regla anti-duplicación:** cada dato vive en **un solo** sitio; el resto lo enlazan.

---

## 4. Definition of Ready / Definition of Done

### Definition of Ready (una tarea puede empezar cuando…)
- [ ] Sabemos **qué** hay que conseguir y **por qué**.
- [ ] Sus decisiones abiertas están cerradas.
- [ ] Cabe en un bloque de trabajo de ≤ 1 sesión (si no, se trocea).
- [ ] Sabemos **cómo se verificará** (test, emulador, métrica).

### Definition of Done (una tarea está terminada cuando…)
- [ ] El código compila (`./gradlew assembleDebug`).
- [ ] Los tests pasan (`./gradlew testDebugUnitTest`).
- [ ] Se ha verificado en emulador si afecta a UI o runtime (con captura/log).
- [ ] Cumple las convenciones de `AGENTS.md` (versiones en catálogo, secretos fuera del repo, M3, `DESIGN.md`).
- [ ] Commit convencional con el **porqué** en el cuerpo.
- [ ] Documentación afectada actualizada (roadmap / ADR / changelog / DESIGN).

---

## 5. Convenciones de commits y versionado

**Conventional Commits**, prefijos permitidos: `feat`, `fix`, `refactor`, `docs`,
`build`, `test`, `perf`, `chore`, `config`.

```
<tipo>(<alcance>): <resumen en imperativo>

<por qué se hace, no solo qué>
<trazabilidad: fase/tarea, ej. "F0/T0.4">
```

**Versionado SemVer** (`MAJOR.MINOR.PATCH`): `MAJOR` = cambio incompatible de datos o
API; `MINOR` = función nueva compatible; `PATCH` = corrección. El `versionCode` sube
siempre (entero monótono).

---

## 6. Rituales de trabajo

| Ritual | Cadencia | Qué se hace |
|---|---|---|
| **Sesión de fase** | Al abrir un bloque | Debatir decisiones abiertas → cerrarlas → aprobar |
| **Check de avance** | Durante la fase | Marcar tareas, commitear pequeño y a menudo |
| **Cierre de fase** | Al terminar un bloque | Verificar criterios con evidencia → commit de cierre → changelog → ADR |
| **Revisión de objetivos** | Semanal | Repriorizar `objetivos-2026.md` según métricas y bloqueos |
| **Mantenimiento de memoria** | Cada pocos días | Destilar `memory/*.md` a `MEMORY.md`; limpiar lo obsoleto |

---

## 7. Métricas

### De proceso (salud del equipo de trabajo)
- Fases cerradas con evidencia vs abiertas.
- Deuda técnica abierta (tareas "deuda" en el roadmap).
- Tests verdes / total; tiempo desde cambio hasta verificación.

### De producto (salud de la app) — se instrumentan en F1/F4
| Métrica | Objetivo | Por qué |
|---|---|---|
| Tiempo hasta el primer registro | < 60 s | Decide el onboarding |
| % usuarios con ≥ 1 registro en 7 días | > 40 % | Mide el abandono |
| Retención D1 / D7 / D30 | D7 > 25 % | Estándar de producto |
| Amigos por usuario | > 1 | Retención social |
| Registros por usuario activo / semana | > 2 | ¿Existe el hábito? |
| Compartidos del Rewind | Creciente | Efecto viral |
| Crash-free rate | > 99,5 % | Umbral de calidad de Play |

Detalle de instrumentación en `docs/metodologia/objetivos-2026.md`.

---

## 8. Cómo trabajan los agentes de IA en este repo

1. **Leer antes de escribir:** `AGENTS.md` → `DESIGN.md` (si toca UI) → la fase del roadmap afectada.
2. **No inventar versiones:** todo pasa por `gradle/libs.versions.toml`.
3. **No tocar secretos:** `local.properties` y `google-services.json` son locales.
4. **Verificar, no afirmar:** ejecutar y comprobar; reportar la evidencia.
5. **Dejar rastro:** actualizar roadmap/changelog/memoria al cerrar un bloque.
6. **Una decisión, un ADR:** si el agente propone una decisión cara de revertir, se escribe como ADR y se aprueba.

---

## 9. Plantillas

- ADR → [`adr/0000-plantilla.md`](adr/0000-plantilla.md)
- Fase del roadmap → [`../roadmap/fase-0-cimientos-datos.md`](../roadmap/fase-0-cimientos-datos.md) (usar como modelo)
- Commit → ver §5
