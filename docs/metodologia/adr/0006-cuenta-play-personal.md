# ADR-0006 — Cuenta de desarrollador de Google Play: personal

- **ADR:** 0006
- **Título:** GameVision se publica con una cuenta de desarrollador **personal** de Google Play
- **Estado:** Aceptado
- **Fecha:** 2026-09-29
- **Decisores:** Alejandro Olivares Escapa
- **Fase relacionada:** F5 (Publicación) — vertientes V4/V5 — resuelve la decisión **D-E1** de la [síntesis](../../investigacion-2026/sintesis-2026.md)

## Contexto

Desde septiembre de 2026 todos los desarrolladores requieren **verificación de identidad**
(nombre, dirección, teléfono, photo ID opcional) para publicar y que las apps se instalen en
dispositivos certificados ([evidencia](../../investigacion-2026/fuentes/01-play-store-2026.md)).
La diferencia real entre tipos de cuenta está en el gate de producción y el papeleo: las cuentas
**personales nuevas** exigen una beta cerrada de **12 testers opt-in continuos durante 14 días**
antes de solicitar acceso a producción; las cuentas de **organización** están exentas de ese
requisito pero exigen **número D-U-N-S + documentos de empresa** (autónomo o SL). El paso
personal→organización no es directo: exige cuenta nueva y transferencia de la app. El proyecto
no tiene hoy figura legal propia, y el veredicto filosófico del Plan Maestro §3.4 es un producto
«sostenible, no hiperescalable».

## Decisión

La publicación se hará con **cuenta personal**. La beta cerrada obligatoria (12 testers / 14 días)
se asume como **activo**: es la primera comunidad de GameVision y la fuente de feedback real
F1→F2 antes del staged rollout.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| **Personal (elegida)** | Coste mínimo (~25 $ único); sin papeleo D-U-N-S; beta como primera comunidad | Gate de 12/14 días antes de producción; transferencia futura si llega figura legal | **Elegida**: nada justifica hoy montar una figura legal solo por saltarse una beta |
| Organización | Sin requisito de testers; marca «profesional» en ficha | D-U-N-S + documentos + mantenimiento de la figura legal; no reversible a personal | Solo tendría sentido si ya existiera autónomo/SL con D-U-N-S; no es el caso |
| Aplazar a F5 | Cero decisión hoy | La decisión es cara de revertir y condiciona F5 entero (verificación, beta, ficha) | Cerrada ahora para que F5 se prepare sin bloqueos |

## Consecuencias

**Positivas**
- Coste de entrada mínimo y verificación resuelta con la identidad personal (coherente con la decisión de mostrar el nombre del propietario).
- 12–20 testers reclutados conscientemente = comunidad semilla + feedback real antes de producción.
- F5 se desbloquea: política de privacidad, Data Safety y plan de testers se pueden redactar ya.

**Negativas / coste asumido**
- Semanas de beta cerrada obligatoria antes del staged rollout (se planifican dentro de F5).
- Si el proyecto crece hacia figura legal, la transferencia de app a una cuenta de organización es viable pero molesta (cuenta nueva, migración).

**Riesgos y mitigación**
- Google ajusta los umbrales de testing con frecuencia (20→12 en 2026) → **revalidar el requisito vigente en Play Console al arrancar F5**, no dar por bueno el 12/14 documentado.
- El nombre del propietario queda asociado a la ficha pública → asumido: es la misma identidad que ya es pública en GitHub y en `AGENTS.md` §0.

## Seguimiento

- Al abrir F5: revalidar en Play Console el requisito de testers vigente y el estado de la verificación de desarrolladores.
- Disparador de revisión: creación de figura legal (autónomo/SL) o monetización que exija cuenta de organización → escribir ADR que supersede a este y planificar la transferencia.
