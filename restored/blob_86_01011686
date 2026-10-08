package com.example.ui.tile

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.example.R

class TileRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        clipChildren = false
        clipToPadding = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val subtitle = findViewById<TextView>(R.id.tv_tile_subtitle)
        val labelsContainer = findViewById<LinearLayout>(R.id.ll_labels_container)
        Log.d("TileLayout", "Parent height: $measuredHeight, LabelsContainer height: ${labelsContainer?.measuredHeight}, Subtitle height: ${subtitle?.measuredHeight}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val subtitle = findViewById<TextView>(R.id.tv_tile_subtitle)
        if (subtitle != null && subtitle.visibility == VISIBLE) {
            Log.d("TileLayout", "Parent height: $height, Subtitle bottom: ${subtitle.bottom}")
            val minBottomGap = (2 * context.resources.displayMetrics.density).toInt()
            val maxAllowedBottom = height - minBottomGap
            if (subtitle.bottom > maxAllowedBottom) {
                val overflow = subtitle.bottom - maxAllowedBottom
                subtitle.translationY -= overflow
            }
        }
    }
}
