package com.lipi.keyboard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.lipi.keyboard.ime.InputController
import com.lipi.keyboard.layout.KeyData
import com.lipi.keyboard.layout.KeyType
import com.lipi.keyboard.layout.KeyboardLayout
import com.lipi.keyboard.util.HapticsHelper

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var controller: InputController? = null

    var layout: KeyboardLayout? = null
        set(value) {
            field = value
            recomputeKeyGeometry()
            invalidate()
        }

    var onKeyPressed: ((KeyData) -> Unit)? = null
    var onKeyLongPressed: ((KeyData) -> Unit)? = null
    var onSpaceLongPressed: (() -> Unit)? = null

    // Theme Colors
    var bgColor: Int = Color.parseColor("#1B1B1F")
    var keyNormalColor: Int = Color.parseColor("#2E2F33")
    var keySpecialColor: Int = Color.parseColor("#202124")
    var keyAccentColor: Int = Color.parseColor("#4D827B")
    var keyPressedColor: Int = Color.parseColor("#42444A")
    var textColor: Int = Color.parseColor("#FFFFFF")
    var hintColor: Int = Color.parseColor("#AAB5B3")
    var accentTextColor: Int = Color.parseColor("#FFFFFF")

    // Pre-allocated Paints (Zero allocations in onDraw)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT
    }
    private val keyRect = RectF()

    private val density = resources.displayMetrics.density
    private val keyGap = 3.5f * density
    private val cornerRadius = 6f * density

    // Touch & Repeat Handler
    private val handler = Handler(Looper.getMainLooper())
    private var pressedKey: KeyData? = null
    private var activePointerId: Int = -1

    private val longPressRunnable = Runnable {
        pressedKey?.let { key ->
            HapticsHelper.performKeyTick(this)
            if (key.type == KeyType.SPACE) {
                onSpaceLongPressed?.invoke()
            } else {
                onKeyLongPressed?.invoke(key)
            }
        }
    }

    private var repeatCount = 0
    private val backspaceRepeatRunnable = object : Runnable {
        override fun run() {
            if (pressedKey?.type == KeyType.BACKSPACE) {
                HapticsHelper.performKeyTick(this@KeyboardView)
                onKeyPressed?.invoke(pressedKey!!)
                repeatCount++
                val delay = if (repeatCount > 5) 50L else 100L
                handler.postDelayed(this, delay)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recomputeKeyGeometry()
    }

    fun recomputeKeyGeometry() {
        val currentLayout = layout ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val rowCount = currentLayout.rows.size
        if (rowCount == 0) return

        val rowHeight = (h - (rowCount + 1) * keyGap) / rowCount

        var currentY = keyGap
        for (row in currentLayout.rows) {
            val totalWeight = row.keys.sumOf { it.weight.toDouble() }.toFloat()
            val availableWidth = w - (row.keys.size + 1) * keyGap
            val unitWidth = availableWidth / totalWeight

            var currentX = keyGap
            for (key in row.keys) {
                val keyWidth = key.weight * unitWidth
                key.x = currentX
                key.y = currentY
                key.width = keyWidth
                key.height = rowHeight

                currentX += keyWidth + keyGap
            }
            currentY += rowHeight + keyGap
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentLayout = layout ?: return

        // 1. Draw Background
        bgPaint.color = bgColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Draw Keys
        for (row in currentLayout.rows) {
            for (key in row.keys) {
                drawKey(canvas, key)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: KeyData) {
        keyRect.set(key.x, key.y, key.x + key.width, key.y + key.height)

        val isShifted = controller?.isShifted == true || controller?.isCapsLock == true
        val isCaps = controller?.isCapsLock == true

        // Select Key Background Color
        keyPaint.color = when {
            key.isPressed -> keyPressedColor
            key.type == KeyType.ENTER -> keyAccentColor
            key.type == KeyType.SHIFT && isShifted -> keyAccentColor
            key.type != KeyType.CHARACTER && key.type != KeyType.SPACE -> keySpecialColor
            else -> keyNormalColor
        }

        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, keyPaint)

        // Select Text Color & Label
        val isAccentKey = key.type == KeyType.ENTER || (key.type == KeyType.SHIFT && isShifted)
        textPaint.color = if (isAccentKey) accentTextColor else textColor
        textPaint.textSize = when {
            key.type == KeyType.SPACE -> 14f * density
            key.type == KeyType.CHARACTER -> 22f * density
            else -> 18f * density
        }

        val isEn = controller?.currentLayoutId == "qwerty_en"
        val labelToDraw = when {
            key.type == KeyType.SHIFT -> if (isCaps) "⇪" else "⇧"
            key.type == KeyType.SPACE -> {
                when (controller?.currentLayoutId) {
                    "qwerty_en" -> "English"
                    "warang_chiti" -> "𑢹𑢺 Warang Chiti"
                    "odia_inscript" -> "ଓଡ଼ିଆ (InScript)"
                    else -> "ଓଡ଼ିଆ (Translit)"
                }
            }
            isEn && key.type == KeyType.CHARACTER -> {
                if (isShifted) key.label else key.output
            }
            key.type == KeyType.CHARACTER && isShifted -> {
                if (key.label.isNotEmpty()) {
                    val codePoint = key.label.codePointAt(0)
                    if (codePoint in 0x118C0..0x118DF) {
                        val capitalCodePoint = codePoint - 0x20
                        String(Character.toChars(capitalCodePoint))
                    } else {
                        val upper = key.label.uppercase()
                        if (upper != key.label) upper else key.alt.firstOrNull() ?: key.label
                    }
                } else key.label
            }
            else -> key.label
        }

        val centerX = keyRect.centerX()
        val textBaseline = keyRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f)

        canvas.drawText(labelToDraw, centerX, textBaseline, textPaint)

        // Draw Hint
        val hintText = when {
            isEn && key.type == KeyType.CHARACTER && !isShifted -> key.alt.firstOrNull()
            isShifted && key.type == KeyType.CHARACTER && layout?.isTransliteration == true -> getTranslitShiftHint(key.output) ?: key.hint ?: key.alt.firstOrNull()
            else -> key.hint ?: if (isEn) null else key.alt.firstOrNull()
        }

        if (hintText != null && key.type == KeyType.CHARACTER) {
            hintPaint.color = hintColor
            hintPaint.textSize = 10f * density
            val hintX = keyRect.right - (6f * density)
            val hintY = keyRect.top + (14f * density)
            canvas.drawText(hintText, hintX, hintY, hintPaint)
        }
    }

    private fun getTranslitShiftHint(output: String): String? {
        return when (output.lowercase()) {
            "a" -> "ଆ"
            "t" -> "ଟ"
            "d" -> "ଡ"
            "n" -> "ଣ"
            "s" -> "ଷ"
            "r" -> "ଡ଼"
            "k" -> "ଖ"
            "g" -> "ଘ"
            "j" -> "ଝ"
            "c" -> "ଛ"
            "p", "f" -> "ଫ"
            "b", "v" -> "ଭ"
            "i" -> "ଈ"
            "u" -> "ଊ"
            "e" -> "ଐ"
            "o" -> "ଔ"
            "h" -> "ଃ"
            "m" -> "ଂ"
            else -> null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
                val x = event.getX(index)
                val y = event.getY(index)

                val key = findKeyAt(x, y)
                if (key != null) {
                    pressedKey = key
                    key.isPressed = true
                    HapticsHelper.performKeyTick(this)
                    invalidate()

                    if (key.type == KeyType.BACKSPACE) {
                        onKeyPressed?.invoke(key)
                        repeatCount = 0
                        handler.postDelayed(backspaceRepeatRunnable, 400L)
                    } else {
                        handler.postDelayed(longPressRunnable, 300L)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(backspaceRepeatRunnable)

                pressedKey?.let {
                    it.isPressed = false
                    if (it.type != KeyType.BACKSPACE) {
                        onKeyPressed?.invoke(it)
                    }
                    pressedKey = null
                    invalidate()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(backspaceRepeatRunnable)
                pressedKey?.let {
                    it.isPressed = false
                    pressedKey = null
                    invalidate()
                }
            }
        }
        return true
    }

    private fun findKeyAt(x: Float, y: Float): KeyData? {
        val currentLayout = layout ?: return null
        for (row in currentLayout.rows) {
            for (key in row.keys) {
                if (x >= key.x && x <= (key.x + key.width) &&
                    y >= key.y && y <= (key.y + key.height)
                ) {
                    return key
                }
            }
        }
        return null
    }
}
