# Plan de Deuda Técnica — GameVision (2026)

> **Creado:** 28/09/2026 · **Estado:** en ejecución (se actualiza al cerrar cada bloque)
> **Método:** inventario completo → priorización (`impacto × riesgo ÷ esfuerzo`) → bloques acotados
> con criterio de cierre y **evidencia verificable**.
> **Contexto:** nace al cerrar el bloque B2 de F0-B (biblioteca nueva). Recoge la deuda encontrada
> durante la sesión y la ya conocida, ordenada para pagarla **antes de arrancar F1**, con las
> piezas que bloquean la publicación marcadas para F5.

---

## 1. Principios de ejecución (criterio senior)

1. **La deuda se paga en ventanas acotadas**, no "en ratos sueltos": cada bloque es una sesión
   (o dos), con criterio de cierre y evidencia (build + tests + verificación en emulador/captura).
2. **Orden por riesgo:** primero lo que protege **datos y accesos** (seguridad), después lo que
   protege la **velocidad de trabajo** (CI e higiene), después el **futuro** (toolchain, release).
3. **Nada de refactors sin disparador:** lo que puede esperar se queda con una **condición
   explícita** de revisión; no hay "algún día".
4. **Todo pasa por el GDF:** Definition of Done, commits convencionales y — si es una decisión
   cara de revertir — ADR.
5. **Cero deuda nueva por prisa:** antes de añadir features (F1), se cierra esta lista; así F1 se
   construye sobre base limpia.

---

## 2. Inventario de deuda

### 2.1 Seguridad — resuelto hoy; residual documentado

| ID | Deuda | Estado |
|---|---|---|
| **D-S1** | Reglas de Firestore desplegadas demasiado abiertas (perfil público sin login; cualquier usuario autenticado podía acceder por comodín a los datos de cualquier otro) | ✅ **Resuelto (28/09)**: reglas nuevas publicadas por el propietario y **verificadas desde la app** (login, lectura y escritura de biblioteca). Los flujos sociales quedaron revisados: el borrado del muro ya estaba guardado para mensajes propios |
| **D-S2** | Verificación **negativa** de las reglas | ✅ **Resuelto (28/09)**: [`firebase-tests/`](../../firebase-tests/README.md) con Emulator Suite — **17/17 OK**, incluidos todos los negativos (acceso cruzado denegado, perfiles protegidos, listas antiguas cerradas) |
| **D-S3** | `docs/firebase-setup.md` desactualizado | ✅ Refrescado (28/09): «Estado actual» arriba e historial marcado |

### 2.2 Cierre de F0-B (paso B3)

| ID | Deuda | Notas |
|---|---|---|
| **D-B3-1** | Datos de prueba antiguos: listas viejas (`playedlist`/`wishlist`/`favorites`/`history`), basura (`aa`, `ee`), cuentas de prueba | ✅ **Ejecutado (29/09)**: backup JSON + borrado (12 subcolecciones + `aa`/`ee`) con verificación ✅ — [`scripts/b3-limpieza/`](../../scripts/b3-limpieza/README.md) |
| **D-B3-2** | Métodos muertos de las listas antiguas en `UserRepository` (sin uso tras B2) | ✅ Eliminados (28/09) |
| **D-B3-3** | Reunificación de ramas: `ui-redesign-2026` vs `master` | ✅ **Resuelto (29/09)**: `ui-redesign-2026` fusionada a `master` por fast-forward (decisión del propietario) |

### 2.3 Código (higiene)

| ID | Deuda | Notas |
|---|---|---|
| **D-C1** | API adaptativa deprecada en `BottomBarNavigation` | ✅ Migrada a la API V2 (`currentWindowAdaptiveInfoV2` + `isWidthAtLeastBreakpoint`) el 28/09 |
| **D-C2** | `DDBBViewModel` es una fachada legacy (nombre "DDBB"; mezcla biblioteca y social) | Renombrar/partir durante F1 (`LibraryViewModel` + `SocialViewModel`) |
| **D-C3** | Botón "Me gusta" del timeline es decorativo (no hace nada) | Implementar reacción en F2 (S16) o ocultar hasta entonces |
| **D-C4** | Avisos de compilación menores | ✅ A cero (28/09); incluidos los tests instrumentados migrados a las APIs v2 de Compose Test |
| **D-C5** | Strings hardcodeados en español (i18n) | **Diferido**: disparador = plantear multiidioma |

