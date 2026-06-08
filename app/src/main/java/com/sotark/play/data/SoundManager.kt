package com.sotark.play.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
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

    // id звука → загружен?
    private data class Sound(var id: Int = 0, var ready: Boolean = false)

    private val scamper   = Sound()
    private val carbonate = Sound()
    private val iota      = Sound()
    private val gradient  = Sound()
    private val discovery = Sound()

    private var launchPlayed = false

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                when (sampleId) {
                    scamper.id   -> scamper.ready   = true
                    carbonate.id -> carbonate.ready = true
                    iota.id      -> iota.ready      = true
                    gradient.id  -> gradient.ready  = true
                    discovery.id -> discovery.ready = true
                }
            }
        }
        scamper.id   = pool.load(ctx, R.raw.scamper,   1)
        carbonate.id = pool.load(ctx, R.raw.carbonate, 1)
        iota.id      = pool.load(ctx, R.raw.iota,      1)
        gradient.id  = pool.load(ctx, R.raw.gradient,  1)
        discovery.id = pool.load(ctx, R.raw.discovery, 1)
    }

    private fun se() = settings.soundEnabled.value
    private fun he() = settings.hapticEnabled.value

    private fun play(s: Sound, vol: Float = 1f) {
        if (se() && s.ready) pool.play(s.id, vol, vol, 1, 0, 1f)
    }

    /** scamper — 1 раз при запуске */
    fun playLaunch() {
        if (launchPlayed) return
        launchPlayed = true
        play(scamper)
        vibrate(40)
    }

    /** carbonate — клик / смена вкладки */
    fun playClick() {
        play(carbonate, 0.7f)
        vibrate(18)
    }

    /** iota — APK установлен */
    fun playInstall() {
        play(iota)
        vibrate(80)
    }

    /** iota — уведомление */
    fun playNotification() {
        play(iota, 0.8f)
        vibrate(30)
    }

    /** gradient — ошибка */
    fun playError() {
        play(gradient)
        vibrateError()
    }

    /** discovery — пасхалка / секретное меню */
    fun playSecret() {
        play(discovery)
        vibrate(60)
    }

    // Алиас
    fun playSuccess() = playInstall()

    private fun vibrate(ms: Long) {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun vibrateError() {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
    }
}
