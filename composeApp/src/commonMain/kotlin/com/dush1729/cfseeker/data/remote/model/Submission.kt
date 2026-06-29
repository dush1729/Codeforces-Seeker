package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Submission(
    val id: Long,
    val contestId: Int? = null,
    val creationTimeSeconds: Long,
    val relativeTimeSeconds: Long,
    val problem: Problem,
    val author: Party,
    val programmingLanguage: String,
    val verdict: String? = null,
    val testset: String,
    val passedTestCount: Int,
    val timeConsumedMillis: Int,
    val memoryConsumedBytes: Long,
    val points: Double? = null
)
