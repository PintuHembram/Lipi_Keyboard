package com.lipi.keyboard.suggest

object GibberishFilter {

    private val qwertyNeighbors = mapOf(
        'q' to setOf('w', 'a', 's'),
        'w' to setOf('q', 'e', 'a', 's', 'd'),
        'e' to setOf('w', 'r', 's', 'd', 'f'),
        'r' to setOf('e', 't', 'd', 'f', 'g'),
        't' to setOf('r', 'y', 'f', 'g', 'h'),
        'y' to setOf('t', 'u', 'g', 'h', 'j'),
        'u' to setOf('y', 'i', 'h', 'j', 'k'),
        'i' to setOf('u', 'o', 'j', 'k', 'l'),
        'o' to setOf('i', 'p', 'k', 'l'),
        'p' to setOf('o', 'l'),
        'a' to setOf('q', 'w', 's', 'z'),
        's' to setOf('q', 'w', 'e', 'a', 'd', 'z', 'x'),
        'd' to setOf('w', 'e', 'r', 's', 'f', 'x', 'c'),
        'f' to setOf('e', 'r', 't', 'd', 'g', 'c', 'v'),
        'g' to setOf('r', 't', 'y', 'f', 'h', 'v', 'b'),
        'h' to setOf('t', 'y', 'u', 'g', 'j', 'b', 'n'),
        'j' to setOf('y', 'u', 'i', 'h', 'k', 'n', 'm'),
        'k' to setOf('u', 'i', 'o', 'j', 'l', 'm'),
        'l' to setOf('i', 'o', 'p', 'k'),
        'z' to setOf('a', 's', 'x'),
        'x' to setOf('s', 'd', 'z', 'c'),
        'c' to setOf('d', 'f', 'x', 'v'),
        'v' to setOf('f', 'g', 'c', 'b'),
        'b' to setOf('g', 'h', 'v', 'n'),
        'n' to setOf('h', 'j', 'b', 'm'),
        'm' to setOf('j', 'k', 'n')
    )

    private val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y')

    private val rareClusters = listOf(
        "hx", "xh", "jx", "xj", "jhx", "hxh", "qj", "jq", "zx", "xz",
        "vq", "qv", "vk", "kv", "qg", "gq", "wx", "xw", "fq", "qf"
    )

    fun isGibberish(word: String): Boolean {
        val w = word.lowercase()

        // 1. Length bounds: length < 2 or > 24
        if (w.length < 2 || w.length > 24) return true

        // 2. Contains digits mixed with letters
        val hasLetter = w.any { it.isLetter() }
        val hasDigit = w.any { it.isDigit() }
        if (hasLetter && hasDigit) return true

        // 3. No vowel and length >= 4
        val hasVowel = w.any { it in vowels }
        if (!hasVowel && w.length >= 4) return true

        // 4. 4 or more consonants in a row
        var consecutiveConsonants = 0
        for (ch in w) {
            if (ch.isLetter() && ch !in vowels) {
                consecutiveConsonants++
                if (consecutiveConsonants >= 4) return true
            } else {
                consecutiveConsonants = 0
            }
        }

        // 5. A character repeated 3 or more times in a row
        var repeatCount = 1
        var prevChar = ' '
        for (ch in w) {
            if (ch == prevChar) {
                repeatCount++
                if (repeatCount >= 3) return true
            } else {
                repeatCount = 1
                prevChar = ch
            }
        }

        // 6. Contains impossible/rare consonant clusters (like "hx", "xh", "jhx", "hxh")
        for (cluster in rareClusters) {
            if (w.contains(cluster)) return true
        }

        // 7. QWERTY key mashing check (mostly adjacent-key mashing)
        if (w.length >= 4 && w.all { it.isLetter() }) {
            var neighborTransitions = 0
            for (i in 0 until w.length - 1) {
                val c1 = w[i]
                val c2 = w[i + 1]
                val neighbors = qwertyNeighbors[c1]
                if (neighbors != null && c2 in neighbors) {
                    neighborTransitions++
                }
            }
            if (neighborTransitions >= w.length - 1) return true
        }

        return false
    }
}
