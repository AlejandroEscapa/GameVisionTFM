# De "app de TFM" a "producto en Play Store" — Investigación 2026 y plan de decisión

> **Qué es este documento.** La síntesis en un solo sitio de una investigación profunda
> (6 frentes, ~80 búsquedas) sobre cómo llevar GameVision desde donde está hoy hasta una
> app **publicada, monetizada y sostenible** en Google Play en 2026. Termina con **decisiones
> recomendadas** listas para convertirse en ADRs y fases.
>
> **Método.** Skill `deep-research`: 6 dimensiones investigadas en paralelo por agentes
> independientes, cada afirmación con su nivel de confianza, contradicciones señaladas y no
> aplanadas. Las fuentes crudas de cada frente están en [`fuentes/`](fuentes/).
> Fecha: **28/09/2026**.
>
> **Aviso de honestidad.** Donde la investigación no pudo respaldar un dato con fuente
> citable, se dice explícitamente; no se rellenan huecos de memoria. Ver §8.

---

## 1. Resumen ejecutivo (lo que hay que saber en 2 minutos)

1. **El listón de publicación en 2026 es más alto de lo que era.** Tres obligaciones duras
   nos afectan: **targetSdk ≥ 36** (ya lo cumplimos, justo), **edge-to-edge sin opt-out** y
   **predictive back por defecto** (heredados del targetSdk 36), y un requisito de
   **verificación de desarrollador** que se refuerza durante 2026. Además, las **cuentas
   personales nuevas** exigen **12 testers / 14 días** de prueba cerrada antes de producción.
   → Buena noticia: si publicamos como **organización** (o cuando la cuenta madure), ese
   requisito de testing no aplica; y nuestro targetSdk ya está en regla.
2. **La monetización realista de una app de nicho es anuncios discretos, no agresivos.**
   AdMob + **banner/native + rewarded opcional**, con UMP (consentimiento) desde el día uno.
   Los formatos que mejor pagan (interstitial, app-open) son los que **destruyen la retención**
   de un tracker que se abre 20 segundos. El "remove ads" de pago único es el primer flujo de
   pago sensato; la suscripción, más adelante. **Mediación avanzada (LevelPlay/MAX): no
   todavía** (se recomienda para >100K DAU).
3. **El stack está casi en la frontera, con dos rezagos claros.** Gradle/AGP/Compose BOM/
   Material3/compileSdk están al día; **Kotlin (2.2.10 vs 2.4.20)** y **navegación (Nav2 vs
   Nav3 ya estable)** son las dos deudas técnicas. Ninguna es urgente, pero Kotlin se agrava
   cada 6 meses y arrastra compatibilidad de librerías.
4. **El diseño ya tiene identidad; lo que falta es "sistema operativo de producto".** Nuestro
   monocromo + verde ácido + Space Grotesk es una **postura diferenciadora** frente al color
   dinámico de Material You. La jugada correcta: **tomar la física/motion de M3 Expressive y
   NO su paleta**. Y blindar accesibilidad (contraste AA, target size) + adaptativo.
5. **El marketing de pago no es el motor inicial.** CPI de ~1,5–4,6 $/instalación hace
   inviable comprar tracción con audiencia pequeña. El motor real son **comunidades
   (Reddit/Discord/creadores) + ASO honesto + retención**. La ventaja "nativo vs Backloggd web"
   ayuda a **convertir**, no a **descubrir**.
6. **El proceso ya es bueno; falta el bucle "medir → decidir".** F0–F4 + changelog nos pone
   por delante del indie medio. Falta: **SLO de calidad (crash-free ≥ 99,7 %)**, **plan de
   medición GA4**, **ADRs** y **baseline propio de retención**. Todo eso ya está montado en
   [`docs/metodologia/`](../metodologia/README.md).

---

## 2. Publicación en Google Play (2026) — lo que nos obliga

| Requisito | Detalle | Estado en GameVision |
|---|---|---|
| **Target API** | Nuevas apps/updates deben apuntar a **Android 16 (API 36)** desde **31-ago-2026** | ✅ `targetSdk 36` (cumplimos justo) |
| **Edge-to-edge** | Obligatorio en targetSdk 36; **no se puede desactivar** | ✅ Ya aplicado |
| **Predictive back** | Activado por defecto en targetSdk 36; `onBackPressed` deja de llamarse | ⚠️ Verificar gestos de back |
| **Verificación de desarrollador** | Refuerzo en 2026: apps instalables exigen dev verificado | ⬜ Al registrarnos |
| **Testing obligatorio** | **Cuentas personales nuevas:** 12 testers / 14 días en closed testing antes de producción | ⬜ Planificar |
| **Data safety** | Formulario obligatorio (recogida/uso/borrado de datos) | ⬜ Redactar |
| **Política de privacidad** | Enlace obligatorio; su ausencia es causa típica de rechazo | ⬜ Redactar |
| **UGC + menores** | Apps sociales deben cumplir Child Safety; moderación/reporte/bloqueo | ⚠️ **Crítico** (somos red social) |
| **Cuenta** | Personal (25 $, 12 testers) vs **organización (D-U-N-S, sin requisito de 12 testers)** | ⬜ **Decidir** |

