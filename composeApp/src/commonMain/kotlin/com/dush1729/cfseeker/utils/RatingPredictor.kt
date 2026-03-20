package com.dush1729.cfseeker.utils

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

data class ContestantForPrediction(
    val handle: String,
    val rank: Int,
    val rating: Int
)

object RatingPredictor {

    private fun eloWinProbability(ratingA: Int, ratingB: Int): Double {
        return 1.0 / (1.0 + 10.0.pow((ratingB.toDouble() - ratingA.toDouble()) / 400.0))
    }

    private fun getSeed(ratings: List<Int>, index: Int): Double {
        var seed = 1.0
        for (i in ratings.indices) {
            if (i != index) {
                seed += eloWinProbability(ratings[i], ratings[index])
            }
        }
        return seed
    }

    private fun getSeedForRating(ratings: List<Int>, rating: Int): Double {
        var seed = 1.0
        for (r in ratings) {
            seed += 1.0 / (1.0 + 10.0.pow((rating.toDouble() - r.toDouble()) / 400.0))
        }
        return seed
    }

    private fun getRatingForSeed(ratings: List<Int>, targetSeed: Double): Int {
        var lo = 1
        var hi = 8000
        while (hi - lo > 1) {
            val mid = (lo + hi) / 2
            if (getSeedForRating(ratings, mid) < targetSeed) {
                hi = mid
            } else {
                lo = mid
            }
        }
        return lo
    }

    fun predict(contestants: List<ContestantForPrediction>): Map<String, Int> {
        if (contestants.isEmpty()) return emptyMap()

        val n = contestants.size
        if (n > 8000) return emptyMap()

        val ratings = contestants.map { it.rating }
        val deltas = IntArray(n)

        for (i in 0 until n) {
            val seed = getSeed(ratings, i)
            val midRank = sqrt(contestants[i].rank.toDouble() * seed)
            val performanceRating = getRatingForSeed(ratings, midRank)
            deltas[i] = (performanceRating - ratings[i]) / 2
        }

        // Sort indices by rating descending for correction phases
        val sortedIndices = (0 until n).sortedByDescending { contestants[it].rating }

        // Correction 1: adjust all deltas to reduce sum toward zero
        val totalDelta = deltas.sum()
        val inc = -(totalDelta / n) - 1
        for (i in 0 until n) {
            deltas[i] += inc
        }

        // Correction 2: adjust top s (highest-rated) participants
        val s = min((4 * sqrt(n.toDouble())).toInt(), n)
        var topSum = 0
        for (i in 0 until s) {
            topSum += deltas[sortedIndices[i]]
        }
        val dec = min(0, max(-10, -(topSum / s)))
        for (i in 0 until s) {
            deltas[sortedIndices[i]] += dec
        }

        val result = mutableMapOf<String, Int>()
        for (i in 0 until n) {
            result[contestants[i].handle] = deltas[i]
        }
        return result
    }
}
