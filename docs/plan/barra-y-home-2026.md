# Rediseño de la barra de navegación + nueva Home (2026-10)

> **Qué es.** Dos piezas emparentadas: (1) las **5 alternativas profesionales** para la barra de
> navegación, brain-stormeadas el 01/10/2026 y guardadas por petición del propietario; (2) el
> **concepto madurado de la nueva Home**, que sustituye la pestaña de noticias por un inicio con
> gancho. Cualquier cambio de estructura pasa por aquí antes de tocar código.

## 1. Las cinco alternativas (guardadas)

| # | Nombre | Qué es | Coste | Estado |
|---|---|---|---|---|
| 1 | **Galería** | Dock flotante translúcido despegado del borde, hairline, activo con relleno suave del acento + micro-escala | Medio | ✅ **ADOPTADA (01/10)** — implementada en `GVDock` |
| 2 | **Expresiva** | Barra M3 con píldora indicadora que se desliza entre pestañas + labels visibles | Bajo | 🗄 Archivada (opción segura) |
| 3 | **Ahora** | 4 tabs + botón central héroe "¿Qué juego ahora?" sobresaliendo | Medio | 🃏 **EN BARAJA** — se reevalúa cuando exista la pantalla T3.2; combina con la 1 |
| 4 | **Cartucho** | Iconografía propia de marca (cartucho, D-pad, mando, logro…) stroke 1,5-2 | Medio-alto | 🗄 Archivada — fase 2 de marca, requiere validación visual |
| 5 | **Buque insignia** | 1 + 3 + 4 juntas | Alto | 🗄 Archivada — solo si la 3 se activa y funciona |

**Decisión del propietario (01/10):** adoptar la 1, barajar la 3, conservar el resto por si
queremos cambiar. Nota: la 1 y la 3 son compatibles (el héroe central cabe en el dock flotante).

## 2. La nueva Home: «Tu mesa de juego»

**El problema.** Hoy la pestaña "Home" ES la pantalla de noticias con icono de casa: el nombre
miente, la casa no es la portada de la app y las noticias (contenido pasivo) ocupan el sitio que
debería ocupar el gancho.

**El concepto.** Una Home que responde «¿qué hago ahora?» en tres capas — de tu biblioteca, de tu
motor y del mercado — sin que salgas de la pantalla:

1. **«Continúa»** — tu última sesión y tus juegos en curso con lo que queda (HLTB): datos 100 %
   locales que YA tenemos (biblioteca + diario + `RecommendationEngine` de T3.1). Acción directa:
   «Registrar sesión».
2. **«¿Qué juego ahora?»** — tarjeta héroe con la recomendación explicada del día (T3.1/T3.2).
   El gancho principal: la app recomienda antes de que preguntes.
3. **«Te está esperando»** — rebajas de tu lista de deseos («Elden Ring −35 % en Steam»).
4. **«Lo que está pasando»** — 2-3 titulares de noticias (NewsAPI ya integrada) + último hito de
   tus seguidos (feed ya existe).
5. **Fase 2 — Steam Inside (P1, adoptada en D-AI):** últimos logros y sesión de Steam si la cuenta
   está vinculada; requiere el proxy Cloud Functions ya planificado.

**Por qué encaja:** reutiliza integraciones ya adoptadas (P1 Steam, CheapShark/ITAD de la
investigación de APIs) y funcionalidad ya construida; no añade dependencias nuevas salvo la API
de precios.

## 3. Decisiones abiertas (cerrar antes de construir la Home)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| D-NH1 | **Estructura del dock** | (a) 5 tabs: Inicio · Noticias · Biblioteca(+buscar) · Diario · Social; Perfil → avatar en la cabecera de todas las pantallas · (b) 6 tabs como hoy | **(a)**: 5 es el máximo profesional; el perfil en la esquina es el patrón universal 2026; la búsqueda de catálogo vive dentro de Biblioteca (toggle «mi biblioteca / catálogo») y como lupa en Inicio | ⬜ |
| D-NH2 | **Fuente de precios** | (a) CheapShark: gratis, sin clave, redirects (investigación: reglas claras) · (b) ITAD: mejor cobertura, clave + límite 1000/5min, comercial OK | **(a) CheapShark para v1** (cero fricción); ITAD cuando haya clave de publicación | ⬜ |
| D-NH3 | **Módulos de la v1** | Continúa + Ahora + Te está esperando + Lo que está pasando (Steam a v2) | Tal cual: todo usa datos/integraciones existentes salvo precios | ⬜ |
| D-NH4 | **Perfil fuera del dock** | Avatar en cabecera (todas las pantallas) vs tab propia | Avatar en cabecera, coherente con D-NH1a | ⬜ |
| D-NH5 | **¿La 3 en baraja se activa?** | Botón héroe central cuando exista T3.2 | Decidir al ver la pantalla «¿Qué juego ahora?» en el dock nuevo | ⬜ |

## 4. Estado de implementación

- [x] Dock flotante (opción 1) — `GVDock` en `BottomBarNavigation.kt`, labels en español,
  pestaña de noticias renombrada con icono de periódico (`Newspaper`). Rail de tablet sin cambios.
- [ ] Restructura del dock (D-NH1) y nueva Home (D-NH2/D-NH3) — pendiente de cerrar decisiones.
