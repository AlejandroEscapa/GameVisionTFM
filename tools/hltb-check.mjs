// Prueba en vivo del flujo de búsqueda de HLTB (replicando su web).
// Casos de verificación: 5 juegos variados.
const UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36';
const BASE = 'https://howlongtobeat.com';

function bodyFor(terms) {
  return {
    searchType: 'games',
    searchTerms: terms.trim().split(' '),
    searchPage: 1,
    size: 20,
    searchOptions: {
      games: {
        userId: 0,
        platform: { mode: 'include', values: [] },
        sortCategory: 'popular',
        rangeCategory: 'main',
        rangeTime: { min: null, max: null },
        gameplay: {
          perspective: { mode: 'include', values: [] },
          flow: { mode: 'include', values: [] },
          genre: { mode: 'include', values: [] },
        },
        year: { mode: 'include', values: [] },
        modifier: '',
      },
      users: { sortCategory: 'postcount' },
      lists: { sortCategory: 'follows' },
      filter: '',
      sort: 0,
      randomizer: 0,
    },
    useCache: true,
  };
}

async function getToken() {
  const r = await fetch(`${BASE}/api/search/site/init?t=${Date.now()}`, {
    headers: { 'User-Agent': UA, Referer: `${BASE}/` },
  });
  const j = await r.json();
  return j.token;
}

async function search(q, token) {
  const r = await fetch(`${BASE}/api/search/site`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-auth-token': token,
      'User-Agent': UA,
      Referer: `${BASE}/`,
      Origin: BASE,
    },
    body: JSON.stringify(bodyFor(q)),
  });
  if (r.status === 403) {
    const fresh = await getToken();
    return search(q, fresh);
  }
  const json = await r.json().catch(() => null);
  return { status: r.status, json };
}

const cases = ['Elden Ring', 'Limbo', 'Super Mario 64', 'Persona 5 Royal', 'The Elder Scrolls VI'];

let token = await getToken();
console.log('token ok:', !!token, '| len', token?.length);

for (const q of cases) {
  try {
    const { status, json } = await search(q, token);
    const top = json?.data?.[0];
    console.log(`\n### ${q} (http=${status}, count=${json?.count ?? '?'})`);
    if (!top) {
      console.log('   SIN RESULTADOS');
    } else {
      console.log('   RAW top:', JSON.stringify(top).slice(0, 900));
    }
  } catch (e) {
    console.log(`\n### ${q} -> ERROR: ${e.message}`);
  }
  await new Promise((r) => setTimeout(r, 1500));
}
