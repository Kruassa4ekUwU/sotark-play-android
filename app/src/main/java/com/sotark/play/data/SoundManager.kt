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
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var sndLaunch  = 0
    private var sndClick   = 0
    private var sndSuccess = 0
    private var sndError   = 0
    private var sndSecret  = 0

    init {
        sndLaunch  = pool.load(ctx, R.raw.scamper,   1)
        sndClick   = pool.load(ctx, R.raw.carbonate, 1)
        sndSuccess = pool.load(ctx, R.raw.iota,      1)
        sndError   = pool.load(ctx, R.raw.gradient,  1)
        sndSecret  = pool.load(ctx, R.raw.discovery, 1)
    }

    private fun soundEnabled() = settings.soundEnabled.value
    private fun hapticEnabled() = settings.hapticEnabled.value

    fun playLaunch()  { if (soundEnabled()) pool.play(sndLaunch,  1f, 1f, 0, 0, 1f) }
    fun playClick()   { if (soundEnabled()) pool.play(sndClick,   0.7f, 0.7f, 0, 0, 1f); vibrate(10) }
    fun playSuccess() { if (soundEnabled()) pool.play(sndSuccess, 1f, 1f, 0, 0, 1f); vibrate(80) }
    fun playError()   { if (soundEnabled()) pool.play(sndError,   1f, 1f, 0, 0, 1f); vibrateError() }
    fun playSecret()  { if (soundEnabled()) pool.play(sndSecret,  1f, 1f, 0, 0, 1f); vibrate(30) }

    private fun vibrate(ms: Long) {
        if (!hapticEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrateError() {
        if (!hapticEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
        }
    }
}
