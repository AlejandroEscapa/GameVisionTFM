## Dimension 06: Métricas, calidad y proceso de desarrollo (2026)

### Current State

GameVision ya opera por fases documentadas (`docs/roadmap`, F0–F4) con roadmap, registro de decisiones abiertas/cerradas, criterios de aceptación y changelog. Eso lo coloca por delante de la mayoría de proyectos indie, que no tienen ni changelog. El hueco real no es "tener proceso", sino **conectar el proceso con evidencia medible en producción**: hoy falta un bucle cerrado *instrumentar → medir → decidir → documentar*. La industria en 2025–2026 converge en un estándar bastante estable:

- **Analítica de producto:** Firebase Analytics (basado en GA4) es el estándar de facto para apps, con eventos recomendados, "key events"/conversiones, audiencias y embudos. La disciplina clave no es qué herramienta, sino **definir el plan de medición antes de implementar** y no marcar eventos "porque son fáciles de trackear".
- **Calidad/crashes:** Google Play Android Vitals publica umbrales concretos de "bad behavior" (**crash percibido por usuario ≥ 1.09%** y **ANR ≥ 0.47%**) que degradan la visibilidad. Los objetivos internos sanos son más estrictos (crash-free ≥ 99.7%, o al menos ≥ 99.5%).
- **Cambios sin recompilar:** Remote Config + Firebase A/B Testing permiten togglear flags y experimentar sin nuevo release; Play/App Store ofrecen experiments de ficha (listing) y staged rollout por porcentaje.
- **Metodología:** para un dev solo + agentes IA, el consenso es **trunk-based development** con ramas cortas, Conventional Commits + SemVer + Keep a Changelog + ADR; y marcos de foco tipo **Shape Up** (ciclos de 6 semanas, "appetite" en vez de estimación) para mantener ritmo sostenible.
- **Lanzamiento seguro:** tracks internal → closed → open → producción + staged rollout porcentual; CI/CD con GitHub Actions; observabilidad vía Crashlytics + feedback de usuarios + gestión de versiones.

La conclusión operativa para GameVision: el roadmap F0–F4 ya es la base; lo que falta es **formalizar KPIs con umbrales numéricos, un DoD verificable, ADRs, y un SLO de calidad (crash-free) con alertas**. El resto del trabajo es disciplina, no herramientas nuevas.

---

### Key Evidence

