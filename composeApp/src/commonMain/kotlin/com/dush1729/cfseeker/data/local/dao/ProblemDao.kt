package com.dush1729.cfseeker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dush1729.cfseeker.data.local.entity.ProblemEntity
import com.dush1729.cfseeker.data.local.entity.SolvedProblemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProblemDao {

    @Upsert
    suspend fun upsertProblems(problems: List<ProblemEntity>)

    @Transaction
    suspend fun insertAllProblems(problems: List<ProblemEntity>) {
        deleteAllProblems()
        upsertProblems(problems)
    }

    @Query("DELETE FROM problemset_problem")
    suspend fun deleteAllProblems(): Int

    @Query("""
        SELECT p.* FROM problemset_problem p
        LEFT JOIN solved_problem sp ON p.contestId = sp.contestId
            AND p.`index` = sp.`index` AND sp.handle = :handle
        WHERE (:minRating IS NULL OR p.rating >= :minRating)
            AND (:maxRating IS NULL OR p.rating <= :maxRating)
            AND (:searchQuery = '' OR p.name LIKE '%' || :searchQuery || '%')
            AND (:hideSolved = 0 OR sp.handle IS NULL)
        ORDER BY p.rating ASC, p.contestId DESC, p.`index` ASC
    """)
    fun getProblems(
        minRating: Int? = null,
        maxRating: Int? = null,
        searchQuery: String = "",
        hideSolved: Boolean = false,
        handle: String = "",
    ): Flow<List<ProblemEntity>>

    @Query("SELECT COUNT(*) FROM problemset_problem")
    suspend fun getProblemCount(): Int

    @Query("SELECT DISTINCT tags FROM problemset_problem")
    fun getAllTags(): Flow<List<String>>

    // Solved problems

    @Upsert
    suspend fun upsertSolvedProblems(solvedProblems: List<SolvedProblemEntity>)

    @Transaction
    suspend fun insertAllSolvedProblems(handle: String, solvedProblems: List<SolvedProblemEntity>) {
        deleteSolvedProblemsForHandle(handle)
        upsertSolvedProblems(solvedProblems)
    }

    @Query("DELETE FROM solved_problem WHERE handle = :handle")
    suspend fun deleteSolvedProblemsForHandle(handle: String): Int

    @Query("SELECT COUNT(*) FROM solved_problem WHERE handle = :handle")
    fun getSolvedCountForHandle(handle: String): Flow<Int>
}
