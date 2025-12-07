package com.example.cfseeker.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "rating_change",
    primaryKeys = ["handle", "contestId"]
)
data class RatingChangeEntity(
    val handle: String,
    val contestId: Int?,
    val contestName: String,
    val contestRank: Int,
    val oldRating: Int,
    val newRating: Int,
    val ratingUpdateTimeSeconds: Long,
)
