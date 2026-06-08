package com.sotark.play.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.sotark.play.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val settings: AppSettings
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private data class SoundEntry(
        var poolId: Int = 0,
        var ready: Boolean = false,
        val pending: MutableList<Pair<Float, Long>> = mutableListOf()
    )

    // Сначала создаём entries, потом загружаем — listener уже установлен
    private val scamper   = SoundEntry()
    private val carbonate = SoundEntry()
    private val iota      = SoundEntry()
    private val gradient  = SoundEntry()
    private val discovery = SoundEntry()

    // Карта poolId → entry (заполняется после load)
    private val idMap = mutableMapOf<Int, SoundEntry>()

    private var launchPlayed = false

    init {
        // СНАЧАЛА listener, ПОТОМ load — иначе быстрые загрузки пропускаем
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            val entry = idMap[sampleId] ?: return@setOnLoadCompleteListener
            entry.ready = true
            mainHandler.post {
                entry.pending.forEach { (vol, vibMs) ->
                    if (settings.soundEnabled.value)
                        pool.play(entry.poolId, vol, vol, 1, 0, 1f)
                    when {
                        vibMs == -1L -> doVibrateError()
                        vibMs  >  0L -> doVibrate(vibMs)
                    }
                }
                entry.pending.clear()
            }
        }
        // Загружаем и регистрируем в карте
        scamper.poolId   = pool.load(ctx, R.raw.scamper,   1).also { idMap[it] = scamper   }
        carbonate.poolId = pool.load(ctx, R.raw.carbonate, 1).also { idMap[it] = carbonate }
        iota.poolId      = pool.load(ctx, R.raw.iota,      1).also { idMap[it] = iota      }
        gradient.poolId  = pool.load(ctx, R.raw.gradient,  1).also { idMap[it] = gradient  }
        discovery.poolId = pool.load(ctx, R.raw.discovery, 1).also { idMap[it] = discovery }
    }

    private fun se() = settings.soundEnabled.value
    private fun he() = settings.hapticEnabled.value

    private fun play(entry: SoundEntry, vol: Float = 1f, vibMs: Long = 0L) {
        if (entry.ready) {
            if (se()) pool.play(entry.poolId, vol, vol, 1, 0, 1f)
            when {
                vibMs == -1L -> doVibrateError()
                vibMs  >  0L -> doVibrate(vibMs)
            }
        } else {
            // Ставим в очередь — воспроизведётся после загрузки
            entry.pending.add(vol to vibMs)
        }
    }

    fun playLaunch() {
        if (launchPlayed) return
        launchPlayed = true
        play(scamper, 1f, 40L)
    }

    fun playClick()        = play(carbonate, 0.7f, 18L)
    fun playInstall()      = play(iota,      1f,   80L)
    fun playNotification() = play(iota,      0.8f, 30L)
    fun playError()        = play(gradient,  1f,   -1L)
    fun playSecret()       = play(discovery, 1f,   60L)
    fun playSuccess()      = playInstall()

    private fun doVibrate(ms: Long) {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun doVibrateError() {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
    }
}
