# ADR-0002 — Escala de valoración: 1–10 entero

- **ADR:** 0002
- **Título:** La valoración de juegos usa una escala numérica entera de 1 a 10
- **Estado:** Aceptado
- **Fecha:** 2026-09-28
- **Decisores:** Alejandro Olivares Escapa
- **Fase relacionada:** F1 (El corazón del tracker) — resuelve la decisión D1.3

## Contexto

La nueva biblioteca rica (F1) introduce la valoración por estrellas/nota, que hoy no existe
(tres listas planas). Había que elegir la escala. La recomendación inicial del roadmap (D1.3)
era **5 estrellas con medias**, por ser el estándar del sector (Backloggd, Letterboxd). El
análisis competitivo 2026 (dim. 02) documentó que **IMDb usa 1–10** y que los juegos se
benefician de una **granularidad fina** (más rango para distinguir experiencias), mientras que
otras referencias usan escalas parciales (StoryGraph permite cuartos de estrella, Beli usa
ranking relativo).

## Decisión

La valoración se hace en una **escala numérica entera de 1 a 10** (sin decimales).

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| 5 estrellas con medias estrellas | Estándar de Letterboxd/Backloggd; comparable | Menos granularidad (9 puntos reales); "medias estrellas" confunde a algunos | Descartada por el propietario |
| 10 puntos (elegida) | Granularidad fina; clásica en crítica de juegos; comparable con IMDb | Puede dar "inflación" de notas | **Elegida** |
| Ambas (5★ y 10) | Flexibilidad | Duplica modelo, UI y agregados; confunde | Complejidad innecesaria |

## Consecuencias

**Positivas**
- Granularidad suficiente para distinguir juegos con matices.
- Alineada con la cultura de crítica de videojuegos (notas 1–10 son el lenguaje habitual).
- Facilita medias y distribuciones ricas para las estadísticas de perfil y el Rewind.

**Negativas / coste asumido**
- Riesgo de "nota de cortesía" (tendencia a puntuar alto); se mitiga con el **Rewind y las
  distribuciones** que muestran el uso real de la escala.
- Requiere migrar el modelo: los campos de valoración serán **Int 1–10**, no estrellas.

**Riesgos y mitigación**
- Inconsistencia con referentes que usan 5★ → documentado aquí; si se quiere mostrar en
  estrellas de cara al usuario, es solo una **capa de presentación**.

## Seguimiento

- Implementar en F1 (tareas T1.3 en adelante) con `Int` de 1 a 10.
- Revisar si en pruebas de usuario la escala resulta ambigua; condición de revisión: queja
  recurrente o datos que muestren concentración anómala de notas.
