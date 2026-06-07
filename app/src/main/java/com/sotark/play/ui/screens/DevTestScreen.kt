package com.sotark.play.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sotark.play.viewmodel.DevTestState
import com.sotark.play.viewmodel.DevTestViewModel
import com.sotark.play.viewmodel.TestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevTestScreen(
    onBack: () -> Unit,
    viewModel: DevTestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dev Test Panel")
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFFFFA000)
                        ) {
                            Text("BETA ONLY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::resetAll) {
                        Icon(Icons.Filled.Refresh, null)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Описание ──────────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BugReport, null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        "Эта панель доступна только в бете. " +
                        "Сервер и стабильная версия не затрагиваются.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // ── Тест 1: Сервер ────────────────────────────────────────────
            TestCard(
                icon    = Icons.Filled.Cloud,
                title   = "Отклик сервера",
                subtitle = if (state.serverStatus == TestStatus.OK)
                    state.serverDetail
                else "Railway · sotark-play-server",
                status  = state.serverStatus,
                buttonLabel = "Пинг",
                onRun   = viewModel::testServer
            )

            // ── Тест 2: Вибро ─────────────────────────────────────────────
            TestCard(
                icon    = Icons.Filled.Vibration,
                title   = "Виброотклик",
                subtitle = "click → success → error паттерн",
                status  = state.hapticStatus,
                buttonLabel = "Тест вибро",
                onRun   = viewModel::testHaptic
            )

            // ── Тест 3: Аудио ─────────────────────────────────────────────
            TestCard(
                icon    = Icons.Filled.VolumeUp,
                title   = "Аудио",
                subtitle = if (state.audioStatus == TestStatus.RUNNING && state.audioPlaying.isNotEmpty())
                    "▶ ${state.audioPlaying}"
                else "5 звуков: scamper / carbonate / iota / gradient / discovery",
                status  = state.audioStatus,
                buttonLabel = "Тест звуков",
                onRun   = viewModel::testAudio
            )

            // ── Тест 4: Уведомления ───────────────────────────────────────
            TestCard(
                icon    = Icons.Filled.Notifications,
                title   = "Уведомления",
                subtitle = "Отправит 2 тестовых уведомления",
                status  = state.notifStatus,
                buttonLabel = "Тест уведомлений",
                onRun   = viewModel::testNotifications
            )

            // ── Тест 5: Файлы ─────────────────────────────────────────────
            TestCard(
                icon    = Icons.Filled.FolderOpen,
                title   = "Целостность файлов",
                subtitle = "APK кеш, res/raw звуки, temp файлы",
                status  = state.filesStatus,
                buttonLabel = "Проверить файлы",
                onRun   = viewModel::testFiles
            )

            // Детали файлов
            if (state.filesDetail.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        state.filesDetail,
                        style    = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── Кнопка "Запустить всё" ────────────────────────────────────
            Button(
                onClick = {
                    viewModel.testServer()
                    viewModel.testHaptic()
                    viewModel.testAudio()
                    viewModel.testNotifications()
                    viewModel.testFiles()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Запустить все тесты", fontWeight = FontWeight.Bold)
            }

            // ── Лог ──────────────────────────────────────────────────────
            if (state.log.isNotEmpty()) {
                HorizontalDivider()
                Text("Лог", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Surface(
                    color = Color(0xFF0D0D1A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .padding(12.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        state.log.forEach { line ->
                            val color = when {
                                "FAIL"  in line -> Color(0xFFFF5252)
                                "WARN"  in line -> Color(0xFFFFD740)
                                "OK"    in line -> Color(0xFF69FF47)
                                "▶"     in line -> Color(0xFF40C4FF)
                                "⚠"    in line -> Color(0xFFFFD740)
                                else            -> Color(0xFFCCCCCC)
                            }
                            Text(line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp),
                                color = color)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Карточка одного теста ─────────────────────────────────────────────────────
@Composable
private fun TestCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: TestStatus,
    buttonLabel: String,
    onRun: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            TestStatus.IDLE    -> Color.Transparent
            TestStatus.RUNNING -> Color(0xFF1565C0)
            TestStatus.OK      -> Color(0xFF2E7D32)
            TestStatus.WARN    -> Color(0xFFF57F17)
            TestStatus.FAIL    -> Color(0xFFC62828)
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    val borderColor = if (status == TestStatus.IDLE)
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    else statusColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка с цветным кружком статуса
            Box(contentAlignment = Alignment.BottomEnd) {
                Icon(icon, null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                if (status != TestStatus.IDLE) {
                    Box(
                        Modifier.size(12.dp).clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge)
                AnimatedContent(targetState = subtitle, label = "subtitle") { sub ->
                    Text(sub, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2)
                }
            }

            // Кнопка / спиннер
            if (status == TestStatus.RUNNING) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            } else {
                val btnColor = when (status) {
                    TestStatus.OK   -> MaterialTheme.colorScheme.primary
                    TestStatus.WARN -> Color(0xFFF57F17)
                    TestStatus.FAIL -> MaterialTheme.colorScheme.error
                    else            -> MaterialTheme.colorScheme.primary
                }
                FilledTonalButton(
                    onClick = onRun,
                    colors  = ButtonDefaults.filledTonalButtonColors(
                        containerColor = btnColor.copy(alpha = 0.15f),
                        contentColor   = btnColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        when (status) {
                            TestStatus.OK   -> "✓ OK"
                            TestStatus.WARN -> "⚠ Ещё раз"
                            TestStatus.FAIL -> "✕ Retry"
                            else            -> buttonLabel
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
