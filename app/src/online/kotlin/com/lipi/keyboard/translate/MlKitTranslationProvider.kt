package com.lipi.keyboard.translate

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitTranslationProvider(private val context: Context) : TranslationProvider {

    private val modelManager = RemoteModelManager.getInstance()
    private val languageIdentifier = LanguageIdentification.getClient()
    private var activeTranslator: Translator? = null
    private var activePair: Pair<String, String>? = null

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
    }

    override fun supportedLanguages(): List<Pair<String, String>> {
        val allCodes = TranslateLanguage.getAllLanguages()
        val list = mutableListOf<Pair<String, String>>()
        for (code in allCodes) {
            val displayName = Locale(code).getDisplayName(Locale.ENGLISH).replaceFirstChar { it.uppercase() }
            list.add(code to displayName)
        }
        return list.sortedBy { it.second }
    }

    override suspend fun isModelDownloaded(langCode: String): Boolean {
        return try {
            val model = TranslateRemoteModel.Builder(langCode).build()
            val downloaded = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).awaitTask()
            downloaded.contains(model)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun downloadModel(langCode: String, wifiOnly: Boolean): Boolean {
        return try {
            val model = TranslateRemoteModel.Builder(langCode).build()
            val conditions = if (wifiOnly) {
                DownloadConditions.Builder().requireWifi().build()
            } else {
                DownloadConditions.Builder().build()
            }
            modelManager.download(model, conditions).awaitTask()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun deleteModel(langCode: String): Boolean {
        return try {
            val model = TranslateRemoteModel.Builder(langCode).build()
            modelManager.deleteDownloadedModel(model).awaitTask()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun detectLanguage(text: String): String? {
        if (text.isBlank()) return null
        return try {
            val code = languageIdentifier.identifyLanguage(text).awaitTask()
            if (code != "und") code else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): TranslateResult {
        if (text.isBlank()) return TranslateResult.Error("Input text is empty")
        if (text.length > 2000) return TranslateResult.Error("Text length exceeds 2000 characters limit")

        // Check unsupported languages for on-device ML Kit
        if (sourceLang == "or" || sourceLang == "hoc") {
            val name = if (sourceLang == "or") "Odia" else "Warang Chiti"
            return TranslateResult.Unsupported(
                language = name,
                note = "On-device translation is not available for $name. You can still translate English and other supported languages."
            )
        }

        val sourceDownloaded = isModelDownloaded(sourceLang)
        val targetDownloaded = isModelDownloaded(targetLang)

        if (!sourceDownloaded || !targetDownloaded) {
            return TranslateResult.ModelMissing(sourceLang, targetLang)
        }

        return try {
            val pair = sourceLang to targetLang
            if (activeTranslator == null || activePair != pair) {
                activeTranslator?.close()
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()
                activeTranslator = Translation.getClient(options)
                activePair = pair
            }

            val result = activeTranslator!!.translate(text).awaitTask()
            TranslateResult.Success(result)
        } catch (e: Exception) {
            TranslateResult.Error(e.message ?: "Translation error occurred")
        }
    }

    override fun close() {
        try {
            activeTranslator?.close()
            activeTranslator = null
            activePair = null
        } catch (_: Exception) {
        }
    }
}
