package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rated_user",
    indices = [Index(value = ["rating"])]
)
data class RatedUserEntity(
    @PrimaryKey
    val handle: String,
    val rating: Int,
    val maxRating: Int?,
    val rank: String?,
    val maxRank: String?,
)
