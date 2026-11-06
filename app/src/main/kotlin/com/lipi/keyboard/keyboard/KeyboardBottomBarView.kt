package com.lipi.keyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.lipi.keyboard.util.HapticsHelper

class KeyboardBottomBarView(
    context: Context,
    private val onHideRequested: () -> Unit
) : LinearLayout(context) {

    init {
        orientation = HORIZONTAL
        setBackgroundColor(Color.parseColor("#0B1918"))
        gravity = Gravity.CENTER_VERTICAL
        val density = resources.displayMetrics.density
        setPadding((16 * density).toInt(), 0, (16 * density).toInt(), (6 * density).toInt())

        // Hide keyboard button (downward chevron) on the left
        val hideBtn = TextView(context).apply {
            text = "⌄"
            textSize = 28f
            setTextColor(Color.parseColor("#B8C0BF"))
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0f)
            setOnClickListener {
                HapticsHelper.performKeyTick(this)
                onHideRequested()
            }
        }
        addView(hideBtn)

        // Gesture navigation indicator bar in the center
        val indicatorContainer = LinearLayout(context).apply {
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0f)
            val bar = View(context).apply {
                val barBg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 4f * density
                    setColor(Color.parseColor("#434C4B"))
                }
                background = barBg
                layoutParams = LayoutParams((120 * density).toInt(), (4 * density).toInt())
            }
            addView(bar)
        }
        addView(indicatorContainer)

        // Empty spacer on the right for symmetry
        val spacer = View(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
        }
        addView(spacer)
    }
}
