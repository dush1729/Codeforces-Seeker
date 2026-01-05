package com.dush1729.cfseeker.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object UserDetails : Screen("user/{handle}") {
        fun createRoute(handle: String) = "user/$handle"
    }
    data object ContestDetails : Screen("contest/{contestId}/{contestName}/{contestType}") {
        fun createRoute(contestId: Int, contestName: String, contestType: String) =
            "contest/$contestId/$contestName/$contestType"
    }
}