### 2.4 Calidad e infraestructura

| ID | Deuda | Notas |
|---|---|---|
| **D-Q1** | Sin CI | ✅ Workflow creado (28/09): 2 jobs — build+tests unitarios y tests de reglas |
| **D-Q2** | Reglas de Firestore sin tests automatizados | ✅ **17/17 OK** en local (28/09); corre también en CI |
| **D-Q3** | Tests instrumentados nunca ejecutados | ✅ Ejecutados (28/09): **7/7 verdes** en el emulador (incl. smoke nuevo) |
| **D-Q4** | Changelog manual | Opcional: autogenerar desde commits convencionales |
| **D-Q5** | Backups de Firestore sin configurar | Se cierra junto a F5 (con alertas de presupuesto y App Check) |

### 2.5 Toolchain

| ID | Deuda | Notas |
|---|---|---|
| **D-T1** | Kotlin 2.2.10 vs 2.4.x; Coil/googleid congelados por el límite de metadatos del Kotlin integrado | 🟡 Spike hecho (28/09): AGP 9.5 sigue alpha y no sube Kotlin; Coil/googleid bloqueados. **Disparador:** AGP ≥9.5 estable con Kotlin ≥2.4 — ver [ADR-0004](../metodologia/adr/0004-upgrade-toolchain.md) |
| **D-T2** | Hilt 2.59.2 vs 2.60.1 | ✅ Subido a 2.60.1 (+ KSP 2.3.12) el 28/09, verificado con build y tests |
| **D-T3** | Nav2 vs Nav3 | **Diferido con disparador**: cuando F4 necesite lista-detalle/tablet |
| **D-T4** | Sin Baseline Profiles | **Diferido**: F4, con runtime estable |

### 2.6 Release

| ID | Deuda | Notas |
|---|---|---|
| **D-R1** | Release sin firmar (falta keystore) | Crear keystore, `signingConfig`, verificar `assembleRelease` + instalación. **Prerrequisito de F5** |

---

## 3. Plan por bloques

### Bloque 0 — Seguridad y cierre de F0-B ✅ (cerrado 29/09/2026)
**Incluye:** D-S1 ✅ (hecho), **B3** (D-B3-1: backup → borrado → usuario nuevo verificado),
decisión de ramas (D-B3-3).
**Criterio de cierre:** datos de prueba fuera; usuario nuevo con flujo completo verificado;
decisión de ramas escrita.
**Evidencia:** consulta de consola + E2E del usuario nuevo.

**Guion B3 (lo ejecuta el propietario — consola/Admin):**
1. **Backup**: Firestore → Backups (crear uno) o `gcloud firestore export`.
2. **Borrar listas antiguas** en cada `users/{email}` antiguo: subcolecciones `playedlist`,
   `wishlist`, `favorites`, `history`; y los documentos basura `aa` y `ee` (raíz de `users`).
3. **Cuentas de prueba**: revisar Authentication (las `qa.*` se pueden borrar).
4. **Usuario nuevo**: registro desde la app → añadir juego → favorito → comprobar pestañas.
5. **Verificación final**: revisar en consola que no quedan `playedlist/wishlist/favorites/history`.

**Ramas (dato para decidir):** `ui-redesign-2026` está **21 commits por delante** de `master` y
**0 por detrás** → la fusión sería limpia (fast-forward). Recomendación: PR de cierre al terminar F0.

**Herramienta lista:** [`scripts/b3-limpieza/`](../../scripts/b3-limpieza/README.md) — dry-run +
`--commit` con backup JSON previo; solo falta que el propietario deje la clave de servicio.

**Resultado (29/09/2026):** B3 **ejecutado** (backup + 12 subcolecciones + `aa`/`ee`, verificación ✅);
ramas **fusionadas** (`master` actualizado por fast-forward, decisión del propietario); E2E con la
cuenta QA ya verificado (28/09). Push/CI pospuesto a petición del propietario (documentado).

