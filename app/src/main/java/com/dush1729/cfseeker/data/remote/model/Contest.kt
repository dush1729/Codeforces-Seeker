package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class Contest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("phase")
    val phase: String,

    @SerializedName("frozen")
    val frozen: Boolean,

    @SerializedName("durationSeconds")
    val durationSeconds: Long,

    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long,

    @SerializedName("relativeTimeSeconds")
    val relativeTimeSeconds: Long
)
