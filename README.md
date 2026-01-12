# 🎮 GameVision

GameVision es una aplicación iOS desarrollada como **TFM**, enfocada en el análisis, descubrimiento y seguimiento de videojuegos, aplicando arquitectura limpia y una experiencia de usuario moderna.

Proyecto diseñado como **pieza principal de portfolio** y referencia técnica para procesos de selección.

---

## 📱 Descripción general

**GameVision** centraliza información relevante sobre videojuegos y permite al usuario explorar títulos, consultar detalles avanzados y gestionar favoritos, todo dentro de una interfaz fluida y consistente.

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
- 📄 Detalle completo de cada título (rating, plataformas, descripción)

---

### ⭐ Gestión de favoritos
- 💾 Sistema de favoritos persistente
- 🔄 Sincronización por usuario
- ⚡ Acceso rápido a títulos guardados

---

### 🎨 Interfaz y experiencia
- 🌟 UI desarrollada íntegramente en *SwiftUI*
- 🧭 Navegación fluida con `NavigationStack`
- ✨ Animaciones suaves y transiciones coherentes
- 🌙 Soporte Dark / Light Mode

---

### 🌐 Consumo de API
- 🌍 Integración con API externa de videojuegos
- 📥 Fetch de información de títulos, plataformas y géneros
- ⚠️ Manejo de errores y estados de carga en tiempo real
- 💾 Cache de datos básicos para mejorar la experiencia

---

## 🛠 Tecnologías y stack

- **Lenguaje:** Swift 5.8+
- **UI:** SwiftUI
- **Arquitectura:** MVVM (`ObservableObject`, `StateObject`)
- **Backend / API:** API externa de videojuegos
- **Persistencia local (opcional):** CoreData

### Componentes auxiliares
- Pull-to-refresh con `ScrollView` y `LazyVStack`
- Animaciones con `.transition`, `.spring` y `.easeIn`
- Componentes reutilizables y testables

---

## 🎯 Enfoque profesional

Proyecto diseñado como:

1. **Pieza clave de portfolio**
2. **Demostración de arquitectura limpia**
3. **Base escalable para testing y evolución**
4. **Referencia realista de app iOS moderna**
