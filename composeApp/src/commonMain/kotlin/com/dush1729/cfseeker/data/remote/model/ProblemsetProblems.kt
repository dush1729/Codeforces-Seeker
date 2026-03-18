package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ProblemsetProblems(
    val problems: List<Problem>,
    val problemStatistics: List<ProblemStatistics>
)
