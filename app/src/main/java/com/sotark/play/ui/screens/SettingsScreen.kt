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
import com.sotark.play.ui.theme.UkrainianBlue
import com.sotark.play.ui.theme.UkrainianYellow
import com.sotark.play.viewmodel.DownloadViewModel
import com.sotark.play.viewmodel.SettingsViewModel

// Данные секретных тем: id → (название-ключ, цвет превью)
private val SECRET_THEMES = listOf(
    SecretTheme.MATTE_METAL to (R.string.theme_matte_metal to Color(0xFFB0BEC5)),
    SecretTheme.NEON        to (R.string.theme_neon        to Color(0xFF00FF88)),
    SecretTheme.ONYX        to (R.string.theme_onyx        to Color(0xFFCFB980)),
    SecretTheme.SUNSET      to (R.string.theme_sunset      to Color(0xFFFF6B35)),
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
    val darkTheme        by viewModel.darkTheme.collectAsState()
    val language         by viewModel.language.collectAsState()
    val ukrainianTheme   by viewModel.ukrainianTheme.collectAsState()
    val easterUnlocked   by viewModel.easterEggUnlocked.collectAsState()
    val easterShown      by viewModel.easterEggShown.collectAsState()
    val secretMenu       by viewModel.secretMenuUnlocked.collectAsState()
    val soundEnabled     by viewModel.soundEnabled.collectAsState()
    val hapticEnabled    by viewModel.hapticEnabled.collectAsState()
    val secretTheme      by viewModel.secretTheme.collectAsState()
    val toggleCount      by viewModel.darkThemeToggleCount.collectAsState()
    val ctx              = LocalContext.current

    var showEasterEgg by remember { mutableStateOf(false) }
    LaunchedEffect(easterUnlocked) {
        if (easterUnlocked && !easterShown) showEasterEgg = true
    }

    var canInstall by remember { mutableStateOf(downloadVm.canInstallUnknownSources()) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { canInstall = downloadVm.canInstallUnknownSources() }

    val isUk = ukrainianTheme || secretTheme == SecretTheme.NONE
    val cardColor    = MaterialTheme.colorScheme.surfaceVariant
    val textColor    = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            // Компактный TopAppBar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_title))
                        if (BuildConfig.IS_BETA) {
                            Surface(shape = MaterialTheme.shapes.extraSmall,
                                color = Color(0xFFFFA000)) {
                                Text("BETA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                // Компактная высота
                windowInsets = TopAppBarDefaults.windowInsets
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
            SettingsRow(cardColor, icon = Icons.Filled.Info,
                label = stringResource(R.string.version_label)) {
                Text(BuildConfig.VERSION_NAME, color = subTextColor,
                    style = MaterialTheme.typography.bodyMedium)
            }

            // ── Нет разрешения на установку ───────────────────────────
            if (!canInstall) {
                Card(shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            permLauncher.launch(Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${ctx.packageName}")
                            ))
                        }
                    }
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Warning, null,
                            tint = MaterialTheme.colorScheme.error)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.no_install_permission),
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.tap_to_allow),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.OpenInNew, null)
                    }
                }
            }

            // ── История ───────────────────────────────────────────────
            SettingsRow(cardColor, icon = Icons.Filled.History,
                label = stringResource(R.string.download_history), onClick = onHistoryClick) {
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
                            Text(stringResource(R.string.dev_test_panel),
                                color = Color.White, fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.dev_test_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0B0CC))
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
                        Icon(Icons.Filled.DarkMode, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(stringResource(R.string.dark_theme))
                            // Подсказка о секрете (показывается когда начали нажимать)
                            AnimatedVisibility(visible = toggleCount in 1..4) {
                                Text("${5 - toggleCount} ${stringResource(R.string.presses_left)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Switch(checked = darkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() })
                }
            }

            // ── Украинская тема (пасхалка) ────────────────────────────
            AnimatedVisibility(visible = easterUnlocked) {
                Card(shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor =
                        if (ukrainianTheme) Color(0xFF002D70) else cardColor)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🇺🇦", fontSize = 20.sp)
                            Column {
                                Text(stringResource(R.string.ukrainian_theme),
                                    color = UkrainianYellow, fontWeight = FontWeight.Medium)
                                Text(stringResource(R.string.secret_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = UkrainianYellow.copy(alpha = 0.7f))
                            }
                        }
                        Switch(checked = ukrainianTheme,
                            onCheckedChange = { viewModel.toggleUkrainianTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = UkrainianYellow,
                                checkedTrackColor   = Color(0xFF001A50)))
                    }
                }
            }

            // ── Секретное меню тем ────────────────────────────────────
            AnimatedVisibility(
                visible = secretMenu,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SLabel(stringResource(R.string.secret_themes))
                    SECRET_THEMES.forEach { (theme, nameAndColor) ->
                        val (nameRes, previewColor) = nameAndColor
                        val isActive = secretTheme == theme
                        Card(
                            shape  = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) previewColor.copy(alpha = 0.2f)
                                                 else cardColor),
                            border = if (isActive) CardDefaults.outlinedCardBorder()
                                     else null,
                            onClick = { viewModel.setSecretTheme(theme) }
                        ) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Цветовой кружок-превью
                                Box(Modifier.size(28.dp)
                                    .clip(CircleShape)
                                    .background(previewColor))
                                Text(stringResource(nameRes),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                                if (isActive) {
                                    Icon(Icons.Filled.Check, null,
                                        tint = previewColor,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    // Сброс
                    if (secretTheme != SecretTheme.NONE) {
                        TextButton(
                            onClick = { viewModel.setSecretTheme(SecretTheme.NONE) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.reset_theme))
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))
            SLabel(stringResource(R.string.sound_haptic))

            SettingsRow(cardColor, icon = Icons.Filled.VolumeUp,
                label = stringResource(R.string.sound_on)) {
                Switch(checked = soundEnabled, onCheckedChange = { viewModel.toggleSound() })
            }
            SettingsRow(cardColor, icon = Icons.Filled.Vibration,
                label = stringResource(R.string.haptic_on)) {
                Switch(checked = hapticEnabled, onCheckedChange = { viewModel.toggleHaptic() })
            }

            Spacer(Modifier.height(2.dp))
            SLabel(stringResource(R.string.language))

            Card(shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.values().forEachIndexed { idx, lang ->
                        Row(Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Language, null,
                                    tint = if (language == lang)
                                        MaterialTheme.colorScheme.primary
                                    else subTextColor,
                                    modifier = Modifier.size(20.dp))
                                Text(lang.label,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            RadioButton(
                                selected = language == lang,
                                onClick  = { viewModel.setLanguage(lang) }
                            )
                        }
                        if (idx < AppLanguage.values().size - 1)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Пасхалка ─────────────────────────────────────────────────
    if (showEasterEgg) {
        Dialog(onDismissRequest = { showEasterEgg = false; viewModel.markEasterEggShown() }) {
            Card(shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = UkrainianBlue)) {
                Column(Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.easter_egg_title),
                        color = UkrainianYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Box(Modifier.size(100.dp).clip(CircleShape)
                            .background(UkrainianYellow.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text("🇺🇦", fontSize = 48.sp)
                    }
                    Text(stringResource(R.string.easter_egg_desc),
                        color = UkrainianYellow, textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium)
                    Text(stringResource(R.string.ukrainian_theme_unlocked),
                        color = Color.White, style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center)
                    Button(
                        onClick = { showEasterEgg = false; viewModel.markEasterEggShown() },
                        colors  = ButtonDefaults.buttonColors(containerColor = UkrainianYellow)
                    ) { Text(stringResource(R.string.cool_button),
                        color = UkrainianBlue, fontWeight = FontWeight.Bold) }
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
private fun SettingsRow(
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val mod = if (onClick != null)
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick)
    else Modifier.fillMaxWidth()
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
