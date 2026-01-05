package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContestStandingsDao {

    @Transaction
    suspend fun insertContestStandings(
        contestId: Int,
        problems: List<ContestProblemEntity>,
        standings: List<ContestStandingRowEntity>
    ) {
        deleteContestProblems(contestId)
        deleteContestStandings(contestId)
        upsertProblems(problems)
        upsertStandings(standings)
    }

    @Upsert
    suspend fun upsertProblems(problems: List<ContestProblemEntity>)

    @Upsert
    suspend fun upsertStandings(standings: List<ContestStandingRowEntity>)

    @Query("DELETE FROM contest_problem WHERE contestId = :contestId")
    suspend fun deleteContestProblems(contestId: Int)

    @Query("DELETE FROM contest_standing_row WHERE contestId = :contestId")
    suspend fun deleteContestStandings(contestId: Int)

    @Query("SELECT * FROM contest_problem WHERE contestId = :contestId ORDER BY `index` ASC")
    fun getContestProblems(contestId: Int): Flow<List<ContestProblemEntity>>

    @Query("SELECT * FROM contest_standing_row WHERE contestId = :contestId AND (:searchQuery = '' OR memberHandles LIKE '%' || :searchQuery || '%') ORDER BY rank ASC")
    fun getContestStandings(contestId: Int, searchQuery: String = ""): Flow<List<ContestStandingRowEntity>>
}