| Dato/Práctica | Descripción | Fecha | Cita textual verbatim | Confianza | Fuente |
|---|---|---|---|---|---|
| Umbral "bad behavior" Play Vitals | Google Play penaliza (degradación de visibilidad) por encima de estos umbrales | 2026 | "Google Play's own Android vitals treats a user-perceived crash rate at or above 1.09% (or an ANR rate at or above 0.47%) as a “bad behavior” threshold that …" | Alta | qualflare.com [^1] |
| Objetivos de calidad (saludable vs riesgo) | Bandas de referencia para crash y ANR en Android | 2026 | "Crash-free sessions, ≥ 99.7%, 99.0–99.7% ; User-perceived crash rate (Android), < 0.5% DAU, 0.5–1.09% ; User-perceived ANR rate (Android), < 0.2% DAU, 0.2–0.47%" | Alta | kobiton.com [^2] |
| Target crash-free en producción | Objetivo recomendado de estabilidad | 2026-03 | "Crash-free user rate: Target >99.5% for production apps." | Media | johal.in [^3] |
| Penalización por Vitals bajos | Impacto directo de crashes/ANR en la ficha | 2026-07 | "Google Play's Android Vitals flags apps that exceed crash rate thresholds and application-not-responding limits. Poor scores directly impact …" | Media | digital.ai [^4] |
| Retención D7/D30 iOS y Android | Benchmarks 2025 de retención | 2025-11 | "According to the Pushwoosh Benchmarks Study 2025, the average Day 7 retention rate on iOS is 6.89%, dropping to 3.10% by Day 30." | Alta | pushwoosh.com [^5] |
| Benchmarks D1/D7/D30 (rango) | Cifras agregadas de industry benchmarks | 2026-03 | "D1 around 29–33%, D7 around 16%, and D30 around 8.7%, with figures split between Adjust's benchmark report and Sendbird's industry benchmarks." | Alta | lovable.dev [^6] |
| D1/D7/D30 fintech (buen techo) | Mejor sector en retención | 2026 | "Fintech leads on retention: D1 30%, D7 17.6%, D30 11.6% — driven by habitual balance and transaction checks" | Media | core-mba.pro [^7] |
| Framework D1/D7/D30 (a16z) | Marco estándar de curvas de retención | 2023-03 | "For n-day retention, we focus on three primary points in time: d1, d7, and d30." | Alta | a16z.com [^8] |
| D1 típico y caída primera semana | Pérdida temprana de usuarios | 2024-12 | "Day-1 retention 30-40 % Retention drops sharply in the first week, then the decline slows. A typical mobile app loses 75% of its users within …" | Media | uxcam.com [^9] |
| D7/D30 shopping | Referencia por vertical | 2024-03 | "The day seven benchmark is 10.7%, while the average shopping app sees a day 30 retention rate of 5.6%, or 4.83% according to AppsFlyer." | Media | sendbird.com [^10] |
| Plan de medición antes de launch | Práctica de instrumentación | 2026 | "You should test events, parameters, conversions, audiences, and sequence logic before launch, not after the reports are already in front of stakeholders." | Media | ituonline.com [^11] |
| Definir key events, no "fáciles" | Anti-patrón de analítica | 2026 | "A measurement plan should define key events before implementation rather than marking arbitrary interactions simply because they are easy to track." | Media | compubrain.com [^12] |
| Remote Config como feature flags | Experimentar sin redeploy | 2026-07 | "Implement feature flags using Firebase Remote Config to toggle UI experiments without redeploying. Conduct beta testing via Google Play's …" | Media | shivatechdigital.com [^13] |
| Remote Config + A/B para tuning | Ajuste de parámetros sin release | 2026 | "By using Firebase Remote Config and running A/B testing, you can quickly adjust ad serving rates, reward levels, and user …" | Media | itpiran.net [^14] |
| Trunk-based para equipos pequeños | Encaje con dev solo/equipo chico | 2025-09 | "For small teams, trunk-based feels natural." | Alta | mergify.com [^15] |
| Definición de trunk-based | Práctica de ramas cortas | 2026 | "Trunk-based development is a version control management practice where developers merge small, frequent updates to a core “trunk” or main branch." | Alta | atlassian.com [^16] |
| Decision log (ADR) + SemVer/commits | Trio de trazabilidad de decisiones | 2026 | "Publish a decision log (ADR folder) for architectural choices, so debates don't repeat. … We use SemVer, conventional commits, and automated changelog + release …" | Media | wild.codes [^17] |
| Conventional Commits + ADR | Disciplina de historial auditable | 2026-07 | "Conventional Commits, SemVer, changelog discipline, gives it a clean conventional-commit history, and records the decision in an ADR." | Media | ayokoding.com [^18] |
| Flujo commits→changelog (Keep a Changelog) | Plantilla de contribución replicable | — | "Make commits using the Conventional Commits format; Update CHANGELOG.md using the Keep a Changelog format; Open a merge request into the main branch; Add a …" | Media | docs-bigbang.dso.mil [^19] |
| Shape Up: ventana protegida de 6 semanas | Foco sin interrupciones | 2026-08 | "Give the assigned team a stable window, normally up to six weeks, and protect it from casual interruptions." | Alta | umbrex.com [^20] |
| Shape Up: ciclos de 6 semanas | Estructura iterativa para equipos chicos | 2025-01 | "Once a project is selected, it moves into a fixed six-week cycle, during which teams work autonomously to deliver it." | Alta | world.hey.com [^21] |
| Shape Up: roles y "appetite" | Inversión de estimate→appetite | 2026-05 | "The methodology is built around three roles (shapers, makers, captains), six-week build cycles, and the inversion of "estimate" with "appetite"." | Alta | guptadeepak.com [^22] |
| CI/CD con GitHub Actions | Automatización build+test+release | 2026-05 | "We use GitHub Actions as our CI/CD platform, with multiple workflows configured to ensure code quality, automate builds, and streamline …" | Media | developers.home-assistant.io [^23] |
| CI/CD móvil con GitHub Actions | Flujo end-to-end a Play | 2026-07 | "In this article, I'll document the complete end-to-end flow to publish a Flutter Android app to Google Play using GitHub Actions." | Media | dev.to [^24] |
| Error indie: demasiadas features a la vez | Anti-patrón recurrente | 2025-11 | "Common mistakes include trying to build too many features at once, skipping market research, underestimating costs, and not testing with real …" | Media | manektech.com [^25] |
| Error: ignorar input y falta de documentación | Anti-patrones de proceso | 2022-11 | "Ignoring User Input … Inadequate Code and Test Automation … Lack of Technical Documentation … Not Using Source Control … Over-…" | Media | stackify.com [^26] |
| Error: arrancar con requisitos vagos | Causa de fracaso post-arranque | 2026-09 | "Why Do App Projects Fail After Development Has Already Started? Mistake 4 Starting Development With Vague Requirements" | Media | conceptinfoway.net [^27] |

