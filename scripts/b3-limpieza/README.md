# B3 — Limpieza de datos de prueba (admin)

Herramienta de **un solo uso** para el paso B3 (cierre de F0-B): borra las listas antiguas
(`playedlist`, `wishlist`, `favorites`, `history`) de todos los usuarios y los documentos
basura `aa`/`ee`, **con backup previo en JSON**. Usa el Admin SDK (ignora las reglas de
seguridad; por eso es una herramienta de administración, no parte de la app).

## Requisitos

1. **Clave de servicio** (la descarga el propietario del proyecto):
   Firebase Console → ⚙️ Configuración del proyecto → **Cuentas de servicio** →
   «Firebase Admin SDK» → **Generar nueva clave privada** (JSON).
   Guardarla **fuera del repo**, p. ej. en
   `C:\Users\Usuario\Desktop\GameVision\.secrets\gamevision-admin.json`.
2. Dependencias:

```bash
cd scripts/b3-limpieza
npm install firebase-admin
```

## Uso

```bash
# 1) Simulación (dry-run): enseña qué se borraría, sin tocar nada
node b3-limpieza.mjs "C:\Users\Usuario\Desktop\GameVision\.secrets\gamevision-admin.json"

# 2) Ejecución real: backup primero, luego borrado, luego verificación
node b3-limpieza.mjs "C:\Users\Usuario\Desktop\GameVision\.secrets\gamevision-admin.json" --commit
```

Con `--commit` se guarda un backup en `backups/b3-<fecha>/backup-users.json` (gitignored)
**antes** de borrar nada. Al terminar imprime un resumen de verificación.

> Nota: los **perfiles** (`users/{email}`) NO se tocan — la app sigue usándolos hasta B3/F2.
> Solo se borran las subcolecciones de listas antiguas y los documentos `aa`/`ee`.
