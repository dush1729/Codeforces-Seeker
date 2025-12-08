package com.example.cfseeker.utils

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