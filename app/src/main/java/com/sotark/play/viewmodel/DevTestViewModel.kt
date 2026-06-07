package com.sotark.play.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.BuildConfig
import com.sotark.play.data.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class DevTestState(
    // Сервер
    val serverStatus: TestStatus = TestStatus.IDLE,
    val serverPing:   Long       = -1L,
    val serverDetail: String     = "",
    // Вибро
    val hapticStatus: TestStatus = TestStatus.IDLE,
    // Аудио
    val audioStatus:  TestStatus = TestStatus.IDLE,
    val audioPlaying: String     = "",
    // Уведомления
    val notifStatus:  TestStatus = TestStatus.IDLE,
    // Файлы кеша
    val filesStatus:  TestStatus = TestStatus.IDLE,
    val filesDetail:  String     = "",
    // Общий лог
    val log: List<String> = emptyList()
)

enum class TestStatus { IDLE, RUNNING, OK, WARN, FAIL }

@HiltViewModel
class DevTestViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val sound: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(DevTestState())
    val state: StateFlow<DevTestState> = _state.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val CHANNEL_ID = "sotark_devtest"
    private val NOTIF_ID   = 9001

    init { createNotifChannel() }

    // ── Лог ──────────────────────────────────────────────────────────────────
    private fun log(msg: String) {
        val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _state.update { it.copy(log = (listOf("[$ts] $msg") + it.log).take(50)) }
    }

    // ── 1. Тест сервера ───────────────────────────────────────────────────────
    fun testServer() {
        viewModelScope.launch {
            _state.update { it.copy(serverStatus = TestStatus.RUNNING, serverDetail = "Пингуем...") }
            log("Тест сервера → ${BuildConfig.BASE_URL}")
            try {
                val url  = BuildConfig.BASE_URL.trimEnd('/') + "/apps?limit=1"
                val t0   = System.currentTimeMillis()
                val resp = withContext(Dispatchers.IO) {
                    client.newCall(Request.Builder().url(url).build()).execute()
                }
                val ping = System.currentTimeMillis() - t0
                val code = resp.code
                resp.close()
                if (resp.isSuccessful) {
                    _state.update { it.copy(
                        serverStatus = TestStatus.OK,
                        serverPing   = ping,
                        serverDetail = "HTTP $code · ${ping}ms"
                    )}
                    log("Сервер OK · HTTP $code · ${ping}ms")
                    sound.playSuccess()
                } else {
                    _state.update { it.copy(
                        serverStatus = TestStatus.WARN,
                        serverPing   = ping,
                        serverDetail = "HTTP $code · ${ping}ms"
                    )}
                    log("Сервер WARN · HTTP $code")
                    sound.playError()
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    serverStatus = TestStatus.FAIL,
                    serverDetail = e.message ?: "Timeout"
                )}
                log("Сервер FAIL · ${e.message}")
                sound.playError()
            }
        }
    }

    // ── 2. Тест вибро ─────────────────────────────────────────────────────────
    fun testHaptic() {
        viewModelScope.launch {
            _state.update { it.copy(hapticStatus = TestStatus.RUNNING) }
            log("Тест вибро...")
            // короткий
            sound.playClick()
            delay(400)
            // средний
            sound.playSuccess()
            delay(400)
            // ошибка (двойной)
            sound.playError()
            delay(300)
            _state.update { it.copy(hapticStatus = TestStatus.OK) }
            log("Вибро OK — click / success / error")
        }
    }

    // ── 3. Тест аудио — все 5 звуков по очереди ──────────────────────────────
    fun testAudio() {
        viewModelScope.launch {
            _state.update { it.copy(audioStatus = TestStatus.RUNNING) }
            log("Тест аудио — 5 звуков...")
            val sounds = listOf(
                "scamper (запуск)"    to { sound.playLaunch()  },
                "carbonate (клик)"   to { sound.playClick()   },
                "iota (установка)"   to { sound.playSuccess() },
                "gradient (ошибка)"  to { sound.playError()   },
                "discovery (секрет)" to { sound.playSecret()  }
            )
            for ((name, play) in sounds) {
                _state.update { it.copy(audioPlaying = name) }
                log("▶ $name")
                play()
                delay(900)
            }
            _state.update { it.copy(audioStatus = TestStatus.OK, audioPlaying = "") }
            log("Аудио OK — все 5 звуков")
        }
    }

    // ── 4. Тест уведомлений ───────────────────────────────────────────────────
    fun testNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(notifStatus = TestStatus.RUNNING) }
            log("Тест уведомлений...")

            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true

            if (!hasPermission) {
                _state.update { it.copy(notifStatus = TestStatus.WARN) }
                log("Уведомления WARN — нет разрешения POST_NOTIFICATIONS")
                return@launch
            }

            try {
                val notifMgr = NotificationManagerCompat.from(ctx)
                // Уведомление 1 — обычное
                notifMgr.notify(NOTIF_ID,
                    NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Sotark Beta · Dev Test")
                        .setContentText("Тест уведомлений работает ✓")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                )
                log("Уведомление отправлено")
                delay(2000)
                // Уведомление 2 — с прогрессом
                notifMgr.notify(NOTIF_ID + 1,
                    NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle("Sotark Beta · Прогресс-тест")
                        .setContentText("Симуляция загрузки...")
                        .setProgress(100, 75, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .build()
                )
                log("Уведомление с прогрессом отправлено")
                delay(3000)
                notifMgr.cancel(NOTIF_ID)
                notifMgr.cancel(NOTIF_ID + 1)
                _state.update { it.copy(notifStatus = TestStatus.OK) }
                log("Уведомления OK")
                sound.playSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(notifStatus = TestStatus.FAIL) }
                log("Уведомления FAIL · ${e.message}")
                sound.playError()
            }
        }
    }

    // ── 5. Проверка файлов кеша ───────────────────────────────────────────────
    fun testFiles() {
        viewModelScope.launch {
            _state.update { it.copy(filesStatus = TestStatus.RUNNING, filesDetail = "Сканируем...") }
            log("Проверка файлов кеша и res/raw...")

            val issues = mutableListOf<String>()
            val report = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                // 1. Кеш APK
                val apkDir = File(ctx.cacheDir, "apks")
                if (apkDir.exists()) {
                    val apks = apkDir.listFiles() ?: emptyArray()
                    report += "APK кеш: ${apks.size} файл(ов)"
                    apks.forEach { f ->
                        val ok = f.exists() && f.length() > 0
                        report += "  ${f.name} · ${f.length() / 1024}KB · ${if (ok) "OK" else "ПУСТ!"}"
                        if (!ok) issues += "Битый APK: ${f.name}"
                    }
                } else {
                    report += "APK кеш: пусто"
                }

                // 2. res/raw звуки — проверяем через assets
                val rawSounds = listOf("scamper", "carbonate", "iota", "gradient", "discovery")
                val resIds = mapOf(
                    "scamper"   to com.sotark.play.R.raw.scamper,
                    "carbonate" to com.sotark.play.R.raw.carbonate,
                    "iota"      to com.sotark.play.R.raw.iota,
                    "gradient"  to com.sotark.play.R.raw.gradient,
                    "discovery" to com.sotark.play.R.raw.discovery
                )
                report += "Звуковые файлы res/raw:"
                resIds.forEach { (name, resId) ->
                    try {
                        val afd = ctx.resources.openRawResourceFd(resId)
                        val size = afd.length
                        afd.close()
                        val ok = size > 1024L
                        report += "  $name.mp3 · ${size / 1024}KB · ${if (ok) "OK" else "СЛИШКОМ МАЛ!"}"
                        if (!ok) issues += "Подозрительный звук: $name (${size}B)"
                    } catch (e: Exception) {
                        report += "  $name.mp3 · ОШИБКА: ${e.message}"
                        issues += "Звук не найден: $name"
                    }
                }

                // 3. Temp файлы upload
                val tmpFiles = ctx.cacheDir.listFiles { f ->
                    f.name.startsWith("upload_")
                } ?: emptyArray()
                if (tmpFiles.isNotEmpty()) {
                    report += "Temp upload: ${tmpFiles.size} файл(ов) — возможна утечка"
                    issues += "Temp файлы не удалены: ${tmpFiles.size}шт"
                } else {
                    report += "Temp upload: чисто"
                }

                // 4. Общий размер кеша
                val cacheSize = ctx.cacheDir.walkTopDown().sumOf { it.length() }
                report += "Общий кеш: ${cacheSize / 1024}KB"
                if (cacheSize > 100 * 1024 * 1024L) {
                    issues += "Кеш > 100MB!"
                }
            }

            val summary = if (issues.isEmpty()) "Всё OK" else "${issues.size} проблем(а)"
            val detail  = report.joinToString("\n")
            _state.update { it.copy(
                filesStatus = if (issues.isEmpty()) TestStatus.OK else TestStatus.WARN,
                filesDetail = detail
            )}
            report.forEach { log(it) }
            if (issues.isNotEmpty()) {
                issues.forEach { log("⚠ $it") }
                sound.playError()
            } else {
                sound.playSuccess()
            }
            log("Файлы: $summary")
        }
    }

    // ── Сброс всех тестов ─────────────────────────────────────────────────────
    fun resetAll() {
        _state.update { DevTestState() }
        log("Сброс тестов")
    }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Dev Test",
                        NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Канал для тестирования уведомлений (только бета)"
                    }
                )
        }
    }
}
