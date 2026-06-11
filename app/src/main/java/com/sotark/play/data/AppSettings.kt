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
    HEBREW("iw",   "עברית")
}

enum class SecretTheme(val id: String) {
    NONE("none"),
    UKRAINIAN("ukrainian"),
    ISRAEL("israel"),
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

    private val _secretTheme = MutableStateFlow(
        SecretTheme.values().find { it.id == prefs.getString("secret_theme", "none") }
            ?: SecretTheme.NONE
    )
    val secretTheme: StateFlow<SecretTheme> = _secretTheme.asStateFlow()

    private val _ukrainianThemeCompat = MutableStateFlow(
        _secretTheme.value == SecretTheme.UKRAINIAN
    )
    val ukrainianTheme: StateFlow<Boolean> = _ukrainianThemeCompat.asStateFlow()

    private val _easterEggUnlocked = MutableStateFlow(prefs.getBoolean("easter_egg", false))
    val easterEggUnlocked: StateFlow<Boolean> = _easterEggUnlocked.asStateFlow()

    private val _easterEggShown = MutableStateFlow(prefs.getBoolean("easter_egg_shown", false))
    val easterEggShown: StateFlow<Boolean> = _easterEggShown.asStateFlow()

    private val _israelEasterEggUnlocked = MutableStateFlow(prefs.getBoolean("israel_easter_egg", false))
    val israelEasterEggUnlocked: StateFlow<Boolean> = _israelEasterEggUnlocked.asStateFlow()

    private val _israelEasterEggShown = MutableStateFlow(prefs.getBoolean("israel_easter_egg_shown", false))
    val israelEasterEggShown: StateFlow<Boolean> = _israelEasterEggShown.asStateFlow()

    private val _secretMenuUnlocked = MutableStateFlow(prefs.getBoolean("secret_menu", false))
    val secretMenuUnlocked: StateFlow<Boolean> = _secretMenuUnlocked.asStateFlow()

    private val _language = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("language", "en") }
            ?: AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _darkThemeToggleCount = MutableStateFlow(prefs.getInt("dark_toggle_count", 0))
    val darkThemeToggleCount: StateFlow<Int> = _darkThemeToggleCount.asStateFlow()

    // Заглушки для совместимости с SoundManager
    val soundEnabled  = MutableStateFlow(false)
    val hapticEnabled = MutableStateFlow(false)

    fun setDarkTheme(v: Boolean) {
        val count = _darkThemeToggleCount.value + 1
        prefs.edit().putBoolean("dark_theme", v).putInt("dark_toggle_count", count).apply()
        _darkTheme.value = v
        _darkThemeToggleCount.value = count
    }

    fun resetDarkThemeToggleCount() {
        prefs.edit().putInt("dark_toggle_count", 0).apply()
        _darkThemeToggleCount.value = 0
    }

    fun unlockEasterEgg() {
        prefs.edit().putBoolean("easter_egg", true).apply()
        _easterEggUnlocked.value = true
        setSecretTheme(SecretTheme.UKRAINIAN)
    }

    fun markEasterEggShown() {
        prefs.edit().putBoolean("easter_egg_shown", true).apply()
        _easterEggShown.value = true
    }

    fun unlockIsraelEasterEgg() {
        prefs.edit().putBoolean("israel_easter_egg", true).apply()
        _israelEasterEggUnlocked.value = true
        setSecretTheme(SecretTheme.ISRAEL)
    }

    fun markIsraelEasterEggShown() {
        prefs.edit().putBoolean("israel_easter_egg_shown", true).apply()
        _israelEasterEggShown.value = true
    }

    fun unlockSecretMenu() {
        prefs.edit().putBoolean("secret_menu", true).apply()
        _secretMenuUnlocked.value = true
    }

    fun setSecretTheme(t: SecretTheme) {
        prefs.edit().putString("secret_theme", t.id).apply()
        _secretTheme.value = t
        _ukrainianThemeCompat.value = (t == SecretTheme.UKRAINIAN)
    }

    fun setUkrainianTheme(v: Boolean) =
        setSecretTheme(if (v) SecretTheme.UKRAINIAN else SecretTheme.NONE)

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("language", lang.code).apply()
        _language.value = lang
    }

    fun setSoundEnabled(v: Boolean)  { soundEnabled.value  = v }
    fun setHapticEnabled(v: Boolean) { hapticEnabled.value = v }
}
