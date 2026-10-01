# Acta — Sesión de diseño de 01/10/2026

> **Qué es.** Registro de la jornada en la que GameVision pasó de "provisional" a tener un
> **sistema visual decidido, implementado y verificado**, más el arranque real de Steam.
> Complementa (no sustituye) a las fuentes de verdad: estado en
> [docs/roadmap/](../../docs/roadmap/README.md), decisiones en [docs/metodologia/adr/](../metodologia/adr/).

---

## 1. Decisiones tomadas

| Decisión | Resultado | Registro |
|---|---|---|
| Design system nuevo: ¿cuál y cuándo? | Sistema **Apple** (galería limpia: blanco/pergamino/tile + Action Blue único + Inter), re-anclaje **adelantado** a antes de F3 | [ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md) |
| ¿Se pierde el look anterior? | No: **snapshot** (tag `diseno-provisional-2026-09` + rama + documento con 11 capturas) | [snapshot](snapshot-design-provisional-2026-09.md) |
| Cabecera de pantalla | **Fija**, título como texto simple, vuelta a la izquierda, aire de 20 dp | `DESIGN.md` §7 |
| Escala de espaciado | **`GVSpacing`** (16 de pantalla, 20 de cabecera, 4/8/12/24/32) — no existía | `DESIGN.md` §7 |
| Duración de un juego | **Tu tiempo primero**, HLTB como referencia; el campo manual se retira | `DESIGN.md` §6 |
| Barra de navegación | **Dock flotante** (opción «Galería»), 5 pestañas; Diario y Noticias salen del dock | [barra-y-home-2026.md](barra-y-home-2026.md) |
| Iconografía | **Lucide** en toda la app; fuera `material-icons-extended` | commit `6f3e1de` |
| Steam: ¿sin Blaze? | **Sí**: proxy en **Cloudflare Workers** (gratis, y da dominio para el OpenID) | [ADR-0011](../metodologia/adr/0011-proxy-steam-cloudflare-workers.md) |

## 2. Lo implementado y verificado

**Sistema visual**
- Re-anclaje completo: `GVTheme` (Action Blue único + oscuro derivado de los tiles), `GVTypography`
  (Inter, cuerpo 17, sin peso 500), `GVShapes` (8/11/18 + píldora para la acción), `GVSpacing`.
- `DESIGN.md` **reescrito (v2)** como fuente de verdad del sistema nuevo, con el documento fuente
  versionado en el repo.

**UI, pantalla a pantalla**
- **Cabeceras unificadas y fijas** en las 20 pantallas: Noticias, Para ti, Estadísticas, Ajustes,
  Ficha, Editar perfil, Biblioteca, Timeline… mismo componente, mismo margen, mismo aire.
- **Portadas sin recortes**: placeholder de iniciales centrado y escalado; `Ellipsis` en los títulos
  de juego (sin `overflow`, Compose cortaba los glifos a media letra).
- **Timeline**: el feed arrancaba desplazado por el anclaje de la lista (el listener emite
  caché→servidor y anclaba la tarjeta vieja); ahora re-ancla al llegar un post nuevo.
- **Ficha**: «Tu tiempo» (sesiones) primero, HLTB como referencia, barra jugado/historia; memo en
  memoria para que reabrir una ficha no toque red ni Room.
- **Perfil**: acciones en grid 1×3, biografía con más sección, «Cerrar sesión» al fondo.

**Producto**
- **Home «Para ti»**: Continúa · ¿Qué juego ahora? (motor T3.1 explicado) · Te está esperando ·
  Novedades por géneros · Lo que está pasando · invitación a Steam.
- **Descubrimiento en Buscar**: Populares · Por tus géneros · Sorpresa del día (semilla diaria,
  filtro de nota para no servir relleno).
- **Onboarding** de 3 pasos (bio, localización, **géneros** → `users/{uid}`), saltable, con el flag
  en el perfil y sembrado en las 10 cuentas anteriores.
- **Ajustes v1**: modo noche y privacidad reales, esqueleto honesto con «Próximamente».

**Steam (fase 1, activa)**
- `steam-worker/` desplegado en `https://gv-steam.gamevision.workers.dev` y **verificado en vivo**.
- Vinculación por OpenID 2.0 en Auth Tab (nunca WebView) con pantalla de consentimiento,
  e **import de horas reales** a la biblioteca (máximo local/Steam, sin inventar sesiones).

## 3. Qué NO se hizo (y por qué)

- **Fase 2 de Steam** (logros y % global, «Sigue jugando», arte de SteamGridDB, ofertas de la
  wishlist): son el siguiente tramo, con el proxy ya en pie.
- **Logros de PSN/Xbox**: siguen aparcados por depender de APIs no oficiales.
- **Validación visual del modo oscuro derivado** y **cronometraje de CA1.1**: requieren al
  propietario.

## 4. Pendiente del propietario

1. **Rotar la clave de Steam** (pasó por un chat durante la sesión) y volver a subirla con
   `npx wrangler secret put STEAM_API_KEY --name gv-steam`.
2. Validar el **modo oscuro derivado** y probar el **flujo de Steam** con su cuenta real.
3. Los diez minutos de siempre: cronómetro de CA1.1 y revisar el run de instrumentados en CI.
