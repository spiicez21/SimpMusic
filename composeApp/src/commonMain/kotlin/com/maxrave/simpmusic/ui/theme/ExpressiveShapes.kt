package com.maxrave.simpmusic.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A "6-side cookie" shape (Hexagonal with smooth scalloped lobes).
 */
val CookieShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.width / 2f
            val innerRadius = radius * 0.75f // Slightly softer scalloping
            val numLobes = 8
            
            for (i in 0 until numLobes) {
                val angle = (i * 2 * PI / numLobes) - PI / 2
                val nextAngle = ((i + 1) * 2 * PI / numLobes) - PI / 2
                val midAngle = (angle + nextAngle) / 2
                
                val x1 = centerX + radius * cos(angle).toFloat()
                val y1 = centerY + radius * sin(angle).toFloat()
                
                val mx = centerX + innerRadius * cos(midAngle).toFloat()
                val my = centerY + innerRadius * sin(midAngle).toFloat()
                
                val nextX = centerX + radius * cos(nextAngle).toFloat()
                val nextY = centerY + radius * sin(nextAngle).toFloat()

                if (i == 0) {
                    moveTo(x1, y1)
                }
                
                // Use quadraticTo to create a smooth, rounded lobe instead of a sharp point
                quadraticTo(mx, my, nextX, nextY)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A "Square Pentagon" shape with balanced rounded corners.
 */
val PentagonShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.width / 2f
            val numSides = 5
            val cornerRadius = radius * 0.2f // Adjust for desired roundness
            
            for (i in 0 until numSides) {
                val angle = (i * 2 * PI / numSides) - PI / 2
                val nextAngle = ((i + 1) * 2 * PI / numSides) - PI / 2
                
                // Current vertex
                val vx = centerX + radius * cos(angle).toFloat()
                val vy = centerY + radius * sin(angle).toFloat()
                
                // Next vertex
                val nvx = centerX + radius * cos(nextAngle).toFloat()
                val nvy = centerY + radius * sin(nextAngle).toFloat()
                
                // Points for rounded corner
                val anglePrev = angle - (PI / numSides)
                val angleNext = angle + (PI / numSides)
                
                val p1x = vx + cornerRadius * cos(angle + PI + PI / 2).toFloat()
                val p1y = vy + cornerRadius * sin(angle + PI + PI / 2).toFloat()
                
                // Simpler approach: find point on line before and after vertex
                val dx1 = vx - centerX
                val dy1 = vy - centerY
                val mag1 = kotlin.math.sqrt(dx1 * dx1 + dy1 * dy1)
                
                val dx2 = nvx - centerX
                val dy2 = nvy - centerY
                val mag2 = kotlin.math.sqrt(dx2 * dx2 + dy2 * dy2)

                // Actually, let's just use a simpler point calculation for each corner
                val startAngle = angle - 0.2f
                val endAngle = angle + 0.2f
                
                val sx = centerX + radius * cos(startAngle).toFloat()
                val sy = centerY + radius * sin(startAngle).toFloat()
                
                val ex = centerX + radius * cos(endAngle).toFloat()
                val ey = centerY + radius * sin(endAngle).toFloat()

                if (i == 0) {
                    moveTo(sx, sy)
                }
                
                // Curve around the vertex
                quadraticTo(vx, vy, ex, ey)
                
                // Line to the next corner's start
                val nextStartAngle = nextAngle - 0.2f
                val nsx = centerX + radius * cos(nextStartAngle).toFloat()
                val nsy = centerY + radius * sin(nextStartAngle).toFloat()
                lineTo(nsx, nsy)
            }
            close()
        }
        return Outline.Generic(path)
    }
}
