package com.dush1729.cfseeker.data.remote.model


import com.google.gson.annotations.SerializedName

data class RatingChange(
    @SerializedName("contestId")
    val contestId: Int,
    @SerializedName("contestName")
    val contestName: String,
    @SerializedName("handle")
    val handle: String,
    @SerializedName("newRating")
    val newRating: Int,
    @SerializedName("oldRating")
    val oldRating: Int,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("ratingUpdateTimeSeconds")
    val ratingUpdateTimeSeconds: Long
)