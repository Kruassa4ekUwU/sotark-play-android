package com.sotark.play.data.model

import com.google.gson.annotations.SerializedName

data class App(
    val id: Int = 0,
    val name: String = "",
    val `package`: String = "",
    val description: String = "",
    val category: String = "",
    val version: String = "1.0.0",
    @SerializedName("size_mb") val sizeMb: Float = 0f,
    @SerializedName("icon_url") val iconUrl: String? = null,
    @SerializedName("apk_url") val apkUrl: String? = null,
    val developer: String = "",
    @SerializedName("dev_email") val devEmail: String = "",
    val downloads: Long = 0,
    val rating: Float = 0f,
    @SerializedName("rating_cnt") val ratingCnt: Int = 0,
    val screenshots: List<String> = emptyList(),
    @SerializedName("created_at") val createdAt: String = ""
)

data class Review(
    val id: Int = 0,
    @SerializedName("app_id") val appId: Int = 0,
    val author: String = "",
    val rating: Int = 5,
    val text: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

data class Category(
    val category: String,
    val count: Int
)

// ─── Request / Response wrappers ────────────────────────────────────────────

data class AppsResponse(val apps: List<App>, val count: Int = 0)
data class ReviewsResponse(val reviews: List<Review>)
data class CategoriesResponse(val categories: List<Category>)
data class TopResponse(val apps: List<App>)
data class SuggestResponse(val suggestions: List<String>)

data class PostReviewRequest(
    val author: String,
    val rating: Int,
    val text: String
)
