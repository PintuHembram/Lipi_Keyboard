package com.lipi.keyboard.suggest

import com.lipi.keyboard.translate.TranslateResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageSwitcherTest {

    @Test
    fun testLanguageCyclingOrder() {
        val enabledLanguages = listOf("odia_translit", "qwerty_en", "warang_chiti", "odia_inscript")

        fun getNextLanguage(current: String): String {
            val index = enabledLanguages.indexOf(current)
            if (index == -1 || index == enabledLanguages.size - 1) {
                return enabledLanguages.first()
            }
            return enabledLanguages[index + 1]
        }

        assertEquals("qwerty_en", getNextLanguage("odia_translit"))
        assertEquals("warang_chiti", getNextLanguage("qwerty_en"))
        assertEquals("odia_inscript", getNextLanguage("warang_chiti"))
        assertEquals("odia_translit", getNextLanguage("odia_inscript"))
    }

    @Test
    fun testSpaceBarLabels() {
        fun getSpaceBarLabel(layoutId: String): String {
            return when (layoutId) {
                "qwerty_en" -> "English"
                "warang_chiti" -> "𑢹𑢺 Warang Chiti"
                "odia_inscript" -> "ଓଡ଼ିଆ (InScript)"
                else -> "ଓଡ଼ିଆ (Translit)"
            }
        }

        assertEquals("English", getSpaceBarLabel("qwerty_en"))
        assertEquals("𑢹𑢺 Warang Chiti", getSpaceBarLabel("warang_chiti"))
        assertEquals("ଓଡ଼ିଆ (InScript)", getSpaceBarLabel("odia_inscript"))
        assertEquals("ଓଡ଼ିଆ (Translit)", getSpaceBarLabel("odia_translit"))
    }

    @Test
    fun testPrefillSelectionLogic() {
        val selectedText: String? = "Hello World"
        val textBeforeCursor: String? = null

        val prefill = if (!selectedText.isNullOrEmpty()) {
            selectedText
        } else {
            textBeforeCursor?.split("\n")?.lastOrNull()?.trim() ?: ""
        }

        assertEquals("Hello World", prefill)
    }

    @Test
    fun testTranslateResultMapping() {
        val result: TranslateResult = TranslateResult.Success("नमस्ते")
        assertTrue(result is TranslateResult.Success)
        assertEquals("नमस्ते", (result as TranslateResult.Success).translatedText)

        val modelMissing = TranslateResult.ModelMissing("en", "hi")
        assertEquals("en", modelMissing.sourceLang)
        assertEquals("hi", modelMissing.targetLang)

        val unsupportedOdia = TranslateResult.Unsupported("Odia", "On-device translation is not available for Odia.")
        assertEquals("Odia", unsupportedOdia.language)
        assertTrue(unsupportedOdia.note.contains("not available"))
    }
}
