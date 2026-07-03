package com.epn.gravitygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Obstacle(
    val rect: RectF
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(231, 130, 112)
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(185, 28, 28)
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(30, 248, 113, 113)
    }

    fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rect, 28f, 28f, innerGlowPaint)
        canvas.drawRoundRect(rect, 28f, 28f, paint)
        canvas.drawRoundRect(rect, 28f, 28f, borderPaint)
    }
}
