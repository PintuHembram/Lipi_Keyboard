package com.lipi.keyboard.lang

class EnglishEngine : LanguageEngine {
    override val languageCode: String = "en"
    override val displayName: String = "English"

    override fun isTransliterating(): Boolean = false

    override fun processComposing(rawLatin: String): List<String> {
        if (rawLatin.isEmpty()) return emptyList()
        return listOf(rawLatin)
    }

    override fun onCommit(rawLatin: String, selectedCandidate: String?): String {
        return selectedCandidate ?: rawLatin
    }
}
