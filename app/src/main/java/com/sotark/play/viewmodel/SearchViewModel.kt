package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotark.play.data.model.AgeRating
import com.sotark.play.data.model.App
import com.sotark.play.data.repository.AppRepository
import com.sotark.play.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SortOption(val label: String, val value: String) {
    DOWNLOADS("По скачиваниям", "downloads"),
    RATING("По рейтингу", "rating"),
    NEWEST("Новые", "newest"),
    NAME("По названию", "name")
}

data class FilterState(
    val category: String?    = null,
    val sort: SortOption     = SortOption.DOWNLOADS,
    val minRating: Float     = 0f,
    val maxAge: AgeRating?   = null,
    val showFilters: Boolean = false
)

data class SearchUiState(
    val query: String             = "",
    val results: List<App>        = emptyList(),
    val suggestions: List<String> = emptyList(),
    val filter: FilterState       = FilterState(),
    val isLoading: Boolean        = false,
    val error: String?            = null
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
            val s = repo.suggest(q)
            if (s is Result.Success) _state.update { it.copy(suggestions = s.data) }
            search(q)
        }
    }

    fun search(q: String = _state.value.query) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, suggestions = emptyList()) }
            val filter = _state.value.filter
            val r = repo.getApps(
                query    = q.ifBlank { null },
                category = filter.category,
                sort     = filter.sort.value
            )
            _state.update {
                when (r) {
                    is Result.Success -> {
                        var apps = r.data
                        if (filter.minRating > 0f) {
                            apps = apps.filter { app -> app.rating >= filter.minRating }
                        }
                        val maxAge = filter.maxAge
                        if (maxAge != null) {
                            apps = apps.filter { app ->
                                app.getAgeRatingEnum().minAge <= maxAge.minAge
                            }
                        }
                        it.copy(results = apps, isLoading = false)
                    }
                    is Result.Error -> it.copy(error = r.message, isLoading = false)
                    else -> it.copy(isLoading = false)
                }
            }
        }
    }

    fun toggleFilters() = _state.update {
        it.copy(filter = it.filter.copy(showFilters = !it.filter.showFilters))
    }
    fun setCategory(cat: String?) {
        _state.update { it.copy(filter = it.filter.copy(category = cat)) }
        search()
    }
    fun setSort(sort: SortOption) {
        _state.update { it.copy(filter = it.filter.copy(sort = sort)) }
        search()
    }
    fun setMinRating(r: Float) {
        _state.update { it.copy(filter = it.filter.copy(minRating = r)) }
        search()
    }
    fun setMaxAge(age: AgeRating?) {
        _state.update { it.copy(filter = it.filter.copy(maxAge = age)) }
        search()
    }
    fun clearFilters() {
        _state.update { it.copy(filter = FilterState()) }
        search()
    }
}
