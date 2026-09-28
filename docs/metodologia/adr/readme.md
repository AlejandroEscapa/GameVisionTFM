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
