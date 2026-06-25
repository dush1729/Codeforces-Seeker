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
import kotlinx.coroutines.flow.Flow

class AppDatabaseService(private val appDatabase: AppDatabase): DatabaseService {
    override suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        appDatabase.userDao().addUser(user, ratingChanges)
    }

    override suspend fun upsertUsers(users: List<UserEntity>) {
        appDatabase.userDao().upsertUsers(users)
    }

    override suspend fun deleteUser(handle: String) {
        appDatabase.userDao().deleteUserAndRatingChanges(handle)
    }

    override fun getUsersWithLatestRatingChange(sortBy: String, searchQuery: String): Flow<List<UserWithLatestRatingChangeView>> {
        return appDatabase.userDao().getUsersWithLatestRatingChange(sortBy, searchQuery)
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

    override fun getOutdatedUserHandles(): Flow<List<String>> {
        return appDatabase.userDao().getOutdatedUserHandles()
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

    override fun getContestStandings(
        contestId: Int,
        searchQuery: String,
        showLocalUsersOnly: Boolean
    ): Flow<List<ContestStandingRowEntity>> {
        return appDatabase.contestStandingsDao().getContestStandings(contestId, searchQuery, showLocalUsersOnly)
    }

    override suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>) {
        appDatabase.contestStandingsDao().insertRatingChangesIgnoreConflict(ratingChanges)
    }

    override fun getRatingChangesByContest(
        contestId: Int,
        searchQuery: String,
        showLocalUsersOnly: Boolean
    ): Flow<List<RatingChangeEntity>> {
        return appDatabase.contestStandingsDao().getRatingChangesByContest(contestId, searchQuery, showLocalUsersOnly)
    }

    override suspend fun getContestCacheInfo(): ContestCacheInfo {
        return ContestCacheInfo(
            problemCount = appDatabase.contestStandingsDao().getContestProblemCount(),
            standingCount = appDatabase.contestStandingsDao().getContestStandingCount(),
            ratingChangeCount = appDatabase.contestStandingsDao().getContestRatingChangeCount()
        )
    }

    override suspend fun clearContestCache(): List<Int> {
        return appDatabase.contestStandingsDao().clearContestCache()
    }

    // Problemset methods

    override suspend fun insertAllProblems(problems: List<ProblemEntity>) {
        appDatabase.problemDao().insertAllProblems(problems)
    }

    override fun getProblems(
        minRating: Int?,
        maxRating: Int?,
        searchQuery: String,
        hideSolved: Boolean,
        handle: String,
    ): Flow<List<ProblemEntity>> {
        return appDatabase.problemDao().getProblems(minRating, maxRating, searchQuery, hideSolved, handle)
    }

    override suspend fun getProblemCount(): Int {
        return appDatabase.problemDao().getProblemCount()
    }

    override fun getAllTags(): Flow<List<String>> {
        return appDatabase.problemDao().getAllTags()
    }

    // Solved problem methods

    override suspend fun insertAllSolvedProblems(handle: String, solvedProblems: List<SolvedProblemEntity>) {
        appDatabase.problemDao().insertAllSolvedProblems(handle, solvedProblems)
    }

    override fun getSolvedCountForHandle(handle: String): Flow<Int> {
        return appDatabase.problemDao().getSolvedCountForHandle(handle)
    }

    // Rated user methods

    override suspend fun replaceAllRatedUsers(users: List<RatedUserEntity>) {
        appDatabase.ratedUserDao().replaceAll(users)
    }

    override fun searchRatedUsers(query: String, limit: Int): Flow<List<RatedUserEntity>> {
        return appDatabase.ratedUserDao().searchByHandle(query, limit)
    }

    override suspend fun getRatingsForContest(contestId: Int): List<HandleRating> {
        return appDatabase.ratedUserDao().getRatingsForContest(contestId)
    }

    override suspend fun getRatedUserCount(): Int {
        return appDatabase.ratedUserDao().getCount()
    }
}
