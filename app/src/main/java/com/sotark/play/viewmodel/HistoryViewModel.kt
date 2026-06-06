package com.sotark.play.viewmodel

import androidx.lifecycle.ViewModel
import com.sotark.play.data.DownloadHistory
import com.sotark.play.data.DownloadRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val history: DownloadHistory
) : ViewModel() {

    private val _records = MutableStateFlow<List<DownloadRecord>>(emptyList())
    val records: StateFlow<List<DownloadRecord>> = _records.asStateFlow()

    init { load() }

    fun load() { _records.value = history.getAll() }

    fun clear() {
        history.clear()
        _records.value = emptyList()
    }
}
