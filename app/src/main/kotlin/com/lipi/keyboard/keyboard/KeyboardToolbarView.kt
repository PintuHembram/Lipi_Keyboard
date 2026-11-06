package com.lipi.keyboard.keyboard

import android.content.Context
import android.graphics.Color
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
        setPadding((24 * density).toInt(), (8 * density).toInt(), (24 * density).toInt(), (8 * density).toInt())

        val items = listOf("🌐", "😊", "⚙")

        for (item in items) {
            val tv = TextView(context).apply {
                text = item
                textSize = 22f
                setTextColor(Color.parseColor("#FFFFFF"))
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    HapticsHelper.performKeyTick(this)
                    onToolbarItemClicked?.invoke(item)
                }
            }
            addView(tv)
        }
    }
}
