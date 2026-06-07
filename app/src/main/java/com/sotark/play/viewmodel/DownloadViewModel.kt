package com.sotark.play.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.data.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    data class ReadyToInstall(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val sound: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private val CHANNEL_ID = "sotark_download"
    private val NOTIF_ID   = 1001

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    init { createNotifChannel() }

    fun isInstalled(pkg: String): Boolean = try {
        ctx.packageManager.getPackageInfo(pkg, 0); true
    } catch (_: PackageManager.NameNotFoundException) { false }

    fun canInstallUnknownSources(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.packageManager.canRequestPackageInstalls()
        else true

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${ctx.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun download(appName: String, apkUrl: String) {
        if (_state.value is DownloadState.Downloading) return
        sound.playClick()
        cleanCache()
        viewModelScope.launch {
            _state.value = DownloadState.Downloading(0)
            try {
                val file = withContext(Dispatchers.IO) { doDownload(appName, apkUrl) }
                sound.playNotification()   // iota — уведомление о завершении загрузки
                showFinishedNotif(appName)
                _state.value = DownloadState.ReadyToInstall(file)
            } catch (e: Exception) {
                sound.playError()          // gradient — ошибка
                _state.value = DownloadState.Error(e.message ?: "Download failed")
                cancelNotif()
            }
        }
    }

    private fun doDownload(appName: String, url: String): File {
        val resp   = client.newCall(Request.Builder().url(url).build()).execute()
        if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
        val body   = resp.body ?: throw Exception("Empty body")
        val total  = body.contentLength()
        val outDir = File(ctx.cacheDir, "apks").also { it.mkdirs() }
        val safe   = appName.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val out    = File(outDir, "$safe.apk")

        FileOutputStream(out).use { fos ->
            body.byteStream().use { bis ->
                val buf = ByteArray(16384)
                var downloaded = 0L; var lastPct = -1; var n: Int
                while (bis.read(buf).also { n = it } != -1) {
                    fos.write(buf, 0, n); downloaded += n
                    val pct = if (total > 0) (downloaded * 100 / total).toInt() else 0
                    if (pct != lastPct) {
                        lastPct = pct
                        _state.value = DownloadState.Downloading(pct)
                        showProgressNotif(appName, pct)
                    }
                }
            }
        }
        return out
    }

    fun installApk(file: File) {
        sound.playInstall()   // iota — при установке
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", file)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }

    fun reset() { cleanCache(); _state.value = DownloadState.Idle }

    private fun cleanCache() {
        File(ctx.cacheDir, "apks").listFiles()?.forEach { it.delete() }
    }

    private fun hasNotifPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        else true

    private fun showProgressNotif(name: String, pct: Int) {
        if (!hasNotifPermission()) return
        NotificationManagerCompat.from(ctx).notify(NOTIF_ID,
            NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Sotark Play — $name")
                .setContentText("$pct%")
                .setProgress(100, pct, pct == 0)
                .setOngoing(true).setSilent(true).build()
        )
    }

    private fun showFinishedNotif(name: String) {
        if (!hasNotifPermission()) return
        NotificationManagerCompat.from(ctx).notify(NOTIF_ID,
            NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Sotark Play")
                .setContentText("$name готов к установке")
                .setAutoCancel(true).build()
        )
    }

    private fun cancelNotif() { NotificationManagerCompat.from(ctx).cancel(NOTIF_ID) }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Загрузки",
                        NotificationManager.IMPORTANCE_LOW)
                )
        }
    }
}
