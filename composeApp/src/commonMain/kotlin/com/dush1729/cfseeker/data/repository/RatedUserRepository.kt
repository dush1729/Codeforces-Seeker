package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.dao.HandleRating
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.model.User
import com.dush1729.cfseeker.platform.ioDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class RatedUserRepository(
    private val api: CodeforcesApi,
    private val db: DatabaseService,
    private val appPreferences: AppPreferences
) {
    companion object {
        private const val CACHE_DURATION_HOURS = 6
    }

    suspend fun ensureCacheFresh() = withContext(ioDispatcher) {
        val lastSync = appPreferences.getRatedUserLastSyncTime()
        val now = Clock.System.now().epochSeconds
        val cacheAge = now - lastSync

        if (lastSync > 0 && cacheAge < CACHE_DURATION_HOURS * 3600) return@withContext

        fetchAndCacheRatedUsers()
    }

    suspend fun fetchAndCacheRatedUsers() = withContext(ioDispatcher) {
        val users = safeApiCall {
            api.getRatedList()
        }.result ?: return@withContext

        db.replaceAllRatedUsers(users.map { it.toRatedUserEntity() })
        appPreferences.setRatedUserLastSyncTime(Clock.System.now().epochSeconds)
    }

    suspend fun getRatingsForContest(contestId: Int): Map<String, Int> = withContext(ioDispatcher) {
        val ratings = db.getRatingsForContest(contestId)
        ratings.associate { it.handle to it.rating }
    }

    fun searchByHandle(query: String, limit: Int = 50): Flow<List<RatedUserEntity>> {
        return db.searchRatedUsers(query, limit)
    }

    suspend fun getRatedUserCount(): Int {
        return db.getRatedUserCount()
    }
}

fun User.toRatedUserEntity(): RatedUserEntity = RatedUserEntity(
    handle = this.handle,
    rating = this.rating ?: 0,
    maxRating = this.maxRating,
    rank = this.rank,
    maxRank = this.maxRank,
)
