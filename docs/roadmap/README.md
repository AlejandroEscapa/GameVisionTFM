# Roadmap GameVision — cómo se trackea y se debate

> Sistema de trabajo por fases para llevar GameVision al nivel descrito en
> [product-vision-2026.md](../product-vision-2026.md).
> Objetivo de esta carpeta: que **cada fase se pueda debatir antes de empezar,
> seguir mientras se ejecuta y auditar cuando termina**.

## Cómo se usa (flujo de una fase)

```
1. DEBATIR   → abrir el fichero de la fase y revisar "Decisiones abiertas"
               Cada decisión se discute y se cierra con una opción + fecha.
2. APROBAR   → la fase pasa de ⬜ Pendiente a 🔵 Aprobada cuando no quedan
               decisiones abiertas sin cerrar.
3. EJECUTAR  → se marcan las tareas ([x]) y el estado pasa a 🟢 En ejecución.
4. VERIFICAR → se cumplen los "Criterios de aceptación" (con evidencia:
               tests, emulador, capturas). Sin evidencia no se marca hecho.
5. CERRAR    → estado ✅ Completada + commit de la fase + actualización del
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
| [F0](fase-0-cimientos-datos.md) | Cimientos de datos | 1 semana | 🟢 En ejecución | — |
| [F1](fase-1-corazon-tracker.md) | El corazón del tracker | 2 semanas | ⬜ Pendiente | F0 |
| [F2](fase-2-social.md) | Social | 1 semana | ⬜ Pendiente | F1 |
| [F3](fase-3-wow.md) | El "wow": decidir y celebrar | 1 semana | ⬜ Pendiente | F1 |
| [F4](fase-4-nativo.md) | Nativo y pulido | 1 semana | ⬜ Pendiente | F1 |

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
| **Fuente de la duración de los juegos** (scraper de HowLongToBeat, dato manual, IGDB…) | **Antes de T1.11 (F1)** | Pendiente del propietario |
| Integración parcial con IGDB | Cuando se cumpla una condición de disparo (ver F0) | Marcada para el futuro |
| Modelo social: amigos vs seguir | Al empezar F2 | Diferida a F2 |

## Riesgo transversal (vigila todas las fases)

🔴 **Dependencia de RAWG.** Documentado en la visión: caída de 1 d 15 h en agosto 2026
y señales de abandono. Mitigación asignada a **F0** (adapter + caché + vía a IGDB).
Cualquier fase puede verse afectada si RAWG desaparece antes de terminar F0.
