package com.meetarp.shimmertextview

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class ShimmerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val textTracePath = Path()
    private val tracePaint: Paint = Paint()
        .also { it.color = ContextCompat.getColor(context, android.R.color.darker_gray) }
    private val boundsRect = RectF()

    private var shimmerWidth = 0.33f
    private val shimmerPaint = Paint()
        .also { it.alpha = 0x40 } // Hex for 25% alpha

    private var shimmerColor = ContextCompat.getColor(context, android.R.color.white)

    private val shimmerShape = Path()
    private val shimmerShapeOffset = Path()
    private val shimmerPath = Path()

    private var shimmerAnimator: Animator? = null
    private var shimmerProgress = 0

    private val transparent = ContextCompat.getColor(context, android.R.color.transparent)

    override fun onDraw(canvas: Canvas?) {
        if (shimmerAnimator == null) {
            super.onDraw(canvas)
            return
        }

        canvas ?: return
        canvas.drawPath(textTracePath, tracePaint)

        updateShimmerShader()
        shimmerPath.reset()

        // Copy the shape and offset it. Easier to copy+offset than to reset previous offsets.
        shimmerShapeOffset.reset()
        shimmerShapeOffset.set(shimmerShape)
        shimmerShapeOffset.offset(
            boundsRect.left + (boundsRect.right * (shimmerProgress / 100f)),
            boundsRect.top
        )

        // Draw only the intersection of the (offset) shape and traced paths for shimmering
        shimmerPath.op(shimmerShapeOffset, textTracePath, Path.Op.INTERSECT)
        canvas.drawPath(shimmerPath, shimmerPaint)
    }

    /**
     * Start the shimmer animation over the traced silhouette.
     * @param shimmerSpeed The period of the shimmer in milliseconds. Default 1000.
     */
    fun startShimmer(shimmerSpeed: Long = 1000) {
        shimmerProgress = 0
        shimmerAnimator = ValueAnimator.ofInt(0, 100)
            .also { anim ->
                anim.duration = shimmerSpeed
                anim.interpolator = FastOutSlowInInterpolator()
                anim.addUpdateListener {
                    shimmerProgress = anim.animatedValue as Int
                    invalidate()
                }
                anim.repeatCount = ValueAnimator.INFINITE
                anim.repeatMode = ValueAnimator.RESTART
                anim.start()
            }
    }

    /**
     * Stop the shimmer animation over the traced silhouette.
     */
    fun stopShimmer() {
        shimmerAnimator?.cancel()

        shimmerAnimator = null
        shimmerProgress = 0
        invalidate()
    }

    /**
     * Set the [color] for the traced silhouette segments.
     * Must be a color resource integer.
     * If not set specifically, the default is [android.R.color.darker_gray]
     */
    fun setTraceColor(@ColorRes color: Int) {
        tracePaint.color = ContextCompat.getColor(context, color)
        invalidate()
    }

    /**
     * Set the [color] for the shimmer that animates when [startShimmer] is running.
     * Must be a color resource integer.
     * If not set specifically, the default is [android.R.color.white]
     */
    fun setShimmerColor(@ColorRes color: Int) {
        shimmerColor = ContextCompat.getColor(context, color)
        invalidate()
    }

    private fun updateShimmerShader() {
        val shimmerStartPos = boundsRect.left + (boundsRect.right * (shimmerProgress / 100f))
        val shimmerShapeWidth = boundsRect.width() * shimmerWidth
        shimmerPaint.shader = LinearGradient(
            shimmerStartPos,
            0f,
            shimmerStartPos + shimmerShapeWidth,
            0f,
            intArrayOf(
                transparent,
                shimmerColor,
                shimmerColor,
                transparent
            ),
            floatArrayOf(0.0f, 0.25f, 0.75f, 1.0f),
            Shader.TileMode.REPEAT
        )
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        post {
            updateTextTracePath()
            invalidate()
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun updateTextTracePath() {
        textTracePath.reset()

        val textContent = text.toString()
        val lineCount = lineCount
        val textHeight = paint.fontMetrics.run { descent - ascent }

        // Based on how FrameLayout reads gravity properties for its children
        val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
        val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

        var xOffset = 0
        var yOffset = 0

        // Since yOffset doesn't depend on a value that changes per-line, calculate before the loop
        when (verticalGravity) {
            Gravity.TOP ->
                yOffset = 0
            Gravity.CENTER_VERTICAL ->
                yOffset = (measuredHeight - textHeight.toInt() * lineCount) / 2
            Gravity.BOTTOM ->
                yOffset = measuredHeight - (textHeight.toInt() * lineCount)
        }

        val textBounds = Rect()
        for (line in 0 until lineCount) {
            val textForLine = textContent.substring(
                layout.getLineStart(line),
                layout.getLineEnd(line)
            )

            paint.getTextBounds(textForLine, 0, textForLine.length, textBounds)

            when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.LEFT ->
                    xOffset = 0
                Gravity.CENTER_HORIZONTAL ->
                    xOffset = (measuredWidth - textBounds.width()) / 2
                Gravity.RIGHT ->
                    xOffset = measuredWidth - textBounds.width()
            }

            val lineOffset = (line * textHeight)
            val rect = RectF(
                xOffset + SPACE,
                yOffset + lineOffset + SPACE,
                xOffset + textBounds.width() - SPACE,
                yOffset + lineOffset - SPACE + textHeight
            )
            textTracePath.addRoundRect(rect, R_RECT_RADIUS, R_RECT_RADIUS, Path.Direction.CW)
        }

        textTracePath.computeBounds(boundsRect, true)

        shimmerShape.reset()
        shimmerShape.lineTo(boundsRect.width() * shimmerWidth, 0f)
        shimmerShape.lineTo(boundsRect.width() * shimmerWidth, boundsRect.height())
        shimmerShape.lineTo(0f, boundsRect.height())
        shimmerShape.close()
    }

    companion object {
        private const val SPACE = 2.5f
        private const val R_RECT_RADIUS = 20f
    }

}