package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "rating_change",
    primaryKeys = ["handle", "contestId"],
    indices = [
        Index(value = ["source", "handle", "ratingUpdateTimeSeconds"]),
        Index(value = ["contestId", "contestRank"])
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
    val lastSync: Long,
    val source: String  // "USER" or "CONTEST"
)
