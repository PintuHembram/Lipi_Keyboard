package com.lipi.keyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.lipi.keyboard.util.HapticsHelper

class KeyboardToolbarView(context: Context) : LinearLayout(context) {

    var onToolbarItemClicked: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        setBackgroundColor(Color.parseColor("#1B1B1F"))
        gravity = Gravity.CENTER_VERTICAL
        val density = resources.displayMetrics.density
        setPadding((12 * density).toInt(), (2 * density).toInt(), (12 * density).toInt(), (2 * density).toInt())

        val items = listOf("☷", "😊", "🏷️", "📋", "🌐", "⚙", "🎤")

        for ((index, item) in items.withIndex()) {
            val tv = TextView(context).apply {
                text = item
                textSize = 18f
                setTextColor(Color.parseColor("#FFFFFF"))
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                    if (index == items.size - 1) {
                        val pillBg = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 14f * density
                            setColor(Color.parseColor("#4D827B"))
                        }
                        background = pillBg
                        setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
                setOnClickListener {
                    HapticsHelper.performKeyTick(this)
                    onToolbarItemClicked?.invoke(item)
                }
            }
            addView(tv)
        }
    }
}
