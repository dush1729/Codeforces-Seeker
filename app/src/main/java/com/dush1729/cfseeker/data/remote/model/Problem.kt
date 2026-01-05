package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class Problem(
    @SerializedName("contestId")
    val contestId: Int?,

    @SerializedName("problemsetName")
    val problemsetName: String?,

    @SerializedName("index")
    val index: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("points")
    val points: Double?,

    @SerializedName("rating")
    val rating: Int?,

    @SerializedName("tags")
    val tags: List<String>
)
