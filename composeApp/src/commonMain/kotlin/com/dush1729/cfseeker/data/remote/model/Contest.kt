package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Contest(
    val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val frozen: Boolean,
    val durationSeconds: Long,
    val startTimeSeconds: Long = 0,
    val relativeTimeSeconds: Long = 0
)
