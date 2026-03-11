package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestDao {

    @Transaction
    suspend fun insertAllContests(contests: List<ContestEntity>) {
        deleteAllContests()
        upsertContests(contests)
    }

    @Query("DELETE FROM contest")
    suspend fun deleteAllContests()

    @Query("SELECT * FROM contest ORDER BY startTimeSeconds DESC")
    fun getAllContests(): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contest WHERE phase = 'BEFORE' ORDER BY startTimeSeconds ASC")
    fun getUpcomingContests(): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contest WHERE phase = 'FINISHED' AND name LIKE '%' || :searchQuery || '%' ORDER BY startTimeSeconds DESC")
    fun getPastContests(searchQuery: String = ""): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contest WHERE phase IN ('CODING', 'PENDING_SYSTEM_TEST', 'SYSTEM_TEST') ORDER BY startTimeSeconds ASC")
    fun getOngoingContests(): Flow<List<ContestEntity>>

    @Upsert
    suspend fun upsertContests(contests: List<ContestEntity>)
}