**Riesgo específico nuestro:** somos una **red social con contenido de usuarios** (perfiles,
amigos, mensajes, reseñas futuras). Eso nos mete de lleno en las políticas de **UGC + Child
Safety**, con ampliación en 2026 a chat anónimo/aleatorio. Necesitaremos: términos de uso,
normas de contenido, **reportar + bloquear** (ya está en F2), y declararlo todo en Data Safety.

**Implicación de decisión:** valorar publicar como **cuenta de organización** → evita el
requisito de 12 testers/14 días (que puede costar días de calendario y depende de tener 12
personas dispuestas). Contrapartida: exige **D-U-N-S** y papeleo de empresa.

---

## 3. Monetización — qué modelo, con qué formatos

**Recomendación por etapas:**

| Etapa | Qué | Por qué |
|---|---|---|
| **Ahora (publicación)** | AdMob: **banner discreto** (zona no interactiva) + **native** en feeds + **rewarded opcional** para desbloquear funciones | Ingresos sin destrozar la UX de un tracker de sesiones cortas |
| **Fase 2** | **"Quitar anuncios"** como compra única vía **Play Billing v8** (deadline de migración 2026) | Flujo de pago simple, mide intención de pago |
| **Fase 3 (si escala)** | Suscripción premium + **mediación** (MAX/LevelPlay) | Solo con volumen que justifique complejidad |

**Reglas de oro (evitan suspensión):**
- **UMP/GDPR desde el día uno** (consentimiento obligatorio en UE; formulario conforme IAB TCF).
- **Anuncios de prueba en desarrollo**, nunca IDs de producción (suspensión por tráfico no válido).
- **Nunca** banners junto a botones interactivos (clics accidentales) ni interstitials sorpresa.
- Nada de anuncios en pantallas de carga que inciten clic.

**Sobre las cifras:** los eCPM son **volátiles y dispares** entre fuentes (banner Tier 1
$0,50–1,50; medios de ~4,80 $ en Europa en 2023; quejas de eCPM "muy bajo" incluso en Tier 1).
**No proyectar ingresos con un número único**; usar escenarios por rango. Para audiencia
pequeña, el realismo es que los anuncios cubren costes, no que den un sueldo.

---

## 4. Stack Android/Kotlin/Compose — qué tenemos y qué toca

| Componente | Tenemos | Referencia 2026 | Acción |
|---|---|---|---|
| Gradle / AGP | 9.6 / 9.4.1 | 9.6 / 9.4 | ✅ Nada |
| Kotlin | **2.2.10** | **2.4.20** | ⚠️ **Subir con cuidado** (arrastra ABI de librerías) |
| Compose BOM / Material3 | 2026.09 / 1.4 | 2026.09 / 1.4 | ✅ Nada |
| compileSdk / target | 37 / 36 | 37 / 36 | ✅ Nada (verificar 16 KB page size antes de 2027-02-01) |
| Navigation | **Nav2 2.10.2** | **Nav3 1.2.0 estable** | ⚠️ Migrar solo si necesitamos list-detail/tablet |
| Hilt | 2.59.2 | 2.60.1 | ⬜ Subida menor barata |
| Rendimiento | — | Baseline Profiles (+~30 %) | ⬜ Añadir cuando haya runtime estable |

**Notas críticas:**
- **Límite duro se mantiene:** el compilador Kotlin integrado de AGP 9.4.1 lee metadatos hasta
  **2.3.0**; por eso Coil 3.3.0 y googleid 1.1.1 están congelados. Subir Kotlin hay que
  hacerlo **coordinado** con AGP y esas librerías.
- **16 KB page size:** deadline oficial **2027-02-01** (los blogs que dicen 2025-11-01 están
  desactualizados). Solo afecta si hay código nativo; probar igualmente.
- **Nav3** es estable y es la vía para el **lista-detalle** que pide F4/tablet; Nav2 no está
  mal, pero es la rama que se deja atrás.

---

## 5. Diseño y UX — elevar sin perder identidad

- **Tomar de Material 3 Expressive la física (motion), no la paleta.** Nuestro monocromo +
  acento ácido es diferenciación, no defecto.
