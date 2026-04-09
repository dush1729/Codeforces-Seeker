package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.dao.HandleRating
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.streamParseRatedUsers
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

    suspend fun fetchAndCacheRatedUsers(
        activeOnly: Boolean = false,
        includeRetired: Boolean = true
    ) = withContext(ioDispatcher) {
        db.deleteAllRatedUsers()
        val statement = api.getRatedListStreaming(activeOnly = activeOnly, includeRetired = includeRetired)
        streamParseRatedUsers(statement) { batch ->
            db.upsertRatedUsers(batch)
        }
        appPreferences.setRatedUserLastSyncTime(Clock.System.now().epochSeconds)
    }

    suspend fun clearRatedUsers() = withContext(ioDispatcher) {
        db.deleteAllRatedUsers()
        appPreferences.setRatedUserLastSyncTime(0)
    }

    suspend fun getStorageBytes(): Long = withContext(ioDispatcher) {
        db.getRatedUserStorageBytes()
    }

    suspend fun getRatingsForContest(contestId: Int): Map<String, Int> = withContext(ioDispatcher) {
        val ratings = db.getRatingsForContest(contestId)
        ratings.associate { it.handle to it.rating }
    }

    fun searchByHandle(query: String, limit: Int = 50): Flow<List<RatedUserEntity>> {
        return db.searchRatedUsers(query, limit)
    }

    fun searchByHandle(query: String, sortBy: String, limit: Int = 100): Flow<List<RatedUserEntity>> {
        return db.searchRatedUsers(query, sortBy, limit)
    }

    fun searchFiltered(
        query: String,
        sortBy: String,
        country: String,
        city: String,
        organization: String,
        limit: Int
    ): Flow<List<RatedUserEntity>> {
        return db.searchRatedUsersFiltered(query, sortBy, country, city, organization, limit)
    }

    fun getDistinctCountries(): Flow<List<String>> = db.getDistinctCountries()
    fun getDistinctCities(): Flow<List<String>> = db.getDistinctCities()
    fun getDistinctOrganizations(): Flow<List<String>> = db.getDistinctOrganizations()

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
    avatar = this.avatar,
    titlePhoto = this.titlePhoto,
    firstName = this.firstName,
    lastName = this.lastName,
    country = this.country,
    city = this.city,
    organization = this.organization,
    contribution = this.contribution,
    friendOfCount = this.friendOfCount,
    lastOnlineTimeSeconds = this.lastOnlineTimeSeconds,
    registrationTimeSeconds = this.registrationTimeSeconds,
)
