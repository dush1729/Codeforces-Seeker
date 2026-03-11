package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Problem(
    val contestId: Int? = null,
    val problemsetName: String? = null,
    val index: String,
    val name: String,
    val type: String,
    val points: Double? = null,
    val rating: Int? = null,
    val tags: List<String> = emptyList()
)
