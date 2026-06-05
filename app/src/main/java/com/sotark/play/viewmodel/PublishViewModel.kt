package com.sotark.play.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class PublishUiState(
    val name: String        = "",
    val packageName: String = "",
    val description: String = "",
    val developer: String   = "",
    val version: String     = "1.0.0",
    val category: String    = "Other",
    val apkUri: Uri?        = null,
    val apkName: String     = "",
    val iconUri: Uri?       = null,
    val iconName: String    = "",
    val isLoading: Boolean  = false,
    val error: String?      = null,
    val success: Boolean    = false
)

val CATEGORIES = listOf(
    "Games", "Tools", "Social", "Entertainment", "Education",
    "Finance", "Health", "Music", "News", "Shopping",
    "Travel", "Productivity", "Other"
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PublishUiState())
    val state: StateFlow<PublishUiState> = _state.asStateFlow()

    fun onName(v: String)        = _state.update { it.copy(name = v) }
    fun onPackage(v: String)     = _state.update { it.copy(packageName = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onDeveloper(v: String)   = _state.update { it.copy(developer = v) }
    fun onVersion(v: String)     = _state.update { it.copy(version = v) }
    fun onCategory(v: String)    = _state.update { it.copy(category = v) }

    fun onApkPicked(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "app.apk"
        _state.update { it.copy(apkUri = uri, apkName = name) }
    }

    fun onIconPicked(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast('/') ?: "icon.png"
        _state.update { it.copy(iconUri = uri, iconName = name) }
    }

    fun publish() {
        val s = _state.value
        if (s.name.isBlank() || s.packageName.isBlank() || s.developer.isBlank()) {
            _state.update { it.copy(error = "Заполни название, пакет и разработчика") }
            return
        }
        if (s.apkUri == null) {
            _state.update { it.copy(error = "Выбери APK файл") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val result = withContext(Dispatchers.IO) {
                    uploadApp(s)
                }
                if (result) {
                    _state.update { it.copy(isLoading = false, success = true) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Ошибка загрузки на сервер") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка") }
            }
        }
    }

    private fun uriToTempFile(uri: Uri, suffix: String): File {
        val tmp = File.createTempFile("upload_", suffix, ctx.cacheDir)
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tmp).use { output -> input.copyTo(output) }
        }
        return tmp
    }

    private fun uploadApp(s: PublishUiState): Boolean {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val apkFile  = uriToTempFile(s.apkUri!!, ".apk")
        val iconFile = s.iconUri?.let { uriToTempFile(it, ".png") }

        val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("name",        s.name)
            .addFormDataPart("package",     s.packageName)
            .addFormDataPart("description", s.description)
            .addFormDataPart("developer",   s.developer)
            .addFormDataPart("version",     s.version)
            .addFormDataPart("category",    s.category)
            .addFormDataPart("apk", apkFile.name,
                apkFile.asRequestBody("application/vnd.android.package-archive".toMediaTypeOrNull()))

        if (iconFile != null) {
            bodyBuilder.addFormDataPart("icon", iconFile.name,
                iconFile.asRequestBody("image/*".toMediaTypeOrNull()))
        }

        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "apps")
            .post(bodyBuilder.build())
            .build()

        val response = client.newCall(request).execute()
        apkFile.delete()
        iconFile?.delete()
        return response.isSuccessful
    }

    fun resetSuccess() = _state.update { it.copy(success = false) }
    fun clearError()   = _state.update { it.copy(error = null) }
}
