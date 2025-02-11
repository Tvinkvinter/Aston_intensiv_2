package com.atarusov.aston_intensiv_2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class CustomTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            32f,
            context.resources.displayMetrics
        )
    }

    var text = ""
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.CustomTextView, defStyleAttr, 0
        ).apply {
            try {
                text = getString(R.styleable.CustomTextView_text) ?: ""
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val textWidth = paint.measureText(text)
        val textHeight = paint.fontMetrics.bottom - paint.fontMetrics.top

        val desiredWidth = (paddingLeft + textWidth + paddingRight).toInt()
        val desiredHeight = (paddingTop + textHeight + paddingBottom).toInt()

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = paddingLeft.toFloat()
        val y = height / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(text, x, y, paint)
    }

}