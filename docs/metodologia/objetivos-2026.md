# Objetivos GameVision 2026 — horizonte inmediato

> Documento vivo. Se revisa cada semana y cuando cambian las métricas o aparecen
> bloqueos. Responde a: **¿cuál es el siguiente resultado que mueve la aguja?**
>
> Principio rector: **una sola cosa a la vez por prioridad**, y todo objetivo tiene
> un criterio de éxito verificable.

---

## Norte (objetivo de la etapa)

> Convertir GameVision en una **app Android publicada en Google Play**, con
> funcionalidad real, monetización sostenible y una base de usuarios que vuelve.

Se apoya en tres pilares de producto (ver [`../product-vision-2026.md`](../product-vision-2026.md)):
**biblioteca rica**, **decisión ("¿qué juego ahora?")** y **celebración compartible (Rewind)**.

---

## Objetivos por horizonte

### ✅ Cerrado (F0 — Cimientos de datos, 29/09/2026)
| Objetivo | Criterio de éxito | Estado |
|---|---|---|
| Cerrar el adapter de catálogo | `GameCatalog` + `RawgGameCatalog` commiteados, UI sin DTOs de RAWG | ✅ Hecho |
| Caché local de catálogo (Room) | App muestra fichas visitadas sin red (verificado en modo avión) | ✅ Hecha |
| Modelo de biblioteca | `library`/`logs`/`sessions` + repositorio y adopción en pantallas | ✅ Hecho (B1/B2, E2E) |
| Limpieza de datos (B3) | Datos de prueba borrados (verificación ✅) y E2E con cuenta QA verificado | ✅ Hecho |
| Foto de perfil (sin Storage) | La imagen viaja entre dispositivos | ✅ Hecho (ADR-0007, CA0.3 verificado en vivo) |
| Offline real | La app abre y funciona sin conexión con lo visitado | ✅ CA0.1 y CA0.2 verificados en vivo |
| Deuda técnica priorizada | [Plan de deuda](../plan/DEUDA-TECNICA-2026.md) ejecutado por bloques con evidencia | ✅ Bloques 0–4 cerrados; quedan diferidos con disparador |
| Auditoría de cierre de F0 | Informe validado por el propietario | ✅ [Validada 29/09](../metodologia/auditoria-fase-0-2026.md) |

### 🔴 Ahora (F1 — El corazón del tracker, en ejecución)
| Bloque | Contenido | Estado |
|---|---|---|
| 1 · Biblioteca rica | 7 estados, nota, reseña, favorito | ✅ Hecho (29/09; T1.1–T1.4, T1.7) |
| 2b · Duración HLTB | Scraper HLTB con caché + valor manual | ✅ Hecho (29/09; T1.11) |
| 2 · Diario y tiempo | Partidas, diario, horas | ✅ Hecho (29/09) |
| 3 · Estadísticas | Pantalla de stats + «Tu año en un vistazo» | ✅ Hecho (29/09; T1.12/T1.13) |
| 4 · Navegación + cierre | Filtros, instrumentación, auditoría de F1 | ⬜ Pendiente |
| 5 · Migración `uid` | [ADR-0008](adr/0008-clave-unica-uid.md) (antes de F2) | ⬜ Pendiente |

Estados ricos, valoración con estrellas, reseñas, diario con sesiones, múltiples
partidas, duración del juego y estadísticas de perfil. **Decisiones D1.1–D1.7 cerradas**.
**Antes de F2:** ejecutar la migración de identidad a `uid` ([ADR-0008](adr/0008-clave-unica-uid.md), aprobado).

### 🟠 Siguiente inmediato (tras arrancar F1)
- **Instrumentación** (Analytics + Crashlytics) al cerrar F1 — mide el embudo real (tiempo hasta el primer registro, retención).
- **Migración de identidad a `uid`** ([ADR-0008](adr/0008-clave-unica-uid.md)) — obligatoria **antes de F2**.
- **Abrir F5 en borrador** (cuenta de desarrollador, privacidad, Data Safety) en paralelo.

### 🟡 Después (F2 Social · F3 Wow · F4 Nativo · F4.5 Diseño y animaciones)
Perfil público y buscar por username · "¿Qué juego ahora?" + Rewind · widget y
notificaciones.

### 🟢 Hito de publicación (transversal a F1–F4)
Preparar el **lanzamiento en Google Play**: cuenta de desarrollador, ficha de
tienda, formulario de seguridad de datos, política de privacidad, pruebas
cerradas obligatorias, integración de anuncios y entrega escalonada.

---

## Decisiones que hay que cerrar (y cuándo)

| Decisión | Bloquea | Estado |
|---|---|---|
| Fuente de la duración de los juegos (HLTB / manual / IGDB) | T1.11 (F1) | ✅ Cerrada (29/09): scraper HLTB + respaldo manual — ver [fase-1](../roadmap/fase-1-corazon-tracker.md) D1.4 |
| Integración parcial con IGDB | — | Marcada para el futuro (ver ADR-0001) |
| Modelo social: amigos vs seguir | F2 | Diferida a F2 |
| Modelo de monetización (anuncios + desbloqueo premium) | Hito de publicación | ✅ [ADR-0005](adr/0005-monetizacion-por-etapas.md): por etapas con disparadores (29/09) |
| Cuenta de Google Play (personal vs organización) | Hito de publicación | ✅ [ADR-0006](adr/0006-cuenta-play-personal.md): personal (29/09) |

---

## Cómo se mide el éxito

Los KPIs de producto y proceso están definidos en
[`README.md` §7](README.md#7-métricas). La instrumentación (Firebase Analytics,
Crashlytics) se planifica en F1 y se completa antes del lanzamiento.

---

## Riesgos vivos

| Riesgo | Gravedad | Estado de mitigación |
|---|---|---|
| Dependencia de RAWG | 🔴 Crítica | Adapter en curso (F0) |
| Sin dispositivo/emulador para validar runtime | 🟠 Alta | **Emulador `Pixel_9` (API 36) operativo** ✅ |
| Secretos fuera del repo (Firebase, API keys, keystore) | 🟠 Alta | `local.properties` + `google-services.json` locales |
| Release sin firmar | 🟢 Resuelto | Keystore creado y release firmado verificado (29/09); pendiente: backup de credenciales + SHA-1 en Firebase |
