package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class RanklistRow(
    @SerializedName("party")
    val party: Party,

    @SerializedName("rank")
    val rank: Int,

    @SerializedName("points")
    val points: Double,

    @SerializedName("penalty")
    val penalty: Int,

    @SerializedName("successfulHackCount")
    val successfulHackCount: Int,

    @SerializedName("unsuccessfulHackCount")
    val unsuccessfulHackCount: Int,

    @SerializedName("problemResults")
    val problemResults: List<ProblemResult>,

    @SerializedName("lastSubmissionTimeSeconds")
    val lastSubmissionTimeSeconds: Long?
)
