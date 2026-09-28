# ADR-0003 — Escala de valoración: 0,5–5,0 con medias estrellas

- **ADR:** 0003
- **Título:** La valoración usa escala de 5 con medias estrellas (0,5–5,0), alineada con RAWG
- **Estado:** Aceptado
- **Fecha:** 2026-09-28
- **Decisores:** Alejandro Olivares Escapa (preferencia «formato RAWG»); refinamiento a medias estrellas recomendado por el agente y aceptado al delegar los detalles del modelo
- **Supersede a:** ADR-0002 (escala 1–10)
- **Fase relacionada:** F1 (El corazón del tracker) y F0-B (modelo de datos)

## Contexto

El ADR-0002 fijó una escala numérica entera de 1 a 10. Al revisar el modelo de datos (F0-B) se
contrastó con el formato de RAWG: sus notas son sobre 5 con un decimal («4.4») y la UI ya las
muestra así desde la búsqueda. Con dos escalas distintas en pantalla (tu nota «8» junto a la de
RAWG «4.4») hay fricción visual, y el propietario pidió preferir el «formato RAWG». Los decimales
libres son inviables de introducir en móvil; los referentes del nicho (Letterboxd, Backloggd)
usan 5 estrellas con medias.

## Decisión

La nota personal se introduce y muestra en **0,5–5,0, en pasos de 0,5** (medias estrellas).
Se almacena como número (`rating` en la ficha del juego). No se admiten otras fracciones.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| 1–10 entero (ADR-0002) | Granularidad; tradición en prensa de juegos | Escala distinta a la de RAWG en pantalla; 10 dianas pequeñas | Sustituida por esta decisión |
| 5 entero | Máxima simplicidad | Solo 5 niveles: poca discriminación | — |
| **0,5–5,0 con medias (elegida)** | Coincide visualmente con RAWG (4,5 vs 4,4); 10 niveles; UX probada (Letterboxd/Backloggd) | La entrada con medias necesita un control de media estrella en F1 | **Elegida** |
| Decimales libres | — | Inviables de teclear en móvil; imprecisos | — |

## Consecuencias

**Positivas**
- Congruencia visual con los datos de RAWG (ambas cifras «sobre 5»).
- Mismos 10 niveles de granularidad que el 1–10.
- Estadísticas estables: las sumas y medias en pasos de 0,5 son exactas.

**Negativas / coste asumido**
- El control de entrada de F1 necesita media estrella (ya previsto en el diseño).

**Riesgos y mitigación**
- Posible confusión con la nota de la comunidad de RAWG → la UI rotulará claramente
  «Comunidad» frente a «Tu nota».

## Seguimiento

- Revisar con usuarios tras F1: ¿se usa la media estrella o sobra? Condición: quejas recurrentes
  o concentración anómala en los datos.
