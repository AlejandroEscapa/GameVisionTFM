# Fase 4.5 — Diseño, animaciones y auditoría de experiencia

**Estado:** ⬜ Pendiente · **Estimación:** ~1 semana · **Depende de:** F1–F4 · **Bloquea a:** F5 (Publicación)

> **Encaje.** Va **entre F4 (Nativo) y F5 (Publicación)**: se audita y se pule la experiencia
> **antes** de abrir la monetización. Monetizar una experiencia sin pulir es el peor orden posible.

> **Nota (29/09/2026) — design system en transición.** La identidad visual actual (`DESIGN.md` +
> `ui/designsystem/`) queda **provisional**: el propietario aportará un **nuevo design system**
> (documento Markdown) que pasará a ser la **fuente única de verdad visual**. Esta fase ejecuta el
> **re-anclaje** (tokens, tipografía, componentes y motion) sobre las pantallas ya auditadas. Ver
> [ADR-0009](../metodologia/adr/0009-reanclaje-design-system.md) y la decisión **DX.8**.

> **Nota (02/10/2026) — el re-anclaje YA está hecho.** La fuente llegó (sistema Apple) y el
> re-anclaje se **adelantó** a antes de F3 por [ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md):
> tokens, tipografía, formas, espaciado y componentes están aplicados. Por tanto **el bloque G de
> esta fase (DX-T23…DX-T28) y CA4.5.7 están ejecutados** y no se repiten aquí. Además, la
> **auditoría de diseño completa** ya existe ([auditoría 02/10](../plan/auditoria-diseno-2026-10-02.md)),
> con los hallazgos priorizados por impacto y esfuerzo, y el alcance del acento quedó fijado en
> [ADR-0012](../metodologia/adr/0012-alcance-del-acento.md).
>
> **Qué le queda de verdad a F4.5:** el **sistema de movimiento** (hoy `GVMotion` solo se usa en el
> dock y en las transiciones del NavHost), la **accesibilidad** (TalkBack, `RatingStars` como un solo
> nodo, tamaños táctiles), el **barrido de espaciado y formas** que la auditoría dejó pendiente, la
> **consolidación de componentes duplicados** y los **estados vacíos con acción**.

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
| DX.8 | **¿Se conserva el design system actual o se re-ancla al entrante?** | (a) Mantener el actual · (b) **Re-anclar al design system que aportará el propietario** | **(b)**: confirmado un design system nuevo; `DESIGN.md` pasa a provisional y esta fase aplica sus tokens/componentes. Lo agnóstico al sistema (estados, motion, estructura) se conserva | ✅ **Cerrada (01–02/10)**: la fuente llegó y el re-anclaje se adelantó — [ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md). Ejecutado (DX-T23…DX-T28 y CA4.5.7) |

---

## Tareas

### A. Auditoría visual pantalla a pantalla
- [x] DX-T1 Inventario de todas las pantallas + estados — **hecho el 02/10** en
  [auditoría de diseño 2026-10-02](../plan/auditoria-diseno-2026-10-02.md): 43 ficheros de UI,
  hallazgos con `fichero:línea`, priorizados por impacto × esfuerzo.
- [x] DX-T2 Revisión contra `DESIGN.md`: jerarquía, espaciado, tipografía, color, iconografía —
  **hecha el 02/10**, con la primera pasada de correcciones aplicada (sombras, superficies, emoji,
  copy, contraste) y la deuda de espaciado/formas inventariada.
- [x] DX-T3 Detectar inconsistencias con el design system (componentes «a mano» que deberían
  reutilizar) — **hecho**: 4 buscadores → `GVSearchField`, 5 cabeceras de sección, 3 tarjetas de
  juego (una muerta), 68 radios a mano.
- [ ] DX-T4 Priorizar hallazgos (bloqueante / mejora / idea) y cerrar los bloqueantes — **la
  priorización está hecha** (TOP 10 del informe); **quedan bloqueantes por cerrar**: espaciado
  unificado, formas a `GVShapes`, los 7 `e.message` al usuario de la Ficha, estados vacíos y la
  accesibilidad de `RatingStars`.

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

