package com.hackathon.hunter.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.repository.HackathonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HackathonListViewModel @Inject constructor(
    private val repository: HackathonRepository
) : ViewModel() {

    // Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _vietnamOnly = MutableStateFlow(true)
    val vietnamOnly = _vietnamOnly.asStateFlow()

    private val _prizeType = MutableStateFlow("all") // "all", "fiat", "crypto"
    val prizeType = _prizeType.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow(
        setOf("devpost", "devfolio", "hackerearth", "gitcoin", "dorahacks", "bewater")
    )
    val selectedPlatforms = _selectedPlatforms.asStateFlow()

    private val _isOnline = MutableStateFlow<Boolean?>(null) // null = All, true = Online, false = In-person
    val isOnline = _isOnline.asStateFlow()

    private val _minPrizeValue = MutableStateFlow(0.0)
    val minPrizeValue = _minPrizeValue.asStateFlow()

    private val _showBookmarksOnly = MutableStateFlow(false)
    val showBookmarksOnly = _showBookmarksOnly.asStateFlow()

    // UI Status States
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // 1. Observe and filter cached database hackathons in real-time
    val hackathons: StateFlow<List<HackathonEntity>> = combine(
        repository.getHackathons(),
        _searchQuery,
        _vietnamOnly,
        _prizeType,
        _selectedPlatforms,
        _isOnline,
        _minPrizeValue,
        _showBookmarksOnly
    ) { flowsArray ->
        val rawList = flowsArray[0] as List<HackathonEntity>
        val query = flowsArray[1] as String
        val vnOnly = flowsArray[2] as Boolean
        val type = flowsArray[3] as String
        val platforms = flowsArray[4] as Set<String>
        val online = flowsArray[5] as Boolean?
        val minPrize = flowsArray[6] as Double
        val bookmarksOnly = flowsArray[7] as Boolean

        rawList.filter { item ->
            // Filter by Bookmarks Only
            if (bookmarksOnly && !item.isBookmarked) return@filter false

            // Filter by Reported/Ineligible logic (handled in DB query as well, but kept here for safety)
            if (item.isReportedByUser) return@filter false
            if (vnOnly && !item.isVietnamEligible) return@filter false

            // Filter by Online status
            if (online != null && item.isOnline != online) return@filter false

            // Filter by Prize Type
            if (type != "all" && !item.prizeType.equals(type, ignoreCase = true)) return@filter false

            // Filter by Minimum Prize Value
            if (item.prizeValue < minPrize) return@filter false

            // Filter by Platforms
            if (!platforms.contains(item.platform.lowercase())) return@filter false

            // Filter by Keyword Query
            if (query.isNotEmpty()) {
                val matchesTitle = item.title.contains(query, ignoreCase = true)
                val matchesDesc = item.description?.contains(query, ignoreCase = true) ?: false
                if (!matchesTitle && !matchesDesc) return@filter false
            }

            true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial Fetch
        refresh()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setVietnamOnly(value: Boolean) {
        _vietnamOnly.value = value
    }

    fun setPrizeType(value: String) {
        _prizeType.value = value
    }

    fun togglePlatform(platform: String) {
        _selectedPlatforms.update { current ->
            val cleanPlatform = platform.lowercase()
            if (current.contains(cleanPlatform)) {
                current - cleanPlatform
            } else {
                current + cleanPlatform
            }
        }
    }

    fun setOnline(value: Boolean?) {
        _isOnline.value = value
    }

    fun setMinPrizeValue(value: Double) {
        _minPrizeValue.value = value
    }

    fun setShowBookmarksOnly(value: Boolean) {
        _showBookmarksOnly.value = value
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _vietnamOnly.value = true
        _prizeType.value = "all"
        _selectedPlatforms.value = setOf("devpost", "devfolio", "hackerearth", "gitcoin", "dorahacks", "bewater")
        _isOnline.value = null
        _minPrizeValue.value = 0.0
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            
            // Sync with backend API
            val result = repository.fetchHackathons(
                isVietnamEligible = if (_vietnamOnly.value) true else null,
                isOnline = _isOnline.value,
                prizeType = if (_prizeType.value == "all") null else _prizeType.value,
                minPrizeValue = if (_minPrizeValue.value > 0.0) _minPrizeValue.value else null,
                platforms = _selectedPlatforms.value.toList()
            )
            
            result.onFailure {
                _errorMessage.value = "Không thể kết nối máy chủ. Đang hiển thị dữ liệu ngoại tuyến."
            }
            _isRefreshing.value = false
        }
    }

    fun toggleBookmark(id: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(id, isBookmarked)
        }
    }

    fun reportHackathon(id: Int) {
        viewModelScope.launch {
            val result = repository.reportHackathon(id)
            result.onFailure {
                _errorMessage.value = "Báo cáo lỗi, đã lưu trạng thái cục bộ."
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
