package com.carlosarancibia.playfit.model

import kotlin.math.abs
import kotlin.math.min

object ProductTasteDerivation {
    fun build(
        state: ProductState,
        games: Collection<SeedGame>,
    ): ProductTasteModel {
        val profile = state.user.profile ?: ProductProfile()
        val gamesById = games.associateBy { it.gameId }
        val entriesByGame = linkedMapOf<String, ProductTasteHistoryEntry>()

        state.user.gameStates.forEach { (gameId, gameState) ->
            val game = gamesById[gameId]
            val rating = gameState.rating
            val decisionAndTone = when {
                gameState.inPlayfitPicks && rating == null &&
                    (gameState.status == null || gameState.status == ProductPlayStatus.WantToPlay) &&
                    !gameState.excluded ->
                    "picks" to "neutral"
                rating != null -> if (rating >= 4) "liked" to "positive" else "not_for_me" to "negative"
                gameState.status == ProductPlayStatus.Abandoned -> "dropped" to "negative"
                else -> null
            } ?: return@forEach

            entriesByGame[gameId] = ProductTasteHistoryEntry(
                gameId = gameId,
                title = game?.title ?: gameState.title.ifBlank { gameId },
                decision = decisionAndTone.first,
                source = "rating",
                tone = decisionAndTone.second,
                rating = rating,
                status = gameState.status?.apiValue,
                updatedAt = gameState.updatedAt.ifBlank { null },
                traits = game?.traits().orEmpty(),
            )
        }

        state.user.onboarding.likedGameIds.forEach { gameId ->
            if (gameId !in entriesByGame) {
                val game = gamesById[gameId]
                entriesByGame[gameId] = ProductTasteHistoryEntry(
                    gameId = gameId,
                    title = game?.title ?: gameId,
                    decision = "setup_favorite",
                    source = "onboarding_liked",
                    tone = "positive",
                    traits = game?.traits().orEmpty(),
                )
            }
        }

        state.user.onboarding.dislikedGameIds.forEach { gameId ->
            if (gameId !in entriesByGame) {
                val game = gamesById[gameId]
                entriesByGame[gameId] = ProductTasteHistoryEntry(
                    gameId = gameId,
                    title = game?.title ?: gameId,
                    decision = "setup_miss",
                    source = "onboarding_disliked",
                    tone = "negative",
                    traits = game?.traits().orEmpty(),
                )
            }
        }

        val history = entriesByGame.values.sortedWith(
            compareByDescending<ProductTasteHistoryEntry> { it.updatedAt.orEmpty() }
                .thenBy { it.title.lowercase() },
        )
        val traits = buildTraits(history, profile)
        val positive = history.count { it.tone == "positive" }
        val negative = history.count { it.tone == "negative" }

        return ProductTasteModel(
            evidenceCount = history.count { it.tone == "positive" || it.tone == "negative" },
            historyEntries = history,
            mapTraits = traits,
            positiveCount = positive,
            negativeCount = negative,
            confidenceLabel = when {
                profile.ratedCount >= 20 -> "Strong signal"
                profile.ratedCount >= 5 -> "Building signal"
                else -> "First look"
            },
        )
    }

    private fun buildTraits(
        history: List<ProductTasteHistoryEntry>,
        profile: ProductProfile,
    ): List<ProductTasteMapTrait> {
        data class Counts(var positive: Int = 0, var negative: Int = 0, val kind: String, val label: String)
        val counts = mutableMapOf<String, Counts>()

        fun add(id: String, positive: Boolean, kind: String = id.kind(), label: String = id.label()) {
            if (id.isBlank()) return
            val current = counts.getOrPut(id) { Counts(kind = kind, label = label) }
            if (positive) current.positive++ else current.negative++
        }

        history.forEach { entry ->
            val positive = entry.tone == "positive"
            val negative = entry.tone == "negative"
            if (positive || negative) entry.traits.forEach { add(it, positive) }
        }
        profile.likedGenres.forEach { add(it, positive = true, kind = "genre") }
        profile.avoidedGenres.forEach { add(it, positive = false, kind = "genre") }

        return counts.map { (id, count) ->
            val total = count.positive + count.negative
            val net = abs(count.positive - count.negative)
            ProductTasteMapTrait(
                id = id,
                label = count.label,
                kind = count.kind,
                positiveCount = count.positive,
                negativeCount = count.negative,
                netScore = net.toDouble(),
                strength = min(net * 10.0, 100.0),
                confidence = when {
                    total >= 3 -> "High"
                    total >= 2 -> "Medium"
                    else -> "Low"
                },
                direction = if (count.positive >= count.negative) "positive" else "negative",
            )
        }.sortedWith(compareByDescending<ProductTasteMapTrait> { it.strength }.thenBy { it.label })
    }

    private fun SeedGame.traits(): List<String> =
        (listOf(primaryGenre) + tags).filter { it.isNotBlank() }.distinct()

    private fun String.kind(): String = if ('_' in this || length > 12) "tag" else "genre"

    private fun String.label(): String = split('_', '-').joinToString(" ") { part ->
        part.replaceFirstChar { it.uppercase() }
    }
}
