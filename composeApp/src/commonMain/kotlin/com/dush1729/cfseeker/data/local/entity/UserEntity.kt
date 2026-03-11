package com.dush1729.cfseeker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [
        Index("handle"),
        Index("lastSync"),
        Index("rating")
    ]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val handle: String,
    val avatar: String?,
    val city: String?,
    val contribution: Int,
    val country: String?,
    val email: String?,
    val firstName: String?,
    val friendOfCount: Int,
    val lastName: String?,
    val lastOnlineTimeSeconds: Long,
    val maxRank: String?,
    val maxRating: Int?,
    val organization: String?,
    val rank: String?,
    val rating: Int?,
    val registrationTimeSeconds: Long,
    val titlePhoto: String,

    val lastSync: Long,
)
