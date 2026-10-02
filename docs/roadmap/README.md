# Roadmap GameVision — cómo se trackea y se debate

> Sistema de trabajo por fases para llevar GameVision al nivel descrito en
> [product-vision-2026.md](../product-vision-2026.md).
> Objetivo de esta carpeta: que **cada fase se pueda debatir antes de empezar,
> seguir mientras se ejecuta y auditar cuando termina**.

> **Contexto global:** las 8 vertientes, la estrategia, la revisión de la filosofía del
> negocio y las fases F0-F6 están en el [Plan Maestro 2026](../plan/PLAN-MAESTRO-2026.md).

## Cómo se usa (flujo de una fase)

```
1. DEBATIR   → abrir el fichero de la fase y revisar "Decisiones abiertas"
               Cada decisión se discute y se cierra con una opción + fecha.
2. APROBAR   → la fase pasa de ⬜ Pendiente a 🔵 Aprobada cuando no quedan
               decisiones abiertas sin cerrar.
3. EJECUTAR  → se marcan las tareas ([x]) y el estado pasa a 🟢 En ejecución.
4. VERIFICAR → se cumplen los "Criterios de aceptación" (con evidencia:
               tests, emulador, capturas). Sin evidencia no se marca hecho.
5. AUDITAR   → se ejecuta la "Auditoría de cierre de fase" (código + experiencia)
               según el protocolo de docs/metodologia/auditoria-de-cierre.md.
               Sin auditoría validada, la fase no cierra.
6. CERRAR    → estado ✅ Completada + commit de la fase + actualización del
               "Registro de decisiones" con lo que se aprendió.
```

## Leyenda de estados

| Símbolo | Estado | Significado |
|---|---|---|
| ⬜ | Pendiente | Nada iniciado; sus decisiones abiertas sin debatir |
| 🟡 | En debate | Se están discutiendo las decisiones abiertas |
| 🔵 | Aprobada | Decisiones cerradas, lista para ejecutar |
| 🟢 | En ejecución | Tareas en marcha |
| ✅ | Completada | Criterios de aceptación cumplidos con evidencia |
| ⛔ | Bloqueada | Depende de algo externo (dispositivo, credenciales, decisión del propietario) |

## Estado global

| Fase | Título | Estimación | Estado | Depende de |
|---|---|---|---|---|
| [F0](fase-0-cimientos-datos.md) | Cimientos de datos | 1 semana | ✅ Completada (29/09) | — |
| [F1](fase-1-corazon-tracker.md) | El corazón del tracker | 2 semanas | ✅ Completada (29/09) | F0 ✅ |
| [F2](fase-2-social.md) | Social | 1 semana | ✅ Completada (01/10) — auditoría validada por el propietario | F1 |
| [F3](fase-3-wow.md) | El "wow": decidir y celebrar | 1 semana | 🟢 En ejecución — **T3.1–T3.3 y T3.6–T3.11 hechas** (Rewind completo: cálculo, pantalla, imagen compartible, compartir nativo, historia por juego y aviso de fin de año con D3.7 cerrada); quedan T3.4, T3.5 | F1 |
| [F4](fase-4-nativo.md) | Nativo y pulido | 1 semana | ⬜ Pendiente | F1 |
| [F4.5](fase-4-5-diseno-animaciones.md) | Diseño, animaciones y auditoría de experiencia | ~1 semana | 🔵 Aprobada (02/10) — **DX.1–DX.8 cerradas** ([ADR-0013](../metodologia/adr/0013-el-contrato-visual-manda.md)); el re-anclaje ya está hecho (ADR-0010) y la auditoría existe; quedan motion, accesibilidad y el barrido de adopción | F1–F4 |

> **Dónde vive el estado.** Esta tabla es un **índice**, no la fuente de verdad. El estado real de
> una fase lo declara su fichero (`fase-N-*.md`, apartado "Progreso por bloques"). Si esta tabla y
> el fichero discrepan, **manda el fichero** y corrige la tabla. No repitas el detalle de bloques
> aquí: ya se desactualizó una vez y afirmaba "Bloque 1 hecho" cuando F1 estaba cerrada.

**Reglas de ejecución (no negociables):**
1. Una fase **no empieza** hasta que sus decisiones abiertas estén cerradas.
2. Cada fase termina con la app **compilando**, **tests en verde** y **verificada en
   emulador** con evidencia (captura o log), como se hizo en las fases anteriores.
3. Commits por fase con mensaje descriptivo; **no se fusiona a `main`** sin decisión
   explícita del propietario.
4. Si aparece una decisión nueva durante la ejecución, se añade a la fase **antes**
   de improvisar: primero se debate, luego se toca el código.
5. Todo lo aprendido se anota en el "Registro de decisiones" de la fase (para que la
   siguiente sesión no vuelva a debatir lo mismo).

## Decisiones pendientes (marcadas, no bloquean lo actual)

| Decisión | Cuándo hay que cerrarla | Estado |
|---|---|---|
| **Fuente de la duración de los juegos** (scraper de HowLongToBeat, dato manual, IGDB…) | **Antes de T1.11 (F1)** | ✅ Cerrada (29/09): scraper HLTB + respaldo manual — ver D1.4 en [fase-1](fase-1-corazon-tracker.md) |
| **Modelo de monetización** (gratis + premium barato + micromecenazgo) | Antes de F5 (Publicación) | ✅ Cerrada (29/09): monetización por etapas con disparadores — [ADR-0005](../metodologia/adr/0005-monetizacion-por-etapas.md) |
| **Tipo de cuenta de Play** (personal vs organización) | Al preparar F5 | ✅ Cerrada (29/09): cuenta personal — [ADR-0006](../metodologia/adr/0006-cuenta-play-personal.md) |
| **Actualización de Kotlin** (2.2.10 → 2.4.x) | Antes de la release / cuando el tooling lo exija | Diferida: "cuando sea oportuno" |
| Integración parcial con IGDB | Cuando se cumpla una condición de disparo (ver F0) | Marcada para el futuro |
| Modelo social: amigos vs seguir | Al empezar F2 | ✅ Cerrada (30/09): seguir asimétrico (D2.1, [fase 2](fase-2-social.md)) |
| **Alcance del acento** (¿Action Blue para acción *y* dato?) | Antes de seguir tocando UI | ✅ Cerrada (02/10): **solo para lo que se toca** — los datos se jerarquizan por tamaño y peso ([ADR-0012](../metodologia/adr/0012-alcance-del-acento.md)) |
| **¿Re-anclar el design system en F4.5 o antes?** | Al recibir la fuente | ✅ Cerrada (01/10): se **adelanta** a antes de F3 ([ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md)); el re-anclaje ya está ejecutado |

## Riesgo transversal (vigila todas las fases)

🔴 **Dependencia de RAWG.** Documentado en la visión: caída de 1 d 15 h en agosto 2026
y señales de abandono. Mitigación asignada a **F0** (adapter + caché + vía a IGDB).
Cualquier fase puede verse afectada si RAWG desaparece antes de terminar F0.
