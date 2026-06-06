package com.sotark.play.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
    viewModel: SettingsViewModel = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel()
) {
    val darkTheme       by viewModel.darkTheme.collectAsState()
    val language        by viewModel.language.collectAsState()
    val ukrainianTheme  by viewModel.ukrainianTheme.collectAsState()
    val easterUnlocked  by viewModel.easterEggUnlocked.collectAsState()
    val easterShown     by viewModel.easterEggShown.collectAsState()
    val ctx             = LocalContext.current

    // Bug fix 1: показываем пасхалку только если разблокирована И ещё не показывалась
    var showEasterEgg by remember { mutableStateOf(false) }
    LaunchedEffect(easterUnlocked) {
        if (easterUnlocked && !easterShown) {
            showEasterEgg = true
        }
    }

    // Bug fix 2: перепроверяем разрешение после возврата из настроек без перезапуска
    var canInstall by remember { mutableStateOf(downloadViewModel.canInstallUnknownSources()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Перепроверяем сразу после возврата
        canInstall = downloadViewModel.canInstallUnknownSources()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.settings))
                        if (BuildConfig.IS_BETA) {
                            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFFFA000)) {
                                Text("BETA",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Версия
            Card(shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Версия", style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(BuildConfig.VERSION_NAME,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Разрешение на установку — показываем если нет
            if (!canInstall) {
                Card(
                    shape  = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Warning, null,
                                tint = MaterialTheme.colorScheme.error)
                            Column {
                                Text("Нет разрешения на установку",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium)
                                Text("Нажми чтобы разрешить",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${ctx.packageName}")
                                )
                                permissionLauncher.launch(intent)
                            }
                        }) {
                            Icon(Icons.Filled.OpenInNew, null)
                        }
                    }
                }
            }

            // История
            Card(shape = MaterialTheme.shapes.medium, onClick = onHistoryClick) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary)
                        Text("История загрузок", style = MaterialTheme.typography.bodyLarge)
                    }
                    Icon(Icons.Filled.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Dark theme
            Card(shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.dark_theme),
                            style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(checked = darkTheme, onCheckedChange = { viewModel.toggleDarkTheme() })
                }
            }

            // Украинская тема (только если разблокирована)
            AnimatedVisibility(visible = easterUnlocked) {
                Card(
                    shape  = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (ukrainianTheme) UkrainianBlue
                                         else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Украинская тема", fontSize = 16.sp)
                            Text("Секретно!",
                                style = MaterialTheme.typography.labelSmall,
                                color = UkrainianYellow)
                        }
                        Switch(
                            checked = ukrainianTheme,
                            onCheckedChange = { viewModel.toggleUkrainianTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = UkrainianYellow,
                                checkedTrackColor = UkrainianBlue
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Language
            Text(
                stringResource(R.string.language),
                style    = MaterialTheme.typography.titleSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.values().forEachIndexed { idx, lang ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Filled.Language, null,
                                    tint = if (language == lang)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                                Column {
                                    Text(lang.label, style = MaterialTheme.typography.bodyLarge)
                                    if (lang == AppLanguage.UKRAINIAN && !easterUnlocked) {
                                        Text("Нажми 10 раз...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            RadioButton(
                                selected = language == lang,
                                onClick  = { viewModel.setLanguage(lang) }
                            )
                        }
                        if (idx < AppLanguage.values().size - 1) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    // Пасхалка — показывается только 1 раз
    if (showEasterEgg) {
        Dialog(onDismissRequest = {
            showEasterEgg = false
            viewModel.markEasterEggShown()
        }) {
            Card(
                shape  = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = UkrainianBlue)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("СЕКРЕТНАЯ ПАСХАЛКА!",
                        color      = UkrainianYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp)
                    AsyncImage(
                        model              = R.drawable.easter_egg_founder,
                        contentDescription = "Основатель",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.size(120.dp).clip(CircleShape)
                    )
                    Text(
                        text       = "Основатель Sotark / Вест-Индийское IT",
                        color      = UkrainianYellow,
                        textAlign  = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Text("Украинская тема разблокирована!",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            showEasterEgg = false
                            viewModel.markEasterEggShown()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UkrainianYellow)
                    ) {
                        Text("Круто!", color = UkrainianBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