**Lectura de la evidencia.** Los dos números que más importan para GameVision son: (a) el umbral de Play Vitals (crash ≥ 1.09% / ANR ≥ 0.47%) como **línea roja externa**, y (b) el objetivo interno de **crash-free ≥ 99.7%** como meta de ingeniería. Para producto, la banda realista de una app de tracking es **D1 30–40% / D7 ~11–16% / D30 ~5.6–8.7%** (mejor en apps "habituales" tipo fintech). El "time-to-first-value" y "% de usuarios con ≥1 registro" no aparecen con cifras de sector limpias en esta ronda de búsqueda; se recomienda fijarlos como objetivos internos (p. ej. TTFV < 2 min, ≥1 registro en primera sesión) en vez de compararlos contra un benchmark que las fuentes no validan.

Fuentes:
[^1]: Qualflare. "Mobile Release Readiness: Quality Gates for Android & iOS (2026)". 2026. https://qualflare.com/
[^2]: Kobiton. "Mobile Application Performance Monitoring Complete Guide". 2026. https://kobiton.com/
[^3]: Johal. "Incident Response for Mobile App Crashes Affecting User Base". 2026-03-18. https://www.johal.in/
[^4]: Digital.ai. "Decoding Mobile App Crashes — From Chaos to Clarity". 2026-07-01. https://digital.ai/
[^5]: Pushwoosh. "Increase app retention 2026: Benchmarks, strategies". 2025-11-20. https://www.pushwoosh.com/
[^6]: Lovable. "What Is a Good Retention Rate for an Application?". 2026-03-13. https://lovable.dev/
[^7]: CORE MBA. "Mobile App Retention Benchmarks 2026". 2026. https://www.core-mba.pro/
[^8]: a16z. "Do You Have Lightning In a Bottle? How to Benchmark". 2023-03-03. https://a16z.com/
[^9]: UXCam. "Mobile App Retention Benchmarks by Industry (2026)". 2024-12-22. https://uxcam.com/
[^10]: Sendbird. "App retention benchmarks broken down by industry". 2024-03-12. https://sendbird.com/
[^11]: ITU Online. "Prerequisites for Advanced GA4 Implementation". 2026. https://www.ituonline.com/
[^12]: CompuBrain. "Google Analytics: Setup, GA4 & Conversion Tracking Guide". 2026. https://compubrain.com/
[^13]: Shiva Tech Digital. "Flutter vs React Native: 2026 Comparison". 2026-07-16. https://shivatechdigital.com/
[^14]: ITPiran. "Creating an Android game app, launching Google Mob ads". 2026. https://www.itpiran.net/
[^15]: Mergify. "Trunk-Based Development vs Gitflow: Which Branching". 2025-09-05. https://mergify.com/
[^16]: Atlassian. "Trunk-based Development". 2026. https://www.atlassian.com/
[^17]: Wild.codes. "How do you run a workflow for distributed OSS collaborators?". 2026. https://wild.codes/
[^18]: AyoKoding. "Overview". 2026-07-17. https://www.ayokoding.com/
[^19]: Big Bang Docs. "Contributing". —. https://docs-bigbang.dso.mil/
[^20]: Umbrex. "Shape Up Method". 2026-08-21. https://umbrex.com/
[^21]: Basecamp (world.hey.com). "Adapting Basecamp's Shape Up for IT Infrastructure Projects". 2025-01-15. https://world.hey.com/
[^22]: GuptaDeepak. "Shape Up review, Books". 2026-05-18. https://guptadeepak.com/
[^23]: Home Assistant. "Android continuous integration and delivery". 2026-05-01. https://developers.home-assistant.io/
[^24]: DEV Community. "Automating Flutter Android Releases to Google Play using GitHub Actions". 2026-07-06. https://dev.to/
[^25]: Manektech. "10 Common Mobile App Development Mistakes". 2025-11-12. https://www.manektech.com/
[^26]: Stackify. "Mistakes to Avoid in Software Development Projects". 2022-11-16. https://stackify.com/
[^27]: Concept Infoway. "Top 10 Common Mobile App Development Mistakes to Avoid". 2026-09-15. https://www.conceptinfoway.net/

---

### Tensions & Counter-arguments

