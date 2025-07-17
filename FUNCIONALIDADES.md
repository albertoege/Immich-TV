# Funcionalidades de Immich-TV

## Descripción General

Immich-TV es una aplicación Android TV que permite visualizar y gestionar fotos y videos almacenados en un servidor Immich. La aplicación está optimizada para su uso con control remoto y ofrece una experiencia fluida en televisores y dispositivos Android TV.

## Funcionalidades Implementadas Actualmente

### ✅ Autenticación y Conexión

#### Métodos de Autenticación Soportados:
- **Autenticación por API Key:** Ingreso directo de la clave API generada en Immich
- **Autenticación por Teléfono:** Usando QR code y aplicación móvil (requiere servidor de autenticación adicional)
- **Servidor Demo:** Configuración predefinida para probar la aplicación

#### Características:
- Conexión segura HTTPS
- Validación automática de credenciales
- Soporte para múltiples usuarios
- Almacenamiento seguro de credenciales

### ✅ Navegación y Interface

#### Pantalla Principal:
- **Interface Leanback:** Optimizada para Android TV
- **Navegación por control remoto:** Totalmente funcional con D-pad
- **Menú personalizable:** Ocultar/mostrar secciones según preferencias
- **Diseño responsive:** Adaptable a diferentes resoluciones de TV

#### Secciones Disponibles:
1. **Álbumes** - Vista de todos los álbumes del usuario
2. **Fotos Recientes** - Últimas fotos subidas
3. **Fotos Aleatorias** - Selección aleatoria del servidor
4. **Personas** - Agrupación por reconocimiento facial
5. **Fotos Estacionales** - Fotos del mismo período en años anteriores
6. **Tiempo Similar** - Fotos tomadas en momentos similares
7. **Todas las Fotos** - Vista completa de la biblioteca
8. **Carpetas** - Navegación por estructura de carpetas

### ✅ Visualización de Medios

#### Soporte de Formatos:
- **Imágenes:** JPEG, PNG, HEIC, RAW (vista previa)
- **Videos:** MP4, MOV, AVI, MKV y otros formatos comunes
- **Resolución:** Soporte para 4K y contenido HDR

#### Características de Visualización:
- **Thumbnails optimizados:** Carga rápida usando miniaturas 4K
- **Zoom y navegación:** Control con botones del control remoto
- **Transiciones suaves:** Entre fotos y videos
- **Información EXIF:** Metadatos de las fotos (fecha, cámara, configuración)
- **Controles de video:** Play/pause, avance rápido, retroceso

### ✅ Modo Slideshow

#### Funcionalidades:
- **Reproducción automática:** Fotos y videos en secuencia
- **Intervalos configurables:** 5s, 10s, 30s, 1min, 2min, 5min
- **Audio de videos:** Opción de habilitar/deshabilitar sonido
- **Modo pantalla completa:** Sin elementos de UI
- **Controles durante reproducción:** Pausa, siguiente, anterior
- **Orden personalizable:** Aleatorio, cronológico, por álbum

#### Configuraciones Avanzadas:
- Selección de álbumes específicos para slideshow
- Filtros por tipo de contenido (solo fotos, solo videos, mixto)
- Configuración de duración por tipo de medio

### ✅ Salvapantallas (Screensaver)

#### Características:
- **Activación automática:** Configuración como screensaver del sistema
- **Fuentes configurables:** Selección de álbumes específicos
- **Intervalos personalizables:** Tiempo entre cambios de imagen
- **Detección de actividad:** Pausar en uso del control remoto
- **Optimización de energía:** Gestión inteligente de recursos

#### Configuración:
- Múltiples álbumes como fuente
- Filtros de contenido
- Horarios de activación
- Intensidad de brillo

### ✅ Gestión de Álbumes

#### Funcionalidades:
- **Vista de álbumes:** Lista con thumbnails y metadatos
- **Álbumes compartidos:** Acceso a álbumes compartidos por otros usuarios
- **Carga lazy:** Carga eficiente de contenido bajo demanda
- **Ordenamiento:** Por fecha, nombre, tamaño
- **Búsqueda:** Filtrado de álbumes por nombre

#### Información de Álbumes:
- Número de elementos (fotos/videos)
- Fecha de creación y última modificación
- Propietario del álbum
- Thumbnail representativo

### ✅ Reconocimiento de Personas

