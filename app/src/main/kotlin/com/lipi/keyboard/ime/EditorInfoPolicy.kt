package com.lipi.keyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo

object EditorInfoPolicy {

    fun isPasswordField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val isPasswordClass = when (variation) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> true
            else -> false
        }
        val isNumberPassword = (info.inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER &&
                (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

        return isPasswordClass || isNumberPassword
    }

    fun isNumericField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val clazz = info.inputType and InputType.TYPE_MASK_CLASS
        return clazz == InputType.TYPE_CLASS_NUMBER || clazz == InputType.TYPE_CLASS_PHONE || clazz == InputType.TYPE_CLASS_DATETIME
    }

    fun isPersonalizedLearningDisabled(info: EditorInfo?): Boolean {
        if (info == null) return false
        if (isPasswordField(info)) return true
        return (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
    }

    fun isSensitiveField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val inputType = info.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val clazz = inputType and InputType.TYPE_MASK_CLASS

        val isPassword = isPasswordField(info)
        val isEmail = clazz == InputType.TYPE_CLASS_TEXT && (
                variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        )
        val isUri = clazz == InputType.TYPE_CLASS_TEXT && (
                variation == InputType.TYPE_TEXT_VARIATION_URI
        )
        val isNumeric = isNumericField(info)
        val noLearning = (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0

        return isPassword || isEmail || isUri || isNumeric || noLearning
    }

    fun getActionLabel(info: EditorInfo?): String {
        if (info == null) return "↵"
        val action = info.imeOptions and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        return when (action) {
            EditorInfo.IME_ACTION_SEARCH -> "🔍"
            EditorInfo.IME_ACTION_SEND -> "➤"
            EditorInfo.IME_ACTION_GO -> "➔"
            EditorInfo.IME_ACTION_NEXT -> "⇥"
            EditorInfo.IME_ACTION_DONE -> "✓"
            else -> "↵"
        }
    }
}
