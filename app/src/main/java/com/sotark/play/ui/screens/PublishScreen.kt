package com.sotark.play.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.R
import com.sotark.play.viewmodel.AGE_RATINGS
import com.sotark.play.viewmodel.CATEGORIES
import com.sotark.play.viewmodel.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val apkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onApkPicked(it) } }

    val iconLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onIconPicked(it) } }

    // Множественный выбор скриншотов
    val screenshotLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.onScreenshotsPicked(uris) }

    LaunchedEffect(state.success) {
        if (state.success) { viewModel.resetSuccess(); onSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.publish_app)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Иконка ────────────────────────────────────────────────
                Text(stringResource(R.string.icon_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { iconLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.iconUri != null) {
                        AsyncImage(model = state.iconUri, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddPhotoAlternate, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp))
                            Text(stringResource(R.string.pick_icon),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                // ── Поля ──────────────────────────────────────────────────
                SotarkField(state.name,        viewModel::onName,
                    stringResource(R.string.app_name_hint),   Icons.Filled.Apps)
                SotarkField(state.packageName, viewModel::onPackage,
                    stringResource(R.string.package_hint),    Icons.Filled.Code)
                SotarkField(state.developer,   viewModel::onDeveloper,
                    stringResource(R.string.developer_hint),  Icons.Filled.Person)
                SotarkField(state.version,     viewModel::onVersion,
                    stringResource(R.string.version_hint),    Icons.Filled.NewReleases)
                SotarkField(state.description, viewModel::onDescription,
                    stringResource(R.string.description_hint),Icons.Filled.Description,
                    minLines = 3, maxLines = 6)

                // ── Категория ─────────────────────────────────────────────
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(value = state.category, onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        leadingIcon = { Icon(Icons.Filled.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) },
                                onClick = { viewModel.onCategory(cat); catExpanded = false })
                        }
                    }
                }

                // ── Возрастной рейтинг ────────────────────────────────────
                var ageExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = ageExpanded,
                    onExpandedChange = { ageExpanded = it }) {
                    OutlinedTextField(
                        value = AGE_RATINGS.find { it.first == state.ageRating }?.second ?: "0+",
                        onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.age_rating)) },
                        leadingIcon = { Icon(Icons.Filled.Shield, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ageExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = ageExpanded,
                        onDismissRequest = { ageExpanded = false }) {
                        AGE_RATINGS.forEach { (key, label) ->
                            DropdownMenuItem(text = { Text(label) },
                                onClick = { viewModel.onAgeRating(key); ageExpanded = false })
                        }
                    }
                }

                HorizontalDivider()

                // ── Скриншоты (до 5) ──────────────────────────────────────
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.screenshots),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.screenshotUris.size < 5) {
                        TextButton(onClick = { screenshotLauncher.launch("image/*") }) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${state.screenshotUris.size}/5")
                        }
                    } else {
                        Text("5/5 (макс)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (state.screenshotUris.isNotEmpty()) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.screenshotUris.forEachIndexed { idx, uri ->
                            Box(Modifier.size(width = 90.dp, height = 160.dp)) {
                                AsyncImage(model = uri, contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)))
                                // Кнопка удаления
                                IconButton(
                                    onClick = { viewModel.removeScreenshot(idx) },
                                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Filled.Close, null, tint = Color.White,
                                        modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        // Добавить ещё
                        if (state.screenshotUris.size < 5) {
                            Box(
                                Modifier.size(width = 90.dp, height = 160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp))
                                    .clickable { screenshotLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()

                // ── APK файл ──────────────────────────────────────────────
                Text(stringResource(R.string.apk_file),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { apkLauncher.launch("*/*") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            if (state.apkUri != null) Icons.Filled.CheckCircle
                            else Icons.Filled.FileUpload,
                            null,
                            tint = if (state.apkUri != null) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (state.apkUri != null) state.apkName
                                else stringResource(R.string.pick_apk),
                                fontWeight = if (state.apkUri != null) FontWeight.Medium
                                             else FontWeight.Normal
                            )
                            if (state.apkUri == null) {
                                Text("*.apk", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Кнопка публикации ─────────────────────────────────────
                Button(
                    onClick  = viewModel::publish,
                    enabled  = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.uploading), fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.publish), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Snackbar ошибки
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action   = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
                ) { Text(state.error!!) }
            }
        }
    }
}

@Composable
private fun SotarkField(
    value: String, onValueChange: (String) -> Unit,
    label: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1, maxLines: Int = 1
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        minLines = minLines, maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
