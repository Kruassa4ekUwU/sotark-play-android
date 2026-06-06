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
    val darkTheme: StateFlow<Boolean>     = settings.darkTheme
    val language: StateFlow<AppLanguage>  = settings.language

    fun toggleDarkTheme() = settings.setDarkTheme(!darkTheme.value)
    fun setLanguage(lang: AppLanguage) = settings.setLanguage(lang)
}
