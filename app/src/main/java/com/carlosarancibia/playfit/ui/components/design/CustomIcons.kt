package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SparklesIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                quadraticTo(w / 2f, h / 2f, w, h / 2f)
                quadraticTo(w / 2f, h / 2f, w / 2f, h)
                quadraticTo(w / 2f, h / 2f, 0f, h / 2f)
                quadraticTo(w / 2f, h / 2f, w / 2f, 0f)
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

@Composable
fun SunIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val radius = w * 0.25f
            drawCircle(color = color, radius = radius, center = center)
            val rayLength = w * 0.12f
            val rayThickness = w * 0.06f
            for (i in 0 until 8) {
                val angle = i * Math.PI / 4
                val startX = (w / 2f + Math.cos(angle) * (radius + 2.dp.toPx())).toFloat()
                val startY = (h / 2f + Math.sin(angle) * (radius + 2.dp.toPx())).toFloat()
                val endX = (w / 2f + Math.cos(angle) * (radius + 2.dp.toPx() + rayLength)).toFloat()
                val endY = (h / 2f + Math.sin(angle) * (radius + 2.dp.toPx() + rayLength)).toFloat()
                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = rayThickness,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MoonIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.35f, h * 0.15f)
                cubicTo(w * 0.85f, h * 0.15f, w * 0.85f, h * 0.85f, w * 0.35f, h * 0.85f)
                cubicTo(w * 0.65f, h * 0.70f, w * 0.65f, h * 0.30f, w * 0.35f, h * 0.15f)
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

@Composable
fun CompassIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.42f
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = w * 0.08f)
        )
        val needlePath = Path().apply {
            moveTo(w / 2f, h * 0.23f)
            lineTo(w * 0.64f, h / 2f)
            lineTo(w / 2f, h * 0.77f)
            lineTo(w * 0.36f, h / 2f)
            close()
        }
        drawPath(path = needlePath, color = color)
        drawLine(
            color = color.copy(alpha = PlayfitOpacities.half),
            start = Offset(w / 2f, h * 0.23f),
            end = Offset(w / 2f, h * 0.77f),
            strokeWidth = w * 0.04f
        )
        }
    }
}

@Composable
fun LoginIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        val bracketPath = Path().apply {
            moveTo(w * 0.70f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.35f)
            moveTo(w * 0.40f, h * 0.65f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.70f, h * 0.85f)
        }
        drawPath(
            path = bracketPath,
            color = color,
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )
        val arrowPath = Path().apply {
            moveTo(w * 0.15f, h / 2f)
            lineTo(w * 0.60f, h / 2f)
            moveTo(w * 0.43f, h * 0.33f)
            lineTo(w * 0.60f, h / 2f)
            lineTo(w * 0.43f, h * 0.67f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        }
    }
}

@Composable
fun GamepadIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = height * 0.15f,
                    right = width,
                    bottom = height * 0.85f,
                    radiusX = width * 0.25f,
                    radiusY = height * 0.25f
                )
            )
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))

        val dpadCenter = Offset(width * 0.25f, height * 0.5f)
        val dpadSize = width * 0.12f
        drawRect(
            color = color,
            topLeft = Offset(dpadCenter.x - dpadSize / 3, dpadCenter.y - dpadSize),
            size = Size(dpadSize * 2 / 3, dpadSize * 2)
        )
        drawRect(
            color = color,
            topLeft = Offset(dpadCenter.x - dpadSize, dpadCenter.y - dpadSize / 3),
            size = Size(dpadSize * 2, dpadSize * 2 / 3)
        )

        val btnCenter = Offset(width * 0.75f, height * 0.5f)
        val btnRad = width * 0.07f
        drawCircle(color = color, radius = btnRad, center = Offset(btnCenter.x + btnRad * 1.2f, btnCenter.y - btnRad * 0.6f))
        drawCircle(color = color, radius = btnRad, center = Offset(btnCenter.x - btnRad * 1.2f, btnCenter.y + btnRad * 0.6f))
        }
    }
}

@Composable
fun LaptopIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val width = size.width
        val height = size.height

        val screenWidth = width * 0.8f
        val screenHeight = height * 0.55f
        val screenLeft = width * 0.1f
        val screenTop = height * 0.15f

        val screenPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = screenLeft,
                    top = screenTop,
                    right = screenLeft + screenWidth,
                    bottom = screenTop + screenHeight,
                    radiusX = 4.dp.toPx(),
                    radiusY = 4.dp.toPx()
                )
            )
        }
        drawPath(path = screenPath, color = color, style = Stroke(width = 2.dp.toPx()))

        val baseLeft = width * 0.03f
        val baseRight = width * 0.97f
        val baseTop = screenTop + screenHeight
        val baseBottom = height * 0.85f

        val basePath = Path().apply {
            moveTo(screenLeft + 8.dp.toPx(), baseTop)
            lineTo(screenLeft + screenWidth - 8.dp.toPx(), baseTop)
            lineTo(baseRight, baseBottom - 2.dp.toPx())
            lineTo(baseLeft, baseBottom - 2.dp.toPx())
            close()
        }
        drawPath(path = basePath, color = color, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
fun TvIcon(modifier: Modifier = Modifier, color: Color, contentDescription: String? = null) {
    Box(modifier = modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        Canvas(modifier = Modifier.matchParentSize()) {
        val width = size.width
        val height = size.height

        val tvWidth = width * 0.85f
        val tvHeight = height * 0.6f
        val tvLeft = width * 0.075f
        val tvTop = height * 0.25f

        val tvPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = tvLeft,
                    top = tvTop,
                    right = tvLeft + tvWidth,
                    bottom = tvTop + tvHeight,
                    radiusX = 6.dp.toPx(),
                    radiusY = 6.dp.toPx()
                )
            )
        }
        drawPath(path = tvPath, color = color, style = Stroke(width = 2.dp.toPx()))

        drawLine(
            color = color,
            start = Offset(tvLeft + tvWidth * 0.2f, tvTop + tvHeight),
            end = Offset(tvLeft + tvWidth * 0.1f, height * 0.95f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(tvLeft + tvWidth * 0.8f, tvTop + tvHeight),
            end = Offset(tvLeft + tvWidth * 0.9f, height * 0.95f),
            strokeWidth = 2.dp.toPx()
        )

        val center = Offset(width * 0.5f, tvTop)
        drawLine(
            color = color,
            start = center,
            end = Offset(width * 0.25f, height * 0.05f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(width * 0.75f, height * 0.05f),
            strokeWidth = 2.dp.toPx()
        )
        }
    }
}
