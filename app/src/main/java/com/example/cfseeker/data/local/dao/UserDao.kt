package com.example.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Upsert
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM user WHERE handle = :handle")
    suspend fun deleteUser(handle: String)

    @Upsert
    suspend fun insertRatingChanges(ratingChanges: List<RatingChangeEntity>)

    @Query("DELETE FROM rating_change WHERE handle = :handle")
    suspend fun deleteRatingChanges(handle: String)

    @Transaction
    suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        insertUser(user)
        insertRatingChanges(ratingChanges)
    }

    @Transaction
    suspend fun deleteUserAndRatingChanges(handle: String) {
        deleteUser(handle)
        deleteRatingChanges(handle)
    }

    @Query("SELECT * FROM user LEFT JOIN rating_change ON user.handle = rating_change.handle")
    suspend fun getAllUserRatingChanges(): Map<UserEntity, List<RatingChangeEntity>>
}