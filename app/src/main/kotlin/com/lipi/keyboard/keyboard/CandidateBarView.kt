package com.lipi.keyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.lipi.keyboard.util.HapticsHelper

class CandidateBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
    }

    var onCandidateSelected: ((String) -> Unit)? = null

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#0B1918"))
        addView(container)
    }

    fun setCandidates(candidates: List<String>) {
        container.removeAllViews()
        val density = resources.displayMetrics.density

        if (candidates.isEmpty()) {
            return
        }

        for ((index, candidate) in candidates.withIndex()) {
            val textView = TextView(context).apply {
                text = candidate
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding((16 * density).toInt(), 0, (16 * density).toInt(), 0)

                // Highlight first candidate
                if (index == 0) {
                    setTextColor(Color.parseColor("#4D827B"))
                    setTypeface(null, Typeface.BOLD)
                } else {
                    setTextColor(Color.parseColor("#FFFFFF"))
                    setTypeface(null, Typeface.NORMAL)
                }

                setOnClickListener {
                    HapticsHelper.performKeyTick(this)
                    onCandidateSelected?.invoke(candidate)
                }
            }

            container.addView(textView)

            // Add separator
            if (index < candidates.size - 1) {
                val separator = TextView(context).apply {
                    text = "|"
                    setTextColor(Color.parseColor("#365E58"))
                    textSize = 12f
                    gravity = Gravity.CENTER
                }
                container.addView(separator)
            }
        }
        smoothScrollTo(0, 0)
    }
}
