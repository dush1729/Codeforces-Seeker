package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CodeforcesApiResponse<T>(
    val status: String,
    val result: List<T>? = null,
    val comment: String? = null,
)
