package com.example.dartscore.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Standard clockwise order starting at top (20). */
private val SEGMENT_NUMBERS = intArrayOf(
    20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
    3, 19, 7, 16, 8, 11, 14, 9, 12, 5
)

private val BoardBlack = Color(0xFF121212)
private val BoardCream = Color(0xFFE8DCC8)
private val BoardRed = Color(0xFFC62828)
private val BoardGreen = Color(0xFF2E7D32)
private val BoardWire = Color(0xFFB0B0B0)
private val NumberRing = Color(0xFF0A0A0A)
private val OuterRim = Color(0xFF2A2A2A)

/**
 * Realistic dartboard drawn with correct segment order, doubles/triples,
 * bull, and wire spider. Used on home, login, and in-game.
 */
@Composable
fun DartboardCanvas(
    modifier: Modifier = Modifier,
    showNumbers: Boolean = true,
    rotationDegrees: Float = 0f
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = min(size.width, size.height) / 2f

        if (rotationDegrees != 0f) {
            rotate(rotationDegrees, pivot = Offset(cx, cy)) {
                drawDartboard(cx, cy, maxR, showNumbers)
            }
        } else {
            drawDartboard(cx, cy, maxR, showNumbers)
        }
    }
}

private fun DrawScope.drawDartboard(
    cx: Float,
    cy: Float,
    maxR: Float,
    showNumbers: Boolean
) {
    // Radius ratios relative to outer board (including number ring).
    val numberOuter = maxR
    val scoringOuter = maxR * 0.82f
    val doubleOuter = scoringOuter
    val doubleInner = scoringOuter * 0.953f
    val tripleOuter = scoringOuter * 0.629f
    val tripleInner = scoringOuter * 0.582f
    val outerBull = scoringOuter * 0.095f
    val innerBull = scoringOuter * 0.047f

    // Soft shadow / depth under the board
    drawCircle(
        color = Color(0x66000000),
        radius = maxR * 1.02f,
        center = Offset(cx + maxR * 0.02f, cy + maxR * 0.03f)
    )

    // Outer black number ring + rim
    drawCircle(color = OuterRim, radius = numberOuter, center = Offset(cx, cy))
    drawCircle(color = NumberRing, radius = numberOuter * 0.97f, center = Offset(cx, cy))

    val segmentAngle = 18f // 360 / 20
    // Offset so segment 20 is centered at top (−90° in canvas coords).
    val startOffset = -90f - segmentAngle / 2f

    for (i in 0 until 20) {
        val startAngle = startOffset + i * segmentAngle
        val isDark = i % 2 == 0
        val singleColor = if (isDark) BoardBlack else BoardCream
        val multiColor = if (isDark) BoardRed else BoardGreen

        // Single (whole pie to double outer), then overlay rings
        drawSegment(cx, cy, 0f, doubleOuter, startAngle, segmentAngle, singleColor)
        // Double ring
        drawSegment(cx, cy, doubleInner, doubleOuter, startAngle, segmentAngle, multiColor)
        // Triple ring (redraw single under triple, then triple on top)
        drawSegment(cx, cy, tripleInner, tripleOuter, startAngle, segmentAngle, multiColor)
    }

    // Wire spider — radial lines
    val wireWidth = (maxR * 0.006f).coerceAtLeast(0.8f)
    for (i in 0 until 20) {
        val angleRad = Math.toRadians((startOffset + i * segmentAngle).toDouble())
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()
        drawLine(
            color = BoardWire,
            start = Offset(cx + outerBull * cosA, cy + outerBull * sinA),
            end = Offset(cx + doubleOuter * cosA, cy + doubleOuter * sinA),
            strokeWidth = wireWidth
        )
    }

    // Concentric wire rings
    listOf(doubleOuter, doubleInner, tripleOuter, tripleInner, outerBull).forEach { r ->
        drawCircle(
            color = BoardWire,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = wireWidth)
        )
    }

    // Outer / double bull
    drawCircle(color = BoardGreen, radius = outerBull, center = Offset(cx, cy))
    drawCircle(
        color = BoardWire,
        radius = outerBull,
        center = Offset(cx, cy),
        style = Stroke(width = wireWidth)
    )
    drawCircle(color = BoardRed, radius = innerBull, center = Offset(cx, cy))
    drawCircle(
        color = BoardWire,
        radius = innerBull,
        center = Offset(cx, cy),
        style = Stroke(width = wireWidth * 0.8f)
    )

    // Thin highlight rim
    drawCircle(
        color = Color(0x44FFFFFF),
        radius = numberOuter * 0.995f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.dp.toPx())
    )

    if (showNumbers && maxR > 40.dp.toPx()) {
        drawNumbers(cx, cy, scoringOuter, numberOuter, startOffset, segmentAngle)
    }
}

private fun DrawScope.drawSegment(
    cx: Float,
    cy: Float,
    innerR: Float,
    outerR: Float,
    startAngle: Float,
    sweep: Float,
    color: Color
) {
    val path = Path().apply {
        // Compose arc angles: 0° = 3 o'clock, clockwise positive
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                offset = Offset(cx - outerR, cy - outerR),
                size = Size(outerR * 2, outerR * 2)
            ),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = sweep,
            forceMoveTo = true
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                offset = Offset(cx - innerR, cy - innerR),
                size = Size(innerR * 2, innerR * 2)
            ),
            startAngleDegrees = startAngle + sweep,
            sweepAngleDegrees = -sweep,
            forceMoveTo = false
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawNumbers(
    cx: Float,
    cy: Float,
    scoringOuter: Float,
    numberOuter: Float,
    startOffset: Float,
    segmentAngle: Float
) {
    val numberRadius = (scoringOuter + numberOuter) / 2f
    val textSizePx = (numberOuter - scoringOuter) * 0.55f
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textSize = textSizePx
    }

    drawContext.canvas.nativeCanvas.apply {
        for (i in 0 until 20) {
            val midAngle = Math.toRadians((startOffset + i * segmentAngle + segmentAngle / 2.0))
            val x = cx + numberRadius * cos(midAngle).toFloat()
            val y = cy + numberRadius * sin(midAngle).toFloat() - (paint.descent() + paint.ascent()) / 2f
            drawText(SEGMENT_NUMBERS[i].toString(), x, y, paint)
        }
    }
}
