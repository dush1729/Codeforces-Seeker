package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.model.Problem
import com.dush1729.cfseeker.data.remote.model.ProblemResult
import com.dush1729.cfseeker.data.remote.model.RanklistRow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContestStandingsRepository(
    private val api: CodeforcesApi,
    private val db: DatabaseService
) {
    suspend fun fetchContestStandings(contestId: Int): Unit = withContext(Dispatchers.IO) {
        val response = safeApiCall {
            api.getContestStandings(contestId = contestId)
        }.result ?: return@withContext

        val problems = response.problems.map { it.toEntity(contestId) }
        val standings = response.rows.map { it.toEntity(contestId) }

        db.insertContestStandings(contestId, problems, standings)
    }

    fun getContestProblems(contestId: Int): Flow<List<ContestProblemEntity>> {
        return db.getContestProblems(contestId)
    }

    fun getContestStandings(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<ContestStandingRowEntity>> {
        return db.getContestStandings(contestId, searchQuery, showLocalUsersOnly)
    }

    suspend fun fetchContestRatingChanges(contestId: Int): Unit = withContext(Dispatchers.IO) {
        val ratingChanges = safeApiCall {
            api.getContestRatingChanges(contestId)
        }.result ?: return@withContext

        db.insertRatingChangesIgnoreConflict(ratingChanges.toRatingChangeEntity(source = "CONTEST"))
    }

    fun getContestRatingChanges(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<RatingChangeEntity>> {
        return db.getRatingChangesByContest(contestId, searchQuery, showLocalUsersOnly)
    }
}

fun Problem.toEntity(contestId: Int): ContestProblemEntity = ContestProblemEntity(
    contestId = contestId,
    problemsetName = this.problemsetName,
    index = this.index,
    name = this.name,
    type = this.type,
    points = this.points,
    rating = this.rating,
    tags = this.tags.joinToString(",")
)

fun RanklistRow.toEntity(contestId: Int): ContestStandingRowEntity = ContestStandingRowEntity(
    contestId = contestId,
    rank = this.rank,
    points = this.points,
    penalty = this.penalty,
    successfulHackCount = this.successfulHackCount,
    unsuccessfulHackCount = this.unsuccessfulHackCount,
    lastSubmissionTimeSeconds = this.lastSubmissionTimeSeconds,
    participantType = this.party.participantType,
    teamId = this.party.teamId,
    teamName = this.party.teamName,
    ghost = this.party.ghost,
    room = this.party.room,
    memberHandles = this.party.members.joinToString(",") { it.handle },
    problemResults = Json.encodeToString(this.problemResults)
)
