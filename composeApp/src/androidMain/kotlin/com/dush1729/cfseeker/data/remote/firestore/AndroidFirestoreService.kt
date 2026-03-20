package com.dush1729.cfseeker.data.remote.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidFirestoreService(
    private val firestore: FirebaseFirestore
) : FirestoreService {

    override suspend fun registerUser(handle: String) {
        firestore.collection("users")
            .document(handle.lowercase())
            .set(mapOf(
                "handle" to handle,
                "registeredAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            ))
            .await()
    }

    override suspend fun unregisterUser(handle: String) {
        firestore.collection("users")
            .document(handle.lowercase())
            .delete()
            .await()
    }

    override suspend fun getDailyProblems(date: String): List<DailyProblem> {
        val doc = firestore.collection("dailyProblems")
            .document(date)
            .get()
            .await()

        if (!doc.exists()) return emptyList()

        val problems = doc.get("problems") as? Map<*, *> ?: return emptyList()
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
        val problemsDeferred = getDailyProblems(date)

        val leaderboardDoc = firestore.collection("dailyLeaderboard")
            .document(date)
            .get()
            .await()

        if (!leaderboardDoc.exists()) {
            return DailyData(
                problems = problemsDeferred,
                leaderboard = emptyList(),
                submissions = emptyList()
            )
        }

        val scores = leaderboardDoc.get("scores") as? Map<*, *> ?: emptyMap<String, Any>()
        val leaderboard = scores.map { (handle, score) ->
            DailyLeaderboardEntry(
                handle = handle as String,
                score = (score as? Number)?.toInt() ?: 0
            )
        }.sortedByDescending { it.score }

        val rawSubmissions = leaderboardDoc.get("submissions") as? List<*> ?: emptyList<Any>()
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

        val updatedAt = leaderboardDoc.getTimestamp("updatedAt")?.toDate()?.time

        return DailyData(
            problems = problemsDeferred,
            leaderboard = leaderboard,
            submissions = submissions,
            leaderboardUpdatedAt = updatedAt
        )
    }
}