- **Riesgo de contraste AA:** el verde ácido `#C8F135` funciona como *fill* sobre oscuro, **no
  como texto sobre claro**. Revisar pares de color.
- **Skeletons ya adoptados**, pero deben **replicar el layout real** y tener timeout + estado
  vacío; un skeleton genérico se percibe más lento.
- **Patrones del nicho a formalizar** (los líderes los tienen): diario, backlog/watchlist,
  listas, **Top 4**, estados ricos, reseñas cortas, **estadísticas de perfil**. Todo esto ya
  está en F1–F2.
- **Gamificación sana:** celebrar acumulación (Rewind / "tu año"), **nunca castigar la
  ausencia** (sin rachas que se rompan, sin notificaciones culposas). Hay literatura
  peer-reviewed sobre *dark patterns* que lo respalda.
- **Onboarding:** primer valor < 60 s → registrar algo y verlo en el perfil. El social va
  *después* del acto privado de registrar.

---

## 6. Marketing y ASO — la tracción real

- **La ficha vende el hábito, no features.** Mensaje: "registra lo que juegas y no lo
  olvides", no "más listas". Keywords en título/descripción corta (límites oficiales: título
  30, corta 80, larga 4.000, 8 capturas/2 mínimo).
- **Las keywords abren la puerta; conversión + retención deciden la posición.** Google no
  publica factores de ranking; su sistema cambia continuo. Los "lifts" mágicos de las guías
  ASO son **contenido de vendedores**, varios con conflicto de interés → tratar como hipótesis.
- **No comprar instalaciones al principio.** CPI ~1,5–4,6 $ (Games EE.UU. ~4,63 $); con LTV
  bajo no se recupera, y el algoritmo de Google no aprende con volúmenes pequeños.
- **Canales orgánicos de nicho:** subreddits de backlog/retro/tracking, servidores Discord,
  X/Twitter, TikTok/YouTube Shorts (clip-first), y **creadores que ya loguean juegos** (el
  multiplicador más barato). Product Hunt/HN dan pico a *herramientas*, no a la audiencia final.
- **Soft launch:** 3–5 países por *market fit* antes del gran lanzamiento; **beta cerrada**
  para cazar crashes/ANR antes de que las 1★ hundan el rating (umbral competitivo 4,5★).
- **Construir audiencia/waitlist antes de lanzar**; el día 1 en frío es el error clásico.

---

## 7. Métricas, calidad y proceso — de "documentado" a "auditable"

**Líneas rojas de Play (Android Vitals):** crash percibido ≥ **1,09 %** y ANR ≥ **0,47 %**
degradan visibilidad. Nuestro **objetivo interno** debe ser **crash-free ≥ 99,7 %** (los
umbrales de Play son el mínimo para no ser penalizado, no un nivel de calidad).

**Baseline de retención:** fijar nuestro **propio** baseline (semana 1 post-lanzamiento) y
medir **tendencia**; las cifras de sector oscilan demasiado (D7 desde 6,89 % a ~16 % según
metodología). Objetivos internos a fijar: **TTFV < 60 s**, **≥ 1 registro en la 1.ª sesión**,
**D7 > 25 %**.

**Sistema de trabajo (creado en [`docs/metodologia/`](../metodologia/README.md)):**
Shape Up (ciclos con "appetite") + trunk-based + Conventional Commits + SemVer + Keep a
Changelog + **ADR** + Definition of Done + tracks de Play (internal → closed → open) +
staged rollout (5 % → 10 % → 50 % → 100 % con pausa) + CI/CD con GitHub Actions.

**Matiz importante:** todo ese "ceremonial" es mucho para un humano solo, **pero paga doble
con agentes de IA**, porque el agente **no tiene memoria entre sesiones** — el rastro escrito
es lo que permite continuidad. Recomendación: **automatizar** el changelog (generarlo de los
commits convencionales) para que cueste casi cero.

---

## 8. Confianza, contradicciones y huecos (sin maquillar)

**Contradicciones detectadas (no aplanadas):**
1. **Nº de testers (12 vs 20):** el requisito difundido era 20; las fuentes de 2026 dicen 12.
   La ayuda oficial confirma 12/14 días, pero Google ajusta este umbral a menudo → **revalidar
   en Play Console** antes de planificar.
2. **Target API (35 vs 36):** mezcla de mensajes; lo más probable es API 35 en 2025 y API 36
   en 2026. Nuestro 36 cumple el requisito actual.
3. **eCPM:** las cifras se contradicen mucho entre fuentes y años → **no tratar ninguna como
   garantía**.
