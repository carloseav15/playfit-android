package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.model.ProductTasteMapTrait
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun TasteRadarChart(
    traits: List<ProductTasteMapTrait>,
    modifier: Modifier = Modifier
) {
    if (traits.isEmpty()) return

    // Pick top positive (up to 4) and negative (up to 1) traits matching Web memo
    val radarTraits = remember(traits) {
        val posList = traits.filter { it.direction == "positive" && it.strength > 0 }
            .sortedByDescending { it.strength }
        val negList = traits.filter { it.direction == "negative" && it.strength > 0 }
            .sortedByDescending { it.strength }
            
        if (negList.isNotEmpty()) {
            posList.take(4) + negList.take(1)
        } else {
            posList.take(5)
        }
    }

    val n = radarTraits.size
    if (n < 3) return // Radar charts require at least 3 axes

    val maxVal = remember(traits) { traits.maxOfOrNull { it.strength } ?: 1.0 }
    
    val accentColor = PlayfitExtendedTheme.colors.playfitAccent
    val positiveColor = PlayfitExtendedTheme.colors.playfitPositive
    val negativeColor = PlayfitExtendedTheme.colors.playfitNegative
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .semantics {
                contentDescription = "Radar chart: ${radarTraits.joinToString { t -> "${t.label} ${t.strength.toInt()}%" }}"
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.height * 0.33f

        // 1. Draw Grid Levels (circular polygon grids)
        val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val path = androidx.compose.ui.graphics.Path()
            for (i in 0 until n) {
                val angle = (i * 2 * Math.PI) / n - Math.PI / 2
                val currentRadius = level * radius
                val x = center.x + currentRadius * Math.cos(angle).toFloat()
                val y = center.y + currentRadius * Math.sin(angle).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = outlineColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw Spokes (axes lines)
        for (i in 0 until n) {
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val outerX = center.x + radius * Math.cos(angle).toFloat()
            val outerY = center.y + radius * Math.sin(angle).toFloat()
            
            drawLine(
                color = outlineColor,
                start = center,
                end = Offset(outerX, outerY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw axis text labels slightly further out
            val labelRadius = radius + 20.dp.toPx()
            val labelX = center.x + labelRadius * Math.cos(angle).toFloat()
            val labelY = center.y + labelRadius * Math.sin(angle).toFloat()
            
            val trait = radarTraits[i]
            val labelText = trait.label
            
            val textPaint = android.text.TextPaint().apply {
                color = (if (trait.direction == "positive") positiveColor else negativeColor).toArgb()
                textSize = textStyle.fontSize.toPx()
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                labelX,
                labelY + 4.dp.toPx(),
                textPaint
            )
        }

        // 3. Draw Radar Value Polygon
        val valuePath = androidx.compose.ui.graphics.Path()
        for (i in 0 until n) {
            val trait = radarTraits[i]
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val valRatio = if (maxVal > 0) trait.strength / maxVal else 0.5
            val currentRadius = valRatio.toFloat() * radius
            val x = center.x + currentRadius * Math.cos(angle).toFloat()
            val y = center.y + currentRadius * Math.sin(angle).toFloat()
            if (i == 0) {
                valuePath.moveTo(x, y)
            } else {
                valuePath.lineTo(x, y)
            }
        }
        valuePath.close()

        drawPath(
            path = valuePath,
            color = accentColor.copy(alpha = 0.22f)
        )
        drawPath(
            path = valuePath,
            color = accentColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 4. Draw Value Dots
        for (i in 0 until n) {
            val trait = radarTraits[i]
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val valRatio = if (maxVal > 0) trait.strength / maxVal else 0.5
            val currentRadius = valRatio.toFloat() * radius
            val x = center.x + currentRadius * Math.cos(angle).toFloat()
            val y = center.y + currentRadius * Math.sin(angle).toFloat()
            
            drawCircle(
                color = if (trait.direction == "positive") positiveColor else negativeColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
