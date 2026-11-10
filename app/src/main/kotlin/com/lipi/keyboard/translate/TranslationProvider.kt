package com.lipi.keyboard.translate

/**
 * Extension Point: TranslationProvider interface.
 * Allows adding custom or alternative translation engines (e.g. custom ML models or local dictionary engines).
 */
interface TranslationProvider {
    suspend fun translate(text: String, sourceLang: String, targetLang: String): TranslateResult
    fun supportedLanguages(): List<Pair<String, String>>
    suspend fun isModelDownloaded(langCode: String): Boolean
    suspend fun downloadModel(langCode: String, wifiOnly: Boolean): Boolean
    suspend fun deleteModel(langCode: String): Boolean
    suspend fun detectLanguage(text: String): String?
    fun close()
}
