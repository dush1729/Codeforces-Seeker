package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "problemset_problem",
    primaryKeys = ["contestId", "index"],
    indices = [
        Index(value = ["rating"]),
        Index(value = ["name"]),
    ]
)
data class ProblemEntity(
    val contestId: Int,
    val problemsetName: String?,
    val index: String,
    val name: String,
    val type: String,
    val points: Double?,
    val rating: Int?,
    val tags: String,
    val solvedCount: Int,
)
