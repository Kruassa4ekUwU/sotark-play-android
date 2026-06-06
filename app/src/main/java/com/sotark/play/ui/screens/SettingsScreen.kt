package com.sotark.play.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sotark.play.R
import com.sotark.play.data.AppLanguage
import com.sotark.play.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val darkTheme by viewModel.darkTheme.collectAsState()
    val language  by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Dark theme toggle ─────────────────────────────────────────
            Card(shape = MaterialTheme.shapes.medium) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.DarkMode, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.dark_theme),
                            style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked  = darkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Language selector ─────────────────────────────────────────
            Text(stringResource(R.string.language),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp))

            Card(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.values().forEachIndexed { idx, lang ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                Text(lang.label, style = MaterialTheme.typography.bodyLarge)
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
}
