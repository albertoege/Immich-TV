# Guía de Compilación y Publicación - Immich-TV

## Descripción General

Esta guía detalla el proceso completo para compilar, firmar y publicar nuevas versiones de Immich-TV, incluyendo la configuración de tokens, keys y el proceso automatizado de release.

## Proceso de Compilación

### Compilación Local

#### Prerrequisitos:
- Entorno de desarrollo configurado (ver [DESARROLLO-WINDOWS.md](./DESARROLLO-WINDOWS.md))
- Archivos de configuración creados:
  - `app/google-services.json`
  - `app/src/main/res/values/strings_other.xml`

#### Comandos de Compilación:

```bash
# Limpiar proyecto
.\gradlew clean

# Compilar versión debug (para desarrollo)
.\gradlew assembleDebug

# Compilar versión release (para distribución)
.\gradlew assembleRelease

# Ejecutar tests
.\gradlew test

# Verificar código con linter
.\gradlew spotlessCheck
```

#### Ubicación de APKs generados:
- **Debug:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release:** `app/build/outputs/apk/release/ImmichTV-[version].apk`

### Compilación con Firma Digital

Para generar APKs firmados para distribución, se requieren certificados de firma:

#### Variables de Entorno Necesarias:
```bash
RELEASE_KEYSTORE_PATH=ruta/al/keystore.jks
RELEASE_KEYSTORE_PASSWORD=password_del_keystore
RELEASE_KEY_ALIAS=alias_de_la_key
RELEASE_KEY_PASSWORD=password_de_la_key
```

#### Configuración Local del Keystore:
1. Generar keystore (si no existe):
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias com.albertoeg.android.tv.immich-tv-key
```

2. Configurar variables de entorno:
```bash
set RELEASE_KEYSTORE_PATH=%cd%\release-keystore.jks
set RELEASE_KEYSTORE_PASSWORD=tu_password
set RELEASE_KEY_ALIAS=com.albertoeg.android.tv.immich-tv-key
set RELEASE_KEY_PASSWORD=tu_key_password
```

3. Compilar versión firmada:
```bash
.\gradlew assembleRelease
```

## Configuración de Tokens y Accesos

### GitHub Secrets Requeridos

Para el proceso automatizado de CI/CD, configurar los siguientes secrets en GitHub:

#### 1. Configuración de Firebase
```
GOOGLE_SERVICES_BASE64
```
- **Descripción:** Archivo `google-services.json` codificado en base64
- **Obtención:** 
  1. Ir a [Firebase Console](https://console.firebase.google.com/)
  2. Crear proyecto o usar existente
  3. Agregar aplicación Android con package name: `com.albertoeg.android.tv.immich`
  4. Descargar `google-services.json`
  5. Codificar en base64: `base64 -w 0 google-services.json`

#### 2. Configuración del Servidor Demo
```
STRINGS_OTHER
```
- **Descripción:** Archivo `strings_other.xml` codificado en base64
- **Contenido:**
```xml
<resources>
    <string name="host_name">https://tu-servidor-demo.com:2283</string>
    <string name="api_key">TuAPIKeyDemo</string>
    <string name="authentication_url">https://tu-auth-server.com</string>
