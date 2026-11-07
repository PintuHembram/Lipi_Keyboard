package com.lipi.keyboard.lang

/**
 * Warang Chiti Language Engine for Ho script.
 * Supports:
 * - English-India Latin to Ho (Warang Chiti) Unicode Transliteration
 * - Boyo Gagrai & Ghanshyam Bodra ASCII font to Unicode Conversion
 * - Automatic Spacebar Commit to Warang Chiti Unicode
 * - Unicode Standard Version 18.0 (U+118A0–U+118FF)
 */
class WarangChitiEngine : LanguageEngine {

    override val languageCode: String = "hoc"
    override val displayName: String = "𑢹𑢺 (Warang Chiti)"
    override fun isTransliterating(): Boolean = true

    private val phoneticMap = linkedMapOf(
        // Long/Double vowel mappings
        "ngaa" to "𑣀",
        "wi" to "𑣂",
        "yu" to "𑣃",
        "ya" to "𑣄",
        "yo" to "𑣅",
        "ii" to "𑣆",
        "uu" to "𑣇",
        "aa" to "𑣁",
        "a" to "𑣁",
        "e" to "𑣈",
        "o" to "𑣉",
        "i" to "𑣂",
        "u" to "𑣃",

        // Consonant names & multi-letter sounds
        "ang" to "𑣊",
        "ga" to "𑣋",
        "ko" to "𑣌",
        "eny" to "𑣍",
        "yuj" to "𑣎",
        "uc" to "𑣏",
        "enn" to "𑣐",
        "odd" to "𑣑",
        "tte" to "𑣒",
        "nung" to "𑣓",
        "da" to "𑣔",
        "ot" to "𑣕",
        "am" to "𑣖",
        "bu" to "𑣗",
        "pu" to "𑣘",
        "hiyo" to "𑣙",
        "holo" to "𑣚",
        "horr" to "𑣛",
        "har" to "𑣜",
        "ssuu" to "𑣝",
        "sii" to "𑣞",
        "viyo" to "𑣟",

        // Common phonetic & Boyo Gagrai / Ghanshyam Bodra ASCII font mappings
        "chh" to "𑣏",
        "ch" to "𑣏",
        "sh" to "𑣝",
        "ss" to "𑣝",
        "rr" to "𑣛",
        "tt" to "𑣒",
        "ng" to "𑣊",
        "g" to "𑣋",
        "k" to "𑣌",
        "j" to "𑣎",
        "c" to "𑣏",
        "n" to "𑣓",
        "t" to "𑣒",
        "d" to "𑣔",
        "m" to "𑣖",
        "b" to "𑣗",
        "p" to "𑣘",
        "h" to "𑣙",
        "l" to "𑣚",
        "r" to "𑣜",
        "s" to "𑣞",
        "v" to "𑣟",
        "w" to "𑣟",
        "y" to "𑣄",
        "ong" to "𑣿",
        "om" to "𑣿"
    )

    override fun processComposing(rawLatin: String): List<String> {
        if (rawLatin.isEmpty()) return emptyList()

        val firstCodePoint = rawLatin.codePointAt(0)
        if (firstCodePoint in 0x118A0..0x118FF) {
            return listOf(rawLatin)
        }

        val primary = transliterate(rawLatin)
        val candidates = mutableListOf<String>()
        candidates.add(primary)

        when (rawLatin.lowercase()) {
            "johar" -> candidates.add(0, "𑣎𑣉𑣙𑣁𑣜")
            "ho" -> candidates.add(0, "𑣙𑣉")
            "warang" -> candidates.add(0, "𑣟𑣁𑣜𑣁𑣊")
            "chiti" -> candidates.add(0, "𑣏𑣂𑣒𑣂")
            "namaste" -> candidates.add(0, "𑣓𑣁𑣖𑣁𑣞𑣒𑣈")
        }

        return candidates.distinct()
    }

    override fun onCommit(rawLatin: String, selectedCandidate: String?): String {
        if (selectedCandidate != null) return selectedCandidate
        val firstCodePoint = if (rawLatin.isNotEmpty()) rawLatin.codePointAt(0) else 0
        if (firstCodePoint in 0x118A0..0x118FF) {
            return rawLatin
        }
        return processComposing(rawLatin).firstOrNull() ?: transliterate(rawLatin)
    }

    fun transliterate(input: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val remaining = input.substring(i)
            val lowerRemaining = remaining.lowercase()
            var matched = false

            for ((latin, warang) in phoneticMap) {
                if (lowerRemaining.startsWith(latin)) {
                    sb.append(warang)
                    i += latin.length
                    matched = true
                    break
                }
            }

            if (!matched) {
                val c = input[i]
                if (c in '0'..'9') {
                    val digit = String(Character.toChars(0x118E0 + (c - '0')))
                    sb.append(digit)
                } else {
                    sb.append(c)
                }
                i++
            }
        }

        return sb.toString()
    }
}
