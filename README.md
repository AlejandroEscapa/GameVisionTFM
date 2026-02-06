# 🎮 GameVision

GameVision es una aplicación **Android** desarrollada como **TFM**, enfocada en el análisis, descubrimiento y seguimiento de videojuegos, aplicando arquitectura limpia y una experiencia de usuario moderna.

Proyecto diseñado como **pieza principal de portfolio** y referencia técnica para procesos de selección.

---

## 📱 Descripción general

**GameVision** centraliza información relevante sobre videojuegos y permite al usuario explorar títulos, consultar detalles avanzados y gestionar colecciones personales, todo dentro de una interfaz fluida y consistente.

Además, la aplicación incorpora un **perfil de usuario**, donde se pueden gestionar datos personales y preferencias.

El proyecto prioriza:

- Arquitectura escalable  
- Calidad de código  
- Experiencia de usuario  
- Buenas prácticas profesionales orientadas a entorno real  

---

## 🚀 Características principales

### 🎮 Exploración de videojuegos
- 🔍 Búsqueda avanzada de videojuegos  
- 🎯 Filtros por plataformas, géneros y fecha de lanzamiento  
- 📄 Detalle completo de cada título (**rating**, **plataformas**, **descripción**)  

---

### ⭐ Gestión de colecciones (favoritos)
- 💾 **Sistema de persistencia de colecciones**  
- 🗂️ Posibilidad de añadir juegos a:  
  - **Lista de jugados**  
  - **Lista de deseos**  
  - **Favoritos**  
- 🔄 Sincronización por usuario  
- ⚡ Acceso rápido a títulos guardados  

---

### 👤 Perfil de usuario
- 🧾 Gestión de datos personales del usuario  
- 🖼️ Imagen de perfil almacenada a nivel local  
- ✍️ Biografía editable  
- 📍 Localización configurable  
- 💾 Persistencia de preferencias y datos de perfil  

---

### 🎨 Interfaz y experiencia
- 🌟 UI desarrollada íntegramente en *Jetpack Compose*  
- 🧭 Navegación fluida con *Navigation Compose*  
- ✨ Animaciones suaves y transiciones coherentes  
- 🌙 Soporte **Dark / Light Mode** siguiendo Material Design  

---

### 🌐 Consumo de API
- 🌍 Integración con **RAWG** (API de videojuegos)  
- ⚙️ Peticiones realizadas mediante **Retrofit**  
- 📥 Fetch de información de títulos, plataformas y géneros  
- ⚠️ Manejo de errores y estados de carga en tiempo real  
- 💾 Cache de datos básicos para mejorar la experiencia  

---

## 🛠 Tecnologías y stack

- **Lenguaje:** Kotlin  
- **UI:** Jetpack Compose  
- **Arquitectura:** MVVM (*ViewModel*, *StateFlow* y *LiveData*)  
- **Backend / API:** RAWG (consumida con *Retrofit*)  
- **Persistencia local (opcional):** DataStore

### Componentes auxiliares
- Componentes reutilizables y testables  
- Gestión de estado reactiva y desacoplada (Flows / Coroutines)

---

## 🎯 Enfoque profesional

Proyecto diseñado como:
1. **Pieza clave de portfolio**  
2. **Demostración de arquitectura limpia**  
3. **Base escalable para testing y evolución**  
4. **Referencia realista de app Android moderna**  
