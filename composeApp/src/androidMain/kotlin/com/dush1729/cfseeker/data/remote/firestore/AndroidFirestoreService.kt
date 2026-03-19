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
}
