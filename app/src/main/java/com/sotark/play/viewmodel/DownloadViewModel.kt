package com.sotark.play.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val notifManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID  = "sotark_download"
    private val NOTIF_ID    = 1001

    init { createNotifChannel() }

    fun download(appName: String, apkUrl: String) {
        if (_state.value is DownloadState.Downloading) return
        viewModelScope.launch {
            _state.value = DownloadState.Downloading(0)
            try {
                val file = withContext(Dispatchers.IO) { doDownload(appName, apkUrl) }
                _state.value = DownloadState.ReadyToInstall(file)
                notifManager.cancel(NOTIF_ID)
            } catch (e: Exception) {
                _state.value = DownloadState.Error(e.message ?: "Ошибка скачивания")
                notifManager.cancel(NOTIF_ID)
            }
        }
    }

    private fun doDownload(appName: String, url: String): File {
        val request  = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Сервер вернул ${response.code}")

        val body        = response.body ?: throw Exception("Пустой ответ")
        val totalBytes  = body.contentLength()
        val outDir      = File(ctx.cacheDir, "apks").also { it.mkdirs() }
        val outFile     = File(outDir, "${appName.replace(" ", "_")}.apk")

        FileOutputStream(outFile).use { out ->
            body.byteStream().use { input ->
                val buf = ByteArray(8192)
                var downloaded = 0L
                var lastProgress = -1
                var bytes: Int
                while (input.read(buf).also { bytes = it } != -1) {
                    out.write(buf, 0, bytes)
                    downloaded += bytes
                    val progress = if (totalBytes > 0)
                        (downloaded * 100 / totalBytes).toInt() else 0
                    if (progress != lastProgress) {
                        lastProgress = progress
                        _state.value = DownloadState.Downloading(progress)
                        showProgressNotif(appName, progress)
                    }
                }
            }
        }
        return outFile
    }

    fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(ctx, "\${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    fun reset() { _state.value = DownloadState.Idle }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Загрузки", NotificationManager.IMPORTANCE_LOW)
            notifManager.createNotificationChannel(ch)
        }
    }

    private fun showProgressNotif(name: String, progress: Int) {
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Скачивание: $name")
            .setContentText("$progress%")
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setSilent(true)
            .build()
        notifManager.notify(NOTIF_ID, notif)
    }
}
