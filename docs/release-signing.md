# Firma de release — GameVision

> Estado: **configurada y verificada el 29/09/2026**.

## Dónde está cada cosa

| Elemento | Ruta | Notas |
|---|---|---|
| Keystore | `C:\Users\Usuario\Desktop\GameVision\.secrets\gamevision-release.jks` | **FUERA del repo** (nunca se commitea). Copia de seguridad obligatoria |
| Credenciales | `.secrets\gamevision-release-keys.txt` + tu gestor de contraseñas | Alias: `gamevision` |
| Config del build | `local.properties` (gitignored) | `storeFile` / `storePassword` / `keyAlias` / `keyPassword` |
| Config de Gradle | `app/build.gradle.kts` → `signingConfigs.release` | Lee de `local.properties`; sin keystore, el release sale sin firmar |

## Cómo compilar y verificar

```bash
./gradlew :app:assembleRelease
# → app/build/outputs/apk/release/app-release.apk

# Verificación de firma:
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Datos del certificado

- **DN:** CN=GameVision, OU=Mobile, O=GameVision, L=Madrid, ST=Madrid, C=ES
- **SHA-1:** `F9:D6:25:31:B2:DD:03:B3:3E:89:89:E5:28:CF:F6:D7:4F:84:F5:9E`
- **SHA-256:** `16:4E:33:B6:25:95:AF:ED:81:3A:39:51:4C:F9:41:9C:79:7E:2F:1E:34:FF:00:2E:2E:0E:52:8F:43:57:09:A1`
- Algoritmo: RSA 4096 · Validez: 10.000 días

## Pendientes relacionados

- **Backup ya**: copia el `.jks` (y las credenciales) a tu gestor de contraseñas / almacén seguro.
- **Firebase**: añadir el **SHA-1 de release** (Consola → Configuración → Tus apps → huella) para que
  Google Sign-In funcione en builds **release**. (El debug ya estaba registrado.)
- **Play (F5)**: al publicar se usará **Play App Signing**; esta clave será la *upload key*.
