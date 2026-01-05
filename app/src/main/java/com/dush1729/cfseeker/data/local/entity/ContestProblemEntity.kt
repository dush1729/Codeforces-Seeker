package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "contest_problem",
    primaryKeys = ["contestId", "index"]
)
data class ContestProblemEntity(
    val contestId: Int,
    val problemsetName: String?,
    val index: String,
    val name: String,
    val type: String,
    val points: Double?,
    val rating: Int?,
    val tags: String,
)
