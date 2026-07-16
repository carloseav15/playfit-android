package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

data class GameNode(
    val id: String,
    val title: String,
    val x: Double,
    val y: Double,
    val type: NodeType,
    val coverUrl: String? = null,
)

enum class NodeType { Liked, Avoided, Pending }

private fun nodeColor(type: NodeType, colorScheme: ColorScheme): Color = when (type) {
    NodeType.Liked -> colorScheme.primary
    NodeType.Avoided -> colorScheme.error
    NodeType.Pending -> colorScheme.outline
}

private fun nodeTypeLabel(type: NodeType): String = when (type) {
    NodeType.Liked -> "Liked"
    NodeType.Avoided -> "Avoided"
    NodeType.Pending -> "Saved Pick"
}

fun calculateGameCoordinates(
    tags: List<String>,
    primaryGenre: String,
    gameId: String,
): Pair<Double, Double> {
    var x = 0.0
    var y = 0.0

    val demandingKeywords = listOf("souls", "unforgiving", "demanding", "survival", "tactical", "stealth", "combat", "challenging", "difficult", "hardcore")
    val chillKeywords = listOf("chill", "cozy", "accessible", "casual", "pick_up", "lighthearted", "relaxing", "peaceful", "wholesome")
    val systemsKeywords = listOf("open_world", "sandbox", "roguelike", "roguelite", "puzzle", "rhythm", "deck", "systems", "simulation", "builder", "crafting", "procedural", "emergent")
    val storyKeywords = listOf("story", "lore", "narrative", "linear", "horror", "dark", "text", "branching", "dialogue", "atmospheric", "immersive", "worldbuilding")

    for (tag in tags) {
        val t = tag.lowercase().replace(" ", "_").replace("-", "_")
        if (demandingKeywords.any { t.contains(it) || it.contains(t) }) x += 28.0
        if (chillKeywords.any { t.contains(it) || it.contains(t) }) x -= 28.0
        if (systemsKeywords.any { t.contains(it) || it.contains(t) }) y += 28.0
        if (storyKeywords.any { t.contains(it) || it.contains(t) }) y -= 28.0
    }

    if (x == 0.0 && y == 0.0) {
        val genre = primaryGenre.lowercase()
        when {
            genre.contains("rpg") || genre.contains("role_playing") -> {
                x += 10.0; y -= 20.0
            }
            genre.contains("action") || genre.contains("shooter") -> {
                x += 20.0; y += 15.0
            }
            genre.contains("adventure") || genre.contains("indie") -> {
                x -= 15.0; y -= 15.0
            }
            genre.contains("strategy") || genre.contains("simulation") -> {
                x += 25.0; y += 25.0
            }
            genre.contains("puzzle") || genre.contains("casual") -> {
                x -= 25.0; y += 20.0
            }
        }
    }

    var hash = 0
    for (char in gameId) {
        hash = char.code + (hash shl 5) - hash
    }
    val jitterX = ((hash % 16) - 8).toDouble()
    val jitterY = (((hash shr 4) % 16) - 8).toDouble()

    x += jitterX
    y += jitterY

    return Pair(
        maxOf(-90.0, minOf(90.0, x)),
        maxOf(-90.0, minOf(90.0, y)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasteMapVisualizerScreen(
    nodes: List<GameNode>,
    onBack: () -> Unit,
) {
    var activeNodeId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val activeNode = nodes.firstOrNull { it.id == activeNodeId } ?: nodes.firstOrNull()

    LaunchedEffect(activeNodeId) {
        if (activeNodeId != null) {
            val index = nodes.indexOfFirst { it.id == activeNodeId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }
    LaunchedEffect(nodes) {
        if (activeNodeId == null && nodes.isNotEmpty()) {
            activeNodeId = nodes.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Interactive Affinity Map",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayfitSpacing.md),
            ) {
                Text(
                    text = "Visual graph of your gaming traits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(PlayfitSpacing.md))

                if (nodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Rate some games to see your affinity map.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    AffinityMapCanvas(
                    nodes = nodes,
                    activeNodeId = activeNodeId,
                    onNodeTap = { nodeId -> activeNodeId = nodeId },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )

                Spacer(Modifier.height(PlayfitSpacing.md))

                if (nodes.isNotEmpty()) {
                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                        modifier = Modifier.padding(horizontal = PlayfitSpacing.xs),
                    ) {
                        items(nodes, key = { it.id }) { node ->
                            MapNodeCard(
                                node = node,
                                isSelected = activeNode?.id == node.id,
                                onClick = { activeNodeId = node.id },
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun AffinityMapCanvas(
    nodes: List<GameNode>,
    activeNodeId: String?,
    onNodeTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val quadrantLabelStyle = MaterialTheme.typography.labelSmall.copy(color = Color.Gray.copy(alpha = PlayfitOpacities.heavy))
    val axisLabelStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.Gray.copy(alpha = PlayfitOpacities.strong),
        fontWeight = FontWeight.Bold,
    )

    var canvasWidthPx by remember { mutableFloatStateOf(0f) }
    var canvasHeightPx by remember { mutableFloatStateOf(0f) }

    val nodeHitPositions = remember(nodes, canvasWidthPx, canvasHeightPx) {
        if (canvasWidthPx <= 0f || canvasHeightPx <= 0f) emptyMap()
        else {
            val canvasSize = minOf(canvasWidthPx, canvasHeightPx)
            val centerX = canvasWidthPx / 2f
            val centerY = canvasHeightPx / 2f
            val scale = canvasSize / 2f - 20f
            nodes.associate { node ->
                val cx = centerX + (node.x / 100.0 * scale).toFloat()
                val cy = centerY - (node.y / 100.0 * scale).toFloat()
                node.id to Offset(cx, cy)
            }
        }
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(20.dp),
            )
            .semantics {
                contentDescription = "Affinity map with ${nodes.size} games. Use the game cards below to select a map node."
            }
            .padding(20.dp)
            .onSizeChanged { size ->
                canvasWidthPx = size.width.toFloat()
                canvasHeightPx = size.height.toFloat()
            }
            .pointerInput(nodeHitPositions) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.pressed) {
                            val pos = change.position
                            for ((id, hitPos) in nodeHitPositions) {
                                val dist = kotlin.math.sqrt(
                                    (pos.x - hitPos.x) * (pos.x - hitPos.x) +
                                    (pos.y - hitPos.y) * (pos.y - hitPos.y)
                                )
                                if (dist <= 22f) {
                                    onNodeTap(id)
                                    break
                                }
                            }
                            change.consume()
                        }
                    }
                }
            },
    ) {
        fun DrawScope.drawCenteredText(text: String, style: TextStyle, x: Float, y: Float) {
            val measured = textMeasurer.measure(text = text, style = style)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(x - measured.size.width / 2f, y - measured.size.height / 2f),
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = minOf(size.width, size.height)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val scale = canvasSize / 2f - 20f
            val gridColor = Color.Black.copy(alpha = PlayfitOpacities.faint)
            val axisColor = Color.Black.copy(alpha = PlayfitOpacities.subtle)

            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            drawCircle(color = gridColor, radius = scale * 2f, center = Offset(centerX, centerY),
                style = Stroke(width = 1f, pathEffect = dashPathEffect))
            drawCircle(color = gridColor, radius = scale * 1.2f, center = Offset(centerX, centerY),
                style = Stroke(width = 1f, pathEffect = dashPathEffect))
            drawCircle(color = gridColor, radius = scale * 0.5f, center = Offset(centerX, centerY),
                style = Stroke(width = 1f, pathEffect = dashPathEffect))

            val axisPath = Path().apply {
                moveTo(20f, centerY)
                lineTo(size.width - 20f, centerY)
                moveTo(centerX, 20f)
                lineTo(centerX, size.height - 20f)
            }
            drawPath(axisPath, color = axisColor, style = Stroke(width = 1.2f))

            listOf(
                "Chill & Open World" to Offset(centerX - scale * 0.6f, centerY - scale * 0.6f),
                "Complex & Systems" to Offset(centerX + scale * 0.6f, centerY - scale * 0.6f),
                "Cozy & Story-Rich" to Offset(centerX - scale * 0.6f, centerY + scale * 0.6f),
                "Demanding & Linear" to Offset(centerX + scale * 0.6f, centerY + scale * 0.6f),
            ).forEach { (label, pos) ->
                drawCenteredText(label, quadrantLabelStyle, pos.x, pos.y)
            }

            drawCenteredText("Demanding →", axisLabelStyle, size.width - 45f, centerY - 10f)
            drawCenteredText("← Cozy", axisLabelStyle, 40f, centerY - 10f)
            drawCenteredText("Systems ↑", axisLabelStyle, centerX - 35f, 35f)
            drawCenteredText("Story ↓", axisLabelStyle, centerX - 25f, size.height - 35f)

            nodes.forEach { node ->
                val cx = centerX + (node.x / 100.0 * scale).toFloat()
                val cy = centerY - (node.y / 100.0 * scale).toFloat()
                val isSelected = node.id == activeNodeId
                val color = nodeColor(node.type, colorScheme)

                drawCircle(color = color.copy(alpha = if (isSelected) PlayfitOpacities.medium else PlayfitOpacities.light),
                    radius = if (isSelected) 24f else 16f, center = Offset(cx, cy))
                drawCircle(color = color, radius = if (isSelected) 10f else 8f, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = if (isSelected) 10f else 8f,
                    center = Offset(cx, cy), style = Stroke(width = 1.2f))
            }
        }
    }
}

@Composable
private fun MapNodeCard(
    node: GameNode,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val nodeCol = nodeColor(node.type, MaterialTheme.colorScheme)

    Card(
        modifier = Modifier
            .width(104.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${node.title}, ${nodeTypeLabel(node.type)}${if (isSelected) ", selected" else ""}"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                nodeCol.copy(alpha = PlayfitOpacities.light)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(PlayfitSpacing.sm),
        ) {
            PlayfitCoverArt(
                gameId = node.id,
                title = node.title,
                coverUrl = node.coverUrl,
                modifier = Modifier
                    .width(88.dp)
                    .height(64.dp),
                decorative = true,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = node.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = nodeTypeLabel(node.type),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                        color = nodeCol,
                        modifier = Modifier
                            .background(
                                color = nodeCol.copy(alpha = PlayfitOpacities.soft),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}
