package com.dush1729.cfseeker.data.remote.firestore

class IosFirestoreService : FirestoreService {
    override suspend fun registerUser(handle: String) {
        // TODO: Implement via Swift Firebase SDK bridge
    }

    override suspend fun unregisterUser(handle: String) {
        // TODO: Implement via Swift Firebase SDK bridge
    }

    override suspend fun getDailyProblems(date: String): List<DailyProblem> {
        // TODO: Implement via Swift Firebase SDK bridge
        return emptyList()
    }

    override suspend fun getDailyLeaderboard(date: String): DailyData {
        // TODO: Implement via Swift Firebase SDK bridge
        return DailyData(emptyList(), emptyList(), emptyList())
    }
}
