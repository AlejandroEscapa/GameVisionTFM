# Fase 4.5 — Diseño, animaciones y auditoría de experiencia

**Estado:** ⬜ Pendiente · **Estimación:** ~1 semana · **Depende de:** F1–F4 · **Bloquea a:** F5 (Publicación)

> **Encaje.** Va **entre F4 (Nativo) y F5 (Publicación)**: se audita y se pule la experiencia
> **antes** de abrir la monetización. Monetizar una experiencia sin pulir es el peor orden posible.

## Objetivo

Revisar **pantalla a pantalla** que las decisiones de diseño siguen siendo las correctas y que
lo que la app **transmite al jugador** —jerarquía, tono, movimiento— está a la altura. El
propietario hace especial hincapié en el **diseño y las animaciones** como parte del valor del
producto, no como un adorno final.

## Resultado verificable

1. Existe un **inventario de pantallas auditado** (qué funciona, qué se cambia, por qué) **firmado**.
2. Existe un **sistema de movimiento (motion)** definido por escrito y **aplicado** en los flujos
   principales (transiciones, microinteracciones, celebraciones).
3. Ninguna pantalla queda con estados sin resolver (**vacío / carga / error / offline**) ni con
   acciones fuera del sitio donde el usuario las espera.

## Por qué esta fase y no otra

- El diseño tiene **base sólida** (`DESIGN.md` + `ui/designsystem/`) pero **no se ha auditado
  pantalla a pantalla** ni existe una **capa de movimiento** propia.
- Es diferencial real: los competidores web (Backloggd) no pueden igualar la sensación nativa;
  el cuidado del detalle es parte del argumento del producto.
- Colocarla **antes de la monetización** protege la marca: pagar por una app sin pulir es la
  vía rápida a la reseña negativa.

---

## Decisiones abiertas (debate antes de empezar)

