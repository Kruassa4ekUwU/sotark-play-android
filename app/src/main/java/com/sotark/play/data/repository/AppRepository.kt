package com.sotark.play.data.repository

import com.sotark.play.data.api.SotarkApi
import com.sotark.play.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class AppRepository @Inject constructor(private val api: SotarkApi) {

    suspend fun getApps(
        query: String? = null,
        category: String? = null,
        sort: String? = null
    ): Result<List<App>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getApps(query, category, sort)
            if (resp.isSuccessful) Result.Success(resp.body()?.apps ?: emptyList())
            else Result.Error("Server error: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getApp(id: Int): Result<App> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getApp(id)
            if (resp.isSuccessful && resp.body() != null) Result.Success(resp.body()!!)
            else Result.Error("Not found")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getReviews(id: Int): Result<List<Review>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getReviews(id)
            if (resp.isSuccessful) Result.Success(resp.body()?.reviews ?: emptyList())
            else Result.Error("Error: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun postReview(appId: Int, author: String, rating: Int, text: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.postReview(appId, PostReviewRequest(author, rating, text))
                if (resp.isSuccessful) Result.Success(Unit)
                else Result.Error("Error: ${resp.code()}")
            } catch (e: Exception) {
                Result.Error(e.message ?: "Network error")
            }
        }

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getCategories()
            if (resp.isSuccessful) Result.Success(resp.body()?.categories ?: emptyList())
            else Result.Error("Error: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTop(): Result<List<App>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getTop()
            if (resp.isSuccessful) Result.Success(resp.body()?.apps ?: emptyList())
            else Result.Error("Error: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun suggest(q: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.suggest(q)
            if (resp.isSuccessful) Result.Success(resp.body()?.suggestions ?: emptyList())
            else Result.Error("Error: ${resp.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
