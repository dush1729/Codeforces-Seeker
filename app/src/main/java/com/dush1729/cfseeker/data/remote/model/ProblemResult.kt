package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class ProblemResult(
    @SerializedName("points")
    val points: Double,

    @SerializedName("penalty")
    val penalty: Int?,

    @SerializedName("rejectedAttemptCount")
    val rejectedAttemptCount: Int,

    @SerializedName("type")
    val type: String,

    @SerializedName("bestSubmissionTimeSeconds")
    val bestSubmissionTimeSeconds: Long?
)
