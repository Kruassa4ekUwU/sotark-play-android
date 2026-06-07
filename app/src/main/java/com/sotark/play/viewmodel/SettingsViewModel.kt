package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import com.sotark.play.data.AppLanguage
import com.sotark.play.data.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: AppSettings
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
        settings.setDarkTheme(!darkTheme.value)
        // 5 тапов по тумблеру темы = секретное меню
        themeTapCount++
        if (themeTapCount >= 5) {
            settings.unlockSecretMenu()
            themeTapCount = 0
        }
    }

    fun setLanguage(lang: AppLanguage) {
        settings.setLanguage(lang)
        if (lang == AppLanguage.UKRAINIAN) {
            ukrainianTapCount++
            if (ukrainianTapCount >= 10) {
                settings.unlockEasterEgg()
                ukrainianTapCount = 0
            }
        } else {
            ukrainianTapCount = 0
        }
    }

    fun toggleUkrainianTheme()  = settings.setUkrainianTheme(!ukrainianTheme.value)
    fun toggleSound()           = settings.setSoundEnabled(!soundEnabled.value)
    fun toggleHaptic()          = settings.setHapticEnabled(!hapticEnabled.value)
    fun markEasterEggShown()    = settings.markEasterEggShown()
}
