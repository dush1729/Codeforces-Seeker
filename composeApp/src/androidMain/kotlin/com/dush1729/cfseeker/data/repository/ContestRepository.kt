package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.ContestCacheInfo
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.model.Contest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContestRepository(
    private val api: CodeforcesApi,
    private val db: DatabaseService,
    private val preferences: AppPreferences
) {
    suspend fun fetchContests(): Unit = withContext(Dispatchers.IO) {
        val apiContests: List<Contest> = safeApiCall {
            api.getContests(gym = false)
        }.result ?: emptyList()

        db.addAllContests(apiContests.toContestEntity())
        preferences.setContestLastSyncTime(System.currentTimeMillis() / 1000)
    }

    fun getUpcomingContests(): Flow<List<ContestEntity>> {
        return db.getUpcomingContests()
    }

    fun getPastContests(searchQuery: String = ""): Flow<List<ContestEntity>> {
        return db.getPastContests(searchQuery)
    }

    fun getOngoingContests(): Flow<List<ContestEntity>> {
        return db.getOngoingContests()
    }

    suspend fun getLastSyncTime(): Long? {
        val timestamp = preferences.getContestLastSyncTime()
        return if (timestamp > 0) timestamp else null
    }

    suspend fun getCacheInfo(): ContestCacheInfo = withContext(Dispatchers.IO) {
        db.getContestCacheInfo()
    }

    suspend fun clearCache(): Unit = withContext(Dispatchers.IO) {
        val contestIds = db.clearContestCache()
        preferences.clearContestPreferences(contestIds)
    }
}

fun List<Contest>.toContestEntity(): List<ContestEntity> = map { contest ->
    ContestEntity(
        id = contest.id,
        name = contest.name,
        type = contest.type,
        phase = contest.phase,
        frozen = contest.frozen,
        durationSeconds = contest.durationSeconds,
        startTimeSeconds = contest.startTimeSeconds,
        relativeTimeSeconds = contest.relativeTimeSeconds
    )
}
