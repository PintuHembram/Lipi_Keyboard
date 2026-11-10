package com.lipi.keyboard.keyboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.ui.platform.ComposeView
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
    val composePanelHost = ComposeView(context).apply {
        visibility = GONE
    }
    val containerFrame = FrameLayout(context)
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

        // Container Frame (holds keyboardView + composePanelHost at SAME height)
        val frameParams = LayoutParams(LayoutParams.MATCH_PARENT, (200 * density).toInt())
        containerFrame.addView(keyboardView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        containerFrame.addView(composePanelHost, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        addView(containerFrame, frameParams)

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
                "☷" -> {
                    closeTranslatePanel()
                    loadLayout("menu_grid")
                }
                "😊" -> {
                    closeTranslatePanel()
                    loadLayout("emojis")
                }
                "🏷️" -> {
                    closeTranslatePanel()
                    loadLayout("stickers")
                }
                "📋" -> {
                    try {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clipData = clipboard?.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).coerceToText(context).toString()
                            if (text.isNotEmpty()) {
                                controller.commitCandidate(text)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
                "🌐" -> {
                    openTranslatePanel()
                }
                "⚙" -> {
                    val intent = Intent(context, com.lipi.keyboard.MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("OPEN_SETTINGS", true)
                    }
                    context.startActivity(intent)
                }
                "🎤" -> {
                    try {
                        val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                    }
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

        // Wire spacebar long press to open Language Picker dialog
        keyboardView.onSpaceLongPressed = {
            showLanguagePickerDialog()
        }

        // Apply settings & set initial layout
        applySettings()
        loadLayout(controller.currentLayoutId)
    }

    fun openTranslatePanel() {
        keyboardView.visibility = GONE
        composePanelHost.visibility = VISIBLE
        composePanelHost.setContent {
            TranslatePanel(
                connectionProvider = { controller.connectionProvider() },
                activeLayoutId = controller.currentLayoutId,
                onClosePanel = { closeTranslatePanel() }
            )
        }
    }

    fun closeTranslatePanel() {
        composePanelHost.visibility = GONE
        keyboardView.visibility = VISIBLE
    }

    fun showLanguagePickerDialog() {
        val languages = arrayOf(
            "ଓଡ଼ିଆ (Transliteration)",
            "English (US)",
            "𑢹𑢺 Warang Chiti (Ho)",
            "ଓଡ଼ିଆ (InScript)",
            "⌨️ Switch Keyboard (System)"
        )
        val currentIndex = when (controller.currentLayoutId) {
            "qwerty_en" -> 1
            "warang_chiti" -> 2
            "odia_inscript" -> 3
            else -> 0
        }

        AlertDialog.Builder(context)
            .setTitle("Select Keyboard Language")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> controller.switchToLayout("odia_translit")
                    1 -> controller.switchToLayout("qwerty_en")
                    2 -> controller.switchToLayout("warang_chiti")
                    3 -> controller.switchToLayout("odia_inscript")
                    4 -> {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        containerFrame.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, keyboardHeight)
    }

    fun loadLayout(layoutId: String) {
        if (layoutId == "translate") {
            openTranslatePanel()
            return
        }
        closeTranslatePanel()
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
