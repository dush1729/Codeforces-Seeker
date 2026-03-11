package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
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

    @Query("""
        SELECT * FROM contest_standing_row
        WHERE contestId = :contestId
        AND (:searchQuery = '' OR memberHandles LIKE '%' || :searchQuery || '%')
        AND (:showLocalUsersOnly = 0 OR memberHandles IN (SELECT handle FROM user))
        ORDER BY rank ASC
    """)
    fun getContestStandings(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<ContestStandingRowEntity>>

    // Contest rating changes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRatingChangesIgnoreConflict(ratingChanges: List<RatingChangeEntity>)

    @Query("""
        SELECT * FROM rating_change
        WHERE contestId = :contestId
        AND (:searchQuery = '' OR handle LIKE '%' || :searchQuery || '%')
        AND (:showLocalUsersOnly = 0 OR handle IN (SELECT handle FROM user))
        ORDER BY contestRank ASC
    """)
    fun getRatingChangesByContest(
        contestId: Int,
        searchQuery: String = "",
        showLocalUsersOnly: Boolean = false
    ): Flow<List<RatingChangeEntity>>

    // Count queries
    @Query("SELECT COUNT(*) FROM contest_problem")
    suspend fun getContestProblemCount(): Int

    @Query("SELECT COUNT(*) FROM contest_standing_row")
    suspend fun getContestStandingCount(): Int

    @Query("SELECT COUNT(*) FROM rating_change WHERE source = 'CONTEST'")
    suspend fun getContestRatingChangeCount(): Int

    // Delete all queries
    @Query("DELETE FROM contest_problem")
    suspend fun deleteAllContestProblems()

    @Query("DELETE FROM contest_standing_row")
    suspend fun deleteAllContestStandings()

    @Query("DELETE FROM rating_change WHERE source = 'CONTEST'")
    suspend fun deleteAllContestRatingChanges()

    @Query("SELECT DISTINCT contestId FROM contest_problem")
    suspend fun getCachedContestIds(): List<Int>

    @Transaction
    suspend fun clearContestCache(): List<Int> {
        val contestIds = getCachedContestIds()
        deleteAllContestProblems()
        deleteAllContestStandings()
        deleteAllContestRatingChanges()
        return contestIds
    }
}
