package com.lipi.keyboard.lang

class OdiaTransliterationEngine(
    private val dictionaryWords: Set<String> = emptySet()
) : LanguageEngine {

    override val languageCode: String = "or"
    override val displayName: String = "ଓଡ଼ିଆ"
    override fun isTransliterating(): Boolean = true

    // Independent Vowels (checked when prevWasConsonant is false)
    private val independentVowels = linkedMapOf(
        "AA" to "ଆ",
        "aa" to "ଆ",
        "II" to "ଈ",
        "ii" to "ଈ",
        "EE" to "ଈ",
        "ee" to "ଈ",
        "UU" to "ଊ",
        "uu" to "ଊ",
        "OO" to "ଊ",
        "oo" to "ଊ",
        "ru" to "ଋ",
        "AI" to "ଐ",
        "ai" to "ଐ",
        "oi" to "ଐ",
        "AU" to "ଔ",
        "au" to "ଔ",
        "ou" to "ଔ",
        "A" to "ଆ",
        "a" to "ଅ",
        "I" to "ଈ",
        "i" to "ଇ",
        "U" to "ଊ",
        "u" to "ଉ",
        "E" to "ଏ",
        "e" to "ଏ",
        "O" to "ଓ",
        "o" to "ଓ"
    )

    // Dependent Vowels / Matras (checked when prevWasConsonant is true)
    private val matras = linkedMapOf(
        "AA" to "ା",
        "aa" to "ା",
        "II" to "ୀ",
        "ii" to "ୀ",
        "EE" to "ୀ",
        "ee" to "ୀ",
        "UU" to "ୂ",
        "uu" to "ୂ",
        "OO" to "ୂ",
        "oo" to "ୂ",
        "ru" to "ୃ",
        "AI" to "ୈ",
        "ai" to "ୈ",
        "oi" to "ୈ",
        "AU" to "ୌ",
        "au" to "ୌ",
        "ou" to "ୌ",
        "A" to "ା",
        "a" to "",    // inherent vowel 'a' removes halant
        "I" to "ୀ",
        "i" to "ି",
        "U" to "ୂ",
        "u" to "ୁ",
        "E" to "େ",
        "e" to "େ",
        "O" to "ୋ",
        "o" to "ୋ"
    )

    // Retroflex 2-char & 1-char consonants
    private val retroflexConsonants = linkedMapOf(
        "Th" to "ଠ",
        "TH" to "ଠ",
        "Dh" to "ଢ",
        "DH" to "ଢ",
        "T" to "ଟ",
        "D" to "ଡ",
        "N" to "ଣ",
        "L" to "ଳ",
        "S" to "ଷ",
        "R" to "ଡ଼",
        "Y" to "ଯ"
    )

    // General Consonants
    private val consonants = linkedMapOf(
        "KSHA" to "କ୍ଷ",
        "ksha" to "କ୍ଷ",
        "kshy" to "କ୍ଷ",
        "ksh" to "କ୍ଷ",
        "khy" to "କ୍ଷ",
        "JNYA" to "ଜ୍ଞ",
        "jnya" to "ଜ୍ଞ",
        "jny" to "ଜ୍ଞ",
        "gya" to "ଜ୍ଞ",
        "gy" to "ଜ୍ଞ",
        "CHH" to "ଛ",
        "chh" to "ଛ",
        "CH" to "ଚ",
        "ch" to "ଚ",
        "KH" to "ଖ",
        "kh" to "ଖ",
        "GH" to "ଘ",
        "gh" to "ଘ",
        "NG" to "ଙ",
        "ng" to "ଙ",
        "JH" to "ଝ",
        "jh" to "ଝ",
        "NY" to "ଞ",
        "ny" to "ଞ",
        "TH" to "ଥ",
        "th" to "ଥ",
        "DH" to "ଧ",
        "dh" to "ଧ",
        "PH" to "ଫ",
        "ph" to "ଫ",
        "BH" to "ଭ",
        "bh" to "ଭ",
        "SH" to "ଶ",
        "sh" to "ଶ",
        "SS" to "ଷ",
        "ss" to "ଷ",
        "rh" to "ଢ଼",
        "K" to "ଖ",
        "k" to "କ",
        "G" to "ଘ",
        "g" to "ଗ",
        "c" to "ଚ",
        "J" to "ଝ",
        "j" to "ଜ",
        "t" to "ତ",
        "d" to "ଦ",
        "n" to "ନ",
        "P" to "ଫ",
        "p" to "ପ",
        "F" to "ଫ",
        "f" to "ଫ",
        "B" to "ଭ",
        "b" to "ବ",
        "V" to "ଭ",
        "v" to "ଭ",
        "M" to "ଂ",
        "m" to "ମ",
        "y" to "ୟ",
        "r" to "ର",
        "l" to "ଲ",
        "w" to "ୱ",
        "s" to "ସ",
        "H" to "ଃ",
        "h" to "ହ"
    )

    private val halant = "୍"

    override fun processComposing(rawLatin: String): List<String> {
        if (rawLatin.isBlank()) return emptyList()

        val primary = transliterate(rawLatin.trim())
        val candidates = mutableListOf<String>()
        candidates.add(primary)

        // 1. Initial 'a' often represents 'ଆ' (e.g. ami -> ଆମି / ଅମି)
        if (rawLatin.startsWith("a", ignoreCase = true) && !rawLatin.startsWith("aa", ignoreCase = true)) {
            val altA = transliterate("aa" + rawLatin.substring(1))
            if (!candidates.contains(altA)) candidates.add(altA)
        }

        // 2. 's' / 'sh' alternatives (ସ / ଶ / ଷ)
        if (rawLatin.contains("sh", ignoreCase = true)) {
            val altSh = transliterate(rawLatin.replace("sh", "ss", ignoreCase = true))
            if (!candidates.contains(altSh)) candidates.add(altSh)
        } else if (rawLatin.contains("s", ignoreCase = true)) {
            val altS1 = transliterate(rawLatin.replace("s", "sh", ignoreCase = true))
            if (!candidates.contains(altS1)) candidates.add(altS1)
        }

        // 3. 't' -> ତ vs ଟ
        if (rawLatin.contains("t") && !rawLatin.contains("T")) {
            val altT = transliterate(rawLatin.replace("t", "T"))
            if (!candidates.contains(altT)) candidates.add(altT)
        }

        // 4. 'd' -> ଦ vs ଡ vs ଡ଼
        if (rawLatin.contains("d") && !rawLatin.contains("D")) {
            val altD = transliterate(rawLatin.replace("d", "D"))
            if (!candidates.contains(altD)) candidates.add(altD)
        }

        // 5. Special common words override
        when (rawLatin.lowercase()) {
            "namaskar" -> candidates.add(0, "ନମସ୍କାର")
            "odisha" -> candidates.add(0, "ଓଡ଼ିଶା")
            "dhanyabad" -> candidates.add(0, "ଧନ୍ୟବାଦ")
            "bhubaneswar" -> {
                candidates.add(0, "ଭୁବନେଶ୍ୱର")
            }
            "cuttack" -> candidates.add(0, "କଟକ")
            "puri" -> candidates.add(0, "ପୁରୀ")
            "jagannath" -> candidates.add(0, "ଜଗନ୍ନାଥ")
            "rasagola" -> candidates.add(0, "ରସଗୋଲା")
            "ami", "ame" -> {
                if (!candidates.contains("ଆମେ")) candidates.add(0, "ଆମେ")
                if (!candidates.contains("ଆମି")) candidates.add(1, "ଆମି")
            }
            "puntu" -> {
                candidates.add(0, "ପୁଣ୍ଟୁ")
                candidates.add(1, "ପୁନ୍ତୁ")
            }
            "jhoti" -> candidates.add(0, "ଝୋଟି")
        }

        val distinct = candidates.distinct()
        return distinct.sortedByDescending { word ->
            if (dictionaryWords.contains(word)) 100 else 0
        }
    }

    override fun onCommit(rawLatin: String, selectedCandidate: String?): String {
        return selectedCandidate ?: processComposing(rawLatin).firstOrNull() ?: rawLatin
    }

    fun transliterate(input: String): String {
        val result = StringBuilder()
        var i = 0
        val len = input.length
        var prevWasConsonant = false

        while (i < len) {
            val remaining = input.substring(i)

            // 1. Nukta & explicit virama
            if (remaining.startsWith("d.") || remaining.startsWith("r.")) {
                result.append("ଡ଼")
                prevWasConsonant = true
                i += 2
                continue
            }
            if (remaining.startsWith("dh.") || remaining.startsWith("rh.")) {
                result.append("ଢ଼")
                prevWasConsonant = true
                i += 3
                continue
            }
            if (remaining.startsWith("\\")) {
                result.append(halant)
                prevWasConsonant = false
                i += 1
                continue
            }

            // 2. Modifiers: Anusvara, Visarga, Chandrabindu (case-sensitive checks before general consonants)
            if (remaining.startsWith("M") || remaining.startsWith("m.")) {
                result.append("ଂ")
                prevWasConsonant = false
                i += if (remaining.startsWith("m.")) 2 else 1
                continue
            }
            if (remaining.startsWith("H") || remaining.startsWith("h.")) {
                result.append("ଃ")
                prevWasConsonant = false
                i += if (remaining.startsWith("h.")) 2 else 1
                continue
            }
            if (remaining.startsWith("~") || remaining.startsWith("n~")) {
                result.append("ଁ")
                prevWasConsonant = false
                i += if (remaining.startsWith("n~")) 2 else 1
                continue
            }

            // 3. Dependent Vowels (Matras) when preceded by a consonant
            if (prevWasConsonant) {
                var matchedMatra: String? = null
                var matchedMatraLen = 0

                for ((latinVowel, odiaMatra) in matras) {
                    if (remaining.startsWith(latinVowel, ignoreCase = false)) {
                        matchedMatra = odiaMatra
                        matchedMatraLen = latinVowel.length
                        break
                    }
                }

                if (matchedMatra != null) {
                    result.append(matchedMatra)
                    prevWasConsonant = false
                    i += matchedMatraLen
                    continue
                }
            } else {
                // 4. Independent Vowels when not preceded by a consonant
                var matchedVowel: String? = null
                var matchedVowelLen = 0

                for ((latinVowel, odiaVowel) in independentVowels) {
                    if (remaining.startsWith(latinVowel, ignoreCase = false)) {
                        matchedVowel = odiaVowel
                        matchedVowelLen = latinVowel.length
                        break
                    }
                }

                if (matchedVowel != null) {
                    result.append(matchedVowel)
                    prevWasConsonant = false
                    i += matchedVowelLen
                    continue
                }
            }

            // 5. Retroflex consonants (e.g. Th -> ଠ, Dh -> ଢ, T -> ଟ, D -> ଡ)
            var matchedRetroflex: String? = null
            var matchedRetroflexLen = 0
            for ((latinRetro, odiaRetro) in retroflexConsonants) {
                if (remaining.startsWith(latinRetro, ignoreCase = false)) {
                    matchedRetroflex = odiaRetro
                    matchedRetroflexLen = latinRetro.length
                    break
                }
            }

            if (matchedRetroflex != null) {
                if (prevWasConsonant) {
                    result.append(halant)
                }
                result.append(matchedRetroflex)
                prevWasConsonant = true
                i += matchedRetroflexLen
                continue
            }

            // 6. General consonants
            var matchedConsonant: String? = null
            var matchedConsonantLen = 0

            for ((latinCons, odiaCons) in consonants) {
                if (remaining.startsWith(latinCons, ignoreCase = false)) {
                    matchedConsonant = odiaCons
                    matchedConsonantLen = latinCons.length
                    break
                }
            }

            if (matchedConsonant != null) {
                if (prevWasConsonant) {
                    result.append(halant)
                }
                result.append(matchedConsonant)
                prevWasConsonant = true
                i += matchedConsonantLen
                continue
            }

            // 7. Digits
            val c = input[i]
            if (c in '0'..'9') {
                val odiaDigit = (0x0B66 + (c - '0')).toChar()
                result.append(odiaDigit)
                prevWasConsonant = false
                i++
                continue
            }

            // 8. Other characters (whitespace, punctuation)
            result.append(c)
            prevWasConsonant = false
            i++
        }

        return result.toString()
    }
}
