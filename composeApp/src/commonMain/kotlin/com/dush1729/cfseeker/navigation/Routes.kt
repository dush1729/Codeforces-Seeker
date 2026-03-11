package com.dush1729.cfseeker.navigation

import kotlinx.serialization.Serializable

@Serializable
data object MainRoute

@Serializable
data class UserDetailsRoute(val handle: String)

@Serializable
data class ContestDetailsRoute(
    val contestId: Int,
    val contestName: String,
    val contestType: String
)
