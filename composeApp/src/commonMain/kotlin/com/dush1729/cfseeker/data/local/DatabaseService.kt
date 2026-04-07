package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.dao.HandleRating
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.ProblemEntity
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.SolvedProblemEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.ui.SortOption
import kotlin.math.round
import kotlinx.coroutines.flow.Flow

interface DatabaseService {
    suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>)
    suspend fun upsertUsers(users: List<UserEntity>)
    suspend fun deleteUser(handle: String)
    fun getUsersWithLatestRatingChange(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserWithLatestRatingChangeView>>
    suspend fun getAllUserHandles(): List<String>
    fun getUserCount(): Flow<Int>
    fun getUserByHandle(handle: String): Flow<UserEntity>
    fun getRatingChangesByHandle(handle: String, searchQuery: String = ""): Flow<List<RatingChangeEntity>>
    fun getOutdatedUserHandles(): Flow<List<String>>

    // Contest methods
    suspend fun addAllContests(contests: List<ContestEntity>)
    fun getAllContests(): Flow<List<ContestEntity>>
    fun getUpcomingContests(): Flow<List<ContestEntity>>
    fun getPastContests(searchQuery: String = ""): Flow<List<ContestEntity>>
    fun getOngoingContests(): Flow<List<ContestEntity>>

    // Contest standings methods
    suspend fun insertContestStandings(
        contestId: Int,
        problems: List<ContestProblemEntity>,
        standings: List<ContestStandingRowEntity>
    )
    fun getContestProblems(contestId: Int): Flow<List<ContestProblemEntity>>
    fun getContestStandings(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<ContestStandingRowEntity>>

    // Contest rating changes methods
    suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>)
    fun getRatingChangesByContest(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<RatingChangeEntity>>

    // Contest cache info methods
    suspend fun getContestCacheInfo(): ContestCacheInfo
    suspend fun clearContestCache(): List<Int>

    // Problemset methods
    suspend fun insertAllProblems(problems: List<ProblemEntity>)
    fun getProblems(
        minRating: Int? = null,
        maxRating: Int? = null,
        searchQuery: String = "",
        hideSolved: Boolean = true,
        handle: String = "",
    ): Flow<List<ProblemEntity>>
    suspend fun getProblemCount(): Int
    fun getAllTags(): Flow<List<String>>

    // Solved problem methods
    suspend fun insertAllSolvedProblems(handle: String, solvedProblems: List<SolvedProblemEntity>)
    fun getSolvedCountForHandle(handle: String): Flow<Int>

    // Rated user methods
    suspend fun replaceAllRatedUsers(users: List<RatedUserEntity>)
    suspend fun deleteAllRatedUsers()
    suspend fun upsertRatedUsers(users: List<RatedUserEntity>)
    fun searchRatedUsers(query: String, limit: Int = 50): Flow<List<RatedUserEntity>>
    fun searchRatedUsers(query: String, sortBy: String, limit: Int = 100): Flow<List<RatedUserEntity>>
    fun searchRatedUsersFiltered(
        query: String,
        sortBy: String,
        country: String,
        city: String,
        organization: String,
        limit: Int
    ): Flow<List<RatedUserEntity>>
    fun getDistinctCountries(): Flow<List<String>>
    fun getDistinctCities(): Flow<List<String>>
    fun getDistinctOrganizations(): Flow<List<String>>
    suspend fun getRatingsForContest(contestId: Int): List<HandleRating>
    suspend fun getRatedUserCount(): Int
}

data class ContestCacheInfo(
    val problemCount: Int,
    val standingCount: Int,
    val ratingChangeCount: Int
) {
    // Estimated average bytes per row
    private val problemAvgBytes = 150
    private val standingAvgBytes = 300
    private val ratingChangeAvgBytes = 120

    val problemSizeBytes: Long get() = problemCount.toLong() * problemAvgBytes
    val standingSizeBytes: Long get() = standingCount.toLong() * standingAvgBytes
    val ratingChangeSizeBytes: Long get() = ratingChangeCount.toLong() * ratingChangeAvgBytes
    val totalSizeBytes: Long get() = problemSizeBytes + standingSizeBytes + ratingChangeSizeBytes

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> {
                val kb = round(bytes / 1024.0 * 10) / 10.0
                "${formatDecimal(kb)} KB"
            }
            else -> {
                val mb = round(bytes / (1024.0 * 1024.0) * 10) / 10.0
                "${formatDecimal(mb)} MB"
            }
        }
    }

    private fun formatDecimal(value: Double): String {
        val long = value.toLong()
        val decimal = ((value - long) * 10 + 0.5).toLong()
        return "$long.$decimal"
    }
}
