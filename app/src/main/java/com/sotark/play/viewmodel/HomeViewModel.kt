package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.data.SoundManager
import com.sotark.play.data.model.App
import com.sotark.play.data.model.Category
import com.sotark.play.data.repository.AppRepository
import com.sotark.play.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topApps: List<App>         = emptyList(),
    val newApps: List<App>         = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean         = false,
    val error: String?             = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: AppRepository,
    private val sound: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        sound.playLaunch()
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val top    = repo.getTop()
            val newest = repo.getApps(sort = "newest")
            val cats   = repo.getCategories()
            _state.update { s ->
                s.copy(
                    isLoading  = false,
                    topApps    = if (top is Result.Success) top.data else emptyList(),
                    newApps    = if (newest is Result.Success) newest.data else emptyList(),
                    categories = if (cats is Result.Success) cats.data else emptyList(),
                    error      = listOfNotNull(
                        (top as? Result.Error)?.message,
                        (newest as? Result.Error)?.message
                    ).firstOrNull()
                )
            }
        }
    }
}
