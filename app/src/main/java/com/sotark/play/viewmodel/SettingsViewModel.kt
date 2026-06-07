package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import com.sotark.play.data.AppLanguage
import com.sotark.play.data.AppSettings
import com.sotark.play.data.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings,
    private val sound: SoundManager
) : ViewModel() {

    val darkTheme:          StateFlow<Boolean>     = settings.darkTheme
    val language:           StateFlow<AppLanguage> = settings.language
    val ukrainianTheme:     StateFlow<Boolean>     = settings.ukrainianTheme
    val easterEggUnlocked:  StateFlow<Boolean>     = settings.easterEggUnlocked
    val easterEggShown:     StateFlow<Boolean>     = settings.easterEggShown
    val secretMenuUnlocked: StateFlow<Boolean>     = settings.secretMenuUnlocked
    val soundEnabled:       StateFlow<Boolean>     = settings.soundEnabled
    val hapticEnabled:      StateFlow<Boolean>     = settings.hapticEnabled

    private var ukrainianTapCount = 0
    private var themeTapCount     = 0

    fun toggleDarkTheme() {
        sound.playClick()
        settings.setDarkTheme(!darkTheme.value)
        themeTapCount++
        if (themeTapCount >= 5) {
            settings.unlockSecretMenu()
            sound.playSecret()
            themeTapCount = 0
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

    fun toggleUkrainianTheme() { sound.playClick(); settings.setUkrainianTheme(!ukrainianTheme.value) }
    fun toggleSound()           { settings.setSoundEnabled(!soundEnabled.value) }  // без звука при выкл
    fun toggleHaptic()          { sound.playClick(); settings.setHapticEnabled(!hapticEnabled.value) }
    fun markEasterEggShown()    = settings.markEasterEggShown()
}
