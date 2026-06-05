package com.sotark.play.data.api

import com.sotark.play.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SotarkApi {

    @GET("apps")
    suspend fun getApps(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): Response<AppsResponse>

    @GET("apps/{id}")
    suspend fun getApp(@Path("id") id: Int): Response<App>

    @GET("apps/{id}/reviews")
    suspend fun getReviews(@Path("id") id: Int): Response<ReviewsResponse>

    @POST("apps/{id}/reviews")
    suspend fun postReview(@Path("id") id: Int, @Body body: PostReviewRequest): Response<Unit>

    @GET("categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    @GET("top")
    suspend fun getTop(): Response<TopResponse>

    @GET("suggest")
    suspend fun suggest(@Query("q") q: String): Response<SuggestResponse>
}
