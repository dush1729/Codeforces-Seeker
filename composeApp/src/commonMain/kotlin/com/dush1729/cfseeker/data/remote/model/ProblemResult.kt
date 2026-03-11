package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ProblemResult(
    val points: Double,
    val penalty: Int? = null,
    val rejectedAttemptCount: Int,
    val type: String,
    val bestSubmissionTimeSeconds: Long? = null
)
