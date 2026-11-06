package com.lipi.keyboard.lang

interface LanguageEngine {
    val languageCode: String
    val displayName: String
    fun isTransliterating(): Boolean

    /**
     * Converts raw Latin input into ranked candidates in target script.
     * First candidate is the primary composing word.
     */
    fun processComposing(rawLatin: String): List<String>

    /**
     * Determines what string should be committed when the user presses space or punctuation.
     */
    fun onCommit(rawLatin: String, selectedCandidate: String? = null): String
}
