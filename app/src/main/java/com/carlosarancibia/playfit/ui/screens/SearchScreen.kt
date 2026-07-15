package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.familyDisplayName
import com.carlosarancibia.playfit.model.sortedPlatformFamilies
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.ShimmerCard
import com.carlosarancibia.playfit.ui.viewmodel.SearchUiState

private fun searchResultSubtitle(game: SeedGame): String {
    val platformNames = game.availablePlatformNames.take(3).joinToString(", ")
    return listOf(game.primaryGenre, game.releaseYear.orEmpty(), platformNames)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
}

@Composable
fun SearchScreen(
    searchState: SearchUiState,
    platforms: List<Platform>,
    onQueryChange: (String) -> Unit,
    onFamilyChange: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenGame: (String) -> Unit,
) {
    val families = sortedPlatformFamilies(platforms)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PlayfitSpacing.md),
        contentPadding = PaddingValues(vertical = PlayfitSpacing.md),
    ) {
        item {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(PlayfitSpacing.md))
            OutlinedTextField(
                value = searchState.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by title...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(PlayfitSpacing.md))
        }

        if (families.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PlayfitSpacing.md),
                ) {
                    families.forEach { family ->
                        FilterChip(
                            selected = searchState.selectedFamily == family,
                            onClick = { onFamilyChange(family) },
                            label = { Text(familyDisplayName(family)) },
                        )
                    }
                }
            }
        }

        when {
            searchState.loading && searchState.results.isEmpty() -> {
                items(6) { ShimmerCard() }
            }
            searchState.error != null && searchState.results.isEmpty() -> {
                item { SearchErrorState(message = searchState.error, onRetry = onRetry) }
            }
            searchState.results.isEmpty() -> {
                item {
                    SearchEmptyState(
                        text = if (searchState.query.isBlank()) {
                            "Type a game title above to search."
                        } else {
                            "No games found matching your search."
                        },
                    )
                }
            }
            else -> {
                items(searchState.results, key = { it.gameId }) { game ->
                    SearchResultRow(game = game, onClick = { onOpenGame(game.gameId) })
                }
            }
        }

        if (searchState.hasMore) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(PlayfitSpacing.md))
                    OutlinedButton(
                        onClick = onLoadMore,
                        enabled = !searchState.loadingMore,
                    ) {
                        Text(if (searchState.loadingMore) "Loading..." else "Load more")
                    }
                    Spacer(Modifier.height(PlayfitSpacing.md))
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    game: SeedGame,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PlayfitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayfitCoverArt(
            gameId = game.gameId,
            title = game.title,
            coverUrl = game.externalCoverUrl ?: game.coverPath,
            modifier = Modifier
                .width(44.dp)
                .height(60.dp),
        )
        Spacer(Modifier.width(PlayfitSpacing.md))
        Column {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val subtitle = searchResultSubtitle(game)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Search could not load",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(PlayfitSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))
        Button(onClick = onRetry) {
            Text("Try again", fontWeight = FontWeight.Bold)
        }
    }
}
