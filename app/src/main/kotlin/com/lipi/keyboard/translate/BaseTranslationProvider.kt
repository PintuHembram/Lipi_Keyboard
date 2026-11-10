package com.lipi.keyboard.translate

class BaseTranslationProvider : TranslationProvider {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): TranslateResult {
        return TranslateResult.Unsupported(
            language = sourceLang,
            note = "Translation needs the online build"
        )
    }

    override fun supportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "hi" to "Hindi",
            "es" to "Spanish",
            "fr" to "French",
            "de" to "German"
        )
    }

    override suspend fun isModelDownloaded(langCode: String): Boolean = false

    override suspend fun downloadModel(langCode: String, wifiOnly: Boolean): Boolean = false

    override suspend fun deleteModel(langCode: String): Boolean = false

    override suspend fun detectLanguage(text: String): String? = "en"

    override fun close() {}
}
