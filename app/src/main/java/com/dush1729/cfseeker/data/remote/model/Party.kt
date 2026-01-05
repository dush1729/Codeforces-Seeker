package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class Party(
    @SerializedName("contestId")
    val contestId: Int?,

    @SerializedName("members")
    val members: List<Member>,

    @SerializedName("participantType")
    val participantType: String,

    @SerializedName("teamId")
    val teamId: Int?,

    @SerializedName("teamName")
    val teamName: String?,

    @SerializedName("ghost")
    val ghost: Boolean,

    @SerializedName("room")
    val room: Int?,

    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long?
)