### G. Re-anclaje del design system entrante (ADR-0009)
> **Ejecutado el 01–02/10/2026 y adelantado a antes de F3** por
> [ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md). Se deja el detalle
> marcado para que quede el rastro de qué se hizo y dónde.
- [x] DX-T23 Recibir el documento del nuevo design system y **sustituir `DESIGN.md`** como fuente única de verdad — sistema Apple; fuente versionada en `docs/plan/design-system-fuente-apple.md`
- [x] DX-T24 Mapear tokens (color, tipografía, forma, elevación) a `GVTheme` / `GVTypography` / `GVShapes` — más `GVSpacing` (escala de espaciado, nueva)
- [x] DX-T25 Migrar los componentes de `ui/designsystem/` al sistema nuevo — y el re-estilizado de las pantallas que los consumen
- [x] DX-T26 Revisar pantalla a pantalla el resultado del re-anclaje contra el inventario auditado — **cerrado el 02/10** con la [auditoría de diseño](../plan/auditoria-diseno-2026-10-02.md) (que es la revisión pantalla a pantalla)
- [x] DX-T27 Ajustar `GVMotion` a las curvas/duraciones del sistema nuevo — pendiente solo el **sistema de motion completo** (bloque B de esta fase)
- [x] DX-T28 Retirar cualquier token o asset huérfano del sistema anterior — retirado el verde ácido y `material-icons-extended`; **queda código muerto por decidir** (`designsystem/GameCard.kt`, `NewsCard.kt`), anotado en `DESIGN.md` §6

## Criterios de aceptación (con evidencia)

- [ ] CA4.5.1 Inventario de pantallas completo, con veredicto por pantalla (captura antes/después de los cambios) — **el inventario y los veredictos existen** ([auditoría 02/10](../plan/auditoria-diseno-2026-10-02.md)); faltan las capturas antes/después de las pantallas aún pendientes
- [ ] CA4.5.2 Sistema de motion documentado **y aplicado** en los flujos principales (vídeo/gif o capturas)
- [ ] CA4.5.3 Ninguna pantalla con estados sin resolver; cargas con skeleton, no en blanco
- [ ] CA4.5.4 Con «reducir movimiento» activo, la app se entiende y se usa igual (verificado en emulador)
- [ ] CA4.5.5 Auditoría funcional contra las D-V cerrada: sin acciones huérfanas ni controles muertos — **un control muerto ya cayó** (el botón de reintento del feed, 02/10); falta el barrido completo
- [ ] CA4.5.6 TalkBack recorre los flujos principales sin elementos sin etiqueta
- [x] CA4.5.7 El design system entrante está aplicado y `DESIGN.md` actualizado como fuente única (sin restos del sistema anterior) — **02/10**, con la adopción aún en barrido (ver deuda de espaciado y formas en `DESIGN.md` §7)

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

- **29/09/2026** — Punto de partida registrado. Se abre **DX.8**: el propietario confirma que se usará
  un **design system nuevo** (documento entrante) y `DESIGN.md` queda **provisional**. El re-anclaje
  se ejecuta en esta fase ([ADR-0009](../metodologia/adr/0009-reanclaje-design-system.md)). DX.1–DX.7
  siguen abiertas. Entra como material la [auditoría UI 2026](../plan/ui-adopcion-hallazgos-2026.md)
  (P1: `UiState` por pantalla y estados resueltos, que es el criterio CA4.5.3).
- **01/10/2026** — **DX.8 se cierra antes de tiempo:** la fuente llegó (sistema Apple) y el
  re-anclaje se **adelanta** a antes de F3 ([ADR-0010](../metodologia/adr/0010-reanclaje-adelantado-fuente-recibida.md)).
  Esta fase **pierde el bloque de re-anclaje** y conserva auditoría de experiencia, motion y pulido.
- **02/10/2026** — **Auditoría de diseño ejecutada** ([informe](../plan/auditoria-diseno-2026-10-02.md)):
  el design system es sólido y su **adopción** no lo era (19 ficheros maquetaban a mano). Cerrados
  cuatro bugs reales (uno **crash** en Buscar, uno de `!!` en la portada, un botón inerte y el
  «me gusta» sin estado visible) y aplicada la primera pasada (sombras, superficies, copy, emoji,
  contraste, a11y). **El alcance del acento se fija en [ADR-0012](../metodologia/adr/0012-alcance-del-acento.md)**
  (Action Blue solo para lo que se toca). Queda pendiente el barrido de espaciado/formas y la
  consolidación de duplicados, listados en el informe.

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
