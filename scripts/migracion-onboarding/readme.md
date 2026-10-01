# Migración: semilla del onboarding (02/10/2026)

Marca `onboardingDone: true` en las cuentas **anteriores** al onboarding (bloque C de la
iteración). Sin esto, los usuarios ya registrados verían el onboarding al abrir la app.

- **Ejecutado:** 01/10/2026 — 10 cuentas marcadas en producción.
- **Dry-run por defecto**; con `--commit` escribe.
- Requiere `firebase-admin` y la clave de servicio en `.secrets/gamevision-admin.json`.

```bash
npm install firebase-admin          # si no hay node_modules disponible
node seed-onboarding.mjs            # dry-run
node seed-onboarding.mjs --commit   # aplica
```
