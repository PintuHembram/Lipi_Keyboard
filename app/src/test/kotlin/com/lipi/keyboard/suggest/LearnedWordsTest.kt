package com.lipi.keyboard.suggest

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.lipi.keyboard.ime.EditorInfoPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnedWordsTest {

    @Test
    fun testGibberishFilterRejections() {
        // "hojhx", "ojhxh", "asdfgh", "qwrtp" must be rejected by the gibberish filter
        assertTrue(GibberishFilter.isGibberish("hojhx"))
        assertTrue(GibberishFilter.isGibberish("ojhxh"))
        assertTrue(GibberishFilter.isGibberish("asdfgh"))
        assertTrue(GibberishFilter.isGibberish("qwrtp"))
    }

    @Test
    fun testValidWordsAcceptedByFilter() {
        // "hello", "namaste", "Bhubaneswar" must pass the filter
        assertFalse(GibberishFilter.isGibberish("hello"))
        assertFalse(GibberishFilter.isGibberish("namaste"))
        assertFalse(GibberishFilter.isGibberish("Bhubaneswar"))
    }

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, Levenshtein.distance("hello", "hello"))
        assertEquals(1, Levenshtein.distance("helo", "hello"))
        assertEquals(2, Levenshtein.distance("helo", "hellos"))
    }

    @Test
    fun testSensitiveFieldDetection() {
        val passwordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertTrue(EditorInfoPolicy.isSensitiveField(passwordInfo))

        val emailInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        assertTrue(EditorInfoPolicy.isSensitiveField(emailInfo))

        val normalInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse(EditorInfoPolicy.isSensitiveField(normalInfo))
    }
}
