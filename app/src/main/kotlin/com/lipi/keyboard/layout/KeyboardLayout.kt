package com.lipi.keyboard.layout

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SYMBOL,
    ABC,
    LANGUAGE,
    ACTION,
    SETTINGS,
    EMOJI
}

data class KeyData(
    val label: String,
    val output: String = label,
    val hint: String? = null,
    val alt: List<String> = emptyList(),
    val weight: Float = 1.0f,
    val type: KeyType = KeyType.CHARACTER,
    val targetLayer: String? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var isPressed: Boolean = false
)

data class KeyboardRow(
    val keys: List<KeyData>
)

data class KeyboardLayout(
    val id: String,
    val name: String,
    val language: String,
    val isTransliteration: Boolean = false,
    val isInscript: Boolean = false,
    val rows: List<KeyboardRow>
)
