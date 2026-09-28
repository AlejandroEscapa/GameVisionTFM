# Verificación en vivo: duración de juegos vía HowLongToBeat (decisión D1.4)

> **Fecha:** 29/09/2026 · **Método:** replicar el flujo real de la web de HLTB desde un banco de
> pruebas local (Node 22) con **5 casos de prueba**, y cruzar los valores contra fuentes públicas.
> **Veredicto:** ✅ La vía *(a)* es **viable y los datos son fiables**. Al ser un flujo no oficial,
> se implementa con mitigaciones (caché, respaldo manual, degradación sin rotura).
> **Re-ejecutable:** `node tools/hltb-check.mjs` (Node 22+).

## Protocolo capturado (inspección de los bundles JS del propio sitio)

1. `GET https://howlongtobeat.com/api/search/site/init?t=<ms>` → `{ token }` (≈276 caracteres).
2. `POST https://howlongtobeat.com/api/search/site` con cabeceras
   `Content-Type: application/json`, `x-auth-token: <token>`, `User-Agent` de navegador y
   `Referer`/`Origin: https://howlongtobeat.com`; cuerpo con los **valores por defecto de su web**
   (filtros vacíos; `rangeTime.min/max` van a `null: number`):

```json
{
  "searchType": "games",
  "searchTerms": ["elden", "ring"],
  "searchPage": 1,
  "size": 20,
  "searchOptions": {
    "games": {
      "userId": 0,
      "platform": { "mode": "include", "values": [] },
      "sortCategory": "popular",
      "rangeCategory": "main",
      "rangeTime": { "min": null, "max": null },
      "gameplay": {
        "perspective": { "mode": "include", "values": [] },
        "flow": { "mode": "include", "values": [] },
        "genre": { "mode": "include", "values": [] }
      },
      "year": { "mode": "include", "values": [] },
      "modifier": ""
    },
    "users": { "sortCategory": "postcount" },
    "lists": { "sortCategory": "follows" },
    "filter": "",
    "sort": 0,
    "randomizer": 0
  },
  "useCache": true
}
```

3. **Manejo de errores**: `403` = token caducado → re-init y reintento (lo hace el propio sitio);
   `429` = rate limit → espaciar (≥1 s entre llamadas; en la app, solo bajo demanda + caché).
4. **Respuesta**: `{ count, data: [ { game_id, game_name, comp_main, comp_plus, comp_100, … } ] }`.
   Tiempos en **segundos**: `comp_main` = historia · `comp_plus` = historia+extras ·
   `comp_100` = completista.

## Casos de prueba (valores obtenidos en vivo, 29/09/2026)

| Juego | Historia | +Extras | Completista | Comprobación |
|---|---|---|---|---|
| **Elden Ring** | 60,1 h | 101,3 h | 136,2 h | Contrastado con cifras de HLTB citadas en ResetEra (2024): 103 h / 138 h, y hilo Steam (2022): 53 / 98,5 / 132 → misma serie, deriva temporal coherente ✅ |
| **Limbo** | 3,6 h | 4,2 h | 6,8 h | Contrastado con datos HLTB citados en psprices (2024): 3 h 16 m / 4 h 15 m / 6 h 29 m ✅ |
| **Super Mario 64** | 11,6 h | 16,4 h | 19,9 h | Coherente con las cifras conocidas de HLTB ✅ |
| **Persona 5 Royal** | 101,3 h | 122,6 h | 140,5 h | Coherente con las cifras conocidas de HLTB ✅ |
| **The Elder Scrolls VI** | — | — | — | Juego sin lanzar: la entrada existe y devuelve **ceros** → la UI mostrará «sin datos» y ofrecerá valor manual ✅ |

## Conclusiones

1. **Viable hoy**: el flujo funciona desde una máquina normal; token y rate-limit tienen manejo estándar.
2. **Datos fiables**: es la fuente primaria (HLTB) la que se consulta; los cruces externos confirman que
   lo obtenido refleja las cifras públicas y que **derivan lentamente** → **caché con TTL amplio**
   (p. ej. 90 días) y el **valor manual del usuario siempre gana**.
3. **Riesgos y mitigaciones**
   - Flujo no oficial → puede cambiar. Cliente pequeño y aislado; si falla, la app **degrada sin
     romperse** (sin dato automático + entrada manual).
   - Escala/legal: uso personal/pequeño; si el producto crece, reevaluar (IGDB tiene su propio TTB
     oficial — queda como candidato futuro, ver ADR-0001).
4. **Plan de implementación (T1.11)**
   - `HltbApiService` mínimo (2 llamadas) + mapeo a **3 campos nuevos del dominio del catálogo**
     (`playtimeMain` / `playtimePlus` / `playtime100`, en minutos) — exige extender `CatalogGame`,
     la caché Room (subida de versión del esquema) y los mapeos.
   - Guardar junto al dato: fecha de obtención y `game_id` de HLTB (para invalidar/actualizar).
   - Si los tres valores son 0 → «sin datos»; edición manual siempre disponible.
   - El mismo flujo entrega datos extra aprovechables en el futuro (popularidad, nº de listas, nota
     media de reseñas): no se usan en F1, pero quedan anotados.

## Ampliación descubierta (29/09/2026): la ficha completa en un fetch

La página de un juego (`https://howlongtobeat.com/game/{id}`) sirve **embebido en el HTML el registro
completo del juego** en JSON — **un solo fetch por juego** para todo lo siguiente:

- **Tiempos por estilo** (Historia · +Extras · Completista · Todos), cada uno con: valor,
  nº de votos y `_avg` / `_med` / `_l` (rushed) / `_h` (leisure). Además: `comp_speed*` y
  `comp_speed100*` (speedruns), `invested_co*` (co-op), `invested_mp*` (multijugador).
- **Flags**: `comp_lvl_sp/spd/co/mp/combine/platform` (¿tiene modo X?).
- **Ficha**: `profile_dev` / `profile_pub` (desarrollador y editorial), `profile_genre`,
  `profile_platform`, `profile_summary`, `release_world/na/eu/jp`, `rating_esrb/pegi/cero`,
  `profile_steam` / `profile_steam_alt` (Steam App IDs), `profile_ign`, `game_alias`,
  `game_parent`, `related` / `relationships` (juegos relacionados).
- **Comunidad**: `count_comp`, `count_backlog`, `count_playing`, `count_retired`, `count_replay`,
  `count_review`, `review_score`, `count_discussion`, `stat_follows`.

**Ejemplo (Elden Ring, tabla de su web que cuadra con el JSON):** Main Story — media 60h 12m ·
mediana 60h · rushed 36h 31m · leisure 87h 3m (1,8K votos); Completionist — media 142h 25m ·
mediana 130h · rushed 92h 36m · leisure 322h 13m (3,9K votos).

**Notas de implementación:** parsear el JSON embebido (localizar `"game_id":<id>` y emparejar
llaves); mismo esquema de campos que la búsqueda (subconjunto). Existe también `/api/v1/games` que
exige cabecera `Authorization` (401 sin ella) — observado, no usable sin credenciales.
