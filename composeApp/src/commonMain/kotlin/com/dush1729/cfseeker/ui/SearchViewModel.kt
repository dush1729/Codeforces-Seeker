package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.repository.RatedUserRepository
import com.dush1729.cfseeker.platform.ioDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val ratedUserRepository: RatedUserRepository,
    private val crashlyticsService: CrashlyticsService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SearchSortOption.RATING)
    val sortOption = _sortOption.asStateFlow()

    private val _isCacheLoading = MutableStateFlow(false)
    val isCacheLoading = _isCacheLoading.asStateFlow()

    private val _cachedUserCount = MutableStateFlow(0)
    val cachedUserCount = _cachedUserCount.asStateFlow()

    private val _displayLimit = MutableStateFlow(PAGE_SIZE)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<RatedUserEntity>> =
        combine(_searchQuery, _sortOption, _displayLimit) { query, sort, limit ->
            Triple(query, sort, limit)
        }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { (query, sort, limit) ->
                ratedUserRepository.searchByHandle(query.trim(), sort.value, limit)
            }
            .flowOn(ioDispatcher)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            try {
                _isCacheLoading.value = true
                ratedUserRepository.ensureCacheFresh()
                _cachedUserCount.value = ratedUserRepository.getRatedUserCount()
            } catch (e: Exception) {
                crashlyticsService.logException(e)
            } finally {
                _isCacheLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _displayLimit.value = PAGE_SIZE
    }

    fun setSortOption(option: SearchSortOption) {
        _sortOption.value = option
        _displayLimit.value = PAGE_SIZE
    }

    fun loadMore() {
        _displayLimit.value += PAGE_SIZE
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}
