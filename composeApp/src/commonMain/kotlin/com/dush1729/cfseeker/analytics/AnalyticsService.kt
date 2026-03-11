package com.dush1729.cfseeker.analytics

interface AnalyticsService {
    fun logMilestoneLaunch(launchCount: Int)

    // User events
    fun logUserAdded(handle: String)
    fun logUserAddFailed(handle: String, error: String)
    fun logUserDeleted(handle: String)
    fun logUserSyncedFromDetails(handle: String)
    fun logBulkSyncStarted()
    fun logBulkSyncCompleted(durationMs: Long, userCount: Int, successCount: Int, failureCount: Int)

    fun logSortChanged(sortOption: String)

    // Navigation events
    fun logScreenView(screenName: String)

    // About screen events
    fun logFeedbackOpened()
    fun logGitHubOpened()
    fun logPlayStoreOpened(source: String)
    fun logAppShared(source: String)
}
