package com.sotark.play.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.BuildConfig
import com.sotark.play.R
import com.sotark.play.data.AppLanguage
import com.sotark.play.data.SecretTheme
import com.sotark.play.ui.theme.IsraelBlue
import com.sotark.play.ui.theme.UkrainianBlue
import com.sotark.play.ui.theme.UkrainianYellow
import com.sotark.play.viewmodel.DownloadViewModel
import com.sotark.play.viewmodel.SettingsViewModel

// Секретные темы: enum → (stringRes, цвет превью, эмодзи)
private data class ThemeEntry(
    val theme: SecretTheme,
    val nameRes: Int,
    val color: Color,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onDevTestClick: () -> Unit = {},
    viewModel: SettingsViewModel  = hiltViewModel(),
    downloadVm: DownloadViewModel = hiltViewModel()
) {
    val darkTheme           by viewModel.darkTheme.collectAsState()
    val language            by viewModel.language.collectAsState()
    val easterUnlocked      by viewModel.easterEggUnlocked.collectAsState()
    val easterShown         by viewModel.easterEggShown.collectAsState()
    val israelUnlocked      by viewModel.israelEasterEggUnlocked.collectAsState()
    val israelShown         by viewModel.israelEasterEggShown.collectAsState()
    val secretMenu          by viewModel.secretMenuUnlocked.collectAsState()
    val soundEnabled        by viewModel.soundEnabled.collectAsState()
    val hapticEnabled       by viewModel.hapticEnabled.collectAsState()
    val secretTheme         by viewModel.secretTheme.collectAsState()
    val toggleCount         by viewModel.darkThemeToggleCount.collectAsState()
    val ctx                 = LocalContext.current

    var canInstall by remember { mutableStateOf(downloadVm.canInstallUnknownSources()) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { canInstall = downloadVm.canInstallUnknownSources() }

    // Показываем пасхалки
    var showUkraineEgg by remember { mutableStateOf(false) }
    var showIsraelEgg  by remember { mutableStateOf(false) }
    LaunchedEffect(easterUnlocked) { if (easterUnlocked && !easterShown) showUkraineEgg = true }
    LaunchedEffect(israelUnlocked) { if (israelUnlocked && !israelShown) showIsraelEgg  = true }

    val cardColor    = MaterialTheme.colorScheme.surfaceVariant
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Все секретные темы для меню
    val themeEntries = buildList {
        if (easterUnlocked) add(ThemeEntry(SecretTheme.UKRAINIAN, R.string.ukrainian_theme, UkrainianYellow, "🇺🇦"))
        if (israelUnlocked)  add(ThemeEntry(SecretTheme.ISRAEL,   R.string.theme_israel,    IsraelBlue,      "🇮🇱"))
        add(ThemeEntry(SecretTheme.MATTE_METAL, R.string.theme_matte_metal, Color(0xFFB0BEC5), "🔩"))
        add(ThemeEntry(SecretTheme.NEON,        R.string.theme_neon,        Color(0xFF00FF88), "💚"))
        add(ThemeEntry(SecretTheme.ONYX,        R.string.theme_onyx,        Color(0xFFCFB980), "🏆"))
        add(ThemeEntry(SecretTheme.SUNSET,      R.string.theme_sunset,      Color(0xFFFF6B35), "🌅"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_title))
                        if (BuildConfig.IS_BETA) {
                            Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFFFFA000)) {
                                Text("BETA", style = MaterialTheme.typography.labelSmall,
                                    color = Color.White, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Версия ────────────────────────────────────────────────
            SRow(cardColor, Icons.Filled.Info, stringResource(R.string.version_label)) {
                Text(BuildConfig.VERSION_NAME, color = subTextColor,
                    style = MaterialTheme.typography.bodyMedium)
            }

            // ── Нет разрешения установки ──────────────────────────────
            if (!canInstall) {
                Card(shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            permLauncher.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${ctx.packageName}")))
                    }) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.no_install_permission), fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.tap_to_allow),
                                style = MaterialTheme.typography.bodySmall, color = subTextColor)
                        }
                        Icon(Icons.Filled.OpenInNew, null)
                    }
                }
            }

            // ── История ───────────────────────────────────────────────
            SRow(cardColor, Icons.Filled.History, stringResource(R.string.download_history),
                onClick = onHistoryClick) {
                Icon(Icons.Filled.ChevronRight, null, tint = subTextColor)
            }

            // ── Dev Test (бета) ───────────────────────────────────────
            if (BuildConfig.IS_BETA) {
                Card(shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                    onClick = onDevTestClick) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.BugReport, null, tint = Color(0xFFFFA000))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.dev_test_panel), color = Color.White,
                                fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.dev_test_subtitle),
                                style = MaterialTheme.typography.bodySmall, color = Color(0xFFB0B0CC))
                        }
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFFFFA000)) {
                            Text("BETA", style = MaterialTheme.typography.labelSmall,
                                color = Color.White, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            SLabel(stringResource(R.string.appearance))

            // ── Тёмная тема — счётчик 5 переключений ─────────────────
            Card(shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(stringResource(R.string.dark_theme))
                            AnimatedVisibility(visible = toggleCount in 1..4) {
                                Text("${5 - toggleCount} ${stringResource(R.string.presses_left)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Switch(checked = darkTheme, onCheckedChange = { viewModel.toggleDarkTheme() })
                }
            }

            // ── Секретное меню тем ────────────────────────────────────
            AnimatedVisibility(visible = secretMenu || easterUnlocked || israelUnlocked,
                enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SLabel(stringResource(R.string.secret_themes))
                    themeEntries.forEach { entry ->
                        val isActive = secretTheme == entry.theme
                        Card(shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) entry.color.copy(alpha = 0.15f)
                                                 else cardColor),
                            onClick = { viewModel.setSecretTheme(entry.theme) }) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Эмодзи флага
                                Text(entry.emoji, fontSize = 22.sp)
                                // Цветной кружок
                                Box(Modifier.size(20.dp).clip(CircleShape).background(entry.color))
                                Text(stringResource(entry.nameRes), Modifier.weight(1f),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                                if (isActive) {
                                    Icon(Icons.Filled.Check, null, tint = entry.color,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    if (secretTheme != SecretTheme.NONE) {
                        TextButton(onClick = { viewModel.setSecretTheme(SecretTheme.NONE) },
                            modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.reset_theme))
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            SLabel(stringResource(R.string.sound_haptic))

            SRow(cardColor, Icons.Filled.VolumeUp, stringResource(R.string.sound_on)) {
                Switch(checked = soundEnabled, onCheckedChange = { viewModel.toggleSound() })
            }
            SRow(cardColor, Icons.Filled.Vibration, stringResource(R.string.haptic_on)) {
                Switch(checked = hapticEnabled, onCheckedChange = { viewModel.toggleHaptic() })
            }

            Spacer(Modifier.height(2.dp))
            SLabel(stringResource(R.string.language))

            Card(shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.values().forEachIndexed { idx, lang ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Language, null,
                                    tint = if (language == lang) MaterialTheme.colorScheme.primary
                                           else subTextColor,
                                    modifier = Modifier.size(20.dp))
                                Text(lang.label, style = MaterialTheme.typography.bodyMedium)
                            }
                            RadioButton(selected = language == lang,
                                onClick = { viewModel.setLanguage(lang) })
                        }
                        if (idx < AppLanguage.values().size - 1)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Пасхалка Украина ──────────────────────────────────────────────────────
    if (showUkraineEgg) {
        EasterEggDialog(
            bgColor     = UkrainianBlue,
            accentColor = UkrainianYellow,
            imageRes    = R.drawable.easter_egg_founder,
            emoji       = "🇺🇦",
            titleRes    = R.string.easter_egg_title,
            descRes     = R.string.easter_egg_desc,
            subtitleRes = R.string.ukrainian_theme_unlocked,
            buttonRes   = R.string.cool_button,
            onDismiss   = { showUkraineEgg = false; viewModel.markEasterEggShown() }
        )
    }

    // ── Пасхалка Израиль ──────────────────────────────────────────────────────
    if (showIsraelEgg) {
        EasterEggDialog(
            bgColor     = IsraelBlue,
            accentColor = Color.White,
            imageRes    = R.drawable.easter_egg_israel,
            emoji       = "🇮🇱",
            titleRes    = R.string.easter_egg_israel_title,
            descRes     = R.string.easter_egg_israel_desc,
            subtitleRes = R.string.israel_theme_unlocked,
            buttonRes   = R.string.cool_button,
            onDismiss   = { showIsraelEgg = false; viewModel.markIsraelEasterEggShown() }
        )
    }
}

@Composable
private fun EasterEggDialog(
    bgColor: Color,
    accentColor: Color,
    imageRes: Int,
    emoji: String,
    titleRes: Int,
    descRes: Int,
    subtitleRes: Int,
    buttonRes: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = bgColor)) {
            Column(Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(titleRes), color = accentColor,
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                    textAlign = TextAlign.Center)
                // Фото в круге
                Box(Modifier.size(110.dp).clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model            = imageRes,
                        contentDescription = null,
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                Text(emoji, fontSize = 36.sp)
                Text(stringResource(descRes), color = accentColor,
                    fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                Text(stringResource(subtitleRes), color = accentColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Button(onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                    Text(stringResource(buttonRes), color = bgColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
}

@Composable
private fun SRow(
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Card(shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick ?: {}) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            trailing()
        }
    }
}
