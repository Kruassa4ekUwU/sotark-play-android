package com.sotark.play.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadRecord(
    val appId: Int,
    val appName: String,
    val iconUrl: String?,
    val version: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class DownloadHistory @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("download_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun add(record: DownloadRecord) {
        val list = getAll().toMutableList()
        list.removeAll { it.appId == record.appId }
        list.add(0, record)
        val trimmed = list.take(50) // максимум 50 записей
        prefs.edit().putString("history", gson.toJson(trimmed)).apply()
    }

    fun getAll(): List<DownloadRecord> {
        val json = prefs.getString("history", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DownloadRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun clear() = prefs.edit().remove("history").apply()
}
