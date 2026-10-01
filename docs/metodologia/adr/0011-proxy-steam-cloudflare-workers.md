# ADR-0011 — Proxy de Steam en Cloudflare Workers (en lugar de Cloud Functions)

- **ADR:** 0011
- **Título:** La capa servidor de Steam vive en un Cloudflare Worker, no en Firebase Cloud Functions
- **Estado:** Aceptado
- **Fecha:** 2026-10-01
- **Decisores:** propietario (Alejandro)
- **Fase relacionada:** F3 (se adelanta parte del pack P1 «Steam Inside», decidido en D-AI-1)
- **Afecta a:** [integraciones-apis-2026.md](../../investigacion-2026/integraciones-apis-2026.md) §5 (arquitectura de datos), que daba por hecha la vía Cloud Functions

## Contexto

El pack **P1 «Steam Inside»** (D-AI-1, aceptado el 29/09) exige una capa servidor por un motivo
no negociable: la clave de la Steam Web API **no puede viajar en la app** (ToS de Valve: *no ceder
la clave*), y la Documentación del proyecto resolvía esa capa con **Firebase Cloud Functions**.

Al llegar la implementación (01/10) aparecieron dos hechos:

1. **Cloud Functions requiere el plan Blaze** (pago por uso). El proyecto ya tuvo que descartar
   Firebase Storage en F0 por este motivo (`profile_images` acabó en Firestore).
2. El propietario **no quiere activar Blaze** para una pieza que, en su fase inicial, solo necesita
   esconder una clave y reenviar una respuesta JSON.

## Decisión

**La capa servidor de Steam se implementa como un Cloudflare Worker** (`steam-worker/`), no como
Cloud Function:

- **Capa gratuita de 100.000 peticiones/día**, exactamente el mismo techo que impone la Steam Web
  API — no hay escenario en el que el límite del worker sea el cuello de botella.
- Proporciona **un dominio propio** (`gv-steam.gamevision.workers.dev`), que resuelve de paso la
  **URL de retorno del OpenID 2.0** sin necesidad de comprar un dominio ni publicar `assetlinks.json`
  (se vuelve a la app por esquema propio `gamevision://steam/linked`).
- El código vive en el mismo repositorio (`steam-worker/`) y se despliega con `wrangler deploy`;
  la clave se guarda como **secreto del worker** (`wrangler secret put STEAM_API_KEY`), nunca en el
  repositorio ni en la app.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| A. Cloud Functions (plan Blaze) | Vía ya documentada; todo en el ecosistema Firebase | Exige pago por uso; el propietario no lo quiere para esto | Coste y fricción innecesarios para esconder una clave |
| B. La clave en la app | Cero infraestructura | **Viola los ToS de Valve** y expone la clave | Descartada de raíz |
| C. Endpoint público sin clave (XML de steamcommunity) | Gratis y sin servidor | No oficial, frágil, sin logros ni % global | Se conserva como idea de degradación, pero no como vía principal |
| D. **Cloudflare Worker** (elegida) | Gratis, mismo techo que Steam, da dominio, despliegue en 1 comando | Una pieza fuera de Firebase que mantener | El coste es mínimo y el beneficio, inmediato |

## Consecuencias

**Positivas**
- Steam queda activo **sin coste y sin Blaze**, en la misma sesión en que se decidió.
- El dominio del worker da un retorno de OpenID limpio sin dominio propio.
- La app **nunca** ve la clave; el worker valida además la firma del OpenID contra Steam
  (`check_authentication`), así que un steamid no puede falsificarse desde el cliente.

**Negativas / coste asumido**
- Una pieza de infraestructura **fuera de Firebase**: hay que recordar dónde vive y cómo se
  despliega (documentado en `steam-worker/`).
- Si algún día se necesita caché compartida entre usuarios o lógica más pesada, Cloudflare Workers
  puede no ser suficiente y habría que revisar esta decisión (disparador: **más de ~50k
  peticiones/día** o necesidad de agregación social).
- El dominio `*.workers.dev` es de trabajo, no de marca: si el proyecto usa un dominio propio, la
  URL de retorno debería moverse (disparador: **publicación en Play**).
