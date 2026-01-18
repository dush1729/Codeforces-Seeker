package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.ui.SortOption
import kotlinx.coroutines.flow.Flow

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
}