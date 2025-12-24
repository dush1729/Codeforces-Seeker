package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.ui.SortOption
import kotlinx.coroutines.flow.Flow

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
    @Transaction
    @Query("""
        SELECT user.* FROM user
        LEFT JOIN rating_change ON user.handle = rating_change.handle
        WHERE LOWER(user.handle) LIKE '%' || LOWER(:searchQuery) || '%'
        GROUP BY user.handle
        ORDER BY
            CASE WHEN :sortBy = 'LAST_RATING_UPDATE' THEN MAX(rating_change.ratingUpdateTimeSeconds) END DESC,
            CASE WHEN :sortBy = 'RATING' THEN user.rating END DESC,
            CASE WHEN :sortBy = 'LAST_SYNC' THEN user.lastSync END DESC,
            CASE WHEN :sortBy = 'HANDLE' THEN LOWER(user.handle) END ASC,
            user.handle ASC
    """)
    fun getAllUserRatingChanges(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserRatingChanges>>

    @Query("SELECT handle FROM user")
    suspend fun getAllUserHandles(): List<String>
}