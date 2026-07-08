package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.RankedSeedGame

import com.carlosarancibia.playfit.model.ProductConfidence as Confidence

const val HIGH_FRICTION_THRESHOLD = 58
private const val STRONG_FIT_THRESHOLD = 78
private const val PROMISING_FIT_THRESHOLD = 62

object ProductUtils {
    fun decisionLabel(entry: RankedSeedGame): String {
        if (entry.riskScore >= HIGH_FRICTION_THRESHOLD) return "Watch out"
        if (entry.confidence == Confidence.Low) return "Too early to tell"
        if (entry.affinityScore >= STRONG_FIT_THRESHOLD) return "Strong match"
        if (entry.affinityScore >= PROMISING_FIT_THRESHOLD) return "Promising"
        return "Still learning"
    }

    fun confidenceLabel(confidence: Confidence): String {
        return when (confidence) {
            Confidence.High -> "Strong signal"
            Confidence.Medium -> "Building signal"
            Confidence.Low -> "First look"
        }
    }

    fun matchQualityLabel(score: Double): String {
        return when {
            score >= STRONG_FIT_THRESHOLD -> "Strong match"
            score >= PROMISING_FIT_THRESHOLD -> "Promising"
            score >= 35 -> "Moderate match"
            else -> "Early match"
        }
    }

    fun watchOutLabel(score: Double): String {
        return when {
            score >= HIGH_FRICTION_THRESHOLD -> "High friction"
            score >= 35 -> "Some watch-outs"
            score >= 15 -> "Low watch-out"
            else -> "Clear read"
        }
    }

    fun unknownGenre(): String = "Metadata pending"

    fun groupTitle(entry: RankedSeedGame): String {
        return if (entry.confidence == Confidence.Low && entry.affinityScore < 50) "First reads"
        else "Best matches"
    }

    fun groupCopy(entry: RankedSeedGame): String {
        return if (entry.confidence == Confidence.Low && entry.affinityScore < 50)
            "First signals from what you shared. Every rating sharpens the read."
        else "Games with the strongest signal right now."
    }
}
