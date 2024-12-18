package com.atarusov.aston_intensiv_2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.DecelerateInterpolator
import androidx.core.animation.ValueAnimator
import androidx.core.os.BundleCompat
import kotlin.math.min

class SpinningWheel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    enum class SectorType {
        TEXT, IMG
    }

    private val sectors = listOf(
        Pair(Color.parseColor("#e81416"), SectorType.TEXT),
        Pair(Color.parseColor("#ffa500"), SectorType.IMG),
        Pair(Color.parseColor("#faeb36"), SectorType.TEXT),
        Pair(Color.parseColor("#79c314"), SectorType.IMG),
        Pair(Color.parseColor("#487de7"), SectorType.TEXT),
        Pair(Color.parseColor("#4b369d"), SectorType.IMG),
        Pair(Color.parseColor("#70369d"), SectorType.TEXT),
    )

    var scale = 0.5f
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }
    private var rotationAngle = 0f
    private var animationRotationAngleTarget = 0f
    private var remainingAnimationDuration = 0L

    private var animator: ValueAnimator? = null

    private val sectorAngle = 360f / sectors.size
    val currentSectorIndex: Int
        get() = ((rotationAngle + 90f) % 360 / sectorAngle).toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        setOnClickListener {
            spinWheel()
        }
    }

    private var onSectorStopListener: ((SectorType) -> Unit)? = null

    fun setOnSectorStopListener(listener: (SectorType) -> Unit) {
        this.onSectorStopListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putParcelable("superState", superState)
            putFloat("rotationAngle", rotationAngle)
            putFloat("animationRotationAngleTarget", animationRotationAngleTarget)
            putLong("remainingAnimationDuration", remainingAnimationDuration)
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState = BundleCompat.getParcelable(state, "superState", Parcelable::class.java)
            super.onRestoreInstanceState(superState)

            rotationAngle = state.getFloat("rotationAngle", 0f)
            animationRotationAngleTarget = state.getFloat("animationRotationAngleTarget", 0f)
            remainingAnimationDuration = state.getLong("remainingAnimationDuration", 0L)

            if (animationRotationAngleTarget != 0f && remainingAnimationDuration != 0L) {
                startAnimation(rotationAngle, animationRotationAngleTarget, remainingAnimationDuration)
            }
        } else {
            super.onRestoreInstanceState(state)
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val size = minOf(width, height)

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        val centerX = width / 2f
        val centerY = height / 2f
        val defaultWheelPadding = min(centerX, centerY) / 10
        val radius = (min(centerX, centerY) - defaultWheelPadding) * scale

        canvas.rotate(rotationAngle, centerX, centerY)
        drawWheel(canvas, centerX, centerY, radius)
        canvas.restore()

        drawPointer(canvas, centerX, centerY, radius)

    }

    private fun drawWheel(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        for ((index, sector) in sectors.withIndex()) {
            paint.color = sector.first
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                index * sectorAngle,
                sectorAngle,
                true,
                paint
            )
        }
    }

    private fun drawPointer(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        paint.color = Color.parseColor("#C4C4C4")

        val pointDistanceFromCenter = 3 * radius / 4
        val pointerWidth = radius / 6
        val pointerExtraHeight = radius / 10

        path.reset()
        path.moveTo(centerX, centerY - pointDistanceFromCenter)
        path.lineTo(centerX - pointerWidth / 2, centerY - radius - pointerExtraHeight)
        path.lineTo(centerX + pointerWidth / 2, centerY - radius - pointerExtraHeight)
        path.close()

        canvas.drawPath(path, paint)
    }

    fun spinWheel() {
        val animationDuration = 2000L
        val randomTurns = (1..5).random()
        val sweepAngle = randomTurns * 360f + (0..360).random()
        animationRotationAngleTarget = rotationAngle + sweepAngle
        startAnimation(rotationAngle, animationRotationAngleTarget, animationDuration)
    }

    private fun startAnimation(fromAngle: Float, toAngle: Float, animationDuration: Long) {
        animator = ValueAnimator.ofFloat(fromAngle, toAngle).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                rotationAngle = animatedValue as Float
                remainingAnimationDuration = (animationDuration - currentPlayTime).coerceAtLeast(0)
                Log.d("Wheel", "remainingAnimationDuration = $remainingAnimationDuration")
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rotationAngle %= 360f
                    val sectorType = sectors[currentSectorIndex].second
                    onSectorStopListener?.invoke(sectorType)
                }
            })

            start()
        }
    }
}