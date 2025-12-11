package com.dush1729.cfseeker.data.local

import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import kotlinx.coroutines.flow.Flow

interface DatabaseService {
    suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>)
    suspend fun deleteUser(handle: String)
    fun getAllUserRatingChanges(): Flow<List<UserRatingChanges>>
}