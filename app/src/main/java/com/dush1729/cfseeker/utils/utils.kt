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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T: Number> T.toRelativeTime(): String {
    // 1. Get the current time in seconds
    val nowSeconds = System.currentTimeMillis() / 1000
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
    val timestampMillis = toLong() * 1000 // Convert seconds to milliseconds
    val date = Date(timestampMillis)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
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

fun getRatingBackgroundColors(): List<Pair<Int, Int>> {
    // Returns list of (rating threshold, android color with alpha) for chart backgrounds
    // Alpha is 0x40 (25% opacity) for subtle background bands
    return listOf(
        0 to android.graphics.Color.argb(0x40, 0x80, 0x80, 0x80),      // Newbie: Gray
        1200 to android.graphics.Color.argb(0x40, 0x00, 0x80, 0x00),   // Pupil: Green
        1400 to android.graphics.Color.argb(0x40, 0x03, 0xA8, 0x9E),   // Specialist: Cyan
        1600 to android.graphics.Color.argb(0x40, 0x00, 0x00, 0xFF),   // Expert: Blue
        1900 to android.graphics.Color.argb(0x40, 0xAA, 0x00, 0xAA),   // Candidate Master: Violet
        2100 to android.graphics.Color.argb(0x40, 0xFF, 0x8C, 0x00),   // Master: Orange
        2300 to android.graphics.Color.argb(0x40, 0xFF, 0x8C, 0x00),   // International Master: Orange
        2400 to android.graphics.Color.argb(0x40, 0xFF, 0x00, 0x00),   // Grandmaster: Red
        2600 to android.graphics.Color.argb(0x40, 0xCC, 0x00, 0x00),   // International Grandmaster: Dark Red
        3000 to android.graphics.Color.argb(0x40, 0xAA, 0x00, 0x00)    // Legendary Grandmaster: Darker Red
    )
}
fun <T: Number> T.isPowerOfTwo(): Boolean {
    val value = this.toLong()
    return value > 0 && (value and (value - 1)) == 0L
}