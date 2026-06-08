package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import com.sotark.play.data.AppLanguage
import com.sotark.play.data.AppSettings
import com.sotark.play.data.SecretTheme
import com.sotark.play.data.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings,
    private val sound: SoundManager
) : ViewModel() {

    val darkTheme:           StateFlow<Boolean>     = settings.darkTheme
    val language:            StateFlow<AppLanguage> = settings.language
    val ukrainianTheme:      StateFlow<Boolean>     = settings.ukrainianTheme
    val easterEggUnlocked:   StateFlow<Boolean>     = settings.easterEggUnlocked
    val easterEggShown:      StateFlow<Boolean>     = settings.easterEggShown
    val secretMenuUnlocked:  StateFlow<Boolean>     = settings.secretMenuUnlocked
    val soundEnabled:        StateFlow<Boolean>     = settings.soundEnabled
    val hapticEnabled:       StateFlow<Boolean>     = settings.hapticEnabled
    val secretTheme:         StateFlow<SecretTheme> = settings.secretTheme
    val darkThemeToggleCount:StateFlow<Int>         = settings.darkThemeToggleCount

    // Пасхалка: украинский язык × 10
    private var ukrainianTapCount = 0

    fun toggleDarkTheme() {
        sound.playClick()
        settings.setDarkTheme(!darkTheme.value)

        // 5 переключений подряд → секретное меню
        val count = settings.darkThemeToggleCount.value
        if (count >= 5) {
            settings.unlockSecretMenu()
            settings.resetDarkThemeToggleCount()
            sound.playSecret()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        sound.playClick()
        settings.setLanguage(lang)
        if (lang == AppLanguage.UKRAINIAN) {
            ukrainianTapCount++
            if (ukrainianTapCount >= 10) {
                settings.unlockEasterEgg()
                sound.playSecret()
                ukrainianTapCount = 0
            }
        } else {
            ukrainianTapCount = 0
        }
    }

    fun setSecretTheme(theme: SecretTheme) {
        sound.playClick()
        // Если нажали на уже активную — сбрасываем
        val new = if (settings.secretTheme.value == theme) SecretTheme.NONE else theme
        settings.setSecretTheme(new)
        if (new != SecretTheme.NONE) sound.playSecret()
    }

    fun toggleUkrainianTheme() { sound.playClick(); settings.setUkrainianTheme(!ukrainianTheme.value) }
    fun toggleSound()          { settings.setSoundEnabled(!soundEnabled.value) }
    fun toggleHaptic()         { sound.playClick(); settings.setHapticEnabled(!hapticEnabled.value) }
    fun markEasterEggShown()   = settings.markEasterEggShown()
}
