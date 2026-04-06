package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatedUserDao {

    @Upsert
    suspend fun upsertAll(users: List<RatedUserEntity>)

    @Query("DELETE FROM rated_user")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(users: List<RatedUserEntity>) {
        deleteAll()
        upsertAll(users)
    }

    @Query("""
        SELECT * FROM rated_user
        WHERE handle LIKE '%' || :query || '%'
        ORDER BY rating DESC
        LIMIT :limit
    """)
    fun searchByHandle(query: String, limit: Int = 50): Flow<List<RatedUserEntity>>

    @Query("""
        SELECT * FROM rated_user
        WHERE handle LIKE '%' || :query || '%'
        ORDER BY handle ASC
        LIMIT :limit
    """)
    fun searchByHandleSortByHandle(query: String, limit: Int = 50): Flow<List<RatedUserEntity>>

    @Query("""
        SELECT * FROM rated_user
        WHERE handle LIKE '%' || :query || '%'
        ORDER BY maxRating DESC
        LIMIT :limit
    """)
    fun searchByHandleSortByMaxRating(query: String, limit: Int = 50): Flow<List<RatedUserEntity>>

    @Query("""
        SELECT ru.handle, ru.rating FROM rated_user ru
        INNER JOIN contest_standing_row csr
            ON ru.handle = csr.memberHandles
        WHERE csr.contestId = :contestId
            AND csr.participantType = 'CONTESTANT'
            AND csr.memberHandles NOT LIKE '%,%'
    """)
    suspend fun getRatingsForContest(contestId: Int): List<HandleRating>

    @Query("SELECT COUNT(*) FROM rated_user")
    suspend fun getCount(): Int
}

data class HandleRating(
    val handle: String,
    val rating: Int
)
