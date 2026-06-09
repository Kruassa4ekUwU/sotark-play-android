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
import com.sotark.play.R
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
    val serverStatus: TestStatus = TestStatus.IDLE,
    val serverPing:   Long       = -1L,
    val serverDetail: String     = "",
    val hapticStatus: TestStatus = TestStatus.IDLE,
    val audioStatus:  TestStatus = TestStatus.IDLE,
    val audioPlaying: String     = "",
    val notifStatus:  TestStatus = TestStatus.IDLE,
    val filesStatus:  TestStatus = TestStatus.IDLE,
    val filesDetail:  String     = "",
    val log: List<String>        = emptyList()
)

enum class TestStatus { IDLE, RUNNING, OK, WARN, FAIL }

@HiltViewModel
class DevTestViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val sound: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(DevTestState())
    val state: StateFlow<DevTestState> = _state.asStateFlow()

    private val CHANNEL_ID = "sotark_devtest"
    private val NOTIF_ID   = 9001

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init { createNotifChannel() }

    private fun log(msg: String) {
        val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _state.update { it.copy(log = (listOf("[$ts] $msg") + it.log).take(60)) }
    }

    // ── 1. Сервер ─────────────────────────────────────────────────────────────
    fun testServer() = viewModelScope.launch {
        _state.update { it.copy(serverStatus = TestStatus.RUNNING, serverDetail = "Соединяемся...") }
        log("→ Тест сервера: ${BuildConfig.BASE_URL}")
        try {
            val url = BuildConfig.BASE_URL.trimEnd('/') + "/apps?limit=1"
            val t0  = System.currentTimeMillis()
            val code = withContext(Dispatchers.IO) {
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                val c = resp.code
                resp.close()
                c
            }
            val ping = System.currentTimeMillis() - t0
            if (code in 200..299) {
                _state.update { it.copy(serverStatus = TestStatus.OK, serverPing = ping,
                    serverDetail = "HTTP $code · ${ping}ms") }
                log("✓ Сервер OK — HTTP $code, ${ping}ms")
                sound.playInstall()
            } else {
                _state.update { it.copy(serverStatus = TestStatus.WARN, serverPing = ping,
                    serverDetail = "HTTP $code · ${ping}ms") }
                log("⚠ Сервер WARN — HTTP $code")
                sound.playError()
            }
        } catch (e: Exception) {
            _state.update { it.copy(serverStatus = TestStatus.FAIL,
                serverDetail = e.message?.take(60) ?: "Timeout") }
            log("✕ Сервер FAIL — ${e.message?.take(80)}")
            sound.playError()
        }
    }

    // ── 2. Вибро ──────────────────────────────────────────────────────────────
    fun testHaptic() = viewModelScope.launch {
        _state.update { it.copy(hapticStatus = TestStatus.RUNNING) }
        log("→ Тест вибро")
        delay(100)
        sound.playClick();   log("  carbonate — клик"); delay(600)
        sound.playInstall(); log("  iota — установка"); delay(600)
        sound.playError();   log("  gradient — ошибка"); delay(600)
        _state.update { it.copy(hapticStatus = TestStatus.OK) }
        log("✓ Вибро OK")
    }

    // ── 3. Аудио ──────────────────────────────────────────────────────────────
    fun testAudio() = viewModelScope.launch {
        _state.update { it.copy(audioStatus = TestStatus.RUNNING) }
        log("→ Тест аудио")
        val tracks = listOf(
            "scamper (запуск)"      to { sound.playLaunch()       },
            "carbonate (клик)"      to { sound.playClick()        },
            "iota (установка)"      to { sound.playInstall()      },
            "gradient (ошибка)"     to { sound.playError()        },
            "discovery (секрет)"    to { sound.playSecret()       }
        )
        for ((name, play) in tracks) {
            _state.update { it.copy(audioPlaying = name) }
            log("  ▶ $name")
            play()
            delay(1000)
        }
        _state.update { it.copy(audioStatus = TestStatus.OK, audioPlaying = "") }
        log("✓ Аудио OK — 5/5 треков")
    }

    // ── 4. Уведомления ────────────────────────────────────────────────────────
    fun testNotifications() = viewModelScope.launch {
        _state.update { it.copy(notifStatus = TestStatus.RUNNING) }
        log("→ Тест уведомлений")

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true

        if (!hasPermission) {
            _state.update { it.copy(notifStatus = TestStatus.WARN) }
            log("⚠ Нет разрешения POST_NOTIFICATIONS")
            log("  Разрешение запрашивается при старте приложения")
            sound.playError()
            return@launch
        }

        try {
            val mgr = NotificationManagerCompat.from(ctx)
            sound.playNotification()
            mgr.notify(NOTIF_ID, NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Sotark Beta · Dev Test")
                .setContentText("Уведомление работает ✓")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true).build())
            log("  Уведомление #1 отправлено")

            delay(2000)
            mgr.notify(NOTIF_ID + 1, NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Sotark Beta · Прогресс")
                .setContentText("Симуляция загрузки 75%")
                .setProgress(100, 75, false)
                .setAutoCancel(true).build())
            log("  Уведомление #2 с прогрессом отправлено")

            delay(3000)
            mgr.cancel(NOTIF_ID); mgr.cancel(NOTIF_ID + 1)
            _state.update { it.copy(notifStatus = TestStatus.OK) }
            log("✓ Уведомления OK")
        } catch (e: Exception) {
            _state.update { it.copy(notifStatus = TestStatus.FAIL) }
            log("✕ Уведомления FAIL — ${e.message}")
            sound.playError()
        }
    }

    // ── 5. Файлы ──────────────────────────────────────────────────────────────
    fun testFiles() = viewModelScope.launch {
        _state.update { it.copy(filesStatus = TestStatus.RUNNING, filesDetail = "Сканирую...") }
        log("→ Проверка файлов")
        val issues  = mutableListOf<String>()
        val report  = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            // APK кеш
            val apkDir = File(ctx.cacheDir, "apks")
            if (apkDir.exists()) {
                val apks = apkDir.listFiles() ?: emptyArray()
                report += "APK кеш: ${apks.size} файл(ов)"
                apks.forEach { f ->
                    if (f.length() == 0L) issues += "Битый APK: ${f.name}"
                    report += "  ${f.name} ${f.length()/1024}KB ${if (f.length()>0) "OK" else "ПУСТ!"}"
                }
            } else report += "APK кеш: пусто (норма)"

            // res/raw звуки
            val rawMap = mapOf(
                "scamper"   to R.raw.scamper,
                "carbonate" to R.raw.carbonate,
                "iota"      to R.raw.iota,
                "gradient"  to R.raw.gradient,
                "discovery" to R.raw.discovery
            )
            report += "Звуки res/raw:"
            rawMap.forEach { (name, resId) ->
                try {
                    val fd = ctx.resources.openRawResourceFd(resId)
                    val sz = fd.length; fd.close()
                    val ok = sz > 512L
                    report += "  $name — ${sz/1024}KB ${if (ok) "OK" else "ПОДОЗРИТЕЛЬНО МАЛО"}"
                    if (!ok) issues += "Маленький файл: $name"
                } catch (e: Exception) {
                    report += "  $name — ОШИБКА: ${e.message}"
                    issues += "Файл не найден: $name"
                }
            }

            // temp upload
            val tmp = ctx.cacheDir.listFiles { f -> f.name.startsWith("upload_") } ?: emptyArray()
            if (tmp.isNotEmpty()) {
                report += "Temp upload: ${tmp.size} файл(ов) (утечка!)"
                issues += "Temp не удалены: ${tmp.size} файл(ов)"
            } else report += "Temp upload: чисто"

            // общий кеш
            val total = ctx.cacheDir.walkTopDown().sumOf { it.length() }
            report += "Кеш всего: ${total/1024}KB"
            if (total > 100 * 1024 * 1024L) issues += "Кеш > 100MB!"
        }

        val detail = report.joinToString("\n")
        _state.update { it.copy(
            filesStatus = if (issues.isEmpty()) TestStatus.OK else TestStatus.WARN,
            filesDetail = detail
        )}
        report.forEach { log("  $it") }
        if (issues.isEmpty()) {
            sound.playInstall(); log("✓ Файлы OK")
        } else {
            issues.forEach { log("  ⚠ $it") }
            sound.playError(); log("⚠ Файлы: ${issues.size} проблем")
        }
    }

    fun resetAll() { _state.update { DevTestState() }; log("Сброс") }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Dev Test",
                        NotificationManager.IMPORTANCE_HIGH)
                )
        }
    }
}
