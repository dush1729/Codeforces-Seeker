package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppDatabaseService @Inject constructor(private val appDatabase: AppDatabase): DatabaseService {
    override suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        appDatabase.userDao().addUser(user, ratingChanges)
    }

    override suspend fun deleteUser(handle: String) {
        appDatabase.userDao().deleteUserAndRatingChanges(handle)
    }

    override fun getAllUserRatingChanges(sortBy: String, searchQuery: String): Flow<List<UserRatingChanges>> {
        return appDatabase.userDao().getAllUserRatingChanges(sortBy, searchQuery)
    }

    override suspend fun getAllUserHandles(): List<String> {
        return appDatabase.userDao().getAllUserHandles()
    }
}