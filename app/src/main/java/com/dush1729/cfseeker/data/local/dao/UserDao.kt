package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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
    suspend fun upsertRatingChanges(ratingChanges: List<RatingChangeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>)

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
    @Transaction
    @Query("""
        SELECT user.* FROM user
        LEFT JOIN rating_change ON user.handle = rating_change.handle AND rating_change.source = 'USER'
        WHERE user.handle LIKE '%' || :searchQuery || '%'
        GROUP BY user.handle
        ORDER BY
            CASE WHEN :sortBy = 'LAST_RATING_UPDATE' THEN MAX(rating_change.ratingUpdateTimeSeconds) END DESC,
            CASE WHEN :sortBy = 'RATING' THEN user.rating END DESC,
            CASE WHEN :sortBy = 'LAST_SYNC' THEN user.lastSync END DESC,
            CASE WHEN :sortBy = 'HANDLE' THEN user.handle COLLATE NOCASE END ASC,
            user.handle ASC
    """)
    fun getAllUserRatingChanges(
        sortBy: String = SortOption.LAST_RATING_UPDATE.value,
        searchQuery: String = ""
    ): Flow<List<UserRatingChanges>>

    @Query("SELECT handle FROM user")
    suspend fun getAllUserHandles(): List<String>

    @Query("SELECT COUNT(*) FROM user")
    fun getUserCount(): Flow<Int>

    @Query("SELECT * FROM user WHERE handle = :handle")
    fun getUserByHandle(handle: String): Flow<UserEntity>

    @Query("SELECT * FROM rating_change WHERE handle = :handle AND source = 'USER' AND contestName LIKE '%' || :searchQuery || '%' ORDER BY ratingUpdateTimeSeconds DESC")
    fun getRatingChangesByHandle(handle: String, searchQuery: String = ""): Flow<List<RatingChangeEntity>>

    @Query("SELECT * FROM rating_change WHERE contestId = :contestId AND (:searchQuery = '' OR handle LIKE '%' || :searchQuery || '%') ORDER BY contestRank ASC")
    fun getRatingChangesByContest(contestId: Int, searchQuery: String = ""): Flow<List<RatingChangeEntity>>
}