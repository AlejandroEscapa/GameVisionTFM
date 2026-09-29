# Architecture Decision Records (ADR)

> Registro cronológico de las decisiones de GameVision que son **caras de
> revertir**. Cada ADR explica el contexto, la decisión, las alternativas y las
> consecuencias. Un ADR no se edita en su decisión: si cambia, se crea uno nuevo
> que lo supersede.

## Índice

| ADR | Título | Estado | Fecha |
|---|---|---|---|
| [0000](0000-plantilla.md) | Plantilla | — | — |
| [0001](0001-adapter-catalogo-rawg.md) | Adapter de catálogo: RAWG ahora, IGDB preparado | Aceptado | 2026-09-27 |
| [0002](0002-escala-valoracion.md) | Escala de valoración: 1–10 entero | Superseded por [0003](0003-escala-medias-estrellas.md) | 2026-09-28 |
| [0003](0003-escala-medias-estrellas.md) | Escala de valoración: 0,5–5,0 con medias estrellas | Aceptado | 2026-09-28 |
| [0004](0004-upgrade-toolchain.md) | Upgrade del toolchain: micro-mejoras hoy, salto Kotlin bloqueado (con disparador) | Aceptado | 2026-09-28 |
| [0005](0005-monetizacion-por-etapas.md) | Monetización por etapas con disparadores medibles | Aceptado | 2026-09-29 |
| [0006](0006-cuenta-play-personal.md) | Cuenta de desarrollador de Google Play: personal | Aceptado | 2026-09-29 |
| [0007](0007-foto-perfil-firestore.md) | Foto de perfil: base64 comprimido en Firestore (sin Cloud Storage) | Aceptado | 2026-09-29 |
| [0008](0008-clave-unica-uid.md) | Clave única de identidad: `uid` en lugar de email | **Aceptado** | 2026-09-29 |

## Cuándo crear un ADR

Crea un ADR cuando la decisión:
- Afecta a la **arquitectura** (capas, dependencias, patrón de datos).
- Afecta al **proveedor o servicio** (API, backend, SDK de terceros).
- Afecta a la **monetización o la privacidad** (recolecta datos, muestra anuncios).
- Sería **costosa de deshacer** después (migración de datos, cambio de modelo).
- El equipo (o un agente futuro) podría volver a debatir sin ver el porqué.

## Cuándo NO hace falta

- Cambios de estilo o de copy.
- Subir una versión de librería dentro de las reglas de `AGENTS.md`.
- Correcciones de bugs.

En esos casos basta el commit convencional + el changelog.