| # | Decisión | Opciones | Recomendación | Estado |
|---|---|---|---|---|
| DX.1 | **Sistema de animaciones** | (a) Motion de Material 3 tal cual · (b) **motion propio documentado** sobre las APIs de Compose | **(b)**: documentar duraciones, curvas y usos evita animaciones «de cada pantalla» | ⬜ |
| DX.2 | **Nivel de movimiento** | (a) Sobrio/**sutil** · (b) Expresivo · (c) Distinto por vista | **(a) sutil, con celebraciones puntuales** (Rewind, completar un juego): el detalle se nota, el exceso cansa | ⬜ |
| DX.3 | **¿Rediseñar o refinar?** | (a) **Refinar lo existente** · (b) Rediseños puntuales · (c) Rediseño total | **(a) con excepciones**: partir del design system y corregir, no rehacer; rediseño sólo donde la auditoría lo justifique | ⬜ |
| DX.4 | **Herramienta de motion** | (a) **APIs nativas de Compose** (`animate*AsState`, `AnimatedContent`, transiciones) · (b) Librería externa | **(a)**: sin dependencias nuevas, coherente con ser 100 % Compose | ⬜ |
| DX.5 | **Movimiento reducido / accesibilidad** | (a) **Respetar la preferencia del sistema** (`ANIMATOR_DURATION_SCALE` / «reducir movimiento») · (b) Ignorarla | **(a)**: obligatorio; la animación nunca puede ser la única forma de entender algo | ⬜ |
| DX.6 | **Alcance de la auditoría funcional** | (a) **Todas las pantallas contra `integracion-por-vistas-2026.md`** · (b) Sólo las dudosas | **(a)**: verificar que cada acción está donde el usuario la busca (cruza con las D-V ratificadas) | ⬜ |
| DX.7 | **¿Se retoca el onboarding?** | (a) Sí, si la auditoría lo pide · (b) No | **(a)**: el onboarding es la primera impresión y es donde más se nota el motion bien hecho | ⬜ |

---

## Tareas

### A. Auditoría visual pantalla a pantalla
- [ ] DX-T1 Inventario de todas las pantallas + estados (lista viva en `docs/diseno/auditoria-pantallas-2026.md`)
- [ ] DX-T2 Revisión contra `DESIGN.md`: jerarquía, espaciado, tipografía, color, iconografía
- [ ] DX-T3 Detectar inconsistencias con el design system (componentes «a mano» que deberían reutilizar)
- [ ] DX-T4 Priorizar hallazgos (bloqueante / mejora / idea) y cerrar los bloqueantes

### B. Sistema de movimiento (motion)
- [ ] DX-T5 Documentar el **sistema de motion**: duraciones, curvas de easing y cuándo usar cada patrón
- [ ] DX-T6 Transiciones entre pantallas (navegación) coherentes y con **carga diferida** (sin saltos)
- [ ] DX-T7 Microinteracciones: pulsación, marcado de estado, estrellas, «me gusta», guardado
- [ ] DX-T8 **Skeletons / placeholders** de carga en catálogo, ficha y biblioteca (evitar pantallas vacías)
- [ ] DX-T9 **Celebraciones puntuales** (completar un juego, Rewind) alineadas con «celebrar, no castigar»
- [ ] DX-T10 Respetar la preferencia de **movimiento reducido** del sistema en todo lo anterior

### C. Auditoría de funcionalidad por pantalla
- [ ] DX-T11 Cruzar cada pantalla con [`integracion-por-vistas-2026.md`](../plan/integracion-por-vistas-2026.md): ¿cada acción está en la vista correcta?
- [ ] DX-T12 Verificar las 20 D-V ratificadas: ¿siguen siendo la mejor decisión con la app ya construida?
- [ ] DX-T13 Detectar **acciones huérfanas** (funcionalidad que existe y no se encuentra) o controles sin respuesta
- [ ] DX-T14 Revisar **ríos de navegación** completos (¿se puede llegar y volver a todo sin callejones?)

### D. Estados, copy y consistencia
- [ ] DX-T15 Recorrer **vacío / carga / error / offline** en cada pantalla nueva o tocada
- [ ] DX-T16 Revisar **tono del copy**: coherente con el propietario y con los principios de producto (anti-culpa)
- [ ] DX-T17 Coherencia de espaciados y densidades entre pantallas (móvil/tablet/plegable)

### E. Accesibilidad y adaptativo
- [ ] DX-T18 Contraste y tamaños táctiles contra los tokens de `DESIGN.md`
- [ ] DX-T19 TalkBack en los flujos principales (registro, biblioteca, ficha, ajustes)
- [ ] DX-T20 Revisar las ventanas **tablet y plegable** con la experiencia ya construida

### F. Documentación del resultado
- [ ] DX-T21 Informe de auditoría de diseño firmado (con hallazgos y acciones)
- [ ] DX-T22 Actualizar `DESIGN.md` con el sistema de motion y los cambios de tokens

---

## Criterios de aceptación (con evidencia)

- [ ] CA4.5.1 Inventario de pantallas completo, con veredicto por pantalla (captura antes/después de los cambios)
- [ ] CA4.5.2 Sistema de motion documentado **y aplicado** en los flujos principales (vídeo/gif o capturas)
- [ ] CA4.5.3 Ninguna pantalla con estados sin resolver; cargas con skeleton, no en blanco
- [ ] CA4.5.4 Con «reducir movimiento» activo, la app se entiende y se usa igual (verificado en emulador)
- [ ] CA4.5.5 Auditoría funcional contra las D-V cerrada: sin acciones huérfanas ni controles muertos
- [ ] CA4.5.6 TalkBack recorre los flujos principales sin elementos sin etiqueta

---

## Riesgos

| Riesgo | Mitigación |
|---|---|
| «Pulir» se convierte en rediseño infinito | DX.3: refinar lo existente; los rediseños exigen justificación de la auditoría |
| Animaciones que marean o que se comen el rendimiento | DX.2 sobrio + DX-T10 movimiento reducido + medir con el profiler |
| La fase se eterniza por no tener fin claro | Se cierra con el **inventario auditado + motion aplicado**; el resto pasa a deuda con disparador |

## Cómo se verifica

Emulador (`Pixel_9`) en móvil/tablet + «reducir movimiento» activado + TalkBack + capturas
antes/después por pantalla + capturas o vídeo de las transiciones principales.

---

## Registro de decisiones

_(vacío: pendiente de debate)_

---

## 🔍 Auditoría de cierre de fase

> Aplica el [protocolo de auditoría de cierre](../metodologia/auditoria-de-cierre.md).
> **Particularidad:** en esta fase la auditoría **es el producto**, así que su cierre va al
> máximo detalle en los bloques **4 (arquitectura), 6 (experiencia y motion)** y accesibilidad.

- [ ] Bloques 1–3 del protocolo en verde (build, tests, calidad estática)
- [ ] Bloque 4: sin componentes «a mano» que dupliquen el design system
- [ ] Bloque 6: recorrido pantalla a pantalla firmado, con motion y estados verificados
- [ ] Preferencia de **movimiento reducido** verificada en todo el flujo principal
- [ ] `DESIGN.md` y el inventario de pantallas **sincronizados** con lo implementado
