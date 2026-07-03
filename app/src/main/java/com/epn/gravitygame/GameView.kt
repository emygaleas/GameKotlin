package com.epn.gravitygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

class GameView(context: Context) : View(context) {

    private val ball = Ball()
    private val target = Target()
    private val obstacles = mutableListOf<Obstacle>()

    private var sensorX = 0f
    private var sensorY = 0f
    private var score = 0
    private var lives = 3
    private var started = false
    private var gameOver = false

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(40, 148, 163, 184)
        strokeWidth = 2f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        isFakeBoldText = true
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 255, 255)
        textSize = 32f
    }

    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 255, 255)
        textSize = 24f
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 199, 210, 254)
        textSize = 28f
    }

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(79, 70, 229)
    }

    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        isFakeBoldText = true
    }

    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 10, 14, 28)
    }

    private val targetGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(48, 34, 197, 94)
    }

    private val ballGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(35, 168, 85, 247)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(65, 148, 163, 184)
        strokeWidth = 3f
    }

    fun updateSensorValues(x: Float, y: Float) {
        if (!started || gameOver) return
        sensorX = x
        sensorY = y
        updateGame()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureBackground(w, h)
        resetGame()
    }

    private fun configureBackground(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        backgroundPaint.shader = RadialGradient(
            width / 2f,
            height / 2f,
            max(width, height) * 0.8f,
            intArrayOf(Color.rgb(13, 18, 31), Color.rgb(28, 34, 51)),
            floatArrayOf(0.0f, 1.0f),
            Shader.TileMode.CLAMP
        )
    }

    private fun resetGame() {
        score = 0
        lives = 3
        gameOver = false
        started = false
        ball.position.set(width / 2f, height / 2f)
        target.relocate(width, height)
        createObstacles()
        invalidate()
    }

    private fun createObstacles() {
        obstacles.clear()
        if (width == 0 || height == 0) return
        obstacles.add(Obstacle(RectF(width * 0.18f, height * 0.34f, width * 0.48f, height * 0.39f)))
        obstacles.add(Obstacle(RectF(width * 0.55f, height * 0.55f, width * 0.86f, height * 0.60f)))
        obstacles.add(Obstacle(RectF(width * 0.25f, height * 0.73f, width * 0.62f, height * 0.78f)))
    }

    private fun updateGame() {
        ball.update(sensorX, sensorY, width, height)

        if (Collision.circleWithCircle(ball.position, ball.radius(), target.position, target.radius())) {
            score += 10
            target.relocate(width, height)
            vibrate(35)
        }

        obstacles.forEach { obstacle ->
            if (Collision.circleWithRect(ball.position, ball.radius(), obstacle.rect)) {
                lives--
                ball.position.set(width / 2f, height / 2f)
                vibrate(120)
                if (lives <= 0) {
                    gameOver = true
                }
                return@forEach
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawHeader(canvas)

        canvas.drawCircle(target.position.x, target.position.y, target.radius() + 28f, targetGlowPaint)
        canvas.drawCircle(ball.position.x, ball.position.y, ball.radius() + 22f, ballGlowPaint)

        target.draw(canvas)
        obstacles.forEach { it.draw(canvas) }
        ball.draw(canvas)

        if (!started) drawStartOverlay(canvas)
        if (gameOver) drawGameOver(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        val step = 72
        var x = 0
        while (x < width) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
            x += step
        }
        var y = 0
        while (y < height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
            y += step
        }
    }

    private fun drawHeader(canvas: Canvas) {
        canvas.drawRoundRect(24f, 24f, width - 24f, 168f, 28f, 28f, panelPaint)
        canvas.drawText("Gravity Ball Kotlin", 48f, 72f, titlePaint)
        canvas.drawText("Puntaje: $score    Vidas: $lives", 48f, 116f, textPaint)
        canvas.drawText("Inclina el celular y evita los bloques.", 48f, 154f, subtitlePaint)
        canvas.drawText("X: ${ball.position.x.roundToInt()}  Y: ${ball.position.y.roundToInt()}", width - 280f, 116f, smallPaint)
    }

    private fun drawStartOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(210, 7, 11, 26))
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(30, 59, 130, 246)
        }
        canvas.drawCircle(width * 0.25f, height * 0.25f, width * 0.24f, glow)
        canvas.drawCircle(width * 0.76f, height * 0.28f, width * 0.18f, glow)
        canvas.drawCircle(width * 0.5f, height * 0.72f, width * 0.22f, glow)

        val box = RectF(48f, height * 0.22f, width - 48f, height * 0.63f)
        canvas.drawRoundRect(box, 44f, 44f, panelPaint)
        canvas.drawText("Gravity Ball", box.left + 50f, box.top + 90f, titlePaint)
        canvas.drawText("Un desafío de gravedad y reflejos.", box.left + 50f, box.top + 150f, subtitlePaint)
        canvas.drawText("Toca cualquier lugar para comenzar", box.left + 50f, box.top + 210f, textPaint)
        canvas.drawText("Evita obstáculos y recoge objetivos.", box.left + 50f, box.top + 260f, smallPaint)

        val button = RectF(box.left + 50f, box.bottom - 110f, box.right - 50f, box.bottom - 40f)
        canvas.drawRoundRect(button, 24f, 24f, buttonPaint)
        canvas.drawText("Iniciar juego", button.left + 40f, button.centerY() + 10f, buttonTextPaint)
    }

    private fun drawGameOver(canvas: Canvas) {
        val box = RectF(60f, height * 0.30f, width - 60f, height * 0.58f)
        canvas.drawRoundRect(box, 36f, 36f, panelPaint)
        canvas.drawText("Juego terminado", box.left + 50f, box.top + 90f, titlePaint)
        canvas.drawText("Puntaje final: $score", box.left + 50f, box.top + 150f, textPaint)
        canvas.drawText("Toca para reiniciar", box.left + 50f, box.top + 205f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                resetGame()
            } else {
                started = true
            }
            invalidate()
            return true
        }
        return true
    }

    private fun vibrate(milliseconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }
}
