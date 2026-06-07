package com.sotark.play.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

                // ── Иконка приложения ────────────────────────────────────
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
                        AsyncImage(
                            model = state.iconUri, contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
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

                // ── Основные поля ────────────────────────────────────────
                SotarkTextField(
                    value = state.name, onValueChange = viewModel::onName,
                    label = stringResource(R.string.app_name_hint), icon = Icons.Filled.Apps
                )
                SotarkTextField(
                    value = state.packageName, onValueChange = viewModel::onPackage,
                    label = stringResource(R.string.package_hint), icon = Icons.Filled.Code
                )
                SotarkTextField(
                    value = state.developer, onValueChange = viewModel::onDeveloper,
                    label = stringResource(R.string.developer_hint), icon = Icons.Filled.Person
                )
                SotarkTextField(
                    value = state.version, onValueChange = viewModel::onVersion,
                    label = stringResource(R.string.version_hint), icon = Icons.Filled.NewReleases
                )
                SotarkTextField(
                    value = state.description, onValueChange = viewModel::onDescription,
                    label = stringResource(R.string.description_hint), icon = Icons.Filled.Description,
                    minLines = 3, maxLines = 6
                )

                // ── Категория ────────────────────────────────────────────
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.category, onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        leadingIcon = { Icon(Icons.Filled.Category, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { viewModel.onCategory(cat); catExpanded = false }
                            )
                        }
                    }
                }

                // ── Возрастной рейтинг ───────────────────────────────────
                var ageExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = ageExpanded,
                    onExpandedChange = { ageExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.ageRating, onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.age_rating)) },
                        leadingIcon = { Icon(Icons.Filled.Shield, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ageExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = ageExpanded, onDismissRequest = { ageExpanded = false }) {
                        AGE_RATINGS.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { viewModel.onAgeRating(key); ageExpanded = false }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // ── APK файл ─────────────────────────────────────────────
                Text(stringResource(R.string.apk_file),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { apkLauncher.launch("*/*") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (state.apkUri != null) Icons.Filled.CheckCircle else Icons.Filled.FileUpload,
                            null,
                            tint = if (state.apkUri != null) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (state.apkUri != null) state.apkName
                                else stringResource(R.string.pick_apk),
                                fontWeight = if (state.apkUri != null) FontWeight.Medium else FontWeight.Normal
                            )
                            if (state.apkUri == null) {
                                Text("*.apk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (state.apkUri != null) {
                            IconButton(onClick = { apkLauncher.launch("*/*") }) {
                                Icon(Icons.Filled.Edit, null)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Кнопка публикации ────────────────────────────────────
                Button(
                    onClick = viewModel::publish,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                        )
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

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
                ) { Text(state.error!!) }
            }
        }
    }
}

@Composable
private fun SotarkTextField(
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
