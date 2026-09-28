# ADR-0004 — Upgrade del toolchain (spike): micro-mejoras hoy, salto de Kotlin bloqueado con disparador

- **ADR:** 0004
- **Título:** Estrategia de actualización del toolchain tras el spike de AGP/Kotlin (28/09/2026)
- **Estado:** Aceptado → **Aplicado (29/09/2026)** vía palancas de transición (ver «Actualización» al final)
- **Fecha:** 2026-09-28
- **Decisores:** agente (spike y propuesta) · propietario (puede revertir los micro-bumps)
- **Fase relacionada:** deuda técnica, Bloque 3

## Contexto

El proyecto tiene **Coil congelado en 3.3.0** y **googleid en 1.1.1** por el límite de metadatos
del compilador: AGP 9.4.1 integra **Kotlin 2.2.10**, que solo lee metadatos hasta la versión 2.2.0.
Objetivo del Bloque 3: subir Kotlin (2.4.x) y desbloquear esas librerías.

## Datos verificados (28/09/2026)

**Maven oficial (consultado hoy):**

| Artefacto | Última estable | Nota |
|---|---|---|
| AGP (`com.android.tools.build:gradle`) | **9.4.1** | ⬅️ ya la usamos; la siguiente es 9.5.0-alpha07 (alpha) |
| Kotlin (`kotlin-gradle-plugin`) | **2.4.20** | 2.5.0-Beta1 en beta |
| KSP | **2.3.12** | compatible con el toolchain actual (probado) |
| Coil (coil3) | **3.6.3** | bloqueado (ver abajo) |
| Hilt | **2.60.1** | compatible (probado) |
| googleid | **1.2.1** | bloqueado (ver abajo) |

**Documentación Android (referencia):** página «AGP, D8, and R8 versions required for Kotlin
versions» (actualizada 06/07/2026) y notas de migración a built-in Kotlin (22/07/2026);
roadmap **AGP 10.0 previsto para finales de 2026**.

## Experimentos ejecutados (locales, revertidos tras medir)

1. **AGP 9.5.0-alpha07, solo:** build + **30/30 tests verdes**. Hallazgo: **no cambia el Kotlin
   integrado** (siguió compilando con el toolchain 2.2.10; no descargó Kotlin nuevo).
2. **AGP alpha + Coil 3.6.3 + googleid 1.2.1 + Hilt 2.60.1 + KSP 2.3.12:** **FALLA** con
   `The binary version of its metadata is 2.4.0, expected version is 2.2.0` → confirma que Coil
   y googleid siguen **bloqueados** mientras el Kotlin integrado sea 2.2.10 (también con la alpha).
3. **AGP 9.4.1 (estable) + Hilt 2.60.1 + KSP 2.3.12** (sin tocar Coil/googleid): **VERDE** —
   build + 30 tests + instrumentados. → **micro-mejoras aplicables hoy**.

## Decisión

1. **Aplicar ya** (implementado en este cambio): **Hilt 2.59.2 → 2.60.1** y **KSP 2.3.6 → 2.3.12**.
2. **No subir AGP a alpha** en el proyecto. El salto grande (Kotlin ≥2.4 + Coil ≥3.6 + googleid ≥1.2)
   se ejecuta **cuando AGP ≥9.5 estable** (o 10.0, previsto fin de 2026) traiga Kotlin nuevo.
3. **Disparador de revisión:** publicación estable de AGP ≥9.5; comprobar en sus notas qué Kotlin
   integra; si ≥2.4, ejecutar el checklist de abajo. (Revisión mínima trimestral aunque no salte el
   disparador.)

## Checklist para el día del disparador

1. Subir AGP en `libs.versions.toml` → compilar.
2. Confirmar el Kotlin integrado (≥2.4) en la salida del build/artefactos.
3. Desbloquear: **Coil → 3.6.x+**, **googleid → 1.2.x+**; subir `kotlinCompose`/`kotlinSerialization`
   a la versión integrada si aplica.
4. Verificación completa: 30 unit + 7 instrumentados + app en emulador (añadir aquí la captura).
5. Actualizar `AGENTS.md` (notas de versiones) y marcar este ADR como **aplicado**.

## Consecuencias

**Positivas**
- Micro-mejoras de DI/compilación aplicadas con evidencia y riesgo mínimo.
- Ruta del salto grande estudiada, medida y documentada (sin meter alpha en producción).

**Negativas / coste asumido**
- Coil sigue en 3.3.0 (sin novedades de la librería) hasta el disparador.
- El salto, cuando llegue, será de varios saltos a la vez → mitigado por el checklist.

**Riesgos y mitigación**
- Que AGP 10 se retrase → revisión trimestral; si una feature depende de Coil nuevo, reevaluar
  (posible experimento con KGP externo o una versión intermedia en una rama aparte).

## Seguimiento

- **Disparador:** AGP ≥9.5 estable con Kotlin ≥2.4 (consultar la tabla oficial AGP↔Kotlin).
- Fuentes consultadas (28/09/2026): metadatos Maven de AGP, Kotlin, KSP, Coil, Hilt y googleid;
  páginas de `developer.android.com` sobre versiones de Kotlin compatibles con AGP/D8/R8 y la
  migración a built-in Kotlin.

---

## Actualización (29/09/2026) — APLICADO vía palancas de transición

El mismo día del spike se encontró y verificó la vía completa:

- **Descubrimiento clave:** además de `android.builtInKotlin=false`, hace falta
  **`android.newDsl=false`** — sin él, el KGP externo falla al aplicar
  (`ApplicationExtensionImpl cannot be cast to BaseExtension`; probado también con el orden
  inverso de plugins y con KGP 2.4.20).
- **Cambios aplicados:** KGP externo **Kotlin 2.4.20** (`org.jetbrains.kotlin.android`),
  plugins compose/serialization 2.4.20, **Coil 3.6.3**, **googleid 1.2.1**,
  `googleCredential.uniqueId` (deprecación resuelta), avisos DEPRECATED_DSL suprimidos con
  `android.sync.suppressAgpWarnings=DEPRECATED_DSL`.
- **Verificación:** 30 tests unitarios + 7 instrumentados en emulador + `assembleRelease`
  firmado con R8 — **todo verde**. Único aviso restante: `Deprecated 'org.jetbrains.kotlin.android'
  plugin usage` (esperado en transición).
- **Deuda de retorno:** ambas palancas se eliminan en AGP 10. Cuando haya un AGP estable con
  Kotlin integrado ≥2.4, ejecutar el retorno a built-in: quitar las 2 palancas + el plugin KGP,
  ajustar versiones y re-verificar (mismo circuito de verificación de arriba).