</resources>
```
- **Codificación:** `base64 -w 0 strings_other.xml`

#### 3. Configuración de Firma Digital
```
RELEASE_KEYSTORE_BASE64
RELEASE_KEYSTORE_PASSWORD
RELEASE_KEY_ALIAS
RELEASE_KEY_PASSWORD
```
- **RELEASE_KEYSTORE_BASE64:** Keystore codificado en base64
- **Otros:** Passwords y alias del keystore

### Configuración de Firebase

#### Crear Proyecto Firebase:
1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear nuevo proyecto: "Immich-TV"
3. Habilitar Google Analytics (opcional)
4. Agregar aplicación Android:
   - **Package name:** `com.albertoeg.android.tv.immich`
   - **App nickname:** "Immich-TV"
   - **SHA-1:** Obtener con `keytool -list -v -keystore release-keystore.jks`

#### Servicios Firebase a Habilitar:
- **Crashlytics:** Para reporte de errores
- **Analytics:** Para métricas de uso
- **Authentication:** Si se implementa auth Firebase

### Configuración de GitHub Actions

Los workflows están en `.github/workflows/`:

#### android.yml - CI Continuo
- Se ejecuta en cada push/PR a main
- Compila APK firmado
- Ejecuta tests
- Genera artefactos

#### release.yml - Publicación de Releases
- Se ejecuta cuando se crea un tag
- Genera APK firmado
- Crea release en GitHub
- Publica APK como asset

## Proceso de Release

### Crear Nueva Versión

#### Método 1: Script Automatizado
```bash
# Usar el script incluido
.\create-release.sh 3.9.12
```

El script:
1. Actualiza `versionName` en `app/build.gradle`
2. Incrementa `versionCode` automáticamente
3. Hace commit de los cambios
4. Crea tag de git

#### Método 2: Manual
1. Editar `app/build.gradle`:
```gradle
versionCode 109  // Incrementar
versionName "3.9.12"  // Nueva versión
```

2. Commit y tag:
```bash
git add app/build.gradle
git commit -m "Create new release: 3.9.12 (109)"
git tag v3.9.12
git push origin main
git push origin v3.9.12
```

### Publicación Automática

Una vez creado el tag, GitHub Actions:
1. Detecta el nuevo tag
2. Compila APK firmado
3. Ejecuta tests
4. Crea release en GitHub
5. Adjunta APK al release
6. Genera changelog automático

### Distribución Manual

#### Google Play Store (si aplicable):
1. Acceder a [Google Play Console](https://play.google.com/console/)
2. Subir APK firmado
3. Completar información del release
4. Ejecutar tests internos
5. Publicar en canal deseado (interno/cerrado/abierto)

#### F-Droid (repositorio alternativo):
1. Fork del repositorio [fdroiddata](https://gitlab.com/fdroid/fdroiddata)
2. Crear metadata para la aplicación
3. Enviar merge request
4. Proceso de revisión de F-Droid

#### Distribución Directa:
- APK disponible en GitHub Releases
- Instalación manual vía ADB
- Distribución por canales propios

## Configuración de Entorno CI/CD

### GitHub Actions Requirements:
- Repository con permisos de admin
- Secrets configurados correctamente
- Keystore válido para firma
- Proyecto Firebase configurado

### Debugging de CI/CD:
```bash
# Ver logs de workflow
gh run list
gh run view [run-id]

# Descargar artefactos
gh run download [run-id]
```

### Testing Local del Workflow:
```bash
# Instalar act (para ejecutar Actions localmente)
# https://github.com/nektos/act

# Ejecutar workflow locally
act push
```

## Versionado y Changelog

### Esquema de Versionado:
- **Formato:** MAJOR.MINOR.PATCH (semver)
- **Ejemplo:** 3.9.12
  - MAJOR: Cambios incompatibles
  - MINOR: Nuevas funcionalidades compatible
  - PATCH: Bug fixes

### Generación de Changelog:
El changelog se genera automáticamente basado en commits desde el último tag:
```bash
# Ver commits para changelog manual
git log v3.9.11..HEAD --pretty=format:'%h %s'
```

### Convenciones de Commit:
```
feat: nueva funcionalidad
fix: corrección de bug
docs: cambios en documentación
style: cambios de formato
refactor: refactoring de código
test: agregando tests
chore: mantenimiento
```

## Troubleshooting

### Errores Comunes:

#### Build Failed - Firebase
```
Plugin [id: 'com.google.firebase.crashlytics'] was not found
```
**Solución:** Verificar que `google-services.json` está presente y es válido

#### Signing Failed
```
Keystore file not found
```
**Solución:** Verificar variables de entorno de keystore

#### Upload Failed
```
GitHub token permissions insufficient
```
**Solución:** Verificar permisos del token GITHUB_TOKEN

### Validación de Release:
```bash
# Verificar APK firmado
jarsigner -verify -verbose -certs app/build/outputs/apk/release/ImmichTV-*.apk

# Instalar en dispositivo test
adb install app/build/outputs/apk/release/ImmichTV-*.apk

# Verificar funcionamiento básico
adb shell monkey -p com.albertoeg.android.tv.immich -c android.intent.category.LAUNCHER 1
```

## Distribución Post-Release

### Comunicación:
1. Actualizar README con nueva versión
2. Anunciar en canales de comunicación
3. Documentar breaking changes si existen
4. Actualizar documentación de usuario

### Monitoreo:
1. Revisar Crashlytics por nuevos errores
2. Monitorear descargas y adopción
3. Recopilar feedback de usuarios
4. Planificar próxima versión

Para configuración del entorno de desarrollo, consultar [DESARROLLO-WINDOWS.md](./DESARROLLO-WINDOWS.md)
Para información sobre funcionalidades, consultar [FUNCIONALIDADES.md](./FUNCIONALIDADES.md)