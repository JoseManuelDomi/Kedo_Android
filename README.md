# 📱 Kedo Android App

Aplicación móvil nativa diseñada como la interfaz principal (Cliente) para interactuar con el ecosistema **Kedo Backend**. Proporciona una experiencia de usuario fluida, segura y optimizada para dispositivos Android, permitiendo la gestión eficiente del sistema.

## ✨ Características Principales

* **Modelo de Usuario Unificado:** Soporte completo para el sistema de roles integrados de la plataforma, donde la vista se adapta dinámicamente dependiendo de si el usuario es administrador o cliente estándar.
* **Sincronización Asíncrona:** Consumo de la API REST del backend para la persistencia, lectura y actualización de datos en tiempo real.
* **Interfaz de Usuario (UI) Nativa:** Componentes diseñados con los estándares de Material Design para asegurar un alto rendimiento y una usabilidad intuitiva.

## 🛠️ Stack Tecnológico

* **Plataforma:** Android (Desarrollo Nativo).
* **Arquitectura:** Modelo Cliente-Servidor.
* **Comunicaciones:** Gestión de peticiones HTTP/JSON para el intercambio de datos con la API central.

## ⚙️ Requisitos y Configuración Local

Para ejecutar este proyecto en un entorno de desarrollo local, sigue estos pasos:

1. Clona este repositorio en tu máquina.
2. Abre el proyecto utilizando **Android Studio**.
3. Sincroniza los archivos de Gradle (`Sync Project with Gradle Files`).
4. **Importante:** Asegúrate de que tu instancia de `Kedo_Backend` esté ejecutándose. Si pruebas la app en un dispositivo físico, deberás cambiar las URLs base de la API apuntando a la IP local de tu ordenador en la misma red Wi-Fi (ej. `http://192.168.X.X:8080`), ya que `localhost` apuntaría internamente al propio teléfono.
5. Compila y ejecuta en tu emulador o dispositivo físico.

---
*Diseñado para ofrecer escalabilidad y rendimiento en el ecosistema móvil.*