package com.dush1729.cfseeker.data.remote.firestore

data class DailyProblem(
    val contestId: Int,
    val index: String,
    val name: String,
    val rating: Int
)

data class DailySubmission(
    val handle: String,
    val contestId: Int,
    val index: String,
    val rating: Int,
    val timestamp: Long
)

data class DailyLeaderboardEntry(
    val handle: String,
    val score: Int
)

data class DailyData(
    val problems: List<DailyProblem>,
    val leaderboard: List<DailyLeaderboardEntry>,
    val submissions: List<DailySubmission>,
    val leaderboardUpdatedAt: Long? = null,
    val cumulativeLeaderboard: List<DailyLeaderboardEntry> = emptyList(),
    val cumulativeUpdatedAt: Long? = null
)

interface FirestoreService {
    suspend fun registerUser(handle: String)
    suspend fun unregisterUser(handle: String)
    suspend fun getDailyProblems(date: String): List<DailyProblem>
    suspend fun getDailyLeaderboard(date: String): DailyData
}
