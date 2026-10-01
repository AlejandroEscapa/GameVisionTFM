# Plan de iteración: Home «Para ti», descubrimiento, onboarding y ajustes (2026-10)

> **Qué es.** Iteración de producto acordada con el propietario el 01/10/2026: brain-storming
> madurado + plan de ejecución para el día 02/10. Complementa [F3](../roadmap/fase-3-wow.md)
> (el "wow") y [barra-y-home-2026.md](barra-y-home-2026.md) (dock flotante + concepto de Home).
> Las decisiones que quedan abiertas se cierran CON el propietario antes de tocar código (regla
> del workspace). Lo obvio no se detalla aquí: se ejecuta.

---

## Bloque A — Ajustes rápidos de UI (primer tramo del día)

- **A1. Biblioteca y el filtro por defecto.** La pestaña «Jugando» YA es la inicial
  (`selectedList = "playing"`). Lo que pide el propietario se interpreta como: el **panel de
  filtros arranca plegado** y el botón de filtros lo abre/cierra (hoy no está claro qué memoria
  tiene) → verificar el comportamiento real y dejar: filtro visible-solo-al-pedirlo, sin estado
  que persista por accidente. **D-UX1 ✅ CERRADA (01/10)**: los filtros arrancan DESPLEGADOS y el botón los desactiva/activa.
- **A2. Perfil en grid 1×3.** Estadísticas · Seguidos · Editar perfil pasan a tres **cuadrados**
  en fila (accesibilidad: 48 dp mínimo); «Cerrar sesión» al fondo, discreto; la **biografía gana
  protagonismo** (tipografía mayor, zona propia con el location/contact debajo). Material ya en su
  sitio, es re-layout de `ProfileScreen`.
- **A3. Pantalla de Ajustes v1.** Nueva ruta `ajustes`, accesible desde Perfil (icono en la
  cabecera). Contenido v1 real: **modo noche** (el toggle migra aquí desde Perfil; el «Modo día»
  actual se sustituye) + placeholders listados en el Bloque E. Ver D-E2 para el dock.

## Bloque B — Descubrimiento en Buscar (la pantalla nunca vacía)

Hoy la búsqueda en frío muestra «¡Busca tu primer juego!». Propuesta: mientras no hay query, la
pantalla se llena de **tres filas horizontales** (LazyRow — el hueco real detectado por la
auditoría UI):

1. **«Populares ahora»** — RAWG ordenado por popularidad (gratis, sin clave nueva).
2. **«Porque jugaste X»** — recomendación por los géneros del historial del usuario
   (`StatisticsUtils` + géneros de `LibraryEntry`; el motor T3.1 reutilizable).
3. **«Descubre»** — página aleatoria de RAWG con **semilla diaria** (fecha del día): cada día,
   una sorpresa distinta; para cuentas nuevas sin historial es la fila por defecto.

**D-B1 ✅ CERRADA (01/10)**: orden 1-2-3. **D-B2 ✅** filas extra con disparador.

## Bloque C — Onboarding post-registro (bio + localización + géneros)

Tras crear cuenta (email O Google), si el perfil recién creado **no tiene** biografía ni
localización → mini-onboarding de **3 pasos saltables** (nunca bloqueante, en la línea de D1.2):

1. **Bio** (280 car. máx, mismo límite que el feed).
2. **Localización** (texto libre, ya existe el campo).
3. **Géneros favoritos** (chips multi-select) — **alimenta el motor de recomendación y la fila
   «Porque jugaste X»**: el onboarding deja de ser decorativo y siembra las recomendaciones.

Con Google: si el `users/{uid}` ya existe con bio rellenada, se salta directo (sin repetir).
**D-C1 ✅ CERRADA (01/10)**: los tres pasos, con géneros; se guardan en `users/{uid}` como array (consultable con array-contains, la forma óptima para filtrar por género sin subcolecciones).

## Bloque D — Ajustes: brain-storming de opciones (para los placeholders)

Lista típica 2026 de apps de tracking/social — la v1 muestra todo el esqueleto, con «Próximamente»
donde aún no hay funcionalidad:

