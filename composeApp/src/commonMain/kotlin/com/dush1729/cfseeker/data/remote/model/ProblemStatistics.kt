package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ProblemStatistics(
    val contestId: Int? = null,
    val index: String,
    val solvedCount: Int
)
