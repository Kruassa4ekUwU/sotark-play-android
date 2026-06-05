package com.sotark.play.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.ui.components.*
import com.sotark.play.ui.theme.GreenPrimary
import com.sotark.play.viewmodel.AppDetailViewModel
import com.sotark.play.viewmodel.DownloadState
import com.sotark.play.viewmodel.DownloadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    appId: Int,
    onBack: () -> Unit,
    detailViewModel: AppDetailViewModel = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel()
) {
    val state    by detailViewModel.state.collectAsState()
    val dlState  by downloadViewModel.state.collectAsState()

    LaunchedEffect(appId) { detailViewModel.load(appId) }

    // Авто-запуск установки когда файл готов
    LaunchedEffect(dlState) {
        if (dlState is DownloadState.ReadyToInstall) {
            downloadViewModel.installApk((dlState as DownloadState.ReadyToInstall).file)
        }
    }

    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.app?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ошибка загрузки")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { detailViewModel.load(appId) }) { Text("Повторить") }
                }
            }
            state.app != null -> {
                val app = state.app!!
                LazyColumn(
                    Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // ── Header ───────────────────────────────────────────
                    item {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(url = app.iconUrl, size = 80)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.name, style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold)
                                Text(app.developer, color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(app.category,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // ── Stats ────────────────────────────────────────────
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("${app.rating}", "Рейтинг")
                            VerticalDivider(Modifier.height(40.dp))
                            StatItem("${app.downloads}", "Скачиваний")
                            VerticalDivider(Modifier.height(40.dp))
                            StatItem("${app.sizeMb} МБ", "Размер")
                            VerticalDivider(Modifier.height(40.dp))
                            StatItem("v${app.version}", "Версия")
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    }

                    // ── Download button ──────────────────────────────────
                    item {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            when (val dl = dlState) {
                                is DownloadState.Idle -> {
                                    Button(
                                        onClick = {
                                            app.apkUrl?.let {
                                                downloadViewModel.download(app.name, it)
                                            }
                                        },
                                        enabled  = app.apkUrl != null,
                                        shape    = RoundedCornerShape(10.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text(
                                            if (app.apkUrl != null) "⬇  Скачать и установить"
                                            else "APK недоступен",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                is DownloadState.Downloading -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Скачивается...", fontWeight = FontWeight.Medium)
                                            Text("${dl.progress}%",
                                                color = GreenPrimary,
                                                fontWeight = FontWeight.Bold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { dl.progress / 100f },
                                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = GreenPrimary
                                        )
                                        TextButton(
                                            onClick  = { downloadViewModel.reset() },
                                            modifier = Modifier.align(Alignment.End)
                                        ) { Text("Отмена") }
                                    }
                                }
                                is DownloadState.ReadyToInstall -> {
                                    Button(
                                        onClick  = { downloadViewModel.installApk(dl.file) },
                                        shape    = RoundedCornerShape(10.dp),
                                        colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) { Text("✓  Установить", fontWeight = FontWeight.Bold) }
                                }
                                is DownloadState.Error -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Ошибка: ${dl.message}",
                                            color = MaterialTheme.colorScheme.error)
                                        Button(
                                            onClick = {
                                                downloadViewModel.reset()
                                                app.apkUrl?.let {
                                                    downloadViewModel.download(app.name, it)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Повторить") }
                                    }
                                }
                            }
                        }
                    }

                    // ── Screenshots ──────────────────────────────────────
                    if (app.screenshots.isNotEmpty()) {
                        item {
                            SectionHeader("Скриншоты")
                            Row(
                                Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                app.screenshots.forEach { url ->
                                    AsyncImage(
                                        model  = url, contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.height(180.dp).width(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }

                    // ── Description ──────────────────────────────────────
                    if (app.description.isNotEmpty()) {
                        item {
                            SectionHeader("Описание")
                            Text(app.description,
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    // ── Reviews ──────────────────────────────────────────
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Отзывы (${state.reviews.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showReviewDialog = true }) { Text("Написать") }
                        }
                    }

                    if (state.reviews.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center) {
                                Text("Отзывов пока нет",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(state.reviews) { review ->
                            ReviewItem(review = review)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    // ── Review dialog ────────────────────────────────────────────────────────
    if (showReviewDialog) {
        var author by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(5) }
        var text   by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title   = { Text("Написать отзыв") },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = author, onValueChange = { author = it },
                        label = { Text("Ваше имя") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 },
                                modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Star, null,
                                    tint = if (i < rating) Color(0xFFFFC107) else Color.Gray)
                            }
                        }
                    }
                    OutlinedTextField(value = text, onValueChange = { text = it },
                        label = { Text("Комментарий") }, minLines = 3,
                        modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (author.isNotBlank()) {
                        detailViewModel.postReview(appId, author, rating, text)
                        showReviewDialog = false
                    }
                }, enabled = author.isNotBlank()) { Text("Отправить") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReviewItem(review: com.sotark.play.data.model.Review) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            Text(review.author, fontWeight = FontWeight.SemiBold)
            RatingStars(rating = review.rating.toFloat())
        }
        if (review.text.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(review.text, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text(review.createdAt.take(10), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
