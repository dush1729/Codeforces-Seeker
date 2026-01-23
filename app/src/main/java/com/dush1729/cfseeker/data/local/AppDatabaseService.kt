package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppDatabaseService @Inject constructor(private val appDatabase: AppDatabase): DatabaseService {
    override suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        appDatabase.userDao().addUser(user, ratingChanges)
    }

    override suspend fun upsertUsers(users: List<UserEntity>) {
        appDatabase.userDao().upsertUsers(users)
    }

    override suspend fun deleteUser(handle: String) {
        appDatabase.userDao().deleteUserAndRatingChanges(handle)
    }

    override fun getAllUserRatingChanges(sortBy: String, searchQuery: String): Flow<List<UserRatingChanges>> {
        return appDatabase.userDao().getAllUserRatingChanges(sortBy, searchQuery)
    }

    override suspend fun getAllUserHandles(): List<String> {
        return appDatabase.userDao().getAllUserHandles()
    }

    override fun getUserCount(): Flow<Int> {
        return appDatabase.userDao().getUserCount()
    }

    override fun getUserByHandle(handle: String): Flow<UserEntity> {
        return appDatabase.userDao().getUserByHandle(handle)
    }

    override fun getRatingChangesByHandle(handle: String, searchQuery: String): Flow<List<RatingChangeEntity>> {
        return appDatabase.userDao().getRatingChangesByHandle(handle, searchQuery)
    }

    override suspend fun addAllContests(contests: List<ContestEntity>) {
        appDatabase.contestDao().insertAllContests(contests)
    }

    override fun getAllContests(): Flow<List<ContestEntity>> {
        return appDatabase.contestDao().getAllContests()
    }

    override fun getUpcomingContests(): Flow<List<ContestEntity>> {
        return appDatabase.contestDao().getUpcomingContests()
    }

    override fun getPastContests(searchQuery: String): Flow<List<ContestEntity>> {
        return appDatabase.contestDao().getPastContests(searchQuery)
    }

    override fun getOngoingContests(): Flow<List<ContestEntity>> {
        return appDatabase.contestDao().getOngoingContests()
    }

    override suspend fun insertContestStandings(
        contestId: Int,
        problems: List<ContestProblemEntity>,
        standings: List<ContestStandingRowEntity>
    ) {
        appDatabase.contestStandingsDao().insertContestStandings(contestId, problems, standings)
    }

    override fun getContestProblems(contestId: Int): Flow<List<ContestProblemEntity>> {
        return appDatabase.contestStandingsDao().getContestProblems(contestId)
    }

    override fun getContestStandings(contestId: Int, searchQuery: String): Flow<List<ContestStandingRowEntity>> {
        return appDatabase.contestStandingsDao().getContestStandings(contestId, searchQuery)
    }

    override suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>) {
        appDatabase.contestStandingsDao().insertRatingChangesIgnoreConflict(ratingChanges)
    }

    override fun getRatingChangesByContest(contestId: Int, searchQuery: String): Flow<List<RatingChangeEntity>> {
        return appDatabase.contestStandingsDao().getRatingChangesByContest(contestId, searchQuery)
    }

    override suspend fun getContestCacheInfo(): ContestCacheInfo {
        return ContestCacheInfo(
            problemCount = appDatabase.contestStandingsDao().getContestProblemCount(),
            standingCount = appDatabase.contestStandingsDao().getContestStandingCount(),
            ratingChangeCount = appDatabase.contestStandingsDao().getContestRatingChangeCount()
        )
    }

    override suspend fun clearContestCache() {
        appDatabase.contestStandingsDao().clearContestCache()
    }
}