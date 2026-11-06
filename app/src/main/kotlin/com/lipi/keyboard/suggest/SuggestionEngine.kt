package com.lipi.keyboard.suggest

import android.content.Context
import com.lipi.keyboard.lang.LanguageEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SuggestionEngine(private val context: Context) {

    private val enTrie = Trie()
    private val odiaTrie = Trie()
    val odiaDictionaryWords = mutableSetOf<String>()
    private val userLearnedWords = mutableMapOf<String, Int>()

    var isLearningEnabled: Boolean = true

    suspend fun initialize() = withContext(Dispatchers.IO) {
        loadDictionary("dict/en_words.txt", enTrie, null)
        loadDictionary("dict/odia_words.txt", odiaTrie, odiaDictionaryWords)
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
                            wordSet?.add(word)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Asset might not exist or be empty
        }
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
            // English / Standard mode suggestions
            val query = rawBuffer.lowercase()
            val matches = enTrie.getWordsWithPrefix(query, maxCount = 5).toMutableList()

            val isCapitalized = rawBuffer.firstOrNull()?.isUpperCase() == true
            val isUpper = rawBuffer.length > 1 && rawBuffer.all { it.isUpperCase() }

            val formattedMatches = matches.map { word ->
                when {
                    isUpper -> word.uppercase()
                    isCapitalized -> word.replaceFirstChar { it.uppercase() }
                    else -> word
                }
            }.toMutableList()

            if (!formattedMatches.contains(rawBuffer)) {
                formattedMatches.add(0, rawBuffer)
            } else {
                formattedMatches.remove(rawBuffer)
                formattedMatches.add(0, rawBuffer)
            }
            return formattedMatches.distinct().take(5)
        }
    }

    fun learnWord(word: String, isSensitiveField: Boolean) {
        if (!isLearningEnabled || isSensitiveField || word.length < 2) return
        val currentCount = userLearnedWords.getOrDefault(word, 0)
        userLearnedWords[word] = currentCount + 1
        odiaTrie.insert(word, 100 + currentCount * 10)
        enTrie.insert(word, 100 + currentCount * 10)
    }
}
