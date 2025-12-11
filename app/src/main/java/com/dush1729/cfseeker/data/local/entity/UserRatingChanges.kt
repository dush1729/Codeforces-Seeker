package com.dush1729.cfseeker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserRatingChanges(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "handle",
        entityColumn = "handle"
    )
    val ratingChanges: List<RatingChangeEntity>
)
