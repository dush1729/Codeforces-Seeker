package com.dush1729.cfseeker.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val avatar: String,
    val city: String? = null,
    val contribution: Int,
    val country: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val friendOfCount: Int,
    val handle: String,
    val lastName: String? = null,
    val lastOnlineTimeSeconds: Long,
    val maxRank: String? = null,
    val maxRating: Int? = null,
    val organization: String? = null,
    val rank: String? = null,
    val rating: Int? = null,
    val registrationTimeSeconds: Long,
    val titlePhoto: String
)
