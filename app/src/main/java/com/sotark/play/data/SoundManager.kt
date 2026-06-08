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

    private inner class Sound(val resId: Int) {
        var poolId: Int  = 0
        var ready: Boolean = false
        // Очередь: пара (volume, vibrateMs/-1 для error pattern)
        val pending = mutableListOf<Pair<Float, Long>>()
    }

    private val scamper   = Sound(R.raw.scamper)
    private val carbonate = Sound(R.raw.carbonate)
    private val iota      = Sound(R.raw.iota)
    private val gradient  = Sound(R.raw.gradient)
    private val discovery = Sound(R.raw.discovery)

    private val allSounds = listOf(scamper, carbonate, iota, gradient, discovery)

    private var launchPending = false
    private var launchPlayed  = false

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                val sound = allSounds.firstOrNull { it.poolId == sampleId } ?: return@setOnLoadCompleteListener
                sound.ready = true
                // Воспроизводим всё из очереди на главном потоке
                mainHandler.post {
                    sound.pending.forEach { (vol, vibMs) ->
                        pool.play(sound.poolId, vol, vol, 1, 0, 1f)
                        if (vibMs == -1L) doVibrateError() else doVibrate(vibMs)
                    }
                    sound.pending.clear()
                }
            }
        }
        // Загружаем все звуки — poolId присваивается после load()
        allSounds.forEach { it.poolId = pool.load(ctx, it.resId, 1) }
    }

    private fun se() = settings.soundEnabled.value
    private fun he() = settings.hapticEnabled.value

    /** Воспроизвести звук. Если ещё не загружен — добавить в очередь. */
    private fun play(sound: Sound, vol: Float = 1f, vibMs: Long = 0L) {
        if (!se() && vibMs == 0L) return
        if (sound.ready) {
            if (se()) pool.play(sound.poolId, vol, vol, 1, 0, 1f)
            if (vibMs == -1L) doVibrateError() else if (vibMs > 0) doVibrate(vibMs)
        } else {
            // Звук ещё грузится — ставим в очередь
            sound.pending.add(vol to vibMs)
        }
    }

    // ── Публичное API ─────────────────────────────────────────────

    /** scamper — только 1 раз при запуске */
    fun playLaunch() {
        if (launchPlayed || launchPending) return
        launchPending = true
        launchPlayed  = true
        play(scamper, 1f, 40L)
    }

    /** carbonate — клик / навигация */
    fun playClick() = play(carbonate, 0.7f, 18L)

    /** iota — APK установлен */
    fun playInstall() = play(iota, 1f, 80L)

    /** iota — уведомление */
    fun playNotification() = play(iota, 0.8f, 30L)

    /** gradient — ошибка */
    fun playError() = play(gradient, 1f, -1L)  // -1 = error pattern

    /** discovery — пасхалка */
    fun playSecret() = play(discovery, 1f, 60L)

    // Алиас для обратной совместимости
    fun playSuccess() = playInstall()

    // ── Вибрация ──────────────────────────────────────────────────

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
