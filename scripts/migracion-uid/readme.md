# Migración de identidad email → uid (ADR-0008)

> Herramienta que migró los datos del modelo antiguo (clave = **email**) al modelo nuevo
> (clave = **uid**, ADR-0008). Se conserva para poder **reproducir la migración** en otro
> entorno (p. ej. producción) o auditar cómo se hizo.

## Qué migra

1. **Perfil**: de `users/{email}` a `users/{uid}`, añadiendo el **campo `email`**.
2. **Amigos**: de `users/{email}/friends/{friendEmail}` a `users/{uid}/friends/{friendUid}`
   (resolviendo cada email a su uid).
3. **Mensajes**: copiados de `users/{email}/messages/*` a `users/{uid}/messages/*`.
4. **Índices**: crea `email_index/{email} → { uid }` y `usernames/{username} → { uid }`.
5. **Limpieza**: borra el documento antiguo `users/{email}` y sus subcolecciones de primer nivel.

## Cómo se usa

```bash
# 1) Ensayo (no escribe nada): muestra qué haría
node migrate-uid.js <ruta-service-account.json> --dry-run

# 2) Migración real
node migrate-uid.js <ruta-service-account.json>
```

El script usa la **cuenta de servicio** de Firebase (Admin) para:
- resolver `email → uid` con Identity Toolkit,
- leer/escribir Firestore por REST.

**Backup automático:** antes de tocar nada, guarda un JSON por usuario en
`migration-backup/` (junto al script) con su perfil, amigos y mensajes originales.
Es la red de seguridad para revertir.

## Requisitos

- Node 22+.
- Cuenta de servicio con permisos de Firestore + Identity Toolkit (la de `firebase-adminsdk`).
- **Nunca** subir la cuenta de servicio al repositorio (vive en `.secrets/`, fuera del control de versiones).

## Idempotencia

Se puede ejecutar más de una vez: los documentos raíz cuyo id **ya es un uid** (sin `@`) se
ignoran. Es lo que permitió reintentar tras corregir el escapado de rutas con espacios.
