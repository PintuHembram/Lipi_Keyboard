package com.lipi.keyboard.keyboard

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.util.AttributeSet
import android.widget.LinearLayout
import com.lipi.keyboard.data.SettingsManager
import com.lipi.keyboard.ime.InputController
import com.lipi.keyboard.layout.LayoutParser

class KeyboardRootView @JvmOverloads constructor(
    context: Context,
    private val controller: InputController,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val toolbarView = KeyboardToolbarView(context)
    val candidateBarView = CandidateBarView(context)
    val keyboardView = KeyboardView(context)
    val bottomBarView = KeyboardBottomBarView(context) {
        (context as? InputMethodService)?.requestHideSelf(0)
    }

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density

        // Toolbar View (34dp)
        val toolbarParams = LayoutParams(LayoutParams.MATCH_PARENT, (34 * density).toInt())
        addView(toolbarView, toolbarParams)

        // Candidate Bar (38dp)
        val barParams = LayoutParams(LayoutParams.MATCH_PARENT, (38 * density).toInt())
        addView(candidateBarView, barParams)

        // Keyboard View (200dp)
        val keyboardParams = LayoutParams(LayoutParams.MATCH_PARENT, (200 * density).toInt())
        addView(keyboardView, keyboardParams)

        // Bottom Navigation Bar (34dp)
        val bottomParams = LayoutParams(LayoutParams.MATCH_PARENT, (34 * density).toInt())
        addView(bottomBarView, bottomParams)

        keyboardView.controller = controller
        controller.onShiftStateChanged = {
            keyboardView.invalidate()
        }

        // Toolbar actions
        toolbarView.onToolbarItemClicked = { item ->
            when (item) {
                "😊" -> loadLayout("emojis")
                "🌐" -> {
                    controller.handleKey(com.lipi.keyboard.layout.KeyData(label = "🌐", type = com.lipi.keyboard.layout.KeyType.LANGUAGE))
                }
                "⚙" -> {
                    val intent = Intent(context, com.lipi.keyboard.MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("OPEN_SETTINGS", true)
                    }
                    context.startActivity(intent)
                }
                else -> {
                    // Default action
                }
            }
        }

        // Wire candidate tap to controller
        candidateBarView.onCandidateSelected = { candidate ->
            controller.commitCandidate(candidate)
        }

        // Wire key press to controller
        keyboardView.onKeyPressed = { key ->
            controller.handleKey(key)
        }

        // Apply settings & set initial layout
        applySettings()
        loadLayout(controller.currentLayoutId)
    }

    fun applySettings() {
        val themeName = SettingsManager.getTheme(context)
        val colors = SettingsManager.getThemeColors(themeName)

        keyboardView.bgColor = colors.bgColor
        keyboardView.keyNormalColor = colors.keyNormalColor
        keyboardView.keySpecialColor = colors.keySpecialColor
        keyboardView.keyAccentColor = colors.keyAccentColor
        keyboardView.keyPressedColor = colors.keyPressedColor
        keyboardView.textColor = colors.textColor
        keyboardView.hintColor = colors.hintColor
        keyboardView.invalidate()

        toolbarView.setBackgroundColor(colors.bgColor)
        candidateBarView.setBackgroundColor(colors.bgColor)
        bottomBarView.setBackgroundColor(colors.bgColor)

        val density = resources.displayMetrics.density
        val scale = SettingsManager.getHeightScale(context) / 100f
        val keyboardHeight = (200 * density * scale).toInt()
        keyboardView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, keyboardHeight)
    }

    fun loadLayout(layoutId: String) {
        val assetPath = "layouts/$layoutId.json"
        try {
            val layout = LayoutParser.loadLayout(context, assetPath)
            keyboardView.layout = layout
        } catch (_: Exception) {
            // Fallback to English layout
            val fallback = LayoutParser.loadLayout(context, "layouts/qwerty_en.json")
            keyboardView.layout = fallback
        }
    }
}
