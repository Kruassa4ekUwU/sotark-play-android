package com.sotark.play.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import com.sotark.play.ui.theme.UkrainianBlue
import com.sotark.play.ui.theme.UkrainianYellow
import com.sotark.play.viewmodel.DownloadViewModel
import com.sotark.play.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: SettingsViewModel   = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel()
) {
    val darkTheme        by viewModel.darkTheme.collectAsState()
    val language         by viewModel.language.collectAsState()
    val ukrainianTheme   by viewModel.ukrainianTheme.collectAsState()
    val easterUnlocked   by viewModel.easterEggUnlocked.collectAsState()
    val easterShown      by viewModel.easterEggShown.collectAsState()
    val secretMenu       by viewModel.secretMenuUnlocked.collectAsState()
    val soundEnabled     by viewModel.soundEnabled.collectAsState()
    val hapticEnabled    by viewModel.hapticEnabled.collectAsState()
    val ctx              = LocalContext.current

    var showEasterEgg    by remember { mutableStateOf(false) }

    // Пасхалка — только 1 раз
    LaunchedEffect(easterUnlocked) {
        if (easterUnlocked && !easterShown) showEasterEgg = true
    }

    // Разрешение без перезапуска
    var canInstall by remember { mutableStateOf(downloadViewModel.canInstallUnknownSources()) }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { canInstall = downloadViewModel.canInstallUnknownSources() }

    // Цвета для украинской темы — всё контрастно
    val cardColor    = if (ukrainianTheme) Color(0xFF003D85) else MaterialTheme.colorScheme.surfaceVariant
    val textColor    = if (ukrainianTheme) UkrainianYellow   else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (ukrainianTheme) Color(0xFFFFEB80)  else MaterialTheme.colorScheme.onSurfaceVariant
    val switchColors = if (ukrainianTheme) SwitchDefaults.colors(
        checkedThumbColor   = UkrainianYellow,
        checkedTrackColor   = Color(0xFF002D70),
        uncheckedThumbColor = Color(0xFFFFEB80),
        uncheckedTrackColor = Color(0xFF004A9E)
    ) else SwitchDefaults.colors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings), color = textColor)
                        if (BuildConfig.IS_BETA) {
                            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFFFA000)) {
                                Text("BETA", style = MaterialTheme.typography.labelSmall,
                                    color = Color.White, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textColor)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Версия
            SettingsCard(bgColor = cardColor) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Версия", color = textColor)
                    }
                    Text(BuildConfig.VERSION_NAME, color = subTextColor)
                }
            }

            // Разрешение — только если нет
            if (!canInstall) {
                SettingsCard(bgColor = MaterialTheme.colorScheme.errorContainer) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Column {
                                Text("Нет разрешения на установку", fontWeight = FontWeight.Medium)
                                Text("Нажми чтобы разрешить",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                permLauncher.launch(Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${ctx.packageName}")
                                ))
                            }
                        }) { Icon(Icons.Filled.OpenInNew, null) }
                    }
                }
            }

            // История
            SettingsCard(bgColor = cardColor, onClick = onHistoryClick) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary)
                        Text("История загрузок", color = textColor)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = subTextColor)
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("Внешний вид", subTextColor)

            // Тёмная тема — 5 тапов = секретное меню (подсказка скрыта)
            SettingsCard(bgColor = cardColor) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.dark_theme), color = textColor)
                    }
                    Switch(checked = darkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() },
                        colors = switchColors)
                }
            }

            // Украинская тема
            AnimatedVisibility(visible = easterUnlocked) {
                SettingsCard(bgColor = if (ukrainianTheme) Color(0xFF002D70) else cardColor) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Украинская тема", color = UkrainianYellow, fontSize = 16.sp)
                            Text("Секретно!", style = MaterialTheme.typography.labelSmall,
                                color = UkrainianYellow.copy(alpha = 0.7f))
                        }
                        Switch(checked = ukrainianTheme,
                            onCheckedChange = { viewModel.toggleUkrainianTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = UkrainianYellow,
                                checkedTrackColor   = Color(0xFF001A50),
                                uncheckedThumbColor = Color(0xFFFFEB80),
                                uncheckedTrackColor = Color(0xFF003580)
                            ))
                    }
                }
            }

            // Секретное меню тем
            AnimatedVisibility(visible = secretMenu) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Секретное меню тем", subTextColor)
                    listOf("Матовый металл", "Неон", "Оникс", "Закат").forEach { theme ->
                        SettingsCard(bgColor = cardColor, onClick = {}) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(theme, color = textColor)
                                Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("Звук и вибрация", subTextColor)

            SettingsCard(bgColor = cardColor) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Звуки", color = textColor)
                    }
                    Switch(checked = soundEnabled, onCheckedChange = { viewModel.toggleSound() }, colors = switchColors)
                }
            }

            SettingsCard(bgColor = cardColor) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Vibration, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Виброотклик", color = textColor)
                    }
                    Switch(checked = hapticEnabled, onCheckedChange = { viewModel.toggleHaptic() }, colors = switchColors)
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel(stringResource(R.string.language), subTextColor)

            // Язык — подсказки для секреток скрыты
            Card(shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = cardColor)) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.values().forEachIndexed { idx, lang ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Language, null,
                                    tint = if (language == lang) MaterialTheme.colorScheme.primary
                                           else subTextColor)
                                Text(lang.label, color = textColor,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                            RadioButton(selected = language == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor   = if (ukrainianTheme) UkrainianYellow
                                                      else MaterialTheme.colorScheme.primary,
                                    unselectedColor = subTextColor
                                ))
                        }
                        if (idx < AppLanguage.values().size - 1) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                color = subTextColor.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }

    // Пасхалка — 1 раз
    if (showEasterEgg) {
        Dialog(onDismissRequest = { showEasterEgg = false; viewModel.markEasterEggShown() }) {
            Card(shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = UkrainianBlue)) {
                Column(Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("СЕКРЕТНАЯ ПАСХАЛКА!", color = UkrainianYellow,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    AsyncImage(model = R.drawable.easter_egg_founder,
                        contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(120.dp).clip(CircleShape))
                    Text(text = "Основатель Sotark / Вест-Индийское IT",
                        color = UkrainianYellow, textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium)
                    Text("Украинская тема разблокирована!",
                        color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { showEasterEgg = false; viewModel.markEasterEggShown() },
                        colors = ButtonDefaults.buttonColors(containerColor = UkrainianYellow)) {
                        Text("Круто!", color = UkrainianBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(text, style = MaterialTheme.typography.labelMedium,
        color = color, modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun SettingsCard(
    bgColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(shape = MaterialTheme.shapes.medium, onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = bgColor)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { content() }
        }
    } else {
        Card(shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = bgColor)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { content() }
        }
    }
}
