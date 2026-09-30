# Plan: optimización del ciclo verificación → cierre de fase (2026-10)

> **Qué es.** Plan para reducir el tiempo que cuesta cerrar una fase (verificación + auditoría +
> validación) **sin bajar el rigor**. Nace de la sensación del propietario de que «las auditorías y
> fases tardan demasiado» y del análisis con evidencia del 30/09 (commits con hora + diarios de
> sesión).
>
> **Qué NO es.** No relaja el protocolo de [auditoría de cierre](../metodologia/auditoria-de-cierre.md)
> ni la regla «sin evidencia no está hecho». Cambia *quién y cuándo* verifica, no *qué* se verifica.

## Diagnóstico (con evidencia, no sensación)

| Dónde se fue el tiempo | Evidencia | Peso |
|---|---|---|
| E2E en emulador conducido a `input tap` | Diarios 29-30/09: teclado que se come taps, panel de biblioteca que no responde, `uiautomator dump`, login que rompe la automatización | **Alto** — el mayor consumidor |
| Rework por bugs e interrupciones | 9 errores acumulados en F2, bug de pérdida de datos de Editar perfil, `listen()` sin invocar | Medio — no es proceso, es incidente |
| Fases esperando validación del propietario | F2 lista desde 30/09 19:36; sigue 🟡 sin validación | **Alto** — el proyecto nunca queda «cerrado» |
| Framework (docs, decisiones, auditoría) | ~5-6 commits de ~22 en el día de F2 son docs; informe de auditoría = 1 página | Bajo — el framework no es el problema |

Contra-hipótesis descartada: **el emulador no tiene incompatibilidad** (Pixel 9 API 36 funciona); lo
caro es *conducirlo por fuerza bruta*, no ejecutarlo.

## Principio rector

**Todo criterio de aceptación repetible nace como test, no como checklist de emulador.** El E2E
manual en emulador se reserva para (a) la *primera* verificación de un flujo nuevo y (b) lo que es
visual/UX por naturaleza. El emulador deja de ser el instrumento de regresión.

## Bloques

### A — Banco de verificación automatizada (palanca principal)

- **A1. Inventario de CA repetibles.** Recorrer los CA de F0-F2 y clasificar: ya cubiertos por
  test / convertibles a instrumentado / inherentemente visuales (solo E2E manual la primera vez).
- **A2. Helpers de navegación y estado para instrumentados** (`app/src/androidTest`): un mini page-object
  (arrancar app logueada con cuenta QA, navegar a ruta, asertar estado) para que escribir el próximo
  instrumentado cueste minutos, no una tarde de `adb`.
- **A3. Regla para F3 en adelante:** cada tarea que toque UI nueva añade al menos un instrumentado
  o un test de su lógica; el checklist de emulador de la fase solo verifica lo visual la primera vez.
- Evidencia de que funciona: `SocialIntegrationTest` (10/10 contra Firestore real y reglas reales),
  cerrado anoche como deuda 5 de F2.

### B — Auditoría en dos carriles

- **B1. Carril máquina (bloques 1-5 del protocolo).** Script `scripts/auditoria/` que ejecuta y
  pre-rellena el informe: `assembleDebug` + `assembleRelease`, `testDebugUnitTest`, suite de reglas,
  `lintDebug`, y recoge recuentos (tests, warnings, TODOs). La CI ya cubre build+tests+reglas en cada
  push; el script solo consolida la evidencia en el informe.
- **B2. Carril propietario (bloque 6 + firma).** La validación humana se reduce a: recorrer el
  informe pre-rellenado, juzgar la experiencia (bloque 6) y firmar. Objetivo: **10 minutos de lectura**.
- **B3. Validación en caliente.** Acuerdo de trabajo: la fase se valida el mismo día o el siguiente
  de su cierre; una fase no duerme más de 24 h en 🟡. (Ver D-OP1 para el caso en que no haya
  validación en 48 h.)

### C — Entorno y arranque de sesión

- **C1. Script `scripts/dev/` de arranque:** `JAVA_HOME` (JBR de Android Studio), emulador con los
  flags correctos (`-no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect`),
  espera de boot, `adb` listo, e instalación opcional del debug. Meta: de sesión abierta a app
  corriendo sin redescubrir nada.
- **C2. Nota de push dpapi** junto al script (recordatorio del workaround GCM).
- El **handoff** entre máquinas ya funciona (se usó en F2); se queda como está.

### D — Métrica y cierre del plan

- **D1. Métrica por fase:** horas del cierre (última tarea → fase ✅) y % del tiempo de fase que se
  fue a verificación. Baseline F2: ~19 h de fase, cierre consumido por E2E + espera de validación.
- **D2. Meta F3:** verificación automatizada cubre ≥80 % de los CA repetibles; cierre completo
  (código ✅ + auditoría validada) en **< 24 h** desde la última tarea.
- **D3. Revisión:** al cerrar F3 se audita este plan (¿bajó el coste real?) antes de aplicar más.

## Decisiones (cerradas con el propietario el 30/09/2026)

| # | Decisión | Opciones | Cierre |
|---|---|---|---|
| D-OP1 | ¿Fase pasa a ✅ si los bloques 1-5 están verdes y el bloque 6 se validó en el E2E, aunque el propietario no haya firmado en 48 h? | (a) No: siempre espera firma · (b) Sí: ✅ presumido + nota pendiente de ratificación | ✅ **(b)** (30/09): el rigor vive en la evidencia; el propietario ratifica cuando pueda y puede reabrir |
| D-OP2 | ¿CI ejecuta instrumentados con emulador en GitHub Actions? | (a) Sí (AVD en Actions) · (b) No: instrumentados solo en local | ✅ **(a)** (30/09): job añadido; arranca en modo `workflow_dispatch` hasta registrar el SHA-1 del keystore de CI en Firebase (los instrumentados tocan Firestore real) |
| D-OP3 | ¿Dónde viven los helpers de instrumentados? | (a) `app/src/androidTest/helpers/` · (b) módulo aparte | ✅ **(a)** (30/09): paquete `helpers` en androidTest |

## Reglas de ejecución

1. A1-A2 y C1 pueden ejecutarse ya (no tocan producto). B1 requiere D-OP3.
2. La migración de checklists a tests (A1→A3) se hace **por fase**: en F3 se adopta el flujo nuevo
   completo; el backfill de F0-F2 se limita a lo que regrese a menudo.
3. Nada de esto retrasa F3: A y C son medio día en total y se pueden hacer en paralelo al debate de
   decisiones de F3.
