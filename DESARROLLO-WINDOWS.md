# Guía de Desarrollo - Immich-TV en Windows 11

## Descripción del Proyecto

Immich-TV es una aplicación Android TV desarrollada en Kotlin que permite visualizar fotos y videos desde un servidor Immich. Es un fork de la aplicación original [immich-android-tv](https://github.com/giejay/Immich-Android-TV) con funcionalidades adicionales y personalizaciones.

## Requisitos del Sistema para Windows 11

### Software Requerido

1. **Java Development Kit (JDK) 17**
   - Descargar desde: https://adoptium.net/temurin/releases/
   - Seleccionar versión 17 LTS para Windows x64
   - Configurar variable de entorno `JAVA_HOME`

2. **Android Studio**
   - Descargar desde: https://developer.android.com/studio
   - Versión recomendada: Electric Eel o superior
   - Durante la instalación, incluir:
     - Android SDK
     - Android SDK Platform-Tools
     - Intel HAXM (para emulación)

3. **Git para Windows**
   - Descargar desde: https://git-scm.com/download/win
   - Configurar con tus credenciales de GitHub

### Configuración del SDK de Android

1. Abrir Android Studio
2. Ir a **File > Settings > Appearance & Behavior > System Settings > Android SDK**
3. Instalar los siguientes componentes:
   - **SDK Platforms:**
     - Android 14 (API level 34)
     - Android 13 (API level 33)
     - Android TV API levels correspondientes
   - **SDK Tools:**
     - Android SDK Build-Tools
     - Android Emulator
     - Android SDK Platform-Tools
     - Intel x86 Emulator Accelerator (HAXM installer)

### Variables de Entorno

Agregar las siguientes variables al PATH del sistema:

```
JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot
ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
```

Y agregar al PATH:
```
%JAVA_HOME%\bin
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%ANDROID_HOME%\tools\bin
```

## Configuración del Emulador Android TV

### Crear un Dispositivo Virtual Android TV

1. En Android Studio, abrir **AVD Manager** (Tools > AVD Manager)
2. Hacer clic en **Create Virtual Device**
3. Seleccionar categoría **TV**
4. Elegir un dispositivo TV (recomendado: **Android TV (1080p)**)
5. Seleccionar una imagen del sistema:
   - **API Level 30** o superior
   - Imagen con Google APIs
   - Arquitectura x86_64 para mejor rendimiento
6. Configurar las opciones avanzadas:
   - **RAM:** 2048 MB mínimo
   - **Internal Storage:** 4096 MB mínimo
   - **SD Card:** 1024 MB (opcional)
   - Habilitar **Hardware Acceleration**

### Configuración del Emulador

1. Iniciar el emulador desde AVD Manager
2. Configurar la resolución de pantalla para TV
3. Habilitar el modo desarrollador:
   - Ir a Settings > Device Preferences > About
   - Hacer clic 7 veces en "Android TV OS build"
4. Habilitar **USB Debugging** en Developer Options

### Alternativas de Emulación

**Opción 1: Genymotion (Recomendado para desarrollo intensivo)**
- Descargar desde: https://www.genymotion.com/
- Crear dispositivo Android TV personalizado
- Mejor rendimiento que el emulador estándar

**Opción 2: BlueStacks (Para pruebas básicas)**
- Descargar BlueStacks 5 o superior
- Configurar para ejecutar aplicaciones Android TV

## Clonación y Configuración del Proyecto

### 1. Clonar el Repositorio

```bash
git clone --recurse-submodules https://github.com/albertoege/Immich-TV.git
cd Immich-TV
```

### 2. Configurar Archivos de Configuración

**Google Services (Firebase):**
```bash
# Copiar el archivo de ejemplo
copy app\google-services.example app\google-services.json
```

**Configuración del servidor demo:**
```bash
# Copiar configuración de strings
copy app\strings_other.xml.example app\src\main\res\values\strings_other.xml
```

### 3. Editar Configuración del Servidor

Editar `app/src/main/res/values/strings_other.xml`:

```xml
<resources>
    <!-- URL del servidor Immich (sin /api al final) -->
    <string name="host_name">https://tu-servidor-immich.com:2283</string>
    <!-- API Key obtenida desde la interfaz de Immich -->
    <string name="api_key">TuAPIKeyAqui</string>
    <!-- URL para autenticación por teléfono (opcional) -->
    <string name="authentication_url">https://ejemplo.com/auth_url</string>
</resources>
```

## Compilación del Proyecto

### Compilación desde Android Studio

1. Abrir Android Studio
2. **File > Open** y seleccionar la carpeta del proyecto
3. Esperar a que Gradle sincronice
4. **Build > Make Project** (Ctrl+F9)

### Compilación desde Línea de Comandos

```bash
# Limpiar y compilar
.\gradlew clean build

# Generar APK de release
.\gradlew assembleRelease

# Generar APK de debug
.\gradlew assembleDebug
```

### Ejecución en el Emulador

```bash
# Instalar en el emulador conectado
.\gradlew installDebug

# O desde Android Studio: Run > Run 'app'
```

## Depuración y Desarrollo

### Herramientas de Depuración

1. **Logcat:** Para ver logs en tiempo real
2. **Layout Inspector:** Para analizar la interfaz
3. **Network Inspector:** Para monitorear llamadas API
4. **Memory Profiler:** Para optimizar memoria

### Comandos ADB Útiles

```bash
# Conectar al emulador
adb connect 127.0.0.1:5554

# Instalar APK manualmente
adb install app\build\outputs\apk\debug\app-debug.apk

# Ver logs en tiempo real
adb logcat | findstr "ImmichTV"

# Configurar app como screensaver (requiere modo desarrollador)
adb shell settings put secure screensaver_components com.albertoeg.android.tv.immich/.screensaver.ScreenSaverService
```

## Estructura del Proyecto

```
Immich-TV/
├── app/                          # Aplicación principal
│   ├── src/main/java/nl/giejay/android/tv/immich/
│   │   ├── MainActivity.kt       # Actividad principal
│   │   ├── home/                 # Fragmentos de inicio
│   │   ├── album/                # Gestión de álbumes
│   │   ├── assets/               # Gestión de archivos multimedia
│   │   ├── auth/                 # Autenticación
│   │   ├── people/               # Vista de personas
│   │   ├── screensaver/          # Salvapantallas
│   │   ├── settings/             # Configuraciones
│   │   └── shared/               # Componentes compartidos
│   ├── src/main/res/             # Recursos (layouts, strings, etc.)
│   └── build.gradle              # Configuración de build
├── mediaslider/                  # Módulo del slider de medios
├── .github/workflows/            # Flujos de CI/CD
├── build.gradle                  # Configuración raíz del proyecto
└── README.md                     # Documentación original
```

## Resolución de Problemas Comunes

### Error de Compilación con Firebase

Si aparece error relacionado con Firebase/Crashlytics:
1. Verificar que `google-services.json` existe y es válido
2. Asegurar conexión a internet para descargar dependencias
3. Limpiar proyecto: `.\gradlew clean`

### Problemas de Emulación

1. **Emulador lento:** Habilitar aceleración de hardware (HAXM/Hyper-V)
2. **Error de conexión:** Verificar que el emulador está en la misma red
3. **Aplicación no inicia:** Verificar que el dispositivo tiene Android API 24+

### Problemas de Dependencias

```bash
# Actualizar wrapper de Gradle
.\gradlew wrapper --gradle-version=8.9

# Limpiar cache de Gradle
.\gradlew clean --refresh-dependencies
```

## Siguientes Pasos

Una vez configurado el entorno:
1. Familiarizarse con la estructura del código
2. Revisar las funcionalidades existentes
3. Configurar un servidor Immich de prueba
4. Experimentar con las diferentes vistas y características

Para más información sobre funcionalidades específicas, consultar [FUNCIONALIDADES.md](./FUNCIONALIDADES.md).