1. **Trunk-based vs git-flow no es binario.** Para un dev solo, trunk-based + ramas cortas es lo natural ([^15]) y encaja con CI/CD; pero el paper comparativo (arXiv, 2025) muestra que modelos tipo GitFlow siguen siendo populares y no hay evidencia de que *uno* sea universalmente superior. Riesgo real: adoptar trunk-based *sin* tests automatizados ni feature flags convierte "main siempre desplegable" en un dogma que rompe producción. Regla sana: trunk-based **solo si** existe CI verde + feature flags (Remote Config) como red de seguridad.

2. **Remote Config "sin recompilar" tiene límites.** Las fuentes ([^13], [^14]) venden el "adjust without redeploy", pero Remote Config sólo cambia *valores/parámetros*, no lógica nueva ni UI no compilada; y las cachés/intervalos de fetch introducen latencia y sesgo de rollout (usuarios que no actualizan el valor). A/B testing en Firebase además no da poder estadístico con volúmenes bajos — un indie con pocos usuarios no puede declarar ganadores fiables. Contrapunto: usar Remote Config para *kill-switches* y tuning, no como motor de producto.

3. **Umbrales de Vitals: línea roja ≠ objetivo.** Que Play marque "bad behavior" a partir de 1.09% de crash no significa que 1.0% sea aceptable; un indie debería apuntar a crash-free ≥ 99.7% ([^2]) porque los umbrales de Play son el *mínimo para no ser penalizado*, no un nivel de calidad. Confundir ambos lleva a normalizar una app mediocre.

4. **Benchmarks de retención son ruidosos y dependen del vertical.** Las cifras oscilan mucho: Pushwoosh da D7 iOS 6.89% ([^5]), mientras agregados de industry benchmarks dan D7 ~16% ([^6]) y UXCam dice D1 30–40% ([^9]). Son metodologías y cohortes distintas. Comparar GameVision contra un número único es engañoso; lo correcto es **fijar baseline propio** (semana 1 post-launch) y medir *tendencia*, no absolutismo. Además, D1/D7/D30 sin segmentación por fuente de adquisición (orgánico vs referidos) producen conclusiones falsas.

5. **Documentar en exceso es un anti-patrón de dev solo.** ADR + Conventional Commits + Keep a Changelog + Shape Up + roadmap por fases ([^17], [^18], [^20]) es mucho ceremonial para una persona. El propio campo advierte "ignoring user input" y "over-engineering" ([^26]). Contrapunto: con agentes IA actuando como "equipo", la trazabilidad (ADR, changelog) paga más que en un humano solo, porque **el agente no tiene memoria entre sesiones**. Recomendación: automatizar el ceremonial (changelog autogenerado de Conventional Commits, plantillas de ADR) para que cueste casi cero.

6. **Staged rollout y tracks de Play: práctica sólida, evidencia primaria no recuperada.** Google Play ofrece tracks internal → closed → open → producción y despliegue por porcentaje con opción de *halt*. Esta ronda de búsqueda **no recuperó una fuente primaria verbatim** de developers.android.com con los porcentajes exactos, así que se marca como **Confianza media-baja**: la práctica es estándar y verificable en la consola, pero conviene citar `developer.android.com` directamente antes de fijarla como referencia numérica. Aplicarla igual: 5% → 10% → 50% → 100% con pausa, es de bajo riesgo.

7. **"¿Cuándo es la app 'terminada'?"** El marco Shape Up ("fixed time, variable scope", [^21][^22]) choca con la mentalidad TFM de "entregar el alcance completo". Para un TFM→producto, el riesgo es arrastrar alcance académico a un producto que debe iterar. Contrapunto: Shape Up es potente pero presupone un "shaper" que decide qué *no* hacer; un dev único puede acabar siendo shaper y maker a la vez, colapsando el proceso. Mitigación: separar explícitamente en el roadmap "ciclos de construcción" (producto) de "hitos de entrega" (académico/TFM).

8. **Observabilidad ≠ solo Crashlytics.** El feedback cualitativo (reseñas, soporte) suele explicar *por qué* cae la retención mejor que la telemetría, pero no aparece con peso en las fuentes técnicas de calidad ([^1][^2]), sesgadas a métricas de crash. Riesgo de sobre-optimizar estabilidad (números) e ignorar experiencia (causa raíz del churn).

**Síntesis accionable para GameVision:** mantener F0–F4, añadir (a) SLO crash-free ≥ 99.7% con alertas Crashlytics/Velocity, (b) plan de medición GA4 con eventos recomendados y ≤10 key events, (c) ADRs + Conventional Commits + changelog autogenerado, (d) tracks de Play + staged rollout porcentual, (e) baseline propio de retención D1/D7/D30 y TTFV definido internamente. Con eso el sistema pasa de "documentado" a "auditable y sostenible".
