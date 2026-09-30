# Despliegue de índices Firestore sin CLI

En esta máquina **no hay CLI `firebase` ni `gcloud`**, pero la app depende de
índices compuestos declarados en [`firebase/firestore.indexes.json`](../../firebase/firestore.indexes.json).
Este script los despliega por REST v1 con JWT de service account (mismo patrón
que las migraciones).

## Uso

```bash
node scripts/firestore-deploy/deploy-indexes.js ../.secrets/gamevision-admin.json
```

(`--file <ruta>` para usar otro JSON de índices; por defecto el del repo.)

## Qué hace

- PATCH idempotente en `projects/{p}/databases/(default)/collectionGroups/{g}/indexes`
  por cada índice del JSON (updateMask con los fieldPaths).
- Imprime el estado resultante (`CREATING → READY`).
- Exit 1 si algún índice falla.

## Cuándo toca tocar esto

Cada vez que el logcat de la app muestre
`FAILED_PRECONDITION: The query requires an index` para una consulta compuesta:
declarar el índice en `firestore.indexes.json`, desplegar con este script y
**añadirlo al commit** junto al código que introduce la consulta.

## Historial

- 30/09/2026 (F2): primer uso — faltaba `feed(authorUid ASC, createdAt DESC)`;
  sin él la pestaña Social no cargaba el feed.
