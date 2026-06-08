package com.sotark.play.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Звуки временно отключены — заглушка для совместимости DI */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val settings: AppSettings
) {
    fun playLaunch()       = Unit
    fun playClick()        = Unit
    fun playInstall()      = Unit
    fun playNotification() = Unit
    fun playError()        = Unit
    fun playSecret()       = Unit
    fun playSuccess()      = Unit
}
