# Epicenter Pro X — Fase 2 (arquitectura)

## Restricción principal
Android no ofrece una API pública general para interceptar, modificar y devolver al mezclador todo el audio de otras apps. AudioPlaybackCapture (Android 10+) permite capturar ciertas reproducciones con consentimiento explícito mediante MediaProjection, pero depende de la política de captura de la app fuente y del uso de audio; no permite reinsertar el audio procesado en la misma ruta original. La reproducción procesada por AudioTrack puede introducir latencia, duplicación y eco. El efecto global con sesión 0 es obsoleto/no confiable, y Shizuku por sí solo no concede una ruta de procesamiento global.

## Arquitectura propuesta
- UI: Kotlin Activity -> ViewModel/StateFlow (futuro) con presets, medidores y avisos de compatibilidad.
- Controlador de rutas: modo local (soportado); modo captura autorizada (experimental); modo sistema (solo si existe una integración de sistema/root compatible y con consentimiento).
- Captura experimental: MediaProjection + AudioPlaybackCaptureConfiguration + AudioRecord; foreground service tipo mediaProjection; consentimiento por sesión.
- Motor DSP: PCM float estéreo -> filtro HP de protección 25–30 Hz -> detector de graves 45–160 Hz -> generador de subarmónicos /2 -> mezcla limitada -> EQ paramétrico -> limitador con lookahead -> salida AudioTrack.
- Implementación inicial: `SubharmonicProcessor.kt` (mono, prototipo sin filtros completos ni limitador profesional); no se conecta a la ruta global ni al reproductor MediaPlayer.
- Ruta productiva: motor C++ Oboe/AAudio para baja latencia, buffers preasignados, procesado estéreo y medición de CPU, THD y clipping.

## Próximos hitos
1. Integrar un reproductor PCM (MediaExtractor + MediaCodec + AudioTrack) para probar realmente el DSP, antes de captura global.
2. Añadir modo captura autorizada con MediaProjection y comprobar apps que permiten captura; impedir bucles/eco.
3. Añadir filtros Linkwitz-Riley, EQ de 10 bandas, limitador y protección para bocinas.
4. Pruebas en dispositivos reales y medición de latencia y niveles.

## Legalidad y procedencia
Código nuevo e independiente. No copiar ni redistribuir bibliotecas nativas, recursos, marcas ni algoritmos propietarios de DanteFX.
