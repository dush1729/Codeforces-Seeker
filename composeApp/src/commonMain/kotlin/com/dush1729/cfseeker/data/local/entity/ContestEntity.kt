package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contest",
    indices = [
        Index(value = ["phase", "startTimeSeconds"], name = "index_contest_phase_startTime")
    ]
)
data class ContestEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val frozen: Boolean,
    val durationSeconds: Long,
    val startTimeSeconds: Long,
    val relativeTimeSeconds: Long
)
