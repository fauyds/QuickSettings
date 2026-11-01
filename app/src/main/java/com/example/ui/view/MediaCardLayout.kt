package com.example.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.R

/**
 * Custom FrameLayout that ensures the Android 10 media notification card height is strictly
 * determined by its content (titles, playback buttons, seekbar) and NEVER expanded by the
 * background album artwork's intrinsic bitmap dimensions.
 */
class MediaCardLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val content = findViewById<View>(R.id.ll_media_content)
        if (content != null) {
            // 1. Measure content first to determine the natural height of the card
            measureChildWithMargins(content, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val contentHeight = content.measuredHeight + paddingTop + paddingBottom
            val cardWidth = MeasureSpec.getSize(widthMeasureSpec)

            // 2. Measure artwork and background views strictly to match the content height
            val exactHeightSpec = MeasureSpec.makeMeasureSpec(content.measuredHeight, MeasureSpec.EXACTLY)
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !== content && child.visibility != GONE) {
                    val lp = child.layoutParams as? LayoutParams
                    val childWidthSpec = getChildMeasureSpec(
                        widthMeasureSpec,
                        paddingLeft + paddingRight + (lp?.leftMargin ?: 0) + (lp?.rightMargin ?: 0),
                        lp?.width ?: LayoutParams.WRAP_CONTENT
                    )
                    child.measure(childWidthSpec, exactHeightSpec)
                }
            }

            setMeasuredDimension(cardWidth, contentHeight)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
