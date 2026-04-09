package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.AppPreferences
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

data class SearchFilters(
    val country: String = "",
    val city: String = "",
    val organization: String = ""
) {
    val activeCount: Int get() = listOf(country, city, organization).count { it.isNotEmpty() }
    val hasFilters: Boolean get() = activeCount > 0
}

class SearchViewModel(
    private val ratedUserRepository: RatedUserRepository,
    private val crashlyticsService: CrashlyticsService,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SearchSortOption.RATING)
    val sortOption = _sortOption.asStateFlow()

    private val _filters = MutableStateFlow(SearchFilters())
    val filters = _filters.asStateFlow()

    private val _activeOnly = MutableStateFlow(false)
    val activeOnly = _activeOnly.asStateFlow()

    private val _includeRetired = MutableStateFlow(false)
    val includeRetired = _includeRetired.asStateFlow()

    private val _isCacheLoading = MutableStateFlow(false)
    val isCacheLoading = _isCacheLoading.asStateFlow()

    private val _cachedUserCount = MutableStateFlow(0)
    val cachedUserCount = _cachedUserCount.asStateFlow()

    private val _storageBytes = MutableStateFlow(0L)
    val storageBytes = _storageBytes.asStateFlow()

    private val _displayLimit = MutableStateFlow(PAGE_SIZE)

    val countries: StateFlow<List<String>> = ratedUserRepository.getDistinctCountries()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cities: StateFlow<List<String>> = ratedUserRepository.getDistinctCities()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val organizations: StateFlow<List<String>> = ratedUserRepository.getDistinctOrganizations()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<RatedUserEntity>> =
        combine(_searchQuery, _sortOption, _displayLimit, _filters) { query, sort, limit, filters ->
            SearchParams(query, sort, limit, filters)
        }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { params ->
                if (params.filters.hasFilters) {
                    ratedUserRepository.searchFiltered(
                        query = params.query.trim(),
                        sortBy = params.sort.value,
                        country = params.filters.country,
                        city = params.filters.city,
                        organization = params.filters.organization,
                        limit = params.limit
                    )
                } else {
                    ratedUserRepository.searchByHandle(params.query.trim(), params.sort.value, params.limit)
                }
            }
            .flowOn(ioDispatcher)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            _activeOnly.value = appPreferences.getRatedUserActiveOnly()
            _includeRetired.value = appPreferences.getRatedUserIncludeRetired()
        }
        refreshCacheInfo()
    }

    fun toggleActiveOnly() {
        _activeOnly.value = !_activeOnly.value
        viewModelScope.launch { appPreferences.setRatedUserActiveOnly(_activeOnly.value) }
    }

    fun toggleIncludeRetired() {
        _includeRetired.value = !_includeRetired.value
        viewModelScope.launch { appPreferences.setRatedUserIncludeRetired(_includeRetired.value) }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _isCacheLoading.value = true
                ratedUserRepository.fetchAndCacheRatedUsers(
                    activeOnly = _activeOnly.value,
                    includeRetired = _includeRetired.value
                )
                refreshCacheInfo()
            } catch (e: Exception) {
                crashlyticsService.logException(e)
            } finally {
                _isCacheLoading.value = false
            }
        }
    }

    fun clearUsers() {
        viewModelScope.launch {
            try {
                ratedUserRepository.clearRatedUsers()
                refreshCacheInfo()
            } catch (e: Exception) {
                crashlyticsService.logException(e)
            }
        }
    }

    private fun refreshCacheInfo() {
        viewModelScope.launch {
            _cachedUserCount.value = ratedUserRepository.getRatedUserCount()
            _storageBytes.value = ratedUserRepository.getStorageBytes()
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

    fun setFilters(filters: SearchFilters) {
        _filters.value = filters
        _displayLimit.value = PAGE_SIZE
    }

    fun clearFilters() {
        _filters.value = SearchFilters()
        _displayLimit.value = PAGE_SIZE
    }

    fun loadMore() {
        _displayLimit.value += PAGE_SIZE
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}

private data class SearchParams(
    val query: String,
    val sort: SearchSortOption,
    val limit: Int,
    val filters: SearchFilters
)
