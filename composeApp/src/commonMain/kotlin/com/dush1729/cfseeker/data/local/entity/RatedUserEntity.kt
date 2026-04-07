package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rated_user",
    indices = [
        Index(value = ["rating"]),
        Index(value = ["country"]),
        Index(value = ["city"]),
        Index(value = ["organization"]),
        Index(value = ["maxRating"])
    ]
)
data class RatedUserEntity(
    @PrimaryKey
    val handle: String,
    val rating: Int,
    val maxRating: Int?,
    val rank: String?,
    val maxRank: String?,
    val avatar: String?,
    val titlePhoto: String?,
    val firstName: String?,
    val lastName: String?,
    val country: String?,
    val city: String?,
    val organization: String?,
    val contribution: Int,
    val friendOfCount: Int,
    val lastOnlineTimeSeconds: Long,
    val registrationTimeSeconds: Long,
)
