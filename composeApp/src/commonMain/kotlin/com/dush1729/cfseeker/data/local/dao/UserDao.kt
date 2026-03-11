package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.ui.SortOption
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun insertUser(user: UserEntity)

    @Upsert
    suspend fun upsertUsers(users: List<UserEntity>)

    @Query("DELETE FROM user WHERE handle = :handle")
    suspend fun deleteUser(handle: String)

    @Upsert
    suspend fun upsertRatingChanges(ratingChanges: List<RatingChangeEntity>)

    @Query("DELETE FROM rating_change WHERE handle = :handle")
    suspend fun deleteRatingChanges(handle: String)

    @Transaction
    suspend fun addUser(user: UserEntity, ratingChanges: List<RatingChangeEntity>) {
        insertUser(user)
        upsertRatingChanges(ratingChanges)
    }

    @Transaction
    suspend fun deleteUserAndRatingChanges(handle: String) {
        deleteUser(handle)
        deleteRatingChanges(handle)
    }

    @Query("""
        SELECT * FROM user_with_latest_rating_change
        WHERE handle LIKE '%' || :searchQuery || '%'
        ORDER BY
            CASE WHEN :sortBy = 'LAST_RATING_UPDATE' THEN latestRatingUpdateTimeSeconds END DESC,
            CASE WHEN :sortBy = 'RATING' THEN rating END DESC,
            CASE WHEN :sortBy = 'LAST_SYNC' THEN lastSync END DESC,
            CASE WHEN :sortBy = 'HANDLE' THEN handle COLLATE NOCASE END ASC,
            handle ASC
    """)
    fun getUsersWithLatestRatingChange(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserWithLatestRatingChangeView>>

    @Query("SELECT handle FROM user")
    suspend fun getAllUserHandles(): List<String>

    @Query("SELECT COUNT(*) FROM user")
    fun getUserCount(): Flow<Int>

    @Query("SELECT * FROM user WHERE handle = :handle")
    fun getUserByHandle(handle: String): Flow<UserEntity>

    @Query("SELECT * FROM rating_change WHERE handle = :handle AND source = 'USER' AND contestName LIKE '%' || :searchQuery || '%' ORDER BY ratingUpdateTimeSeconds DESC")
    fun getRatingChangesByHandle(handle: String, searchQuery: String = ""): Flow<List<RatingChangeEntity>>

    @Query("SELECT handle FROM user_with_latest_rating_change WHERE isRatingOutdated = 1")
    fun getOutdatedUserHandles(): Flow<List<String>>
}