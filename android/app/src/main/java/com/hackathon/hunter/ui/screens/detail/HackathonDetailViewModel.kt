package com.hackathon.hunter.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.repository.HackathonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HackathonDetailViewModel @Inject constructor(
    private val repository: HackathonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _hackathonId = MutableStateFlow<Int>(-1)
    val hackathonIdFlow = _hackathonId.asStateFlow()

    val hackathonId: Int
        get() = _hackathonId.value

    init {
        // Fallback to SavedStateHandle if available immediately
        val initialId = savedStateHandle.get<Int>("hackathonId")
            ?: savedStateHandle.get<String>("hackathonId")?.toIntOrNull()
        if (initialId != null) {
            _hackathonId.value = initialId
        }
    }

    fun setHackathonId(id: Int) {
        if (_hackathonId.value != id) {
            _hackathonId.value = id
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val hackathon: StateFlow<HackathonEntity?> = _hackathonId
        .flatMapLatest { id ->
            repository.getHackathonById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isReporting = MutableStateFlow(false)
    val isReporting = _isReporting.asStateFlow()

    private val _reportSuccess = MutableStateFlow(false)
    val reportSuccess = _reportSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun toggleBookmark(isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(hackathonId, isBookmarked)
        }
    }

    fun reportHackathon() {
        viewModelScope.launch {
            _isReporting.value = true
            _errorMessage.value = null
            
            val result = repository.reportHackathon(hackathonId)
            
            result.onSuccess {
                _reportSuccess.value = true
            }
            result.onFailure {
                // We still report success locally to trigger screen transition since it was marked locally
                _reportSuccess.value = true
                _errorMessage.value = "Gửi báo cáo lên máy chủ thất bại, nhưng đã lưu cục bộ."
            }
            _isReporting.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearReportSuccess() {
        _reportSuccess.value = false
    }
}