| Sección | Opciones | v1 |
|---|---|---|
| **Apariencia** | Modo noche (toggle real) / seguir sistema | ✅ real |
| **Notificaciones** | Hitos de seguidos, ofertas de wishlist (FCM ya dado de alta en F0) | placeholder |
| **Privacidad** | Perfil público/privado (ya existe `isPrivate` — mover aquí desde editar perfil) | ✅ real (movido) |
| **Cuenta** | Cambiar email/contraseña · cerrar sesión · **borrar cuenta** (obligatorio para Play) | cerrar sesión real; resto placeholder |
| **Steam** | Vincular cuenta (P1, ya adoptada) | placeholder |
| **Datos** | Exportar biblioteca (import/export universal — arma de captación) | placeholder |
| **Reprod. automática / Imágenes solo Wi-Fi** | ahorro de datos | placeholder |
| **Acerca de** | Versión, licencias OSS, contacto soporte, política privacidad (requisito Play) | placeholder con versión real |

**D-E1 ✅ CERRADA (01/10)**: modo noche + privacidad (movida desde editar perfil) + cerrar sesión; resto «Próximamente».
**D-E2 ✅ CERRADA (01/10)**: Ajustes fuera del dock, en la cabecera del Perfil.

## Bloque F — La nueva Home «Para ti» (la pieza grande)

**La idea del propietario, madurada.** La pantalla actual de Diario no convence como destino →
se convierte en la **Home central** con scroll vertical de secciones que mezclan lo tuyo, lo
social y el mercado. Las noticias se **fusionan** como una sección pequeña, no como pestaña.

Orden propuesto del scroll (cada sección es una tarjeta del design system):

1. **«Continúa»** — el juego en curso con lo que queda (HLTB) y botón «Registrar sesión» → el
   diario deja de ser pantalla y pasa a ser **acción** en la Home (su histórico vive como
   sub-pantalla accesible desde la propia tarjeta).
2. **«¿Qué juego ahora?»** — la recomendación explicada del motor T3.1 con chips de tiempo
   (30 min / 2 h / una tarde) → esto es T3.2, se construye aquí.
3. **«Te está esperando»** — ofertas de tu wishlist con % (CheapShark v1, D-NH2).
4. **«Novedades en tus géneros»** — lanzamientos recientes de los géneros favoritos del perfil
   (RAWG por género+fecha; los géneros llegan del onboarding C).
5. **«Desde Steam»** (fase 2 — P1, proxy Cloud Functions): actividad, logros, reseñas relacionadas.
6. **«Lo que está pasando»** — 2-3 titulares de noticias (lo que hoy es la pestaña entera) +
   último hito de tus seguidos.
7. **Cuenta nueva / sin datos:** las secciones se rellenan con «Populares ahora» y «Descubre»
   (mismo motor que Bloque B) — **una Home interesante desde el primer minuto**, sin huecos.

**Implicación en el dock** (cierra D-NH1 del plan de barra): Home · Buscar · Biblioteca · Social ·
**Perfil** — el Diario y las Noticias **salen del dock** (diario = acción+subpantalla; noticias =
sección). 5 pestañas, el máximo profesional. El dedicado a «¿Qué juego ahora?» (opción 3 en
baraja) se decide al verlo vivo.

**Datos nuevos necesarios:** solo la API de precios (CheapShark, gratis, reglas documentadas) —
todo lo demás existe (biblioteca, HLTB, motor T3.1, NewsAPI, feed, RAWG).

**D-F1 ✅ CERRADA (01/10)**: orden propuesto. **D-F2 ✅ CERRADA (01/10)**: sí, sub-pantalla. **D-F3 ✅ CERRADA (01/10)**: sí — «en home».

## Estado de ejecución (01/10)

- [x] **Bloque A** — filtros desplegados, perfil 1×3, bio con aire, Ajustes v1 (`abe7bff`).
- [x] **Bloque C** — onboarding 3 pasos + seed de las 10 cuentas anteriores (`0cabf12`).
- [x] **Bloque B** — descubrimiento en Buscar: populares / por géneros / sorpresa del día (`be437a0`).
- [x] **Bloque F** — Home «Para ti» + dock de 5 pestañas (Inicio · Buscar · Biblioteca · Social ·
  Perfil); diario y noticias fuera del dock; T3.2 y T3.3 de F3 cubiertos dentro de la Home.
  *Nota:* la sección «Te está esperando» muestra la lista de deseos y enlaza a la ficha (la
  integración de precios de CheapShark queda como siguiente paso; el servicio ya está cableado).

## Orden de ejecución (02/10)

**A → D → C → B → F** (de lo rápido a lo grande; F necesita decisiones cerradas por la mañana).
Cada bloque: implementación + verificación (emulador + suite) + commit. La suite de reglas no
toca (no hay cambios de Firestore salvo... ninguna). Push al final de cada bloque.
