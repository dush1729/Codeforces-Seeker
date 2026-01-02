package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contest",
    indices = [
        Index("id"),
        Index("phase"),
        Index("startTimeSeconds")
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
