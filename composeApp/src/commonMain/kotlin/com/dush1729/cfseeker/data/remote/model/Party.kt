package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Party(
    val contestId: Int? = null,
    val members: List<Member>,
    val participantType: String,
    val teamId: Int? = null,
    val teamName: String? = null,
    val ghost: Boolean,
    val room: Int? = null,
    val startTimeSeconds: Long? = null
)
