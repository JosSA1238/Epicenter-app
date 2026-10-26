package com.epicenterprox.app.dsp

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/** Experimental, original subharmonic generator for PCM buffers; not connected to global output. */
class SubharmonicProcessor(private val sampleRate: Int) {
    init { require(sampleRate > 0) }
    var mix = 0.25f
    var threshold = 0.035f
    var outputGain = 0.75f
    private var low = 0.0
    private var previous = 0.0
    private var lastCrossing = -1L
    private var period = 0.0
    private var sampleIndex = 0L
    private var phase = 0.0
    private var envelope = 0.0
    private val alpha = 1.0 - kotlin.math.exp(-2.0 * PI * 110.0 / sampleRate)

    /** Input/output mono floating-point PCM [-1,1]. Call sequentially on audio thread. */
    fun process(input: FloatArray): FloatArray {
        val output = FloatArray(input.size)
        for (i in input.indices) {
            val dry = input[i].toDouble().coerceIn(-1.0, 1.0)
            low += alpha * (dry - low)
            val rising = previous <= 0.0 && low > 0.0 && abs(low) > 0.001
            if (rising) {
                if (lastCrossing >= 0) {
                    val measured = (sampleIndex - lastCrossing).toDouble()
                    val frequency = sampleRate / measured
                    if (frequency in 45.0..160.0) period = if (period == 0.0) measured else period * 0.8 + measured * 0.2
                }
                lastCrossing = sampleIndex
            }
            previous = low
            val active = if (period > 0 && abs(low) > threshold) 1.0 else 0.0
            envelope += (active - envelope) * if (active > envelope) 0.003 else 0.0005
            if (period > 0) phase += PI / period // half the detected fundamental
            if (phase >= 2 * PI) phase -= 2 * PI
            val wet = sin(phase) * envelope * mix.coerceIn(0f, 0.6f)
            output[i] = ((dry + wet) * outputGain.coerceIn(0f, 1f)).coerceIn(-1.0, 1.0).toFloat()
            sampleIndex++
        }
        return output
    }
    fun reset() { low=0.0; previous=0.0; lastCrossing=-1; period=0.0; sampleIndex=0; phase=0.0; envelope=0.0 }
}
