package com.carlosarancibia.playfit.ui.screens

import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductTasteHistoryEntry

internal fun buildMapNodes(
    state: ProductState,
    picks: List<RankedSeedGame>,
    playNext: ProductPlayNextModel?,
    tasteModel: ProductTasteModel?,
): List<GameNode> {
    val nodes = mutableListOf<GameNode>()
    val seenIds = mutableSetOf<String>()
    val knownGames = buildKnownGames(picks, playNext)

    tasteModel?.historyEntries.orEmpty().forEach { entry ->
        val game = knownGames[entry.gameId]
        val (x, y) = calculateGameCoordinates(
            tags = entry.traits.ifEmpty { game?.tags.orEmpty() },
            primaryGenre = game?.primaryGenre.orEmpty(),
            gameId = entry.gameId,
        )
        nodes.add(
            GameNode(
                id = entry.gameId,
                title = entry.title.ifBlank { game?.title ?: entry.gameId },
                x = x,
                y = y,
                type = entry.toMapNodeType(),
                coverUrl = entry.coverUrl ?: game?.externalCoverUrl ?: game?.coverPath,
            ),
        )
        seenIds.add(entry.gameId)
    }

    for (entry in picks) {
        val game = entry.game
        if (game.gameId in seenIds) continue
        val gameState = state.user.gameStates[game.gameId]
        val (x, y) = calculateGameCoordinates(game.tags, game.primaryGenre, game.gameId)
        nodes.add(
            GameNode(
                id = game.gameId,
                title = game.title,
                x = x,
                y = y,
                type = gameState?.toMapNodeType() ?: NodeType.Pending,
                coverUrl = game.externalCoverUrl ?: game.coverPath,
            )
        )
        seenIds.add(game.gameId)
    }

    val gameStates = state.user.gameStates
    for ((gameId, gs) in gameStates) {
        if (gameId in seenIds) continue

        val type = gs.toMapNodeType()
        val game = knownGames[gameId]
        val (x, y) = calculateGameCoordinates(game?.tags.orEmpty(), game?.primaryGenre.orEmpty(), gameId)
        nodes.add(
            GameNode(
                id = gameId,
                title = game?.title ?: gs.title.ifEmpty { gameId },
                x = x,
                y = y,
                type = type,
                coverUrl = game?.externalCoverUrl ?: game?.coverPath,
            )
        )
        seenIds.add(gameId)
    }

    val primary = playNext?.primary
    if (primary != null && primary.game.gameId !in seenIds) {
        val game = primary.game
        val (x, y) = calculateGameCoordinates(game.tags, game.primaryGenre, game.gameId)
        nodes.add(
            GameNode(
                id = game.gameId,
                title = game.title,
                x = x,
                y = y,
                type = if (primary.affinityScore >= 70) NodeType.Liked else NodeType.Pending,
                coverUrl = game.externalCoverUrl ?: game.coverPath,
            )
        )
        seenIds.add(game.gameId)
    }

    for (alt in (playNext?.alternatives ?: emptyList())) {
        if (alt.game.gameId in seenIds) continue
        val game = alt.game
        val (x, y) = calculateGameCoordinates(game.tags, game.primaryGenre, game.gameId)
        nodes.add(
            GameNode(
                id = game.gameId,
                title = game.title,
                x = x,
                y = y,
                type = NodeType.Pending,
                coverUrl = game.externalCoverUrl ?: game.coverPath,
            )
        )
        seenIds.add(game.gameId)
    }

    return nodes
}

internal fun ProductGameState.toMapNodeType(): NodeType = when {
    excluded -> NodeType.Avoided
    rating != null && rating >= 4.0 -> NodeType.Liked
    else -> NodeType.Pending
}

private fun ProductTasteHistoryEntry.toMapNodeType(): NodeType = when (tone) {
    "positive" -> NodeType.Liked
    "negative" -> NodeType.Avoided
    else -> NodeType.Pending
}

private fun buildKnownGames(
    picks: List<RankedSeedGame>,
    playNext: ProductPlayNextModel?,
): Map<String, SeedGame> = buildMap {
    picks.forEach { put(it.game.gameId, it.game) }
    playNext?.primary?.let { put(it.game.gameId, it.game) }
    playNext?.alternatives.orEmpty().forEach { put(it.game.gameId, it.game) }
}
