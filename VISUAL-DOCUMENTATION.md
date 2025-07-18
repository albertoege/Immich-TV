# Immich-TV - Documentación Visual de Funcionalidades

## 📺 Descripción General

Immich-TV es una aplicación para Android TV que permite visualizar fotos y videos almacenados en tu servidor Immich personal. La aplicación está optimizada para la experiencia de televisión con controles de navegación por control remoto y una interfaz de usuario adaptada para pantallas grandes.

## 🎯 Funcionalidades Principales Documentadas

### 1. 🏠 Pantalla Principal - Vista de Álbumes

![Pantalla Principal](https://github.com/user-attachments/assets/af51aa91-f8ec-4e63-9c4e-9cc017913f30)

**Características:**
- **Navegación lateral**: Menú izquierdo con opciones principales (Albums, Photos, Random, People, Recent, Seasonal, Edit, Settings)
- **Vista de álbumes**: Área principal mostrando álbumes organizados en una cuadrícula
- **Miniaturas dinámicas**: Cada álbum muestra una imagen representativa
- **Interfaz optimizada para TV**: Diseño espacioso y navegable con control remoto
- **Información contextual**: Nombres de álbumes y detalles visibles

### 2. ⚙️ Configuración de Visualización

![Configuración](https://github.com/user-attachments/assets/ca3716d3-fe0c-4b65-99f3-a28ca508e28c)

**Opciones disponibles:**
- **Ordenamiento**: Configuración de cómo se ordenan los álbumes (Last updated)
- **Fotos en álbumes**: Opciones de visualización (Oldest, Newest)
- **Configuración de fotos**: Ordenamiento general (Newest, Oldest)
- **Slideshow**: Configuraciones para presentaciones automáticas
- **Miniaturas de alta resolución**: Opción para usar thumbnails de mayor calidad
- **Fusión de fotos verticales**: Agrupación inteligente de fotos portrait
- **Mostrar descripciones**: Toggle para mostrar información adicional

### 3. 🔐 Pantalla de Inicio de Sesión

![Login](https://github.com/user-attachments/assets/cbaf81b4-52ac-42ed-87bb-1602f515eca3)

**Funcionalidades de autenticación:**
- **Branding Immich**: Logo colorido y marca reconocible
- **Modo Demo**: Opción para probar la aplicación sin servidor propio
- **Conexión a servidor**: Posibilidad de conectar a instancia personal de Immich
- **Interfaz limpia**: Diseño minimalista optimizado para TV

### 4. 📸 Vista de Fotos Individual

![Vista de Fotos](https://github.com/user-attachments/assets/b27ab232-af4e-4fd2-bea2-f8aa73d11ba3)

**Características de navegación:**
- **Grid de fotos**: Visualización en cuadrícula de fotos individuales
- **Navegación fluida**: Desplazamiento suave entre fotos
- **Metadatos visibles**: Información de archivos mostrada
- **Selección visual**: Indicadores claros de foto seleccionada
- **Carga optimizada**: Thumbnails eficientes para navegación rápida

## 🚀 Funcionalidades Técnicas Implementadas

### Navegación y UI
- ✅ **Leanback UI**: Interfaz nativa de Android TV
- ✅ **Navegación por control remoto**: Optimizada para D-pad
- ✅ **Carga lazy**: Carga progresiva de contenido
- ✅ **Transiciones suaves**: Animaciones optimizadas para TV

### Gestión de Medios
- ✅ **Reproducción de videos**: Soporte completo para ExoPlayer
- ✅ **Slideshow automático**: Presentaciones con intervalo configurable
- ✅ **Múltiples formatos**: Soporte para diferentes tipos de archivo
- ✅ **Metadatos EXIF**: Visualización de información de imagen

### Conectividad
- ✅ **API Immich**: Integración completa con servidor Immich
- ✅ **Autenticación segura**: Login por API key o teléfono
- ✅ **Modo offline**: Funcionalidad demo sin servidor
- ✅ **Sincronización**: Actualización automática de contenido

### Personalización
- ✅ **Protector de pantalla**: Configuración como screensaver del sistema
- ✅ **Intervalos configurables**: Personalización de tiempos de slideshow
- ✅ **Filtros de álbumes**: Selección de contenido para screensaver
- ✅ **Opciones de calidad**: Configuración de resolución de miniaturas

## 🎮 Experiencia de Usuario

### Controles de Navegación
- **D-Pad**: Navegación principal entre elementos
- **Botón central**: Selección y reproducción
- **Botón atrás**: Navegación hacia atrás
- **Botón menú**: Acceso a opciones adicionales

### Flujo de Uso Típico
1. **Inicio**: La app se abre en la pantalla principal de álbumes
2. **Navegación**: Usuario navega por álbumes usando el control remoto
3. **Selección**: Selecciona un álbum para ver las fotos contenidas
4. **Visualización**: Navega por fotos individuales o inicia slideshow
5. **Configuración**: Accede a settings para personalizar la experiencia

## 📱 Compatibilidad

- **Android TV**: API 24+ (Android 7.0+)
- **Dispositivos soportados**: TV Boxes, Smart TVs, Chromecast con Google TV
- **Servidor Immich**: Compatible con versiones 1.90.0+
- **Resoluciones**: Optimizado para 1920x1080 (Full HD) y superiores

## 🔧 Configuración Recomendada

### Para Mejor Rendimiento
- Habilitar "Use high resolution thumbnails" para calidad superior
- Configurar intervalos de slideshow según preferencia (5-30 segundos)
- Activar "Merge portrait photos" para mejor organización

### Para Uso como Screensaver
- Instalar la aplicación como protector de pantalla del sistema
- Seleccionar álbumes específicos para mostrar
- Configurar intervalo apropiado para el entorno

---

**Versión de la aplicación**: 3.9.11+  
**Última actualización de documentación**: Julio 2024  
**Desarrollador**: Fork de [giejay/Immich-Android-TV](https://github.com/giejay/Immich-Android-TV)