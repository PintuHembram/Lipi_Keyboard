package com.lipi.keyboard.ime

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.lipi.keyboard.lang.EnglishEngine
import com.lipi.keyboard.lang.LanguageEngine
import com.lipi.keyboard.lang.OdiaInScriptEngine
import com.lipi.keyboard.lang.OdiaTransliterationEngine
import com.lipi.keyboard.lang.WarangChitiEngine
import com.lipi.keyboard.layout.KeyData
import com.lipi.keyboard.layout.KeyType
import com.lipi.keyboard.suggest.SuggestionEngine

class InputController(
    private val connectionProvider: () -> InputConnection?,
    private val suggestionEngine: SuggestionEngine,
    private val onCandidatesUpdated: (List<String>) -> Unit,
    private val onLayoutRequest: (layoutId: String) -> Unit
) {

    private val composingBuffer = StringBuilder()
    private var currentEditorInfo: EditorInfo? = null
    var isShifted: Boolean = false
    var isCapsLock: Boolean = false
    private var lastShiftPressTime: Long = 0L

    var onShiftStateChanged: (() -> Unit)? = null

    private val enEngine = EnglishEngine()
    private val odiaTranslitEngine = OdiaTransliterationEngine(suggestionEngine.odiaDictionaryWords)
    private val odiaInscriptEngine = OdiaInScriptEngine()
    private val warangChitiEngine = WarangChitiEngine()

    var currentEngine: LanguageEngine = odiaTranslitEngine
    var currentLayoutId: String = "odia_translit"

    fun onStart(info: EditorInfo?, restarting: Boolean) {
        currentEditorInfo = info
        if (!restarting) {
            clearComposing()
        }
        if (EditorInfoPolicy.isNumericField(info)) {
            onLayoutRequest("numpad")
        }
    }

    fun onFinish() {
        finishComposing()
        clearComposing()
    }

    fun getShiftedOutput(key: KeyData): String {
        if (!isShifted && !isCapsLock) {
            return key.output
        }
        val isEn = currentLayoutId == "qwerty_en"
        if (isEn && key.type == KeyType.CHARACTER) {
            return key.label
        }
        if (key.type == KeyType.CHARACTER && key.output.isNotEmpty()) {
            val codePoint = key.output.codePointAt(0)
            if (codePoint in 0x118C0..0x118DF) {
                val capitalCodePoint = codePoint - 0x20
                return String(Character.toChars(capitalCodePoint))
            }
        }
        val upper = key.output.uppercase()
        if (upper != key.output) {
            return upper
        }
        if (key.alt.isNotEmpty()) {
            return key.alt.first()
        }
        return key.output
    }

    fun handleKey(key: KeyData) {
        val ic = connectionProvider() ?: return

        when (key.type) {
            KeyType.CHARACTER -> {
                val output = getShiftedOutput(key)
                composingBuffer.append(output)
                updateComposingText(ic)
                if (isShifted && !isCapsLock) {
                    isShifted = false
                    onShiftStateChanged?.invoke()
                }
            }

            KeyType.SPACE -> {
                if (composingBuffer.isNotEmpty()) {
                    val candidate = currentEngine.onCommit(composingBuffer.toString())
                    commitCandidate(candidate)
                    ic.commitText(" ", 1)
                } else {
                    ic.commitText(" ", 1)
                }
                clearComposing()
            }

            KeyType.BACKSPACE -> {
                handleBackspace(ic)
            }

            KeyType.ENTER -> {
                handleEnter(ic)
            }

            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                if (isCapsLock) {
                    isCapsLock = false
                    isShifted = false
                } else if (isShifted) {
                    if (now - lastShiftPressTime < 350L) {
                        isCapsLock = true
                        isShifted = true
                    } else {
                        isShifted = false
                        isCapsLock = false
                    }
                } else {
                    isShifted = true
                    isCapsLock = false
                }
                lastShiftPressTime = now
                onShiftStateChanged?.invoke()
            }

            KeyType.LANGUAGE -> {
                switchLanguage()
            }

            KeyType.SYMBOL -> {
                onLayoutRequest(key.targetLayer ?: "symbols")
            }

            KeyType.EMOJI -> {
                onLayoutRequest(key.targetLayer ?: "emojis")
            }

            KeyType.ABC -> {
                onLayoutRequest(currentLayoutId)
            }

            else -> {
                if (key.output.isNotEmpty()) {
                    ic.commitText(key.output, 1)
                }
            }
        }
    }

    fun commitCandidate(candidate: String) {
        val ic = connectionProvider() ?: return
        ic.beginBatchEdit()
        if (composingBuffer.isNotEmpty()) {
            ic.commitText(candidate, 1)
        } else {
            val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
            val lastWord = textBefore.split("\\s+".toRegex()).lastOrNull() ?: ""
            if (lastWord.isNotEmpty()) {
                ic.deleteSurroundingText(lastWord.length, 0)
            }
            ic.commitText(candidate, 1)
        }
        ic.endBatchEdit()

        val isSensitive = EditorInfoPolicy.isPersonalizedLearningDisabled(currentEditorInfo)
        suggestionEngine.learnWord(candidate, isSensitive)

        clearComposing()
    }

    private fun handleBackspace(ic: InputConnection) {
        if (composingBuffer.isNotEmpty()) {
            val len = composingBuffer.length
            val codePoint = composingBuffer.codePointBefore(len)
            val charCount = Character.charCount(codePoint)
            composingBuffer.delete(len - charCount, len)
            if (composingBuffer.isEmpty()) {
                ic.finishComposingText()
                onCandidatesUpdated(emptyList())
            } else {
                updateComposingText(ic)
            }
        } else {
            val textBefore = ic.getTextBeforeCursor(4, 0)?.toString() ?: ""
            if (textBefore.isNotEmpty()) {
                val codePoint = textBefore.codePointBefore(textBefore.length)
                val charCount = Character.charCount(codePoint)
                ic.deleteSurroundingText(charCount, 0)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
            onCandidatesUpdated(emptyList())
        }
    }

    private fun handleEnter(ic: InputConnection) {
        finishComposing()
        clearComposing()

        val info = currentEditorInfo
        if (info != null && (info.imeOptions and EditorInfo.IME_MASK_ACTION) != EditorInfo.IME_ACTION_NONE) {
            ic.performEditorAction(info.imeOptions and EditorInfo.IME_MASK_ACTION)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun updateComposingText(ic: InputConnection) {
        val raw = composingBuffer.toString()
        val isSensitive = EditorInfoPolicy.isPersonalizedLearningDisabled(currentEditorInfo)
        val candidates = suggestionEngine.getSuggestions(raw, currentEngine, isSensitive)

        val primary = candidates.firstOrNull() ?: raw
        ic.setComposingText(primary, 1)
        onCandidatesUpdated(candidates)
    }

    private fun updateSuggestions() {
        val isSensitive = EditorInfoPolicy.isPersonalizedLearningDisabled(currentEditorInfo)
        if (isSensitive) {
            onCandidatesUpdated(emptyList())
            return
        }
        if (composingBuffer.isNotEmpty()) {
            val suggestions = suggestionEngine.getSuggestions(composingBuffer.toString(), currentEngine, isSensitive)
            onCandidatesUpdated(suggestions)
        } else {
            val ic = connectionProvider() ?: return
            val textBefore = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
            val lastWord = textBefore.split("\\s+".toRegex()).lastOrNull() ?: ""
            val suggestions = suggestionEngine.getSuggestions(lastWord, currentEngine, isSensitive)
            onCandidatesUpdated(suggestions)
        }
    }

    private fun switchLanguage() {
        finishComposing()
        clearComposing()

        when (currentLayoutId) {
            "odia_translit" -> {
                currentLayoutId = "qwerty_en"
                currentEngine = enEngine
            }
            "qwerty_en" -> {
                currentLayoutId = "warang_chiti"
                currentEngine = warangChitiEngine
            }
            "warang_chiti" -> {
                currentLayoutId = "odia_inscript"
                currentEngine = odiaInscriptEngine
            }
            "odia_inscript" -> {
                currentLayoutId = "odia_translit"
                currentEngine = odiaTranslitEngine
            }
            else -> {
                currentLayoutId = "odia_translit"
                currentEngine = odiaTranslitEngine
            }
        }
        onLayoutRequest(currentLayoutId)
    }

    private fun finishComposing() {
        val ic = connectionProvider() ?: return
        if (composingBuffer.isNotEmpty()) {
            val candidate = currentEngine.onCommit(composingBuffer.toString())
            ic.commitText(candidate, 1)
        }
        ic.finishComposingText()
    }

    private fun clearComposing() {
        composingBuffer.setLength(0)
        onCandidatesUpdated(emptyList())
    }
}
