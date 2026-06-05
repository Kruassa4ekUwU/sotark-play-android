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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.ui.theme.GreenPrimary
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

    // APK picker — любой файл
    val apkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onApkPicked(it) } }

    // Icon picker — только картинки
    val iconLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onIconPicked(it) } }

    // Success → уходим на главную
    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Опубликовать приложение") },
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
                Text("Иконка", style = MaterialTheme.typography.labelLarge,
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
                            model = state.iconUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AddPhotoAlternate, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp))
                            Text("Выбрать", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                // ── Основные поля ────────────────────────────────────────
                SotarkTextField(
                    value    = state.name,
                    onValueChange = viewModel::onName,
                    label    = "Название приложения *",
                    icon     = Icons.Filled.Apps
                )
                SotarkTextField(
                    value    = state.packageName,
                    onValueChange = viewModel::onPackage,
                    label    = "Package name * (com.example.app)",
                    icon     = Icons.Filled.Code
                )
                SotarkTextField(
                    value    = state.developer,
                    onValueChange = viewModel::onDeveloper,
                    label    = "Разработчик *",
                    icon     = Icons.Filled.Person
                )
                SotarkTextField(
                    value    = state.version,
                    onValueChange = viewModel::onVersion,
                    label    = "Версия",
                    icon     = Icons.Filled.NewReleases
                )
                SotarkTextField(
                    value    = state.description,
                    onValueChange = viewModel::onDescription,
                    label    = "Описание",
                    icon     = Icons.Filled.Description,
                    minLines = 3,
                    maxLines = 6
                )

                // ── Категория ────────────────────────────────────────────
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value         = state.category,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Категория") },
                        leadingIcon   = { Icon(Icons.Filled.Category, null) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat) },
                                onClick = { viewModel.onCategory(cat); expanded = false }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // ── APK файл ─────────────────────────────────────────────
                Text("APK файл", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { apkLauncher.launch("*/*") },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (state.apkUri != null) Icons.Filled.CheckCircle
                            else Icons.Filled.FileUpload,
                            null,
                            tint   = if (state.apkUri != null) GreenPrimary
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (state.apkUri != null) state.apkName
                                else "Нажми чтобы выбрать .apk",
                                fontWeight = if (state.apkUri != null) FontWeight.Medium
                                             else FontWeight.Normal
                            )
                            if (state.apkUri == null) {
                                Text("Из файлового менеджера",
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
                    onClick  = viewModel::publish,
                    enabled  = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color    = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Загружается…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Опубликовать", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ── Error snackbar ───────────────────────────────────────────
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) { Text("OK") }
                    }
                ) { Text(state.error!!) }
            }
        }
    }
}

// ── Переиспользуемый TextField ───────────────────────────────────────────────
@Composable
private fun SotarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        leadingIcon   = { Icon(icon, null) },
        minLines      = minLines,
        maxLines      = maxLines,
        shape         = RoundedCornerShape(12.dp),
        modifier      = Modifier.fillMaxWidth()
    )
}
