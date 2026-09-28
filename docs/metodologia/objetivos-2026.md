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

### 🔴 Ahora (cierre de F0 — Cimientos de datos)
| Objetivo | Criterio de éxito | Estado |
|---|---|---|
| Cerrar el adapter de catálogo | `GameCatalog` + `RawgGameCatalog` commiteados, UI sin DTOs de RAWG | ✅ Hecho |
| Caché local de catálogo (Room) | App muestra fichas visitadas sin red (verificado en modo avión) | ✅ Hecha |
| Modelo de biblioteca | `library`/`logs`/`sessions` + repositorio y adopción en pantallas | ✅ Hecho (B1/B2, E2E) |
| Limpieza de datos (B3) | Datos de prueba borrados y usuario nuevo verificado end-to-end | ⬜ |
| Foto de perfil en Storage | La imagen viaja entre dispositivos | ⬜ |
| Offline real | La app abre y funciona sin conexión con lo visitado | ⬜ |
| Deuda técnica priorizada | [Plan de deuda](../plan/DEUDA-TECNICA-2026.md) ejecutado por bloques con evidencia | 🟢 En curso (Bloques 1–2 hechos; B3 pendiente del propietario) |

### 🟠 Siguiente (cierre de F1 — El corazón del tracker)
Estados ricos, valoración con estrellas, reseñas, diario con sesiones, múltiples
partidas, duración del juego y estadísticas de perfil. **Bloqueo activo:** decidir
la fuente de la duración (bloquea T1.11).

### 🟡 Después (F2 Social · F3 Wow · F4 Nativo)
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
| Fuente de la duración de los juegos (HLTB / manual / IGDB) | T1.11 (F1) | ⬜ Pendiente del propietario |
| Integración parcial con IGDB | — | Marcada para el futuro (ver ADR-0001) |
| Modelo social: amigos vs seguir | F2 | Diferida a F2 |
| Modelo de monetización (anuncios + desbloqueo premium) | Hito de publicación | ⬜ A concretar |

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
| Release sin firmar | 🟡 Media | Keystore pendiente de crear en `local.properties` |
