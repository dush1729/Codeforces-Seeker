package com.dush1729.cfseeker.data.remote.firestore

import com.dush1729.cfseeker.bridge.FirestoreBridge
import com.dush1729.cfseeker.bridge.FirestoreCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosFirestoreService(
    private val bridge: FirestoreBridge
) : FirestoreService {

    private suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        return suspendCancellableCoroutine { continuation ->
            bridge.getDocument(collection, documentId, object : FirestoreCallback {
                override fun onSuccess(data: Map<String, Any>?) {
                    if (continuation.isActive) continuation.resume(data)
                }
                override fun onFailure(error: String) {
                    if (continuation.isActive) continuation.resumeWithException(Exception(error))
                }
            })
        }
    }

    override suspend fun registerUser(handle: String) {
        suspendCancellableCoroutine { continuation ->
            bridge.setDocument("users", handle.lowercase(), mapOf(
                "handle" to handle,
                "registeredAt" to "SERVER_TIMESTAMP"
            ), object : FirestoreCallback {
                override fun onSuccess(data: Map<String, Any>?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                override fun onFailure(error: String) {
                    if (continuation.isActive) continuation.resumeWithException(Exception(error))
                }
            })
        }
    }

    override suspend fun unregisterUser(handle: String) {
        suspendCancellableCoroutine { continuation ->
            bridge.deleteDocument("users", handle.lowercase(), object : FirestoreCallback {
                override fun onSuccess(data: Map<String, Any>?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                override fun onFailure(error: String) {
                    if (continuation.isActive) continuation.resumeWithException(Exception(error))
                }
            })
        }
    }

    override suspend fun getDailyProblems(date: String): List<DailyProblem> {
        val data = getDocument("dailyProblems", date) ?: return emptyList()

        val problems = data["problems"] as? Map<*, *> ?: return emptyList()
        return problems.values.mapNotNull { value ->
            val map = value as? Map<*, *> ?: return@mapNotNull null
            DailyProblem(
                contestId = (map["contestId"] as? Number)?.toInt() ?: return@mapNotNull null,
                index = map["index"] as? String ?: return@mapNotNull null,
                name = map["name"] as? String ?: return@mapNotNull null,
                rating = (map["rating"] as? Number)?.toInt() ?: return@mapNotNull null
            )
        }.sortedBy { it.rating }
    }

    override suspend fun getDailyLeaderboard(date: String): DailyData {
        val problems = getDailyProblems(date)

        val leaderboardData = try {
            getDocument("dailyLeaderboard", date)
        } catch (_: Exception) {
            null
        }

        if (leaderboardData == null) {
            return DailyData(
                problems = problems,
                leaderboard = emptyList(),
                submissions = emptyList()
            )
        }

        val scores = leaderboardData["scores"] as? Map<*, *> ?: emptyMap<String, Any>()
        val leaderboard = scores.map { (handle, score) ->
            DailyLeaderboardEntry(
                handle = handle as String,
                score = (score as? Number)?.toInt() ?: 0
            )
        }.sortedByDescending { it.score }

        val rawSubmissions = leaderboardData["submissions"] as? List<*> ?: emptyList<Any>()
        val submissions = rawSubmissions.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            DailySubmission(
                handle = map["handle"] as? String ?: return@mapNotNull null,
                contestId = (map["contestId"] as? Number)?.toInt() ?: return@mapNotNull null,
                index = map["index"] as? String ?: return@mapNotNull null,
                rating = (map["rating"] as? Number)?.toInt() ?: return@mapNotNull null,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: return@mapNotNull null
            )
        }

        val updatedAt = (leaderboardData["updatedAt"] as? Number)?.toLong()

        // Fetch cumulative leaderboard
        val (cumulativeLeaderboard, cumulativeUpdatedAt) = try {
            val cumulativeData = getDocument("cumulativeLeaderboard", "totals")
            if (cumulativeData != null) {
                val cumulativeScores = cumulativeData["scores"] as? Map<*, *> ?: emptyMap<String, Any>()
                val entries = cumulativeScores.map { (handle, score) ->
                    DailyLeaderboardEntry(
                        handle = handle as String,
                        score = (score as? Number)?.toInt() ?: 0
                    )
                }.sortedByDescending { it.score }
                entries to (cumulativeData["updatedAt"] as? Number)?.toLong()
            } else {
                emptyList<DailyLeaderboardEntry>() to null
            }
        } catch (_: Exception) {
            emptyList<DailyLeaderboardEntry>() to null
        }

        return DailyData(
            problems = problems,
            leaderboard = leaderboard,
            submissions = submissions,
            leaderboardUpdatedAt = updatedAt,
            cumulativeLeaderboard = cumulativeLeaderboard,
            cumulativeUpdatedAt = cumulativeUpdatedAt
        )
    }
}
