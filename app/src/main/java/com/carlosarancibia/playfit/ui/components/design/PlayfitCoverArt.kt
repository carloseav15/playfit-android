package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val boxRatio = 0.75f

private fun hashToHue(id: String): Float {
    var hash = 0
    for (char in id) {
        hash = char.code + (hash shl 5) - hash
    }
    return ((((hash * 137.5) % 360) + 360) % 360).toFloat()
}

private fun initials(title: String): String {
    return title
        .replace(Regex("[^\\w\\s]"), "")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifEmpty { "?" }
}

@Composable
fun PlayfitCoverArt(
    gameId: String,
    title: String,
    modifier: Modifier = Modifier,
    coverUrl: String? = null,
    decorative: Boolean = false,
) {
    val hue = hashToHue(gameId)
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color.hsl(hue, 0.4f, 0.18f),
            Color.hsl((hue + 50) % 360, 0.3f, 0.26f),
        ),
    )

    // Resolve relative urls by prepending the host base URL
    val resolvedUrl = if (coverUrl != null && !coverUrl.startsWith("http")) {
        val baseUrl = com.carlosarancibia.playfit.BuildConfig.API_BASE_URL.removeSuffix("/")
        val relativePath = coverUrl.removePrefix("/")
        "$baseUrl/$relativePath"
    } else {
        coverUrl
    }

    val loadingBackdrop = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(boxRatio)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (resolvedUrl.isNullOrEmpty()) Modifier.background(gradient)
                else Modifier.background(loadingBackdrop)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!resolvedUrl.isNullOrEmpty()) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = if (decorative) null else "Cover art for $title",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(boxRatio),
                contentScale = ContentScale.Crop,
            )
        } else {
            androidx.compose.material3.Text(
                text = initials(title),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 0.15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
