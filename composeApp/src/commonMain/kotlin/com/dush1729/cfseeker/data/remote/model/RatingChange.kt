package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RatingChange(
    val contestId: Int,
    val contestName: String,
    val handle: String,
    val newRating: Int,
    val oldRating: Int,
    val rank: Int,
    val ratingUpdateTimeSeconds: Long
)
