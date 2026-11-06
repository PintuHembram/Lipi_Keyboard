package com.lipi.keyboard.layout

import android.content.Context
import org.json.JSONObject

object LayoutParser {

    private val cache = mutableMapOf<String, KeyboardLayout>()

    fun loadLayout(context: Context, assetPath: String): KeyboardLayout {
        cache[assetPath]?.let { return it }

        val jsonString = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val layout = parse(jsonString)
        cache[assetPath] = layout
        return layout
    }

    fun parse(jsonString: String): KeyboardLayout {
        val root = JSONObject(jsonString)
        val id = root.optString("id", "default")
        val name = root.optString("name", id)
        val language = root.optString("language", "en")
        val isTranslit = root.optBoolean("isTransliteration", false)
        val isInscript = root.optBoolean("isInscript", false)

        val rowsArray = root.getJSONArray("rows")
        val rows = mutableListOf<KeyboardRow>()

        for (i in 0 until rowsArray.length()) {
            val rowObj = rowsArray.getJSONObject(i)
            val keysArray = rowObj.getJSONArray("keys")
            val keys = mutableListOf<KeyData>()

            for (j in 0 until keysArray.length()) {
                val keyObj = keysArray.getJSONObject(j)
                val typeStr = keyObj.optString("type", "character").uppercase()
                val type = when (typeStr) {
                    "SHIFT" -> KeyType.SHIFT
                    "BACKSPACE" -> KeyType.BACKSPACE
                    "ENTER" -> KeyType.ENTER
                    "SPACE" -> KeyType.SPACE
                    "SYMBOL" -> KeyType.SYMBOL
                    "ABC" -> KeyType.ABC
                    "LANGUAGE" -> KeyType.LANGUAGE
                    "SETTINGS" -> KeyType.SETTINGS
                    "EMOJI" -> KeyType.EMOJI
                    else -> KeyType.CHARACTER
                }

                val label = keyObj.optString("label", "")
                val output = keyObj.optString("output", label)
                val hint = if (keyObj.has("hint")) keyObj.getString("hint") else null
                val weight = keyObj.optDouble("weight", 1.0).toFloat()
                val targetLayer = if (keyObj.has("targetLayer")) keyObj.getString("targetLayer") else null

                val altList = mutableListOf<String>()
                if (keyObj.has("alt")) {
                    val altArray = keyObj.getJSONArray("alt")
                    for (k in 0 until altArray.length()) {
                        altList.add(altArray.getString(k))
                    }
                }

                keys.add(
                    KeyData(
                        label = label,
                        output = output,
                        hint = hint,
                        alt = altList,
                        weight = weight,
                        type = type,
                        targetLayer = targetLayer
                    )
                )
            }
            rows.add(KeyboardRow(keys))
        }

        return KeyboardLayout(
            id = id,
            name = name,
            language = language,
            isTransliteration = isTranslit,
            isInscript = isInscript,
            rows = rows
        )
    }
}
