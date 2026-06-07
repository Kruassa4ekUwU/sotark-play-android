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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class PublishUiState(
    val name: String              = "",
    val packageName: String       = "",
    val description: String       = "",
    val developer: String         = "",
    val version: String           = "1.0.0",
    val category: String          = "Other",
    val ageRating: String         = "ALL",
    val apkUri: Uri?              = null,
    val apkName: String           = "",
    val iconUri: Uri?             = null,
    val iconName: String          = "",
    val screenshotUris: List<Uri> = emptyList(),
    val isLoading: Boolean        = false,
    val error: String?            = null,
    val success: Boolean          = false
)

val CATEGORIES = listOf(
    "Games", "Tools", "Social", "Entertainment", "Education",
    "Finance", "Health", "Music", "News", "Shopping",
    "Travel", "Productivity", "Other"
)

val AGE_RATINGS = listOf(
    "ALL"      to "0+",
    "SIX"      to "6+",
    "TWELVE"   to "12+",
    "SIXTEEN"  to "16+",
    "EIGHTEEN" to "18+"
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
    fun onAgeRating(v: String)   = _state.update { it.copy(ageRating = v) }

    fun onApkPicked(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast("/") ?: "app.apk"
        _state.update { it.copy(apkUri = uri, apkName = name) }
    }

    fun onIconPicked(uri: Uri) {
        val name = uri.lastPathSegment?.substringAfterLast("/") ?: "icon.png"
        _state.update { it.copy(iconUri = uri, iconName = name) }
    }

    fun onScreenshotsPicked(uris: List<Uri>) {
        val current  = _state.value.screenshotUris
        val combined = (current + uris).distinct().take(5)
        _state.update { it.copy(screenshotUris = combined) }
    }

    fun removeScreenshot(index: Int) {
        val list = _state.value.screenshotUris.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.update { it.copy(screenshotUris = list) }
        }
    }

    fun publish() {
        val s = _state.value
        if (s.name.isBlank() || s.packageName.isBlank() || s.developer.isBlank()) {
            _state.update { it.copy(error = "fill_required") }; return
        }
        if (s.apkUri == null) {
            _state.update { it.copy(error = "pick_apk_required") }; return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val ok = withContext(Dispatchers.IO) { uploadApp(s) }
                _state.update { it.copy(isLoading = false, success = ok,
                    error = if (!ok) "upload_error" else null) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "error") }
            }
        }
    }

    private fun uriToTempFile(uri: Uri, suffix: String): File {
        val tmp = File.createTempFile("upload_", suffix, ctx.cacheDir)
        ctx.contentResolver.openInputStream(uri)?.use { i ->
            FileOutputStream(tmp).use { o -> i.copyTo(o) }
        }
        return tmp
    }

    private fun uploadApp(s: PublishUiState): Boolean {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val apkFile  = uriToTempFile(s.apkUri!!, ".apk")
        val iconFile = s.iconUri?.let { uriToTempFile(it, ".png") }
        val ssFiles  = s.screenshotUris.mapIndexed { i, uri ->
            uriToTempFile(uri, "_ss$i.jpg")
        }

        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("name",        s.name)
            .addFormDataPart("package",     s.packageName)
            .addFormDataPart("description", s.description)
            .addFormDataPart("developer",   s.developer)
            .addFormDataPart("version",     s.version)
            .addFormDataPart("category",    s.category)
            .addFormDataPart("age_rating",  s.ageRating)
            .addFormDataPart("apk", apkFile.name,
                apkFile.asRequestBody("application/vnd.android.package-archive".toMediaTypeOrNull()))
            .apply {
                iconFile?.let { addFormDataPart("icon", it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())) }
                ssFiles.forEach { f -> addFormDataPart("screenshots", f.name,
                    f.asRequestBody("image/*".toMediaTypeOrNull())) }
            }.build()

        val url      = BuildConfig.BASE_URL.trimEnd('/') + "/apps"
        val response = client.newCall(Request.Builder().url(url).post(body).build()).execute()
        apkFile.delete(); iconFile?.delete(); ssFiles.forEach { it.delete() }
        return response.isSuccessful
    }

    fun resetSuccess() = _state.update { it.copy(success = false) }
    fun clearError()   = _state.update { it.copy(error = null) }
}
