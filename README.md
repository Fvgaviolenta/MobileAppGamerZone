# 🎮 Gamer Zone - Tienda Virtual

Aplicación móvil de comercio electrónico especializada en productos gaming, desarrollada en Kotlin con Jetpack Compose y Firebase.

## 📱 Características

### Para Usuarios
- ✅ **Registro e Inicio de Sesión**: Sistema de autenticación seguro
- ✅ **Catálogo de Productos**: Navegación por categorías (Consolas, Juegos, Accesorios)
- ✅ **Carrito de Compras**: 
  - Agregar/eliminar productos
  - Actualizar cantidades
  - Persistencia en Firebase
  - Descuento de stock en tiempo real
- ✅ **Gestión de Perfil**: Actualización de datos personales
- ✅ **Historial de Compras**: Registro de órdenes realizadas

### Para Administradores
- ✅ **Panel de Administración**: CRUD completo de productos
- ✅ **Gestión de Inventario**: Control de stock en tiempo real
- ✅ **Gestión de Usuarios**: Roles y permisos

## 🛠️ Tecnologías

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de Datos**: Firebase Firestore
- **Persistencia Local**: DataStore
- **Navegación**: Navigation Compose
- **Imágenes**: Coil
- **Coroutines**: Programación asíncrona

## 📦 Estructura del Proyecto

```
app/
├── src/main/java/com/example/appgamerzone/
│   ├── data/
│   │   ├── local/          # DataStore para sesiones
│   │   ├── model/          # Modelos de datos
│   │   ├── repository/     # Repositorios (Firebase)
│   │   └── session/        # Gestión de sesiones
│   ├── navigation/         # Sistema de navegación
│   ├── view/               # Pantallas UI (Compose)
│   │   ├── auth/           # Login y Registro
│   │   ├── home/           # Pantalla principal
│   │   ├── catalog/        # Catálogo de productos
│   │   ├── cart/           # Carrito de compras
│   │   ├── profile/        # Perfil de usuario
│   │   └── admin/          # Panel administrativo
│   ├── viewmodel/          # ViewModels
│   └── ui/theme/           # Tema y estilos
```

## 🚀 Configuración

### Prerequisitos
- Android Studio Hedgehog | 2023.1.1 o superior
- JDK 11 o superior
- Cuenta de Firebase

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/TU_USUARIO/GamerZoneAPP.git
   cd GamerZoneAPP
   ```

2. **Configurar Firebase**
   - Crear un proyecto en [Firebase Console](https://console.firebase.google.com)
   - Descargar `google-services.json`
   - Colocar el archivo en `app/google-services.json`
   - Habilitar Firestore Database

3. **Configurar Reglas de Firestore**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow read, write: if request.auth != null;
       }
       match /products/{productId} {
         allow read: if true;
         allow write: if request.auth != null;
       }
       match /carts/{userId} {
         allow read, write: if request.auth != null;
       }
       match /orders/{orderId} {
         allow read, create, update: if request.auth != null;
         allow delete: if false;
       }
     }
   }
   ```

4. **Ejecutar el proyecto**
   - Abrir el proyecto en Android Studio
   - Sincronizar Gradle
   - Ejecutar en emulador o dispositivo físico

## 📊 Estructura de Datos en Firebase

### Colección: `users`
```json
{
  "id": "user123",
  "fullName": "Juan Pérez",
  "email": "juan@example.com",
  "phone": "+56912345678",
  "address": "Av. Principal 123",
  "role": "USER",
  "level": 1,
  "lvlUpPoints": 0
}
```

### Colección: `products`
```json
{
  "id": "prod_001",
  "name": "PlayStation 5",
  "price": 499990,
  "category": "Consolas",
  "description": "Consola de última generación",
  "imageUrl": "https://...",
  "stock": 15,
  "rating": 4.8,
  "reviewCount": 150
}
```

### Colección: `carts`
```json
{
  "userId": "user123",
  "items": [
    {
      "productId": "prod_001",
      "quantity": 2
    }
  ],
  "updatedAt": 1705920000000
}
```

### Colección: `orders`
```json
{
  "id": "order_001",
  "userId": "user123",
  "userName": "Juan Pérez",
  "userEmail": "juan@example.com",
  "items": [...],
  "subtotal": 999980.0,
  "discount": 99998.0,
  "total": 899982.0,
  "date": 1705920000000,
  "status": "COMPLETED"
}
```

## 👤 Usuario Administrador

Para crear un usuario administrador, agregar manualmente en Firestore:

```json
{
  "id": "admin123",
  "fullName": "Administrador",
  "email": "admin@gamerzone.com",
  "password": "admin123",
  "phone": "+56900000000",
  "address": "Oficina Central",
  "role": "ADMIN",
  "level": 1,
  "lvlUpPoints": 0
}
```

## 🎨 Características de UI/UX

- ✅ Material Design 3
- ✅ Navegación con Drawer (menú hamburguesa)
- ✅ Animaciones y transiciones suaves
- ✅ Diseño responsive
- ✅ Tema personalizado para gaming
- ✅ Feedback visual (Snackbars, Diálogos)

## 🔐 Seguridad

- ✅ Contraseñas almacenadas (⚠️ En producción usar hash)
- ✅ Validación de sesiones
- ✅ Validación de stock antes de compra
- ✅ Reglas de seguridad en Firestore

## 📝 Funcionalidades Futuras

- [ ] Integración con pasarela de pagos real
- [ ] Sistema de notificaciones push
- [ ] Wishlist / Lista de deseos
- [ ] Reseñas y calificaciones de usuarios
- [ ] Sistema de puntos y gamificación
- [ ] Búsqueda avanzada de productos
- [ ] Filtros por precio, rating, etc.

## 🐛 Problemas Conocidos

Ninguno reportado actualmente.

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 👨‍💻 Autor

**Tu Nombre**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)

## 🙏 Agradecimientos

Proyecto desarrollado como práctica académica para el curso de Desarrollo de Aplicaciones Móviles.

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub!

