package com.lipi.keyboard.suggest

import android.content.Context
import com.lipi.keyboard.data.PersonalDictionaryDatabase
import com.lipi.keyboard.lang.LanguageEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SuggestionEngine(private val context: Context) {

    private val enTrie = Trie()
    private val odiaTrie = Trie()
    val odiaDictionaryWords = mutableSetOf<String>()
    private val enDictionaryWords = mutableSetOf<String>()

    private val db by lazy { PersonalDictionaryDatabase(context) }

    var isLearningEnabled: Boolean = true

    suspend fun initialize() = withContext(Dispatchers.IO) {
        loadDictionary("dict/en_words.txt", enTrie, enDictionaryWords)
        loadDictionary("dict/odia_words.txt", odiaTrie, odiaDictionaryWords)
        try {
            db.runMigrationPurge()
        } catch (_: Exception) {
        }
    }

    private fun loadDictionary(assetPath: String, trie: Trie, wordSet: MutableSet<String>?) {
        try {
            context.assets.open(assetPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    for (line in lines) {
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.isNotEmpty()) {
                            val word = parts[0]
                            val freq = if (parts.size > 1) parts[1].toIntOrNull() ?: 1 else 1
                            trie.insert(word, freq)
                            wordSet?.add(word.lowercase())
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Asset might not exist or be empty
        }
    }

    fun isBuiltInWord(word: String): Boolean {
        return enTrie.contains(word.lowercase()) || enDictionaryWords.contains(word.lowercase())
    }

    fun getSuggestions(
        rawBuffer: String,
        engine: LanguageEngine,
        isSensitiveField: Boolean
    ): List<String> {
        if (rawBuffer.isEmpty() || isSensitiveField) {
            return emptyList()
        }

        if (engine.isTransliterating()) {
            val candidates = engine.processComposing(rawBuffer)
            if (candidates.isNotEmpty()) {
                val primary = candidates.first()
                val prefixMatches = odiaTrie.getWordsWithPrefix(primary, maxCount = 3)
                val combined = (candidates + prefixMatches).distinct()
                return combined.take(5)
            }
            return candidates.take(5)
        } else {
            // English / Standard mode
            val query = rawBuffer.lowercase()

            // Built-in dictionary prefix matches
            val dictMatches = enTrie.getWordsWithPrefix(query, maxCount = 5)

            // Learned words prefix matches
            val learnedWords = db.getLearnedWords()
                .filter { it.startsWith(query) && !db.isBlocked(it) }

            val combined = (dictMatches + learnedWords).distinct()

            val isCapitalized = rawBuffer.firstOrNull()?.isUpperCase() == true
            val isUpper = rawBuffer.length > 1 && rawBuffer.all { it.isUpperCase() }

            val formattedMatches = combined.map { word ->
                when {
                    isUpper -> word.uppercase()
                    isCapitalized -> word.replaceFirstChar { it.uppercase() }
                    else -> word
                }
            }.toMutableList()

            // Slot 1 is ALWAYS the exact literal typed word
            formattedMatches.remove(rawBuffer)
            formattedMatches.add(0, rawBuffer)

            return formattedMatches.distinct().take(5)
        }
    }

    fun getAutocorrectWord(typedWord: String, isSensitiveField: Boolean): String {
        if (isSensitiveField || typedWord.length < 2 || GibberishFilter.isGibberish(typedWord)) {
            return typedWord
        }

        val lower = typedWord.lowercase()
        if (isBuiltInWord(lower)) {
            return typedWord
        }

        // Never auto-correct to unknown or learned words. Only check high-confidence built-in dictionary matches!
        val maxAllowedDistance = if (typedWord.length <= 5) 1 else 2

        var bestMatch: String? = null
        var bestDistance = Int.MAX_VALUE

        val candidates = enTrie.getWordsWithPrefix(lower.substring(0, 1), maxCount = 100)
        for (cand in candidates) {
            if (Math.abs(cand.length - lower.length) > maxAllowedDistance) continue
            val dist = Levenshtein.distance(lower, cand)
            if (dist <= maxAllowedDistance && dist < bestDistance) {
                bestDistance = dist
                bestMatch = cand
            }
        }

        if (bestMatch != null) {
            val isCapitalized = typedWord.firstOrNull()?.isUpperCase() == true
            val isUpper = typedWord.length > 1 && typedWord.all { it.isUpperCase() }
            return when {
                isUpper -> bestMatch.uppercase()
                isCapitalized -> bestMatch.replaceFirstChar { it.uppercase() }
                else -> bestMatch
            }
        }

        return typedWord
    }

    fun learnWord(word: String, sessionId: Long, isSensitiveField: Boolean) {
        if (!isLearningEnabled || isSensitiveField || GibberishFilter.isGibberish(word) || db.isBlocked(word)) {
            return
        }
        val inBuiltIn = isBuiltInWord(word)
        db.recordWordCommit(word, sessionId, inBuiltIn)
    }

    fun removeLearnedWord(word: String) {
        db.removeUserWord(word)
    }

    fun clearAllLearnedWords() {
        db.clearAllLearnedWords()
    }
}
