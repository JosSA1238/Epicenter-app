# Compilar Epicenter Pro X desde un teléfono Android

Este proyecto incluye un flujo de GitHub Actions para compilar un APK en servidores de GitHub, sin instalar Android Studio en el teléfono.

1. Desde el navegador del teléfono, crea un repositorio privado nuevo en GitHub.
2. Sube **el contenido de la carpeta EpicenterProX** a la raíz del repositorio, incluida `.github/workflows/build-apk.yml`. Para conservar carpetas fácilmente, puedes usar una app Git de Android o un entorno de terminal con Git.
3. En el repositorio abre **Actions** > **Compilar APK Android** > **Run workflow**.
4. Cuando termine correctamente, entra a la ejecución y descarga el artefacto **EpicenterProX-debug-apk**.
5. Descomprime ese artefacto y abre `app-debug.apk` para instalarlo.

Nota: no se ha compilado ni probado aquí. El APK será una versión de demostración con reproductor local, ecualizador, BassBoost y Concert Hall sujeto a compatibilidad. No modifica audio de otras aplicaciones ni incorpora todavía la síntesis subarmónica en la cadena de reproducción.
