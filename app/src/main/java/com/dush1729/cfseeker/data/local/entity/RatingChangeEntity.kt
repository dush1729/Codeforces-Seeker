package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "rating_change",
    primaryKeys = ["handle", "contestId"],
    indices = [
        Index("handle"),
        Index("ratingUpdateTimeSeconds")
    ]
)
data class RatingChangeEntity(
    val handle: String,
    val contestId: Int,
    val contestName: String,
    val contestRank: Int,
    val oldRating: Int,
    val newRating: Int,
    val ratingUpdateTimeSeconds: Long,

    val lastSync: Long
)
