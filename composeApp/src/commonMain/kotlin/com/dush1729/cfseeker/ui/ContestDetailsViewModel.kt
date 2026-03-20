package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.repository.ContestStandingsRepository
import com.dush1729.cfseeker.data.repository.RatedUserRepository
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.platform.ioDispatcher
import com.dush1729.cfseeker.utils.ContestantForPrediction
import com.dush1729.cfseeker.utils.RatingPredictor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ContestDetailsViewModel(
    private val repository: ContestStandingsRepository,
    private val ratedUserRepository: RatedUserRepository,
    private val crashlyticsService: CrashlyticsService,
    private val appPreferences: AppPreferences,
    private val remoteConfigService: RemoteConfigService
) : ViewModel() {

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showLocalUsersOnly = MutableStateFlow(true)
    val showLocalUsersOnly = _showLocalUsersOnly.asStateFlow()

    private val _predictedDeltas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val predictedDeltas = _predictedDeltas.asStateFlow()

    fun getContestProblems(contestId: Int): Flow<List<ContestProblemEntity>> {
        return repository.getContestProblems(contestId)
            .flowOn(ioDispatcher)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun getContestStandings(contestId: Int): Flow<List<ContestStandingRowEntity>> {
        return combine(_searchQuery, _showLocalUsersOnly) { query, localOnly ->
            Pair(query, localOnly)
        }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { (query, localOnly) ->
                repository.getContestStandings(contestId, query, localOnly)
            }.flowOn(ioDispatcher)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun getContestRatingChanges(contestId: Int): Flow<List<RatingChangeEntity>> {
        return combine(_searchQuery, _showLocalUsersOnly) { query, localOnly ->
            Pair(query, localOnly)
        }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { (query, localOnly) ->
                repository.getContestRatingChanges(contestId, query, localOnly)
            }.flowOn(ioDispatcher)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowLocalUsersOnly(show: Boolean) {
        _showLocalUsersOnly.value = show
    }

    fun loadLastSyncTime(contestId: Int) {
        viewModelScope.launch {
            val syncTime = appPreferences.getContestStandingsLastSyncTime(contestId)
            _lastSyncTime.value = if (syncTime > 0) syncTime else null
        }
    }

    fun getRefreshIntervalMinutes(): Long {
        return remoteConfigService.getContestStandingsRefreshIntervalMinutes()
    }

    fun fetchContestStandings(contestId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true

                if (!forceRefresh) {
                    val lastSyncTime = appPreferences.getContestStandingsLastSyncTime(contestId)
                    val currentTime = Clock.System.now().epochSeconds
                    val refreshIntervalMinutes = remoteConfigService.getContestStandingsRefreshIntervalMinutes()
                    val refreshIntervalSeconds = refreshIntervalMinutes * 60

                    if (lastSyncTime > 0 && (currentTime - lastSyncTime) < refreshIntervalSeconds) {
                        computePredictedDeltas(contestId)
                        _isRefreshing.value = false
                        return@launch
                    }
                }

                repository.fetchContestStandings(contestId)
                try {
                    repository.fetchContestRatingChanges(contestId)
                } catch (_: Exception) {}
                computePredictedDeltas(contestId)
                val currentTime = Clock.System.now().epochSeconds
                appPreferences.setContestStandingsLastSyncTime(contestId, currentTime)
                _lastSyncTime.value = currentTime
                _snackbarMessage.emit("Contest data refreshed successfully")
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.setCustomKey("operation", "fetch_contest_standings")
                crashlyticsService.setCustomKey("contestId", contestId.toString())
                _snackbarMessage.emit("Failed to fetch standings: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun computePredictedDeltas(contestId: Int) {
        try {
            // Ensure rated user cache is fresh
            ratedUserRepository.ensureCacheFresh()

            val allStandings = repository.getContestStandings(contestId, "", false).first()

            // Get ratings from cached rated user list (works for ongoing + past contests)
            val ratingMap = ratedUserRepository.getRatingsForContest(contestId)

            val contestants = allStandings
                .filter { it.participantType == "CONTESTANT" && !it.memberHandles.contains(",") }
                .map { standing ->
                    ContestantForPrediction(
                        handle = standing.memberHandles,
                        rank = standing.rank,
                        rating = ratingMap[standing.memberHandles] ?: 1500
                    )
                }

            _predictedDeltas.value = RatingPredictor.predict(contestants)
        } catch (_: Exception) {
            // Predicted deltas are optional — silently ignore errors
        }
    }
}
