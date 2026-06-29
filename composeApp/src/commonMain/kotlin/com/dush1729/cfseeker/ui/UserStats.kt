package com.dush1729.cfseeker.ui

import com.dush1729.cfseeker.data.remote.model.Submission

internal data class UserStats(
    val totalSubmissions: Int,
    val uniqueSolved: Int,
    val verdictCounts: List<Pair<String, Int>>,
    val languageCounts: List<Pair<String, Int>>,
    val tagCounts: List<Pair<String, Int>>,
    val ratingBuckets: List<Pair<Int, Int>>
)

internal fun computeUserStats(submissions: List<Submission>): UserStats {
    val accepted = submissions.filter { it.verdict == "OK" }
    val uniqueSolved = accepted.distinctBy { "${it.problem.contestId}_${it.problem.index}" }.size

    val verdictCounts = submissions
        .groupBy { it.verdict ?: "TESTING" }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }
        .map { it.key to it.value }

    val languageCounts = submissions
        .groupBy { it.programmingLanguage }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }
        .map { it.key to it.value }

    val tagCounts = accepted
        .flatMap { it.problem.tags }
        .groupBy { it }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }
        .take(15)
        .map { it.key to it.value }

    val ratingBuckets = accepted
        .mapNotNull { it.problem.rating }
        .groupBy { (it / 100) * 100 }
        .mapValues { it.value.size }
        .entries.sortedBy { it.key }
        .map { it.key to it.value }

    return UserStats(submissions.size, uniqueSolved, verdictCounts, languageCounts, tagCounts, ratingBuckets)
}

internal fun verdictLabel(verdict: String): String = when (verdict) {
    "OK" -> "Accepted"
    "WRONG_ANSWER" -> "Wrong Answer"
    "TIME_LIMIT_EXCEEDED" -> "TLE"
    "MEMORY_LIMIT_EXCEEDED" -> "MLE"
    "RUNTIME_ERROR" -> "Runtime Error"
    "COMPILATION_ERROR" -> "Compile Error"
    "IDLENESS_LIMIT_EXCEEDED" -> "Idleness Limit"
    "PARTIAL" -> "Partial"
    "SKIPPED" -> "Skipped"
    "REJECTED" -> "Rejected"
    "CHALLENGED" -> "Challenged"
    "PRESENTATION_ERROR" -> "Presentation Error"
    "SECURITY_VIOLATED" -> "Security Violated"
    "CRASHED" -> "Crashed"
    "FAILED" -> "Failed"
    else -> verdict
}
