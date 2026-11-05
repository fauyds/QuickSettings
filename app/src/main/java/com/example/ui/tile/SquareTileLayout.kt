package com.example.ui.tile

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Square tile container enforcing a strict 1:1 aspect ratio (same height as length/width).
 * Used exclusively for Android 4.4 KitKat flat square tile cards.
 */
class SquareTileLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Force square 1:1 aspect ratio (height equals measured width)
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
