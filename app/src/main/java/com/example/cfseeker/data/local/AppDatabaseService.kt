package com.example.cfseeker.data.local

import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity
import javax.inject.Inject

class AppDatabaseService @Inject constructor(private val appDatabase: AppDatabase): DatabaseService {
    override suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        appDatabase.userDao().addUser(user, ratingChanges)
    }

    override suspend fun deleteUser(handle: String) {
        appDatabase.userDao().deleteUserAndRatingChanges(handle)
    }
}