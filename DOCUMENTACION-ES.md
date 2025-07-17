# Documentación Completa - Immich-TV

## Índice de Documentación

Esta documentación está diseñada para desarrolladores que desean trabajar con Immich-TV en Windows 11, desde la configuración inicial hasta la publicación de nuevas versiones.

### 📋 Guías Principales

#### 1. [🖥️ Configuración de Desarrollo en Windows 11](./DESARROLLO-WINDOWS.md)
**¿Para quién?** Desarrolladores que configuran el entorno por primera vez
**Contenido:**
- Requisitos del sistema y software necesario
- Instalación de Android Studio y SDK
- Configuración del emulador Android TV
- Clonación y configuración del proyecto
- Resolución de problemas comunes

#### 2. [⚡ Funcionalidades Implementadas](./FUNCIONALIDADES.md)
**¿Para quién?** Desarrolladores y usuarios que quieren conocer las capacidades
**Contenido:**
- Lista completa de funcionalidades actuales
- Funcionalidades planificadas
- Arquitectura técnica
- Casos de uso típicos
- Requisitos del servidor Immich

#### 3. [🚀 Compilación y Publicación](./COMPILACION-PUBLICACION.md)
**¿Para quién?** Mantenedores y desarrolladores que publican releases
**Contenido:**
- Proceso de compilación local y CI/CD
- Configuración de tokens y secrets
- Firma digital de APKs
- Proceso automatizado de release
- Distribución en diferentes canales

## Flujo de Trabajo Recomendado

### Para Nuevos Desarrolladores:
1. **Configuración inicial:** Seguir [DESARROLLO-WINDOWS.md](./DESARROLLO-WINDOWS.md)
2. **Familiarización:** Revisar [FUNCIONALIDADES.md](./FUNCIONALIDADES.md)
3. **Primer build:** Compilar y ejecutar en emulador
4. **Desarrollo:** Implementar cambios y mejoras

### Para Mantenedores:
1. **Desarrollo:** Trabajar en nuevas funcionalidades
2. **Testing:** Verificar cambios en emulador
3. **Release:** Seguir proceso en [COMPILACION-PUBLICACION.md](./COMPILACION-PUBLICACION.md)
4. **Distribución:** Publicar y comunicar cambios

## Enlaces Rápidos

### Recursos del Proyecto:
- **Repositorio Original:** [giejay/Immich-Android-TV](https://github.com/giejay/Immich-Android-TV)
- **Servidor Immich:** [immich-app/immich](https://github.com/immich-app/immich)
- **Documentación Immich:** [immich.app](https://immich.app/)

### Herramientas de Desarrollo:
- **Android Studio:** [developer.android.com/studio](https://developer.android.com/studio)
- **Java 17 (Temurin):** [adoptium.net](https://adoptium.net/temurin/releases/)
- **Firebase Console:** [console.firebase.google.com](https://console.firebase.google.com/)
- **GitHub Actions:** [github.com/features/actions](https://github.com/features/actions)

### Documentación Técnica:
- **Android TV Development:** [developer.android.com/tv](https://developer.android.com/tv)
- **Leanback Library:** [developer.android.com/jetpack/androidx/releases/leanback](https://developer.android.com/jetpack/androidx/releases/leanback)
- **Kotlin Documentation:** [kotlinlang.org/docs](https://kotlinlang.org/docs/)

## Preguntas Frecuentes

### ¿Es necesario configurar Firebase?
Sí, para el funcionamiento completo de la aplicación (analytics y crashlytics). Se puede usar el archivo de ejemplo para desarrollo local.

### ¿Puedo contribuir al proyecto original?
Este es un fork. Para contribuir al proyecto original, dirigirse a [giejay/Immich-Android-TV](https://github.com/giejay/Immich-Android-TV).

### ¿Qué versión de Immich Server necesito?
Se recomienda Immich Server v1.90.0 o superior para máxima compatibilidad.

### ¿Funciona en dispositivos Android normales?
La aplicación está optimizada para Android TV, pero puede funcionar en tablets Android con algunas limitaciones de UI.

### ¿Cómo reportar bugs?
Usar el sistema de Issues de GitHub con la plantilla correspondiente.

## Estructura de la Documentación

```
Documentación/
├── DOCUMENTACION-ES.md          # Este archivo (índice)
├── DESARROLLO-WINDOWS.md        # Configuración de desarrollo
├── FUNCIONALIDADES.md           # Funcionalidades y arquitectura
├── COMPILACION-PUBLICACION.md   # Proceso de build y release
└── README.md                    # Documentación original en inglés
```

## Actualizaciones de Documentación

Esta documentación se actualiza junto con el código. Si encuentras información desactualizada o errores:

1. Crear Issue describiendo el problema
2. Proponer cambios vía Pull Request
3. Contactar a los mantenedores

## Soporte y Comunidad

- **Issues:** Para reportar bugs y solicitar funcionalidades
- **Discussions:** Para preguntas y conversaciones generales
- **Pull Requests:** Para contribuir código y documentación

---

**Última actualización:** Julio 2024  
**Versión de la aplicación:** 3.9.11+  
**Compatibilidad:** Windows 11, Android TV API 24+