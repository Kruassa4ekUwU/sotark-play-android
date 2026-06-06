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

    val darkTheme:        StateFlow<Boolean>     = settings.darkTheme
    val language:         StateFlow<AppLanguage> = settings.language
    val ukrainianTheme:   StateFlow<Boolean>     = settings.ukrainianTheme
    val easterEggUnlocked:StateFlow<Boolean>     = settings.easterEggUnlocked

    private var ukrainianTapCount = 0

    fun toggleDarkTheme() = settings.setDarkTheme(!darkTheme.value)
    fun setLanguage(lang: AppLanguage) {
        settings.setLanguage(lang)
        // Пасхалка — 10 тапов по Ukrainian
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
    fun toggleUkrainianTheme() = settings.setUkrainianTheme(!ukrainianTheme.value)
}
