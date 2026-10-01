/**
 * GameVision Steam Proxy — Cloudflare Worker (bloque 5, iteración 02/10).
 *
 * Por qué existe: la clave de la Steam Web API NO puede vivir en la app
 * (ToS de Valve: no ceder la clave) y la app no puede llamar a Steam
 * directamente con ella. Este worker la guarda como SECRETO y expone solo
 * los endpoints que GameVision necesita.
 *
 * Endpoints:
 *   GET /steam/owned?steamid=7656119...  → biblioteca + horas (GetOwnedGames)
 *   GET /steam/summary?steamid=...       → resumen de un juego (achievement %) [fase 2]
 *   GET /steam/success?openid.claimed_id=... → puente del OpenID: verifica contra
 *       Steam y redirige a la app por esquema propio (gamevision://steam/linked?steamid=...)
 *
 * Seguridad:
 *   - La clave vive en el secreto STEAM_API_KEY del worker (wrangler secret put).
 *   - CORS restringido al esquema de la app no aplica (la app llama sin CORS);
 *     el navegador NO debe llamar aquí. Endpoint de success solo acepta GET.
 *
 * Despliegue (propietario, ~5 min):
 *   npm install -g wrangler
 *   wrangler login
 *   wrangler secret put STEAM_API_KEY      (pegar la clave)
 *   wrangler deploy                        → anota la URL https://gv-steam.<cuenta>.workers.dev
 */

const STEAM_OPENID_ENDPOINT = "https://steamcommunity.com/openid/login";
const APP_SCHEME = "gamevision"; // esquema de retorno a la app (AndroidManifest)

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    try {
      if (url.pathname === "/steam/owned") {
        return await ownedGames(url, env);
      }
      if (url.pathname === "/steam/success") {
        return await openidReturn(url, env);
      }
      return json({ error: "not_found" }, 404);
    } catch (e) {
      return json({ error: "worker_error", detail: String(e) }, 500);
    }
  },
};

/** GET /steam/owned?steamid=... — biblioteca y horas del jugador. */
async function ownedGames(url, env) {
  const steamid = url.searchParams.get("steamid");
  if (!steamid || !/^\d{17}$/.test(steamid)) {
    return json({ error: "invalid_steamid" }, 400);
  }
  const params = new URLSearchParams({
    key: env.STEAM_API_KEY,
    steamid,
    include_appinfo: "1",
    include_played_free_games: "1",
  });
  const resp = await fetch(
    `https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?${params}`
  );
  if (!resp.ok) {
    return json({ error: "steam_error", status: resp.status }, 502);
  }
  const data = await resp.json();
  const games = (data.response?.games ?? []).map((g) => ({
    appid: g.appid,
    name: g.name ?? "",
    playtimeForever: g.playtime_forever ?? 0, // minutos totales
    playtime2weeks: g.playtime_2weeks ?? 0,   // minutos en las últimas 2 semanas
    imgIconUrl: g.img_icon_url ?? "",
    rtimeLastPlayed: g.rtime_last_played ?? 0,
  }));
  return json({
    steamId: steamid,
    totalGames: data.response?.game_count ?? games.length,
    games,
  });
}

/**
 * GET /steam/success?openid.claimed_id=https://steamcommunity.com/openid/id/<steamid>&...
 * Puente del OpenID 2.0: Steam vuelve aquí tras el login del usuario.
 * Verificamos la firma contra Steam (check_authentication) y redirigimos
 * a la app con el steamid en el esquema propio.
 */
async function openidReturn(url, env) {
  const claimedId = url.searchParams.get("openid.claimed_id");
  if (!claimedId) {
    return html("Falta openid.claimed_id", 400);
  }
  // Verificación de la firma: reenviamos TODOS los parámetros openid.* a
  // Steam con check_authentication. Sin esto, cualquiera podría fingir un steamid.
  const verifyParams = new URLSearchParams();
  for (const [k, v] of url.searchParams.entries()) {
    if (k.startsWith("openid.")) verifyParams.set(k, v);
  }
  verifyParams.set("openid.mode", "check_authentication");
  const verifyResp = await fetch(STEAM_OPENID_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: verifyParams.toString(),
  });
  const verifyText = await verifyResp.text();
  const valido = verifyResp.ok && verifyText.includes("is_valid:true");
  if (!valido) {
    return html("No se pudo verificar la identidad de Steam.", 403);
  }
  const m = claimedId.match(/\/id\/(\d{17})$/);
  if (!m) {
    return html("steamid no reconocido", 400);
  }
  // Éxito: de vuelta a la app por esquema propio (no necesita assetlinks).
  return html(
    `<p>Steam verificado. Volviendo a GameVision…</p>` +
      `<script>location.replace("${APP_SCHEME}://steam/linked?steamid=${m[1]}")</script>` +
      `<p><a href="${APP_SCHEME}://steam/linked?steamid=${m[1]}">Si no vuelve sola, toca aquí.</a></p>`
  );
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8" },
  });
}

function html(body, status = 200) {
  return new Response(
    `<!doctype html><html><head><meta charset="utf-8"><title>GameVision</title></head>` +
      `<body style="font-family:sans-serif;text-align:center;padding-top:3rem">` +
      body +
      `</body></html>`,
    { status, headers: { "Content-Type": "text/html; charset=utf-8" } }
  );
}
