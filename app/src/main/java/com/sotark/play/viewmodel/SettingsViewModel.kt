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

    val darkTheme:            StateFlow<Boolean>     = settings.darkTheme
    val language:             StateFlow<AppLanguage> = settings.language
    val ukrainianTheme:       StateFlow<Boolean>     = settings.ukrainianTheme
    val easterEggUnlocked:    StateFlow<Boolean>     = settings.easterEggUnlocked
    val easterEggShown:       StateFlow<Boolean>     = settings.easterEggShown
    val israelEasterEggUnlocked: StateFlow<Boolean>  = settings.israelEasterEggUnlocked
    val israelEasterEggShown: StateFlow<Boolean>     = settings.israelEasterEggShown
    val secretMenuUnlocked:   StateFlow<Boolean>     = settings.secretMenuUnlocked
    val soundEnabled:         StateFlow<Boolean>     = settings.soundEnabled
    val hapticEnabled:        StateFlow<Boolean>     = settings.hapticEnabled
    val secretTheme:          StateFlow<SecretTheme> = settings.secretTheme
    val darkThemeToggleCount: StateFlow<Int>         = settings.darkThemeToggleCount

    private var ukrainianTapCount = 0
    private var israelTapCount    = 0

    fun toggleDarkTheme() {
        sound.playClick()
        settings.setDarkTheme(!darkTheme.value)
        if (settings.darkThemeToggleCount.value >= 5) {
            settings.unlockSecretMenu()
            settings.resetDarkThemeToggleCount()
            sound.playSecret()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        sound.playClick()
        settings.setLanguage(lang)
        when (lang) {
            AppLanguage.UKRAINIAN -> {
                ukrainianTapCount++
                israelTapCount = 0
                if (ukrainianTapCount >= 10) {
                    settings.unlockEasterEgg()
                    sound.playSecret()
                    ukrainianTapCount = 0
                }
            }
            AppLanguage.HEBREW -> {
                israelTapCount++
                ukrainianTapCount = 0
                if (israelTapCount >= 10) {
                    settings.unlockIsraelEasterEgg()
                    sound.playSecret()
                    israelTapCount = 0
                }
            }
            else -> { ukrainianTapCount = 0; israelTapCount = 0 }
        }
    }

    fun setSecretTheme(theme: SecretTheme) {
        sound.playClick()
        val new = if (settings.secretTheme.value == theme) SecretTheme.NONE else theme
        settings.setSecretTheme(new)
        if (new != SecretTheme.NONE) sound.playSecret()
    }

    fun toggleUkrainianTheme() {
        sound.playClick()
        settings.setUkrainianTheme(settings.secretTheme.value != SecretTheme.UKRAINIAN)
    }
    fun toggleSound()        { settings.setSoundEnabled(!soundEnabled.value) }
    fun toggleHaptic()       { sound.playClick(); settings.setHapticEnabled(!hapticEnabled.value) }
    fun markEasterEggShown() = settings.markEasterEggShown()
    fun markIsraelEasterEggShown() = settings.markIsraelEasterEggShown()
}
