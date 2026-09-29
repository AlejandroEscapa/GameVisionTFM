# ADR-0007 — Foto de perfil: base64 comprimido en Firestore (sin Cloud Storage)

- **ADR:** 0007
- **Título:** La foto de perfil se guarda comprimida (base64) en Firestore, no en Cloud Storage
- **Estado:** Aceptado
- **Fecha:** 2026-09-29
- **Decisores:** Alejandro Olivares Escapa (propietario) · propuesta y ejecución del agente
- **Supersede a:** el uso de **Cloud Storage** introducido en T0.12 (F0)
- **Fase relacionada:** F0 (Cimientos de datos) — deuda conocida C / T0.12

## Contexto

Al activar **Cloud Storage for Firebase** en el proyecto, la consola exige **actualizar al plan
Blaze** (asociar facturación) aunque el uso sea mínimo. El propietario no quiere meter
facturación en esta etapa.

El alcance real es pequeño: **el único uso de Storage en todo el proyecto es la foto de perfil**
(1 archivo por usuario, ≤5 MB). La investigación previa
([almacenamiento-imagenes-2026](../../investigacion-2026/almacenamiento-imagenes-2026.md))
comparó alternativas (RTDB, Firestore, Supabase, R2, hosts externos) y concluyó que cualquier
solución de **≤1 GB** sobra de largo.

Restricción añadida: la alternativa debe **funcionar sin pasos manuales nuevos en la consola**
que dependan de terceros y debe ser **verificable ya** (CA0.3 de F0).

## Decisión

Guardar la foto de perfil **comprimida en el cliente** (JPEG de 512 px máx., calidad 80) como
**base64** en un documento propio de Firestore, **`profile_images/{uid}`**, y persistir en el
perfil solo un **puntero** (`imageUri = "firestore://profile_images"`).

- **Compresión en el dispositivo** antes de subir: es lo que hace viable el base64.
- **Documento aparte**: el perfil no engorda (se lee en cada pantalla), y la imagen se lee solo
  cuando se muestra el avatar.
- **Carga en la UI**: la imagen se sirve como **data URI** (`data:image/jpeg;base64,…`). Coil 3
  soporta data URIs de forma nativa (`DataUriFetcher`), así que la capa de imagen no cambia.
- **Límites explícitos y visibles al usuario**:
  - Archivo elegido: máx. **5 MB** → si lo supera, aviso directo («La imagen supera el máximo de
    5 MB. Elige una más ligera.»).
  - Resultado comprimido: máx. **256 KB** → si aun así lo supera, aviso equivalente.

## Alternativas consideradas

| Alternativa | Pros | Contras | ¿Por qué no? |
|---|---|---|---|
| **Cloud Storage + Blaze** | La vía "oficial"; CDN | **Requiere facturación** | Justo lo que se quiere evitar en esta etapa |
| **Realtime Database + base64** | Gratis (1 GB) en Spark; nodo separado | Requiere **habilitar la API + crear la instancia** (pasos en consola); la API estaba deshabilitada y la cuenta de servicio no puede habilitarla | Bloquea hoy; se queda como **plan alternativo** si hiciera falta |
| **Firestore + base64 (elegida)** | **Cero pasos nuevos**; ya habilitado; verificable hoy; 1 GiB gratis | Comparte el 1 GiB con el resto de datos | **Elegida**: el uso cabe de sobra y no añade infraestructura |
| **Supabase / R2 / ImgBB** | Capacidad | Proveedor nuevo, otra cuenta, (R2 pide tarjeta) | Añade superficie sin necesidad |

## Consecuencias

**Positivas**
- **Sin coste y sin facturación**: se queda dentro de la cuota gratuita de Firestore.
- **Sin servicios nuevos**: un proveedor menos que operar.
- **CA0.3 verificable de inmediato** (hecho: ver auditoría de F0).
- **Reversible**: toda la lógica está en `ProfileImageStorage`; cambiar de backend no toca la UI.

**Negativas / coste asumido**
- La imagen comparte el **1 GiB** gratuito de Firestore con el resto de datos (≈**8.000 avatares**
  a ~130 KB; la salida mensual de 10 GiB cubre ≈**80.000 vistas/mes**).
- Base64 engorda ~33 % respecto al binario (asumido; la compresión lo compensa).
- El límite de **1 MiB por documento** de Firestore obliga a mantener la imagen pequeña
  (por eso el tope de 256 KB).

**Riesgos y mitigación**
- Subir una imagen enorme → la compresión + el tope de 256 KB lo evitan, y el usuario recibe un aviso claro.
- Crecimiento que supere lo gratuito → el aviso por tamaño prepara el terreno; migrar a un bucket
  real (Blaze/R2/RTDB) es un cambio localizado.

## Seguimiento

- Si el producto introduce **muchas imágenes** (carátulas, galerías, export) o **vídeo**, reevaluar
  con un ADR nuevo (bucket real o CDN).
- Vigilar el consumo de Firestore cuando haya usuarios reales; el disparador de monetización de
  etapa 2 (≈1.000 usuarios, ADR-0005) es un buen momento para revisarlo.
