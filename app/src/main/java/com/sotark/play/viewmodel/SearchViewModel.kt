package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.data.model.App
import com.sotark.play.data.repository.AppRepository
import com.sotark.play.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String        = "",
    val results: List<App>   = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean   = false,
    val error: String?       = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var debounceJob: Job? = null

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        debounceJob?.cancel()
        if (q.isBlank()) {
            _state.update { it.copy(results = emptyList(), suggestions = emptyList()) }
            return
        }
        debounceJob = viewModelScope.launch {
            delay(300)
            // suggestions
            val s = repo.suggest(q)
            if (s is Result.Success) _state.update { it.copy(suggestions = s.data) }
            // full search
            search(q)
        }
    }

    fun search(q: String = _state.value.query) {
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val r = repo.getApps(query = q)
            _state.update {
                when (r) {
                    is Result.Success -> it.copy(results = r.data, isLoading = false, suggestions = emptyList())
                    is Result.Error   -> it.copy(error = r.message, isLoading = false)
                    else              -> it.copy(isLoading = false)
                }
            }
        }
    }
}
