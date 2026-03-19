package com.dush1729.cfseeker.data.remote.firestore

interface FirestoreService {
    suspend fun registerUser(handle: String)
    suspend fun unregisterUser(handle: String)
}
