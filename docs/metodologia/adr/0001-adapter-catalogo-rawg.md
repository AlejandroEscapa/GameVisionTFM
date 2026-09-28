# ADR-0001 — Adapter de catálogo: RAWG ahora, IGDB preparado

- **ADR:** 0001
- **Título:** Desacoplar el catálogo de juegos tras una interfaz propia, con RAWG como implementación actual
- **Estado:** Aceptado
- **Fecha:** 2026-09-27
- **Decisores:** Alejandro Olivares Escapa (decisión D0.1 del roadmap)
- **Fase relacionada:** F0 (Cimientos de datos)

## Contexto

Toda la app depende del catálogo de RAWG para funcionar. En agosto de 2026 RAWG
sufrió una caída de **1 día y 15 horas** y hay señales de abandono del proyecto
por parte de sus responsables. Si RAWG desaparece, la app se queda sin catálogo
entero. Además, RAWG solo devuelve datos en inglés y no ofrece entidades ricas
(franquicias, estudios, DLC). Al mismo tiempo, IGDB (usado por Twitch/Discord)
ofrece 50+ idiomas y datos relacionales, pero exige OAuth2 vía Twitch y tiene
licencia comercial si se monetiza.

## Decisión

El acceso al catálogo se hace **siempre a través de la interfaz de dominio
`GameCatalog`**. La implementación actual es `RawgGameCatalog`. Cambiar de
proveedor (p. ej. añadir `IgdbGameCatalog`) debe ser **cambiar una línea en el
módulo de inyección de Hilt**, sin tocar UI ni ViewModels.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| Seguir llamando a RAWG directamente desde los ViewModels | Cero trabajo extra | Acoplamiento total; migrar = reescribir UI | Es la deuda que estamos cerrando |
| Migrar ya a IGDB como proveedor principal | Multilingüe, fiable, datos ricos | OAuth2 + 2FA + posible licencia comercial; sin beneficio inmediato | Aplazado hasta que se cumpla una condición de disparo |
| Adapter con RAWG hoy + IGDB preparado | Cero coste hoy, migración barata mañana | Una capa de indirección más | **Elegida** |

## Consecuencias

**Positivas**
- La UI y los ViewModels dejan de conocer los DTOs de RAWG.
- Migrar a IGDB (o a cualquier otro proveedor) deja de ser un proyecto y pasa a ser una tarea de media jornada.

**Negativas / coste asumido**
- Una indirección adicional (interfaz + mapeo) que hay que mantener.

**Riesgos y mitigación**
- Mantener `GameCatalog` demasiado "RAWG-céntrica" haría inútil el adapter → mitigación: el modelo de dominio `CatalogGame` se diseña con campos neutros (no DTOs del proveedor).

## Seguimiento

- **Disparadores para revisar** (cualquiera de ellos activa la migración parcial a IGDB):
  1. RAWG vuelve a caer de forma prolongada o anuncia cierre.
  2. Se quiere mostrar el catálogo en español.
  3. Se necesita algo que RAWG no da (franquicias, estudios, DLC, modos de juego).
  4. Se monetiza y se quiere IGDB como proveedor principal.
- Ver condiciones completas en [`../../roadmap/fase-0-cimientos-datos.md`](../../roadmap/fase-0-cimientos-datos.md).