### Bloque 1 — Higiene rápida ✅ (28/09/2026)
**Incluye:** D-C1 (API adaptativa V2), D-C4 (avisos), D-B3-2 (código muerto), D-S3 (docs Firebase).
**Resultado:** API adaptativa migrada a `currentWindowAdaptiveInfoV2()` + `isWidthAtLeastBreakpoint`
(window-core 1.5); **avisos de compilación a cero** (incluida la anotación de GoogleViewModel y los
tests instrumentados migrados a las APIs v2 de Compose Test); 4 métodos muertos eliminados de
`UserRepository`; `docs/firebase-setup.md` refrescado.
**Evidencia:** compilación sin warnings + **30 tests unitarios verdes**.

### Bloque 2 — Calidad y CI ✅ (28/09/2026)
**Incluye:** D-Q1 (GitHub Actions), D-Q2 (tests de reglas; cierra D-S2), D-Q3 (smoke instrumentado).
**Resultado:** workflow [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) con 2 jobs
(build+tests unitarios; tests de reglas con Emulator Suite) y dummy de `google-services.json` para CI;
[`firebase-tests/`](../../firebase-tests/README.md) con **17/17 tests de reglas OK** (incluidos los
negativos); tests instrumentados ejecutados por primera vez: **7/7 verdes** (incluido el smoke nuevo
`SmokeTest`). Pendiente menor: ver el primer run real de CI en GitHub al hacer push — **pospuesto por decisión del propietario (29/09); documentado para cuando toque.**

### Bloque 3 — Toolchain Kotlin (+Hilt) 🟡 (spike completado; micro-mejoras aplicadas; salto bloqueado)
**Spike (28/09, 3 builds de prueba):** AGP estable más reciente = 9.4.1 (ya la usamos); 9.5 sigue en
alpha y **no sube el Kotlin integrado**; Coil 3.6.3/googleid 1.2.1 fallan con metadatos 2.4.0 vs
2.2.0 esperado → **salto bloqueado aguas arriba**. Micro-mejoras válidas aplicadas: **Hilt 2.60.1** y
**KSP 2.3.12** (build + 30 tests + instrumentados). Decisión y checklist completos:
[ADR-0004](../metodologia/adr/0004-upgrade-toolchain.md).
**Disparador:** AGP ≥9.5 estable con Kotlin ≥2.4 (AGP 10.0 previsto fin de 2026) → ejecutar checklist del ADR.

### Bloque 4 — Release y firma (≈media sesión)
**Incluye:** D-R1.
**Criterio de cierre:** `assembleRelease` firmado instalado y arrancado en emulador; keystore
fuera del repo (local.properties / gestor de secretos).
**Evidencia:** artefacto + captura.

### Diferidos con disparador (no se tocan ahora)
| Deuda | Disparador |
|---|---|
| Nav3 (D-T3) | F4: lista-detalle / tablet |
| Baseline Profiles (D-T4) | F4: runtime estable |
| App Check, alertas de presupuesto, backups (D-Q5) | Preparación de F5 |
| "Me gusta" (D-C3) | F2 (reacciones) |
| i18n (D-C5) | Decisión de multiidioma |
| Auditoría de costes (investigación F0-B §6) | Acercarse a ~1.000 usuarios activos |

---

## 4. Fuera de alcance (es roadmap, no deuda)

T0.12 (foto de perfil en Storage), T0.13 (FCM), T0.14 (persistencia offline), import/export y todo
F1+ son **trabajo planificado** en el [roadmap](../roadmap/README.md). No se mezclan aquí para no
inflar la mochila: deuda = riesgo acumulado; roadmap = producto pendiente.

---

## 5. Registro

- **28/09/2026** — Plan creado. D-S1 resuelta y verificada (positivos). Bloque 0 en curso.
  Pendiente inmediato: B3 (limpieza) y decisión de ramas.
- **28/09/2026 (noche)** — **Bloque 1 completado** (API adaptativa V2 · avisos a cero · código muerto
  fuera · docs Firebase al día) y **Bloque 2 completado** (CI con 2 jobs · **reglas 17/17** ·
  **instrumentados 7/7**). Bloque 0: guion B3 listo; ramas listas para fast-forward. Único pendiente
  del tramo: ejecutar B3 (propietario) y ver el primer run de CI al hacer push.
- **29/09/2026 (madrugada)** — **B3 ejecutado** (backup JSON + limpieza de 12 subcolecciones y
  `aa`/`ee`, verificación ✅). **`master` actualizado** por fast-forward (decisión del propietario).
  Push/CI pospuesto a petición del propietario. **Bloque 0 cerrado.**
