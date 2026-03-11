package com.dush1729.cfseeker.analytics

/**
 * No-op implementation of AnalyticsService for testing and previews.
 * Does nothing when analytics methods are called.
 */
object DummyAnalyticsService : AnalyticsService {
    override fun logMilestoneLaunch(launchCount: Int) {}
    override fun logUserAdded(handle: String) {}
    override fun logUserAddFailed(handle: String, error: String) {}
    override fun logUserDeleted(handle: String) {}
    override fun logUserSyncedFromDetails(handle: String) {}
    override fun logBulkSyncStarted() {}
    override fun logBulkSyncCompleted(durationMs: Long, userCount: Int, successCount: Int, failureCount: Int) {}
    override fun logSortChanged(sortOption: String) {}
    override fun logScreenView(screenName: String) {}
    override fun logFeedbackOpened() {}
    override fun logGitHubOpened() {}
    override fun logPlayStoreOpened(source: String) {}
    override fun logAppShared(source: String) {}
}
