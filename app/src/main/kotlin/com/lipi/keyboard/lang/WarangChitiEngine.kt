package com.lipi.keyboard.lang

/**
 * Warang Chiti Language Engine for Ho script.
 * Conforms to Unicode Standard Version 18.0 (Range: U+118A0–U+118FF).
 * Supports:
 * - Capital Letters (U+118A0–U+118BF)
 * - Small Letters (U+118C0–U+118DF)
 * - Digits & Numbers 0–90 (U+118E0–U+118F2)
 * - Sign ONG / OM (U+118FF)
 */
class WarangChitiEngine : LanguageEngine {

    override val languageCode: String = "hoc"
    override val displayName: String = "𑢹𑢺 (Warang Chiti)"
    override fun isTransliterating(): Boolean = false

    private val phoneticMap = linkedMapOf(
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
        val result = transliterate(rawLatin)
        return listOf(result)
    }

    override fun onCommit(rawLatin: String, selectedCandidate: String?): String {
        return selectedCandidate ?: transliterate(rawLatin)
    }

    fun transliterate(input: String): String {
        val sb = StringBuilder()
        var i = 0
        val len = input.length

        while (i < len) {
            val remaining = input.substring(i).lowercase()
            var matched = false

            for ((latin, warang) in phoneticMap) {
                if (remaining.startsWith(latin)) {
                    sb.append(warang)
                    i += latin.length
                    matched = true
                    break
                }
            }

            if (!matched) {
                val c = input[i]
                if (c in '0'..'9') {
                    val digit = (0x118E0 + (c - '0')).toChar()
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
