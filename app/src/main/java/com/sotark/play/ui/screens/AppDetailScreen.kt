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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sotark.play.R
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
    detailVm:   AppDetailViewModel  = hiltViewModel(),
    downloadVm: DownloadViewModel   = hiltViewModel(),
    historyVm:  HistoryViewModel    = hiltViewModel()
) {
    val state   by detailVm.state.collectAsState()
    val dlState by downloadVm.state.collectAsState()

    // Загружаем только один раз
    LaunchedEffect(appId) { detailVm.load(appId) }

    LaunchedEffect(dlState) {
        if (dlState is DownloadState.ReadyToInstall) {
            historyVm.load()
            downloadVm.installApk((dlState as DownloadState.ReadyToInstall).file)
        }
    }

    val pkg       = state.app?.`package` ?: ""
    val installed = if (pkg.isNotEmpty()) downloadVm.isInstalled(pkg) else false
    val canInstall = downloadVm.canInstallUnknownSources()

    var fullscreenImg        by remember { mutableStateOf<String?>(null) }
    var showReviewDialog     by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAgeWarning       by remember { mutableStateOf(false) }

    LaunchedEffect(state.app) {
        if (state.app?.getAgeRatingEnum() == AgeRating.EIGHTEEN) showAgeWarning = true
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
            onRefresh    = { detailVm.load(appId) },
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
                            Text(stringResource(R.string.error_loading))
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { detailVm.load(appId) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                state.app != null -> {
                    val app = state.app!!
                    LazyColumn(Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)) {

                        // ── Header ────────────────────────────────────────
                        item {
                            Row(Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(url = app.iconUrl, size = 80)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold)
                                    Text(app.developer,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                            Text(stringResource(R.string.installed),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // ── Статистика ────────────────────────────────────
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("${app.rating}", stringResource(R.string.rating))
                                VerticalDivider(Modifier.height(40.dp))
                                StatItem("${app.downloads}", stringResource(R.string.downloads))
                                VerticalDivider(Modifier.height(40.dp))
                                StatItem("${app.sizeMb} МБ", stringResource(R.string.size))
                                VerticalDivider(Modifier.height(40.dp))
                                StatItem("v${app.version}", stringResource(R.string.version))
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        }

                        // ── Кнопка скачать/установить ─────────────────────
                        item {
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                when (val dl = dlState) {
                                    is DownloadState.Idle -> Button(
                                        onClick = {
                                            if (!canInstall) showPermissionDialog = true
                                            else app.apkUrl?.let {
                                                downloadVm.download(app.name, it)
                                            }
                                        },
                                        enabled  = app.apkUrl != null,
                                        shape    = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text(when {
                                            app.apkUrl == null -> stringResource(R.string.no_apk)
                                            installed          -> stringResource(R.string.update)
                                            else               -> stringResource(R.string.install)
                                        }, fontWeight = FontWeight.Bold)
                                    }
                                    is DownloadState.Downloading -> Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.downloading),
                                                fontWeight = FontWeight.Medium)
                                            Text("${dl.progress}%",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { dl.progress / 100f },
                                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        TextButton(onClick = { downloadVm.reset() },
                                            modifier = Modifier.align(Alignment.End)) {
                                            Text(stringResource(R.string.cancel))
                                        }
                                    }
                                    is DownloadState.ReadyToInstall -> Button(
                                        onClick  = { downloadVm.installApk(dl.file) },
                                        shape    = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) { Text(stringResource(R.string.install_now),
                                        fontWeight = FontWeight.Bold) }
                                    is DownloadState.Error -> Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("${stringResource(R.string.error_loading)}: ${dl.message}",
                                            color = MaterialTheme.colorScheme.error)
                                        Button(onClick = {
                                            downloadVm.reset()
                                            app.apkUrl?.let { downloadVm.download(app.name, it) }
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Text(stringResource(R.string.retry))
                                        }
                                    }
                                }
                            }
                        }

                        // ── Галерея скриншотов ────────────────────────────
                        if (app.screenshots.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                SectionHeader(stringResource(R.string.screenshots))
                                Row(
                                    Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    app.screenshots.forEachIndexed { idx, url ->
                                        Box(
                                            modifier = Modifier
                                                .height(200.dp)
                                                .width(110.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { fullscreenImg = url }
                                        ) {
                                            AsyncImage(
                                                model = url, contentDescription = "Скриншот ${idx+1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Оверлей что кликабельно
                                            Box(
                                                Modifier.fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.05f))
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        // ── Описание ──────────────────────────────────────
                        if (app.description.isNotEmpty()) {
                            item {
                                SectionHeader(stringResource(R.string.description))
                                Text(app.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                        // ── Отзывы ────────────────────────────────────────
                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${stringResource(R.string.reviews)} (${state.reviews.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                TextButton(onClick = { showReviewDialog = true }) {
                                    Text(stringResource(R.string.write_review))
                                }
                            }
                        }
                        if (state.reviews.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_reviews),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(state.reviews, key = { it.id }) { review ->
                                ReviewItem(review)
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Fullscreen скриншот ───────────────────────────────────────────────────
    fullscreenImg?.let { imgUrl ->
        Dialog(onDismissRequest = { fullscreenImg = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imgUrl, contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
                IconButton(
                    onClick = { fullscreenImg = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Filled.Close, null, tint = Color.White,
                        modifier = Modifier.size(32.dp))
                }
            }
        }
    }

    // ── Предупреждение 18+ ────────────────────────────────────────────────────
    if (showAgeWarning) {
        AlertDialog(
            onDismissRequest = { showAgeWarning = false; onBack() },
            title = { Text("18+ контент") },
            text  = { Text("Это приложение содержит контент только для взрослых. Вам есть 18 лет?") },
            confirmButton = {
                Button(onClick = { showAgeWarning = false }) { Text("Да, мне 18+") }
            },
            dismissButton = {
                TextButton(onClick = { showAgeWarning = false; onBack() }) { Text("Нет, назад") }
            }
        )
    }

    // ── Разрешение установки ──────────────────────────────────────────────────
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.permission_needed)) },
            text  = { Text(stringResource(R.string.permission_text)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    downloadVm.openInstallPermissionSettings()
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // ── Диалог отзыва ─────────────────────────────────────────────────────────
    if (showReviewDialog) {
        var author by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(5) }
        var text   by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text(stringResource(R.string.write_review_title)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = author, onValueChange = { author = it },
                        label = { Text(stringResource(R.string.review_name_hint)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 },
                                modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Star, null,
                                    tint = if (i < rating) Color(0xFFFFC107)
                                           else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = text, onValueChange = { text = it },
                        label = { Text(stringResource(R.string.what_to_tell)) },
                        minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (author.isNotBlank()) {
                        detailVm.postReview(appId, author, rating, text)
                        showReviewDialog = false
                    }
                }, enabled = author.isNotBlank()) {
                    Text(stringResource(R.string.send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
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
        Text(rating.label, color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
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
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
