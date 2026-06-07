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
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Звуки по назначению (как в ТЗ):
    // scamper   = запуск приложения (1 раз)
    // carbonate = клик / навигация между вкладками
    // iota      = установка APK + уведомление
    // gradient  = ошибка
    // discovery = секретная пасхалка
    private var sndScamper   = 0  // запуск
    private var sndCarbonate = 0  // клик/навигация
    private var sndIota      = 0  // установка/уведомление
    private var sndGradient  = 0  // ошибка
    private var sndDiscovery = 0  // секрет

    private var launchPlayed = false   // scamper только 1 раз за сессию

    init {
        pool.setOnLoadCompleteListener { _, _, _ -> }
        sndScamper   = pool.load(ctx, R.raw.scamper,   1)
        sndCarbonate = pool.load(ctx, R.raw.carbonate, 1)
        sndIota      = pool.load(ctx, R.raw.iota,      1)
        sndGradient  = pool.load(ctx, R.raw.gradient,  1)
        sndDiscovery = pool.load(ctx, R.raw.discovery, 1)
    }

    private fun se() = settings.soundEnabled.value
    private fun he() = settings.hapticEnabled.value

    /** scamper — ТОЛЬКО 1 раз при старте приложения */
    fun playLaunch() {
        if (!launchPlayed) {
            launchPlayed = true
            if (se()) pool.play(sndScamper, 1f, 1f, 1, 0, 1f)
            vibrate(40)
        }
    }

    /** carbonate — нажатие кнопки / переход между вкладками */
    fun playClick() {
        if (se()) pool.play(sndCarbonate, 0.7f, 0.7f, 0, 0, 1f)
        vibrate(18)
    }

    /** iota — успешная установка APK */
    fun playInstall() {
        if (se()) pool.play(sndIota, 1f, 1f, 1, 0, 1f)
        vibrate(80)
    }

    /** iota — уведомление об обновлении */
    fun playNotification() {
        if (se()) pool.play(sndIota, 0.8f, 0.8f, 1, 0, 1f)
        vibrate(30)
    }

    /** gradient — ошибка */
    fun playError() {
        if (se()) pool.play(sndGradient, 1f, 1f, 1, 0, 1f)
        vibrateError()
    }

    /** discovery — пасхалка / секретное меню */
    fun playSecret() {
        if (se()) pool.play(sndDiscovery, 1f, 1f, 1, 0, 1f)
        vibrate(60)
    }

    // Старые алиасы — чтобы не ломать код
    fun playSuccess() = playInstall()

    private fun vibrate(ms: Long) {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrateError() {
        if (!he()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
        }
    }
}
