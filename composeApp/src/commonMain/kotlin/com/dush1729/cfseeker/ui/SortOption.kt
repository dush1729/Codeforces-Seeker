package com.dush1729.cfseeker.ui

enum class SortOption(val value: String, val displayName: String) {
    LAST_RATING_UPDATE("LAST_RATING_UPDATE", "default"),
    HANDLE("HANDLE", "handle"),
    RATING("RATING", "rating"),
    MAX_RATING("MAX_RATING", "max rating"),
    CONTRIBUTION("CONTRIBUTION", "contribution"),
    FRIEND_OF("FRIEND_OF", "friend of"),
    LAST_ONLINE("LAST_ONLINE", "last online"),
    LAST_SYNC("LAST_SYNC", "last sync"),
}
