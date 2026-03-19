package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "solved_problem",
    primaryKeys = ["handle", "contestId", "index"]
)
data class SolvedProblemEntity(
    val handle: String,
    val contestId: Int,
    val index: String,
)
