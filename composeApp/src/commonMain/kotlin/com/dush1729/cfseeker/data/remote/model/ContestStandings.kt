package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ContestStandings(
    val contest: Contest,
    val problems: List<Problem>,
    val rows: List<RanklistRow>
)
