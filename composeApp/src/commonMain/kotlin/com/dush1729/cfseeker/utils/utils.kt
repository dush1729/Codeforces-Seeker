package com.dush1729.cfseeker.utils

import androidx.compose.ui.graphics.Color
import com.dush1729.cfseeker.ui.theme.CFCandidateMaster
import com.dush1729.cfseeker.ui.theme.CFExpert
import com.dush1729.cfseeker.ui.theme.CFGrandmaster
import com.dush1729.cfseeker.ui.theme.CFInternationalGrandmaster
import com.dush1729.cfseeker.ui.theme.CFInternationalMaster
import com.dush1729.cfseeker.ui.theme.CFMaster
import com.dush1729.cfseeker.ui.theme.CFNewbie
import com.dush1729.cfseeker.ui.theme.CFPupil
import com.dush1729.cfseeker.ui.theme.CFSpecialist
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T: Number> T.toRelativeTime(): String {
    // 1. Get the current time in seconds
    val nowSeconds = Clock.System.now().epochSeconds
    val timestampSeconds = toLong()

    // 2. Calculate the difference (Duration)
    val differenceSeconds = abs(nowSeconds - timestampSeconds)
    val duration: Duration = differenceSeconds.seconds

    // 3. Format the duration into a human-readable string
    val relativeTime = when {
        // Years
        duration.inWholeDays >= 365 -> {
            val years = duration.inWholeDays / 365
            "$years year${if (years > 1) "s" else ""}"
        }
        // Months
        duration.inWholeDays >= 30 -> {
            val months = duration.inWholeDays / 30
            "$months month${if (months > 1) "s" else ""}"
        }
        // Weeks
        duration.inWholeDays >= 7 -> {
            val weeks = duration.inWholeDays / 7
            "$weeks week${if (weeks > 1) "s" else ""}"
        }
        // Days
        duration.inWholeDays >= 1 -> {
            val days = duration.inWholeDays
            "$days day${if (days > 1) "s" else ""}"
        }
        // Hours
        duration.inWholeHours >= 1 -> {
            val hours = duration.inWholeHours
            "$hours hour${if (hours > 1) "s" else ""}"
        }
        // Minutes
        duration.inWholeMinutes >= 1 -> {
            val minutes = duration.inWholeMinutes
            "$minutes minute${if (minutes > 1) "s" else ""}"
        }
        else -> "few seconds"
    }

    return if (timestampSeconds > nowSeconds) {
        return "In $relativeTime"
    } else {
        "$relativeTime ago"
    }
}

fun <T: Number> T.toFormattedDate(): String {
    val instant = Instant.fromEpochSeconds(toLong())
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val year = localDateTime.year
    return "$month $day, $year"
}

fun getRatingColor(rating: Int?): Color {
    return when {
        rating == null -> CFNewbie
        rating < 1200 -> CFNewbie
        rating < 1400 -> CFPupil
        rating < 1600 -> CFSpecialist
        rating < 1900 -> CFExpert
        rating < 2100 -> CFCandidateMaster
        rating < 2300 -> CFMaster
        rating < 2400 -> CFInternationalMaster
        rating < 3000 -> CFGrandmaster
        else -> CFInternationalGrandmaster
    }
}

fun getRatingBackgroundColors(): List<Pair<Int, Color>> {
    // Returns list of (rating threshold, Compose Color with alpha) for chart backgrounds
    return listOf(
        0 to Color(0x40808080),      // Newbie: Gray
        1200 to Color(0x40008000),   // Pupil: Green
        1400 to Color(0x4003A89E),   // Specialist: Cyan
        1600 to Color(0x400000FF),   // Expert: Blue
        1900 to Color(0x40AA00AA),   // Candidate Master: Violet
        2100 to Color(0x40FF8C00),   // Master: Orange
        2300 to Color(0x40FF8C00),   // International Master: Orange
        2400 to Color(0x40FF0000),   // Grandmaster: Red
        2600 to Color(0x40CC0000),   // International Grandmaster: Dark Red
        3000 to Color(0x40AA0000)    // Legendary Grandmaster: Darker Red
    )
}

fun <T: Number> T.isPowerOfTwo(): Boolean {
    val value = this.toLong()
    return value > 0 && (value and (value - 1)) == 0L
}
