package com.dush1729.cfseeker.data.local.view

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "user_with_latest_rating_change",
    value = """
        SELECT
            u.handle,
            u.avatar,
            u.city,
            u.contribution,
            u.country,
            u.email,
            u.firstName,
            u.friendOfCount,
            u.lastName,
            u.lastOnlineTimeSeconds,
            u.maxRank,
            u.maxRating,
            u.organization,
            u.rank,
            u.rating,
            u.registrationTimeSeconds,
            u.titlePhoto,
            u.lastSync,
            rc.contestId AS latestContestId,
            rc.contestName AS latestContestName,
            rc.contestRank AS latestContestRank,
            rc.oldRating AS latestOldRating,
            rc.newRating AS latestNewRating,
            rc.ratingUpdateTimeSeconds AS latestRatingUpdateTimeSeconds,
            CASE WHEN u.rating IS NOT rc.newRating THEN 1 ELSE 0 END AS isRatingOutdated
        FROM user u
        LEFT JOIN rating_change rc ON u.handle = rc.handle
            AND rc.source = 'USER'
            AND rc.ratingUpdateTimeSeconds = (
                SELECT MAX(rc2.ratingUpdateTimeSeconds)
                FROM rating_change rc2
                WHERE rc2.handle = u.handle AND rc2.source = 'USER'
            )
    """
)
data class UserWithLatestRatingChangeView(
    val handle: String,
    val avatar: String?,
    val city: String?,
    val contribution: Int,
    val country: String?,
    val email: String?,
    val firstName: String?,
    val friendOfCount: Int,
    val lastName: String?,
    val lastOnlineTimeSeconds: Long,
    val maxRank: String?,
    val maxRating: Int?,
    val organization: String?,
    val rank: String?,
    val rating: Int?,
    val registrationTimeSeconds: Long,
    val titlePhoto: String,
    val lastSync: Long,
    val latestContestId: Int?,
    val latestContestName: String?,
    val latestContestRank: Int?,
    val latestOldRating: Int?,
    val latestNewRating: Int?,
    val latestRatingUpdateTimeSeconds: Long?,
    val isRatingOutdated: Boolean
)
