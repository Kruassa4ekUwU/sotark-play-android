package com.sotark.play.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en",  "English"),
    RUSSIAN("ru",  "Русский"),
    UKRAINIAN("uk","Українська"),
    HEBREW("he",   "עברית")
}

enum class SecretTheme(val id: String) {
    NONE("none"),
    MATTE_METAL("matte_metal"),
    NEON("neon"),
    ONYX("onyx"),
    SUNSET("sunset")
}

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("sotark_prefs", Context.MODE_PRIVATE)

    private val _darkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _ukrainianTheme = MutableStateFlow(prefs.getBoolean("ukrainian_theme", false))
    val ukrainianTheme: StateFlow<Boolean> = _ukrainianTheme.asStateFlow()

    private val _easterEggUnlocked = MutableStateFlow(prefs.getBoolean("easter_egg", false))
    val easterEggUnlocked: StateFlow<Boolean> = _easterEggUnlocked.asStateFlow()

    private val _easterEggShown = MutableStateFlow(prefs.getBoolean("easter_egg_shown", false))
    val easterEggShown: StateFlow<Boolean> = _easterEggShown.asStateFlow()

    private val _secretMenuUnlocked = MutableStateFlow(prefs.getBoolean("secret_menu", false))
    val secretMenuUnlocked: StateFlow<Boolean> = _secretMenuUnlocked.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _language = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("language", "en") }
            ?: AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _secretTheme = MutableStateFlow(
        SecretTheme.values().find { it.id == prefs.getString("secret_theme", "none") }
            ?: SecretTheme.NONE
    )
    val secretTheme: StateFlow<SecretTheme> = _secretTheme.asStateFlow()

    // Счётчик переключений темной/светлой темы — для разблокировки секретного меню
    private val _darkThemeToggleCount = MutableStateFlow(
        prefs.getInt("dark_theme_toggle_count", 0)
    )
    val darkThemeToggleCount: StateFlow<Int> = _darkThemeToggleCount.asStateFlow()

    fun setDarkTheme(v: Boolean) {
        val newCount = _darkThemeToggleCount.value + 1
        prefs.edit()
            .putBoolean("dark_theme", v)
            .putInt("dark_theme_toggle_count", newCount)
            .apply()
        _darkTheme.value = v
        _darkThemeToggleCount.value = newCount
    }

    fun resetDarkThemeToggleCount() {
        prefs.edit().putInt("dark_theme_toggle_count", 0).apply()
        _darkThemeToggleCount.value = 0
    }

    fun setSoundEnabled(v: Boolean)  { prefs.edit().putBoolean("sound_enabled", v).apply();  _soundEnabled.value = v }
    fun setHapticEnabled(v: Boolean) { prefs.edit().putBoolean("haptic_enabled", v).apply(); _hapticEnabled.value = v }

    fun unlockEasterEgg() {
        prefs.edit().putBoolean("easter_egg", true).putBoolean("ukrainian_theme", true).apply()
        _easterEggUnlocked.value = true
        _ukrainianTheme.value    = true
    }
    fun markEasterEggShown() {
        prefs.edit().putBoolean("easter_egg_shown", true).apply()
        _easterEggShown.value = true
    }
    fun unlockSecretMenu() {
        prefs.edit().putBoolean("secret_menu", true).apply()
        _secretMenuUnlocked.value = true
    }
    fun setUkrainianTheme(v: Boolean) {
        prefs.edit().putBoolean("ukrainian_theme", v).apply()
        _ukrainianTheme.value = v
    }
    fun setSecretTheme(t: SecretTheme) {
        prefs.edit().putString("secret_theme", t.id).apply()
        _secretTheme.value = t
    }
    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("language", lang.code).apply()
        _language.value = lang
    }
}