4. **Peso de keywords:** las guías ASO dicen que el título es decisivo; el análisis 2026 dice
   que las keywords "ya no deciden dónde rankea". Síntesis defendible: **indexan, no coronan**.

**Huecos declarados (no confirmados en esta ronda):**
- Coste exacto de la cuenta (25 $), obligatoriedad de AAB y sus límites, Play Asset Delivery.
- Declaración de anuncios en Play Console (existe el campo, sin cita verbatim capturada).
- Porcentajes exactos de staged rollout desde fuente primaria (la práctica es estándar).
- Última versión de Room y Coil (no re-verificada); ajuste exacto de versión de Kotlin bajo
  *built-in Kotlin* de AGP 9.4.

**Nota de fiabilidad:** varias consultas devolvieron resultados de baja calidad o fuera de
tema; las evidencias de **mayor confianza** son las de fuentes oficiales (Android Developers,
Kotlin docs, AdMob/Play) y la literatura académica sobre dark patterns. Las de "tendencias" y
"tokens SSOT" son de **confianza media-baja**.

---

## 9. Decisiones recomendadas (listas para ADR)

| # | Decisión | Recomendación | Ver |
|---|---|---|---|
| **D-E1** | ¿Cuenta personal u organización? | **Organización** (evita 12 testers/14 días) si el papeleo D-U-N-S es asumible; si no, personal + plan de 12 testers | §2 |
| **D-E2** | Modelo de monetización | **Anuncios discretos (AdMob) + "quitar anuncios" de pago único**; suscripción y mediación más adelante | §3 |
| **D-E3** | Orden de formatos de anuncio | **Banner discreto + native + rewarded opcional**; sin interstitial/app-open agresivos | §3 |
| **D-E4** | Subir Kotlin | **Sí, pero coordinado** (AGP + KSP + Coil/googleid): planificar como una fase técnica, no como un cambio suelto | §4 |
| **D-E5** | Migrar a Navigation 3 | **Cuando toque F4/tablet** (list-detail). No ahora | §4 |
| **D-E6** | Material 3 Expressive | **Adoptar motion/física; mantener paleta propia** | §5 |
| **D-E7** | Tracción inicial | **Orgánico (comunidad + creadores) + ASO; cero pago al principio** | §6 |
| **D-E8** | Calidad | **SLO crash-free ≥ 99,7 %** con Crashlytics; play vitals como línea roja | §7 |
| **D-E9** | Proceso | Adoptar **GDF** (`docs/metodologia/`) + ADRs + changelog autogenerado | §7 |

---

## 10. Cómo encaja con el roadmap actual

- **F0–F4 siguen vigentes** (biblioteca rica, social, "wow", nativo). No cambian.
- Se **añade un hito transversal nuevo: "Publicación"**, que atraviesa F1–F4:
  cuenta + verificación → ficha de tienda + Data Safety + privacidad → integración de
  anuncios + UMP → beta cerrada → staged rollout.
- Se **formalizan 3 decisiones bloqueantes** que ya estaban vivas: cuenta, monetización y
  fuente de la duración. Las dos primeras quedan arriba con recomendación; la tercera sigue
  pendiente de ti.

---

## 11. Próximos pasos concretos (propuesta)

1. **Cerrar las decisiones D-E1, D-E2, D-E4** (cuenta, monetización, Kotlin) como ADRs.
2. **Terminar F0** (adapter en curso): caché Room + modelo de biblioteca + Storage/foto.
3. **Preparar el hito de publicación en paralelo**: cuenta de desarrollador, borrador de
   política de privacidad y de Data Safety, plan de 12 testers (si cuenta personal).
4. **Instrumentar** (Firebase Analytics + Crashlytics) al cerrar F1.
5. **Montar CI** (GitHub Actions: build + tests + changelog autogenerado).

---

## Fuentes

Material crudo por frente en [`fuentes/`](fuentes/):
[01 · Play Store](fuentes/01-play-store-2026.md) ·
[02 · Monetización](fuentes/02-monetizacion-anuncios.md) ·
[03 · Stack Android](fuentes/03-stack-android-2026.md) ·
[04 · Diseño/UX](fuentes/04-diseno-ux-2026.md) ·
[05 · Marketing/ASO](fuentes/05-marketing-aso-lanzamiento.md) ·
[06 · Métricas/proceso](fuentes/06-metricas-calidad-proceso.md).

Cada frente incluye sus URLs y citas textuales con nivel de confianza. Fuentes primarias más
citadas: `developer.android.com`, `kotlinlang.org`, `support.google.com/googleplay`,
`developers.google.com/admob`, `blog.google`, más literatura académica sobre *dark patterns*.
