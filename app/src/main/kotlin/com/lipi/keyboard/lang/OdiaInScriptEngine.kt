package com.lipi.keyboard.lang

class OdiaInScriptEngine : LanguageEngine {
    override val languageCode: String = "or_inscript"
    override val displayName: String = "ଓଡ଼ିଆ (InScript)"
    override fun isTransliterating(): Boolean = false

    override fun processComposing(rawLatin: String): List<String> {
        if (rawLatin.isEmpty()) return emptyList()
        return listOf(rawLatin)
    }

    override fun onCommit(rawLatin: String, selectedCandidate: String?): String {
        return selectedCandidate ?: rawLatin
    }
}
