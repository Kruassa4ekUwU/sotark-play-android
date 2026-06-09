package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.data.model.App
import com.sotark.play.data.model.Review
import com.sotark.play.data.repository.AppRepository
import com.sotark.play.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppDetailUiState(
    val app: App?             = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean    = false,
    val error: String?        = null
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val repo: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppDetailUiState())
    val state: StateFlow<AppDetailUiState> = _state.asStateFlow()

    fun load(appId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val app     = repo.getApp(appId)
            val reviews = repo.getReviews(appId)
            _state.update {
                it.copy(
                    isLoading = false,
                    app       = (app as? Result.Success)?.data,
                    reviews   = (reviews as? Result.Success)?.data ?: emptyList(),
                    error     = (app as? Result.Error)?.message
                )
            }
        }
    }

    fun postReview(appId: Int, author: String, rating: Int, text: String) {
        viewModelScope.launch {
            repo.postReview(appId, author, rating, text)
            val reviews = repo.getReviews(appId)
            if (reviews is Result.Success) _state.update { it.copy(reviews = reviews.data) }
        }
    }
}
