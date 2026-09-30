# Migración social (F2/T2.9): amigos → aristas de seguimiento

Convierte las amistades simétricas del modelo antiguo (`users/{x}/friends`) en
las **aristas mutuas** de F2 (`following/{A}_{B}` + `following/{B_A}`, D2.1).

## Por qué

El modelo de amigos exigía permiso mutuo y no escala (D2.1: seguir asimétrico).
La migración preserva las relaciones existentes: cada amistad A↔B se convierte
en "A sigue a B" y "B sigue a A". El chat pre-F2 (`users/{x}/messages`) **no se
toca** (D2.7: congelado, se oculta pero no se borra).

## Uso

```bash
# 1) Siempre primero: DRY-RUN (por defecto, no escribe nada)
node scripts/migracion-social/migrate-social.js ../.secrets/gamevision-admin.json

# 2) Ejecución real (escribe aristas y borra friends; backup automático antes)
node scripts/migracion-social/migrate-social.js ../.secrets/gamevision-admin.json --commit
```

- El backup JSON por usuario se escribe **antes** de tocar nada, en
  `../.secrets/migracion-social-backup/` (fuera del repo, con las demás secrets).
- Idempotente: si una arista ya existe, se deja como está (`exists`).
- Resuelve claves antiguas en tres formatos: email (vía Identity Toolkit),
  uid de Auth (28 alfanuméricos) y username residual (vía índice `usernames`).

## Qué toca y qué no

| Colección | Acción |
|---|---|
| `following/{a}_{b}` | Crea las dos aristas por amistad |
| `users/{x}/friends/{y}` | Backup → crea aristas → **borra** (solo `--commit`) |
| `users/{x}/messages` | **No se toca** (D2.7) |
| `users/{x}` (perfil) | **No se toca** |
