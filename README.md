# Epicenter Pro X — prototipo Android 0.1

Proyecto **nuevo e independiente** inspirado en la categoría de procesadores de bajos. No contiene código, bibliotecas ni recursos extraídos de DanteFX.

## Funciona ahora
- Selector de audio local mediante Storage Access Framework.
- Reproducción con MediaPlayer.
- BassBoost y ecualizador nativos de Android sobre la sesión de audio propia.
- Presets Flat, Deep Bass y Clarity.
- Interfaz oscura básica. El número de bandas depende del dispositivo (no se promete EQ de 10 bandas).

## Pendiente
- DSP subarmónico original tipo Epicenter, crossover LPF/HPF, limitador, visualizador, presets persistentes y diseño visual refinado.
- Pruebas reales de dispositivos y gestión de audio focus / interrupciones.
- Para efectos globales en otras apps hay restricciones del sistema y no se garantiza compatibilidad.

## Cómo abrir
1. Abrir la carpeta EpicenterProX en Android Studio.
2. Permitir sincronización Gradle con JDK 17 y SDK Android 35.
3. Ejecutar en dispositivo Android 10 o superior.
4. Seleccionar un archivo de audio propio y probar efectos.

**Aviso:** El proyecto no se ha compilado en este entorno (no hay Android SDK/Gradle). Se entrega código fuente, no APK.

## Fase 2: diseño del DSP y ruta externa
Lee `ARCHITECTURE.md`. Se añadió `dsp/SubharmonicProcessor.kt` como prototipo de síntesis subarmónica para buffers PCM; aún no está integrado al reproductor ni a la salida global. No se afirma compatibilidad universal con otras apps.


## Fase 2.1: Concert Hall
Se añadió un efecto `EnvironmentalReverb` a la sesión de audio del reproductor local. Controles de sala, tiempo de decaimiento y nivel de reverberación, con presets Studio, Concert Hall y Arena. La compatibilidad depende de los efectos que implemente Android en cada dispositivo. No hay captura global ni subarmónicos conectados al reproductor.