#### Características:
- **Agrupación automática:** Basada en el reconocimiento facial de Immich
- **Vista por persona:** Navegación de fotos por individuo
- **Thumbnails de personas:** Miniatura representativa para cada persona
- **Filtrado avanzado:** Búsqueda de fotos por persona específica

### ✅ Configuraciones y Preferencias

#### Configuraciones de Visualización:
- **Calidad de imagen:** Selección de resolución de thumbnails
- **Modo de visualización:** Grid, lista, detalles
- **Animaciones:** Habilitar/deshabilitar transiciones
- **Tema:** Modo oscuro/claro

#### Configuraciones de Reproducción:
- **Audio en videos:** Control de volumen y silencio
- **Autoplay:** Reproducción automática de videos
- **Controles en pantalla:** Mostrar/ocultar controles de reproducción

#### Configuraciones de Red:
- **Cache de imágenes:** Tamaño y gestión del cache local
- **Calidad de red:** Ajuste según velocidad de conexión
- **Timeout de conexión:** Configuración de tiempos de espera

### ✅ Optimizaciones de Rendimiento

#### Gestión de Memoria:
- **Cache inteligente:** Sistema de cache multinivel
- **Liberación automática:** Gestión de memoria en segundo plano
- **Carga progresiva:** Thumbnails primero, alta resolución después

#### Optimizaciones de Red:
- **Thumbnails 4K:** Balance entre calidad y velocidad
- **Compresión adaptativa:** Según velocidad de conexión
- **Precarga inteligente:** Anticipación de contenido

## Funcionalidades Planificadas (Todo)

### 🔄 En Desarrollo
- **Fusión inteligente de fotos portrait:** Combinación de fotos similares (mismas personas, fecha, ubicación)
- **Transiciones de slideshow:** Efectos de transición entre medios
- **Vista de lugares/etiquetas:** Navegación por ubicación geográfica
- **Información de reproducción en screensaver:** Metadatos durante el screensaver

### 📋 Funcionalidades Futuras
- **Capacidades de Casting:** Envío de contenido a otros dispositivos
- **Búsqueda avanzada:** Búsqueda en álbumes y contenido
- **Inyección de dependencias:** Implementación con Hilt/Dagger
- **Sincronización offline:** Cache avanzado para uso sin conexión
- **Edición básica:** Rotación, recorte básico
- **Marcadores y favoritos:** Sistema de marcado personal
- **Estadísticas de uso:** Analytics de visualización
- **Soporte multi-servidor:** Conexión a múltiples instancias Immich

## Arquitectura Técnica

### Tecnologías Utilizadas:
- **Lenguaje:** Kotlin 1.9.22
- **Framework:** Android Leanback para TV
- **Reproducción:** ExoPlayer para medios
- **Red:** Retrofit + OkHttp
- **Imágenes:** Glide para carga y cache
- **Navegación:** Navigation Component
- **Async:** RxJava3 + Coroutines
- **Persistencia:** SharedPreferences + Room (planificado)

### Patrones Implementados:
- **MVVM:** Model-View-ViewModel
- **Repository Pattern:** Abstracción de fuentes de datos
- **Observer Pattern:** Para actualizaciones reactivas
- **Singleton:** Para gestores globales
- **Factory Pattern:** Para creación de fragmentos

## Casos de Uso Típicos

### 1. Visualización Familiar
- Conectar a biblioteca familiar de Immich
- Configurar slideshow con álbumes seleccionados
- Usar como marco digital inteligente

### 2. Entretenimiento en TV
- Navegar fotos de vacaciones
- Reproducir videos familiares
- Usar como screensaver personalizado

### 3. Presentaciones
- Mostrar portfolios fotográficos
- Presentar eventos especiales
- Slideshow para reuniones familiares

## Requisitos del Servidor Immich

### Versión Mínima Soportada:
- **Immich Server:** v1.90.0 o superior
- **API Version:** Compatible con API v1

### Configuraciones Recomendadas:
- **Resolución de thumbnails:** 4K habilitado
- **Reconocimiento facial:** Activado para funcionalidad de personas
- **Metadatos EXIF:** Habilitados para información completa
- **Álbumes compartidos:** Configurados según necesidades

Para configuración detallada del desarrollo, consultar [DESARROLLO-WINDOWS.md](./DESARROLLO-WINDOWS.md)
Para proceso de compilación y publicación, consultar [COMPILACION-PUBLICACION.md](./COMPILACION-PUBLICACION.md)