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

    // Простая структура без idMap — ищем по poolId в списке
    private inner class S(val resId: Int) {
        @Volatile var poolId: Int = -1
        @Volatile var ready: Boolean = false
        val pending = ArrayDeque<Pair<Float, Long>>()  // vol, vibMs
    }

    private val scamper   = S(R.raw.scamper)
    private val carbonate = S(R.raw.carbonate)
    private val iota      = S(R.raw.iota)
    private val gradient  = S(R.raw.gradient)
    private val discovery = S(R.raw.discovery)
    private val all       = listOf(scamper, carbonate, iota, gradient, discovery)

    private var launchPlayed = false

    init {
        // Listener ПЕРЕД load()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            // Ищем entry у которого poolId == sampleId
            // poolId уже записан синхронно (load() вернул его до callback)
            val s = all.firstOrNull { it.poolId == sampleId } ?: return@setOnLoadCompleteListener
            s.ready = true
            // Воспроизводим накопленную очередь на главном потоке
            mainHandler.post {
                while (s.pending.isNotEmpty()) {
                    val (vol, vibMs) = s.pending.removeFirst()
                    if (settings.soundEnabled.value)
                        pool.play(s.poolId, vol, vol, 1, 0, 1f)
                    applyVibration(vibMs)
                }
            }
        }

        // load() синхронно возвращает poolId и сразу пишем его в entry.
        // Callback придёт позже (async I/O) — к тому моменту poolId уже записан.
        scamper.poolId   = pool.load(ctx, R.raw.scamper,   1)
        carbonate.poolId = pool.load(ctx, R.raw.carbonate, 1)
        iota.poolId      = pool.load(ctx, R.raw.iota,      1)
        gradient.poolId  = pool.load(ctx, R.raw.gradient,  1)
        discovery.poolId = pool.load(ctx, R.raw.discovery, 1)
    }

    private fun se() = settings.soundEnabled.value
    private fun he() = settings.hapticEnabled.value

    private fun applyVibration(vibMs: Long) {
        when {
            vibMs == -1L -> doVibrateError()
            vibMs  >  0L -> doVibrate(vibMs)
        }
    }

    private fun play(s: S, vol: Float = 1f, vibMs: Long = 0L) {
        if (s.ready) {
            if (se()) pool.play(s.poolId, vol, vol, 1, 0, 1f)
            applyVibration(vibMs)
        } else {
            // Звук ещё грузится — в очередь (только вибрацию тоже откладываем)
            s.pending.addLast(vol to vibMs)
        }
    }

    // ── Публичный API ─────────────────────────────────────────────

    /** scamper — 1 раз при запуске */
    fun playLaunch() {
        if (launchPlayed) return
        launchPlayed = true
        play(scamper, 1f, 40L)
    }

    /** carbonate — клик / навигация */
    fun playClick() = play(carbonate, 0.8f, 18L)

    /** iota — APK установлен */
    fun playInstall() = play(iota, 1f, 80L)

    /** iota — уведомление */
    fun playNotification() = play(iota, 0.8f, 30L)

    /** gradient — ошибка */
    fun playError() = play(gradient, 1f, -1L)

    /** discovery — пасхалка */
    fun playSecret() = play(discovery, 1f, 60L)

    fun playSuccess() = playInstall()

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
