package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.ui.SortOption
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface DatabaseService {
    suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>)
    suspend fun upsertUsers(users: List<UserEntity>)
    suspend fun deleteUser(handle: String)
    fun getAllUserRatingChanges(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserRatingChanges>>
    suspend fun getAllUserHandles(): List<String>
    fun getUserCount(): Flow<Int>
    fun getUserByHandle(handle: String): Flow<UserEntity>
    fun getRatingChangesByHandle(handle: String, searchQuery: String = ""): Flow<List<RatingChangeEntity>>

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
    fun getContestStandings(contestId: Int, searchQuery: String = ""): Flow<List<ContestStandingRowEntity>>

    // Contest rating changes methods
    suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>)
    fun getRatingChangesByContest(contestId: Int, searchQuery: String = ""): Flow<List<RatingChangeEntity>>

    // Contest cache info methods
    suspend fun getContestCacheInfo(): ContestCacheInfo
    suspend fun clearContestCache()
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
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}