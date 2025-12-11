package com.dush1729.cfseeker.data.remote.model


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("city")
    val city: String?,
    @SerializedName("contribution")
    val contribution: Int,
    @SerializedName("country")
    val country: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("friendOfCount")
    val friendOfCount: Int,
    @SerializedName("handle")
    val handle: String,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("lastOnlineTimeSeconds")
    val lastOnlineTimeSeconds: Long,
    @SerializedName("maxRank")
    val maxRank: String?,
    @SerializedName("maxRating")
    val maxRating: Int?,
    @SerializedName("organization")
    val organization: String?,
    @SerializedName("rank")
    val rank: String?,
    @SerializedName("rating")
    val rating: Int?,
    @SerializedName("registrationTimeSeconds")
    val registrationTimeSeconds: Long,
    @SerializedName("titlePhoto")
    val titlePhoto: String
)