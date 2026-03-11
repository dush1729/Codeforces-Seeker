package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CodeforcesApiSingleResponse<T>(
    val status: String,
    val result: T? = null,
    val comment: String? = null,
)
