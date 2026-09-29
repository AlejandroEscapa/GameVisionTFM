# Almacenamiento de imágenes sin Firebase Storage — investigación (29/09/2026)

> **Motivo.** Al intentar activar Cloud Storage for Firebase en el proyecto
> `gamevision-tfm-b1b4d`, la consola exige **«Actualizar proyecto»** (pasar a plan de pago
> **Blaze**), es decir, **asociar una cuenta de facturación** aunque la cuota que usaríamos sea
> minúscula. El propietario no quiere (de momento) meter facturación, así que hay que buscar
> **alternativa gratuita** para la **foto de perfil** (único uso de Storage en F0/T0.12).
>
> **Alcance real del problema.** Hoy solo almacenamos **un archivo por usuario**: la foto de
> perfil (`profile_images/{email}/profile.jpg`, máx. 5 MB). No es vídeo ni audio ni archivos
> masivos. Esto cambia mucho la decisión: cualquier opción de **≤1 GB** sobra de largo.

---

## 1. El hallazgo, en una frase

Firebase **Storage** ya no está disponible en el plan gratuito **Spark**: requiere **Blaze**
(cuenta de facturación). El resto del proyecto (Auth, **Firestore** —1 GiB gratis— y, si hiciera
falta, **Realtime Database** —1 GB gratis—) **sí sigue funcionando gratis**.

Fuentes consultadas: [Firebase Pricing](https://firebase.google.com/pricing),
[Firestore usage and limits](https://firebase.google.com/docs/firestore/quotas).

## 2. Opciones evaluadas

| # | Opción | Cómo funciona | Gratis | Contra | Encaje |
|---|---|---|---|---|---|
| **A** | **Realtime Database (RTDB) + base64** ⭐ | Se comprime la foto en el móvil y se guarda como texto base64 en `profile_images/{uid}` de RTDB | ✅ **1 GB** y 10 GB/mes de descarga en Spark | Base64 engorda ~33 %; RTDB no es CDN de imágenes | **Alto**: todo dentro de Firebase, sin terceros ni tarjeta |
| **B** | **Firestore + base64** | Igual pero en un documento de Firestore (`profile_images/{uid}`) | ✅ dentro del 1 GiB ya usado | Límite **1 MiB por documento**; leer la foto = leer documento | **Alto** si se comprime bien (<200 KB) |
| **C** | **Supabase Storage** | Tercer proveedor: bucket + API propia | ✅ 1 GB (free), pero **pausa** proyectos inactivos y ~5 GB egreso | Segundo backend, segundo login, más piezas | Medio |
| **D** | **Cloudflare R2** | Object storage S3-compatible, egreso 0 € | ✅ 10 GB y 1M/10M operaciones, pero **pide tarjeta** | Requiere cuenta de pago (aunque no cobre) | Bajo (por la tarjeta) |
| **E** | **Host de imágenes de terceros** (ImgBB, Cloudinary…) | Subes por API y te devuelven una URL | ✅ planes gratis | Dependencia externa, privacidad, límites/rate | Medio-bajo |
| **F** | **Firebase Storage con Blaze** | El camino "oficial" | Cuota gratuita amplia en Blaze, pero **exige facturación** | Justo lo que se quiere evitar | Descartado por ahora |

Fuentes de C/D: [Cloudflare R2 pricing](https://developers.cloudflare.com/r2/pricing/),
comparativa Supabase/R2 ([puter.com](https://developer.puter.com/blog/cloudflare-r2-alternatives/)),
[Supabase pricing](https://cotera.co/articles/supabase-pricing-guide).

## 3. Recomendación (criterio senior)

**Opción A (Realtime Database + base64 con compresión en el cliente).** Motivos:

1. **Cero proveedores nuevos y cero tarjeta**: seguimos 100 % Firebase y en plan gratuito.
2. **Encaja con el dato real**: una foto de perfil comprimida a **512 px JPEG (~50–120 KB)**
   cabe de sobra, incluso multiplicada por cientos de usuarios, en el 1 GB de Spark.
3. **No toca la arquitectura de datos** de la biblioteca (Firestore sigue siendo el SSOT); RTDB
   se usa **solo** como "bucket" de la foto, con su propia regla de seguridad por `uid`.
4. **Reversible**: si algún día se pasa a Blaze o a un CDN, la capa `ProfileImageStorage` ya
   está aislada (interfaz única) y solo cambia su implementación.

**Plan B si RTDB diera problemas:** Opción **B** (misma idea, en Firestore, en un documento
aparte para no engordar el perfil).

### Regla de oro del cambio
Mantener la **misma interfaz `ProfileImageStorage`** y **seguir guardando en el perfil la URL
(o el puntero) de la imagen**, para que la UI y `UserRepository.updateProfileImage` no cambien.
La diferencia es *dónde* vive el binario (RTDB en vez de Storage).

## 4. Cómo se implementaría (esbozo)

1. **Compresión en el cliente:** antes de subir, redimensionar a ~512 px y comprimir a JPEG
   (calidad ~80). Es lo que hace que "base64" deje de ser un problema.
2. **`ProfileImageStorage` (nueva implementación):** `upload(email, bytes) → devuelve un
   identificador/URL`; internamente escribe base64 en `profile_images/{uid}`.
3. **Reglas de RTDB:** cada usuario solo escribe en su nodo (`auth.uid == $uid`); lectura para
   autenticados (los avatares se muestran en la capa social desde F2).
4. **`firebase/storage.rules`** queda **archivada** (documentar por qué no se usa) hasta que
   exista un caso que justifique Blaze (p. ej. importar carátulas o el export de imágenes).
5. **Verificación (CA0.3):** subir desde el dispositivo A, ver la foto desde el B (o tras borrar
   datos) — sin tocar la UI.

## 5. Lo que NO cambia

- El **modelo de datos** (Firestore sigue siendo el SSOT).
- La **UI** (sigue leyendo una URL/`imageUri` del perfil).
- El **plan gratuito** (seguimos en Spark).

## 6. Decisión propuesta

Escribir **ADR-0007 — «Imágenes de perfil: base64 en Realtime Database (sin Storage/Blaze)»**
que supersede el uso de Firebase Storage introducido en T0.12. Pendiente de aprobación del
propietario.

## 7. Seguimiento

- Si el proyecto se **comercializa** (F5) y aparecen casos de uso que pidan un bucket real
  (muchas imágenes, vídeo, CDN), reevaluar **Blaze** o **R2/Supabase** con ADR nuevo.
- Vigilar el **límite de 1 MiB por documento** si algún día se migra a la Opción B.
