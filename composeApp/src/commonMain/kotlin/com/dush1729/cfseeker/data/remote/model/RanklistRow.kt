package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RanklistRow(
    val party: Party,
    val rank: Int,
    val points: Double,
    val penalty: Int,
    val successfulHackCount: Int,
    val unsuccessfulHackCount: Int,
    val problemResults: List<ProblemResult>,
    val lastSubmissionTimeSeconds: Long? = null
)
