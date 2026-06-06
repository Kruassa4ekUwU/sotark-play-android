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
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    UKRAINIAN("uk", "Українська"),
    HEBREW("he", "עברית")  // fix: "iw" устарел, используем "he"
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

    // fix: храним показана ли пасхалка уже — не показываем повторно
    private val _easterEggUnlocked = MutableStateFlow(prefs.getBoolean("easter_egg", false))
    val easterEggUnlocked: StateFlow<Boolean> = _easterEggUnlocked.asStateFlow()

    private val _easterEggShown = MutableStateFlow(prefs.getBoolean("easter_egg_shown", false))
    val easterEggShown: StateFlow<Boolean> = _easterEggShown.asStateFlow()

    private val _language = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("language", "en") }
            ?: AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        _darkTheme.value = enabled
    }

    fun unlockEasterEgg() {
        prefs.edit()
            .putBoolean("easter_egg", true)
            .putBoolean("ukrainian_theme", true)
            .apply()
        _easterEggUnlocked.value = true
        _ukrainianTheme.value = true
    }

    fun markEasterEggShown() {
        prefs.edit().putBoolean("easter_egg_shown", true).apply()
        _easterEggShown.value = true
    }

    fun setUkrainianTheme(enabled: Boolean) {
        prefs.edit().putBoolean("ukrainian_theme", enabled).apply()
        _ukrainianTheme.value = enabled
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString("language", lang.code).apply()
        _language.value = lang
    }
}
