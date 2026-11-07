package com.lipi.keyboard.suggest

class Trie {

    private class Node {
        val children = mutableMapOf<Char, Node>()
        var isEndOfWord: Boolean = false
        var frequency: Int = 0
        var word: String? = null
    }

    private val root = Node()

    fun insert(word: String, frequency: Int = 1) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { Node() }
        }
        current.isEndOfWord = true
        current.frequency = maxOf(current.frequency, frequency)
        current.word = word
    }

    fun contains(word: String): Boolean {
        var current = root
        for (char in word.lowercase()) {
            current = current.children[char] ?: return false
        }
        return current.isEndOfWord
    }

    fun getWordsWithPrefix(prefix: String, maxCount: Int = 5): List<String> {
        var current = root
        for (char in prefix) {
            current = current.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectAll(current, results)

        return results.sortedByDescending { it.second }
            .take(maxCount)
            .map { it.first }
    }

    private fun collectAll(node: Node, results: MutableList<Pair<String, Int>>) {
        if (node.isEndOfWord && node.word != null) {
            results.add(Pair(node.word!!, node.frequency))
        }
        for (child in node.children.values) {
            collectAll(child, results)
        }
    }
}
