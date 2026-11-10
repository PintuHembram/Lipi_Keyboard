package com.lipi.keyboard.translate

sealed class TranslateResult {
    data class Success(val translatedText: String) : TranslateResult()
    data class ModelMissing(val sourceLang: String, val targetLang: String) : TranslateResult()
    data class NetworkError(val message: String) : TranslateResult()
    data class Unsupported(val language: String, val note: String) : TranslateResult()
    data class Error(val message: String) : TranslateResult()
}
