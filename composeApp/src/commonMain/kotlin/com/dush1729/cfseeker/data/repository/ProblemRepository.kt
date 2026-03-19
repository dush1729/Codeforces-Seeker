package com.dush1729.cfseeker.data.repository

import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.entity.ProblemEntity
import com.dush1729.cfseeker.data.local.entity.SolvedProblemEntity
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.model.Problem
import com.dush1729.cfseeker.data.remote.model.ProblemStatistics
import com.dush1729.cfseeker.platform.ioDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProblemRepository(
    private val api: CodeforcesApi,
    private val db: DatabaseService
) {
    suspend fun fetchProblems(): Unit = withContext(ioDispatcher) {
        val response = safeApiCall {
            api.getProblemsetProblems()
        }.result ?: return@withContext

        val statisticsMap = response.problemStatistics.associateBy { it.contestId to it.index }

        val entities = response.problems
            .filter { it.contestId != null }
            .map { problem ->
                val stats = statisticsMap[problem.contestId to problem.index]
                problem.toProblemEntity(solvedCount = stats?.solvedCount ?: 0)
            }

        db.insertAllProblems(entities)
    }

    fun getProblems(
        minRating: Int? = null,
        maxRating: Int? = null,
        searchQuery: String = "",
        hideSolved: Boolean = true,
        handle: String = "",
    ): Flow<List<ProblemEntity>> {
        return db.getProblems(minRating, maxRating, searchQuery, hideSolved, handle)
    }

    suspend fun getProblemCount(): Int {
        return db.getProblemCount()
    }

    fun getAllTags(): Flow<List<String>> {
        return db.getAllTags()
    }

    suspend fun fetchSolvedProblems(handle: String): Unit = withContext(ioDispatcher) {
        val response = safeApiCall {
            api.getProblemsetProblems()
        }.result ?: return@withContext

        // We need user.status API to get solved problems, but for now
        // we can track solved problems from the problemset
        // This is a placeholder - solved problems are typically fetched via user.status
    }

    suspend fun insertSolvedProblems(handle: String, solvedProblems: List<SolvedProblemEntity>) {
        withContext(ioDispatcher) {
            db.insertAllSolvedProblems(handle, solvedProblems)
        }
    }

    fun getSolvedCountForHandle(handle: String): Flow<Int> {
        return db.getSolvedCountForHandle(handle)
    }
}

fun Problem.toProblemEntity(solvedCount: Int = 0): ProblemEntity = ProblemEntity(
    contestId = contestId!!,
    problemsetName = problemsetName,
    index = index,
    name = name,
    type = type,
    points = points,
    rating = rating,
    tags = tags.joinToString(";"),
    solvedCount = solvedCount,
)
