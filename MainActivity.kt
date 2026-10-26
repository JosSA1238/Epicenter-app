package com.epicenterprox.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.EnvironmentalReverb
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

/** Demo: processes only audio played by this app. No global audio interception. */
class MainActivity : Activity() {
    private var player: MediaPlayer? = null
    private var bass: BassBoost? = null
    private var equalizer: Equalizer? = null
    private var hall: EnvironmentalReverb? = null
    private lateinit var hallSwitch: Switch
    private lateinit var roomSlider: SeekBar
    private lateinit var decaySlider: SeekBar
    private lateinit var wetSlider: SeekBar
    private lateinit var status: TextView
    private lateinit var bassSlider: SeekBar
    private lateinit var eqBox: LinearLayout
    private val cyan = Color.rgb(55, 217, 235)
    private val bg = Color.rgb(10, 13, 19)
    private val panel = Color.rgb(21, 29, 40)
    private val pickAudio = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 40, 28, 30) }
        scroll.addView(root)
        root.addView(label("EPICENTER PRO X", 27, cyan))
        root.addView(label("DIGITAL BASS PROCESSOR  •  DEMO 0.1", 12, Color.LTGRAY))
        root.addView(label("Reproductor de audio local", 20, Color.WHITE))
        status = label("Selecciona un archivo de audio para activar los efectos.", 14, Color.LTGRAY)
        root.addView(status)
        val open = button("Abrir archivo de audio") { val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "audio/*" }; startActivityForResult(i, pickAudio) }
        root.addView(open)
        root.addView(button("Pausar / reanudar") { player?.let { if (it.isPlaying) it.pause() else it.start() } })
        root.addView(label("BASS BOOST", 19, cyan))
        bassSlider = SeekBar(this).apply { max = 1000; progress = 350; setOnSeekBarChangeListener(changes { bass?.setStrength(progress.toShort()) }) }
        root.addView(bassSlider)
        root.addView(label("ECUALIZADOR", 19, cyan))
        eqBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(eqBox)
        root.addView(label("CONCERT HALL • REVERB", 19, cyan))
        hallSwitch = Switch(this).apply { text = "Activar Concert Hall"; setTextColor(Color.WHITE); isChecked = false
            setOnCheckedChangeListener { _, _ -> updateHall() } }
        root.addView(hallSwitch)
        root.addView(label("Tamaño de sala", 14, Color.WHITE))
        roomSlider = SeekBar(this).apply { max = 100; progress = 70; setOnSeekBarChangeListener(changes { updateHall() }) }
        root.addView(roomSlider)
        root.addView(label("Duración de reverberación", 14, Color.WHITE))
        decaySlider = SeekBar(this).apply { max = 100; progress = 40; setOnSeekBarChangeListener(changes { updateHall() }) }
        root.addView(decaySlider)
        root.addView(label("Mezcla del efecto", 14, Color.WHITE))
        wetSlider = SeekBar(this).apply { max = 100; progress = 25; setOnSeekBarChangeListener(changes { updateHall() }) }
        root.addView(wetSlider)
        val hallPresets = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Studio", "Concert Hall", "Arena").forEach { name ->
            hallPresets.addView(button(name) { hallPreset(name) }, LinearLayout.LayoutParams(0, 110, 1f))
        }
        root.addView(hallPresets)
        root.addView(label("La reverberación depende del soporte de efectos de audio del dispositivo. No modifica otras aplicaciones.", 12, Color.LTGRAY))
        root.addView(label("PRESETS", 19, cyan))
        val presets = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Flat", "Deep Bass", "Clarity").forEach { name ->
            presets.addView(button(name) { applyPreset(name) }, LinearLayout.LayoutParams(0, 110, 1f))
        }
        root.addView(presets)
        root.addView(label("Los efectos actúan únicamente sobre la sesión de audio de este reproductor. La función Epicenter de síntesis subarmónica y el crossover DSP se incorporarán en una fase posterior.", 13, Color.LTGRAY))
        setContentView(scroll)
    }
    private fun label(s: String, size: Int, color: Int) = TextView(this).apply { text=s; textSize=size.toFloat(); setTextColor(color); setPadding(8, 14, 8, 14) }
    private fun button(s: String, f: () -> Unit) = Button(this).apply { text=s; setTextColor(Color.WHITE); setBackgroundColor(panel); setOnClickListener { f() } }
    private fun changes(f: () -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { if (fromUser) f() }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
    @Deprecated("Android activity result API used to avoid dependencies in starter")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickAudio && resultCode == RESULT_OK) data?.data?.let { loadAudio(it) }
    }
    private fun loadAudio(uri: Uri) {
        releaseAudio()
        try {
            player = MediaPlayer().apply { setDataSource(this@MainActivity, uri); prepare() }
            val session = player!!.audioSessionId
            try { bass = BassBoost(0, session).apply { enabled=true; setStrength(bassSlider.progress.toShort()) } } catch (_: Exception) { status.text="BassBoost no disponible en este dispositivo" }
            try { equalizer = Equalizer(0, session).apply { enabled=true }; buildEq() } catch (_: Exception) { eqBox.removeAllViews() }
            try {
                hall = EnvironmentalReverb(0, session).apply { enabled = false }
                updateHall()
            } catch (_: Exception) { status.text = "Concert Hall no disponible en este dispositivo" }
            player!!.start()
            status.text = "Reproduciendo archivo seleccionado"
        } catch (e: Exception) { status.text = "No se pudo reproducir: ${e.message}"; releaseAudio() }
    }
    private fun buildEq() {
        eqBox.removeAllViews()
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        for (i in 0 until eq.numberOfBands.toInt()) {
            val band = i.toShort()
            val hz = eq.getCenterFreq(band) / 1000
            eqBox.addView(label("${hz} Hz", 13, Color.WHITE))
            val slider = SeekBar(this).apply {
                max = (range[1] - range[0]).toInt()
                progress = (eq.getBandLevel(band) - range[0]).toInt()
                setOnSeekBarChangeListener(changes { eq.setBandLevel(band, (range[0] + progress).toShort()) })
            }
            eqBox.addView(slider)
        }
    }
    private fun applyPreset(name: String) {
        val eq = equalizer ?: run { status.text="Carga audio primero"; return }
        val range = eq.bandLevelRange
        for (i in 0 until eq.numberOfBands.toInt()) {
            val band = i.toShort()
            val hz = eq.getCenterFreq(band) / 1000
            val level = when(name) { "Deep Bass" -> if (hz < 150) 450 else if (hz < 400) 180 else 0; "Clarity" -> if (hz > 2000) 300 else 0; else -> 0 }
            eq.setBandLevel(band, level.coerceIn(range[0].toInt(), range[1].toInt()).toShort())
        }
        bassSlider.progress = if (name=="Deep Bass") 650 else 0
        bass?.setStrength(bassSlider.progress.toShort())
        buildEq()
        status.text = "Preset: $name"
    }
    private fun hallPreset(name: String) {
        val values = when (name) {
            "Studio" -> listOf(15, 12, 10)
            "Arena" -> listOf(95, 75, 45)
            else -> listOf(70, 40, 25)
        }
        roomSlider.progress = values[0]
        decaySlider.progress = values[1]
        wetSlider.progress = values[2]
        hallSwitch.isChecked = true
        updateHall()
    }
    private fun updateHall() {
        val effect = hall ?: return
        try {
            // Android EnvironmentalReverb parameters are device-dependent.
            // Room level is -9000..0 mB, decay time is in milliseconds.
            effect.roomLevel = (-9000 + roomSlider.progress * 90).toShort()
            effect.decayTime = 200 + decaySlider.progress * 78
            effect.reverbLevel = (-9000 + wetSlider.progress * 90).toShort()
            effect.enabled = hallSwitch.isChecked
        } catch (_: Exception) {
            status.text = "El dispositivo no admite todos los controles Concert Hall"
        }
    }
    private fun releaseAudio() {
        hall?.release(); hall = null
        player?.run { try { if(isPlaying) stop() } catch (_: Exception) {}; release() }; player=null
        bass?.release(); bass=null; equalizer?.release(); equalizer=null
    }
    override fun onDestroy() { releaseAudio(); super.onDestroy() }
}
