package com.sotark.play.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.data.model.AgeRating
import com.sotark.play.data.model.Review
import com.sotark.play.ui.components.*
import com.sotark.play.viewmodel.AppDetailViewModel
import com.sotark.play.viewmodel.DownloadState
import com.sotark.play.viewmodel.DownloadViewModel
import com.sotark.play.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    appId: Int,
    onBack: () -> Unit,
    detailViewModel: AppDetailViewModel  = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel   = hiltViewModel()
) {
    val state   by detailViewModel.state.collectAsState()
    val dlState by downloadViewModel.state.collectAsState()
    var fullscreenImg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appId) { detailViewModel.load(appId) }

    LaunchedEffect(dlState) {
        if (dlState is DownloadState.ReadyToInstall) {
            state.app?.let { historyViewModel.load() }
            downloadViewModel.installApk((dlState as DownloadState.ReadyToInstall).file)
        }
    }

    val pkg        = state.app?.`package` ?: ""
    val installed  = remember(pkg) { if (pkg.isNotEmpty()) downloadViewModel.isInstalled(pkg) else false }
    val canInstall = remember { downloadViewModel.canInstallUnknownSources() }

    var showReviewDialog     by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAgeWarning       by remember { mutableStateOf(false) }

    LaunchedEffect(state.app) {
        state.app?.let {
            if (it.getAgeRatingEnum() == AgeRating.EIGHTEEN) showAgeWarning = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.app?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh    = { detailViewModel.load(appId) },
            modifier     = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.isLoading && state.app == null ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                state.error != null ->
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ошибка загрузки")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { detailViewModel.load(appId) }) { Text("Повторить") }
                        }
                    }
                state.app != null -> {
                    val app = state.app!!
                    LazyColumn(Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)) {

                        // ── Header ────────────────────────────────────────
                        item {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(url = app.iconUrl, size = 80)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.name, style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold)
                                    Text(app.developer, color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(app.category,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall)
                                        AgeRatingBadge(app.getAgeRatingEnum())
                                    }
                                    if (installed) {
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.CheckCircle, null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Установлено",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // ── Stats ─────────────────────────────────────────
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly) {
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

                        // ── Download button ───────────────────────────────
                        item {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                when (val dl = dlState) {
                                    is DownloadState.Idle -> {
                                        Button(
                                            onClick = {
                                                if (!canInstall) showPermissionDialog = true
                                                else app.apkUrl?.let {
                                                    downloadViewModel.download(app.name, it)
                                                }
                                            },
                                            enabled  = app.apkUrl != null,
                                            shape    = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text(
                                                when {
                                                    app.apkUrl == null -> "APK недоступен"
                                                    installed          -> "Обновить"
                                                    else               -> "Скачать и установить"
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    is DownloadState.Downloading -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Text("Скачивается...", fontWeight = FontWeight.Medium)
                                                Text("${dl.progress}%",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold)
                                            }
                                            LinearProgressIndicator(
                                                progress = { dl.progress / 100f },
                                                modifier = Modifier.fillMaxWidth().height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                            TextButton(onClick = { downloadViewModel.reset() },
                                                modifier = Modifier.align(Alignment.End)) {
                                                Text("Отмена")
                                            }
                                        }
                                    }
                                    is DownloadState.ReadyToInstall -> {
                                        Button(
                                            onClick  = { downloadViewModel.installApk(dl.file) },
                                            shape    = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) { Text("Установить", fontWeight = FontWeight.Bold) }
                                    }
                                    is DownloadState.Error -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Ошибка: ${dl.message}",
                                                color = MaterialTheme.colorScheme.error)
                                            Button(onClick = {
                                                downloadViewModel.reset()
                                                app.apkUrl?.let { downloadViewModel.download(app.name, it) }
                                            }, modifier = Modifier.fillMaxWidth()) { Text("Повторить") }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Screenshots ───────────────────────────────────
                        if (app.screenshots.isNotEmpty()) {
                            item {
                                SectionHeader("Скриншоты")
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    app.screenshots.forEach { url ->
                                        AsyncImage(
                                            model = url, contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.height(200.dp).width(110.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { fullscreenImg = url }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        // ── Description ───────────────────────────────────
                        if (app.description.isNotEmpty()) {
                            item {
                                SectionHeader("Описание")
                                Text(app.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                        // ── Reviews ───────────────────────────────────────
                        item {
                            Row(Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
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
                                ReviewItem(review)
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Fullscreen screenshot ─────────────────────────────────────────────────
    fullscreenImg?.let { imgUrl ->
        Dialog(onDismissRequest = { fullscreenImg = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))
                    .clickable { fullscreenImg = null },
                contentAlignment = Alignment.Center) {
                AsyncImage(model = imgUrl, contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    }

    // ── 18+ предупреждение ────────────────────────────────────────────────────
    if (showAgeWarning) {
        AlertDialog(
            onDismissRequest = { showAgeWarning = false; onBack() },
            title   = { Text("18+ контент") },
            text    = { Text("Это приложение содержит контент только для взрослых. Вам исполнилось 18 лет?") },
            confirmButton = {
                Button(onClick = { showAgeWarning = false }) { Text("Да, мне 18+") }
            },
            dismissButton = {
                TextButton(onClick = { showAgeWarning = false; onBack() }) { Text("Нет, назад") }
            }
        )
    }

    // ── Разрешение на установку ───────────────────────────────────────────────
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title   = { Text("Нужно разрешение") },
            text    = { Text("Разреши Sotark Play устанавливать приложения из неизвестных источников в настройках.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    downloadViewModel.openInstallPermissionSettings()
                }) { Text("Открыть настройки") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Отмена") }
            }
        )
    }

    // ── Review dialog — новый с подзаголовком "что хотите рассказать?" ────────
    if (showReviewDialog) {
        var author by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(5) }
        var text   by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Написать отзыв") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = author, onValueChange = { author = it },
                        label = { Text("Ваше имя") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    // Звёздочки рейтинга
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 },
                                modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Filled.Star, null,
                                    tint = if (i < rating) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = text, onValueChange = { text = it },
                        label   = { Text("Что хотите рассказать?") },
                        minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick  = {
                        if (author.isNotBlank()) {
                            detailViewModel.postReview(appId, author, rating, text)
                            showReviewDialog = false
                        }
                    },
                    enabled = author.isNotBlank()
                ) { Text("Отправить") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun AgeRatingBadge(rating: AgeRating) {
    val color = when (rating) {
        AgeRating.ALL      -> Color(0xFF4CAF50)
        AgeRating.SIX      -> Color(0xFF8BC34A)
        AgeRating.TWELVE   -> Color(0xFFFFC107)
        AgeRating.SIXTEEN  -> Color(0xFFFF9800)
        AgeRating.EIGHTEEN -> Color(0xFFF44336)
    }
    Surface(shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color), color = color.copy(alpha = 0.15f)) {
        Text(text = rating.label, color = color,
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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
private fun ReviewItem(review: Review) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
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
