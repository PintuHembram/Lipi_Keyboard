package com.lipi.keyboard.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lipi.keyboard.suggest.GibberishFilter

class PersonalDictionaryDatabase(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "lipi_personal_dict.db"
        const val DATABASE_VERSION = 2

        const val TABLE_USER_WORDS = "user_words"
        const val TABLE_CANDIDATES = "word_candidates"
        const val TABLE_BIGRAMS = "bigrams"
        const val TABLE_BLOCKED = "blocked_words"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USER_WORDS (
                word TEXT PRIMARY KEY,
                count INTEGER DEFAULT 1,
                session_count INTEGER DEFAULT 1,
                last_session_id INTEGER DEFAULT 0,
                last_used_timestamp INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CANDIDATES (
                word TEXT PRIMARY KEY,
                count INTEGER DEFAULT 1,
                session_count INTEGER DEFAULT 1,
                last_session_id INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BIGRAMS (
                prev_word TEXT,
                next_word TEXT,
                count INTEGER DEFAULT 1,
                PRIMARY KEY (prev_word, next_word)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_BLOCKED (
                word TEXT PRIMARY KEY
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            runMigrationPurge(db)
        }
    }

    fun runMigrationPurge(db: SQLiteDatabase = writableDatabase) {
        // Migration: Purge user_words that fail gibberish filter or have count < 3 or session_count < 2
        val cursor = db.rawQuery("SELECT word, count, session_count FROM $TABLE_USER_WORDS", null)
        val toDelete = mutableListOf<String>()

        while (cursor.moveToNext()) {
            val word = cursor.getString(0)
            val count = cursor.getInt(1)
            val sessionCount = cursor.getInt(2)

            if (GibberishFilter.isGibberish(word) || count < 3 || sessionCount < 2) {
                toDelete.add(word)
            }
        }
        cursor.close()

        db.beginTransaction()
        try {
            for (word in toDelete) {
                db.delete(TABLE_USER_WORDS, "word = ?", arrayOf(word))
            }

            // Also purge candidate table for any gibberish
            val candCursor = db.rawQuery("SELECT word FROM $TABLE_CANDIDATES", null)
            val candDelete = mutableListOf<String>()
            while (candCursor.moveToNext()) {
                val candWord = candCursor.getString(0)
                if (GibberishFilter.isGibberish(candWord)) {
                    candDelete.add(candWord)
                }
            }
            candCursor.close()

            for (candWord in candDelete) {
                db.delete(TABLE_CANDIDATES, "word = ?", arrayOf(candWord))
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun isBlocked(word: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLE_BLOCKED WHERE word = ?", arrayOf(word.lowercase())
        )
        val blocked = cursor.moveToFirst()
        cursor.close()
        return blocked
    }

    fun recordWordCommit(word: String, sessionId: Long, inBuiltInDict: Boolean) {
        val lower = word.lowercase().trim()
        if (lower.isEmpty() || isBlocked(lower) || GibberishFilter.isGibberish(lower)) return

        val db = writableDatabase

        if (inBuiltInDict) {
            // Built-in words don't need user_words promotion, but we can track count if needed
            return
        }

        db.beginTransaction()
        try {
            // Check if already in user_words
            val userCursor = db.rawQuery(
                "SELECT count FROM $TABLE_USER_WORDS WHERE word = ?", arrayOf(lower)
            )
            if (userCursor.moveToFirst()) {
                val currentCount = userCursor.getInt(0)
                val cv = ContentValues().apply {
                    put("count", currentCount + 1)
                    put("last_used_timestamp", System.currentTimeMillis())
                }
                db.update(TABLE_USER_WORDS, cv, "word = ?", arrayOf(lower))
                userCursor.close()
                db.setTransactionSuccessful()
                return
            }
            userCursor.close()

            // Check word_candidates
            val candCursor = db.rawQuery(
                "SELECT count, session_count, last_session_id FROM $TABLE_CANDIDATES WHERE word = ?",
                arrayOf(lower)
            )

            if (candCursor.moveToFirst()) {
                var count = candCursor.getInt(0) + 1
                var sessionCount = candCursor.getInt(1)
                val lastSessionId = candCursor.getLong(2)

                if (lastSessionId != sessionId) {
                    sessionCount += 1
                }

                candCursor.close()

                if (count >= 3 && sessionCount >= 2) {
                    // Promote to user_words!
                    db.delete(TABLE_CANDIDATES, "word = ?", arrayOf(lower))
                    val cv = ContentValues().apply {
                        put("word", lower)
                        put("count", count)
                        put("session_count", sessionCount)
                        put("last_session_id", sessionId)
                        put("last_used_timestamp", System.currentTimeMillis())
                    }
                    db.insertWithOnConflict(TABLE_USER_WORDS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                } else {
                    val cv = ContentValues().apply {
                        put("count", count)
                        put("session_count", sessionCount)
                        put("last_session_id", sessionId)
                    }
                    db.update(TABLE_CANDIDATES, cv, "word = ?", arrayOf(lower))
                }
            } else {
                candCursor.close()
                // New candidate
                val cv = ContentValues().apply {
                    put("word", lower)
                    put("count", 1)
                    put("session_count", 1)
                    put("last_session_id", sessionId)
                }
                db.insertWithOnConflict(TABLE_CANDIDATES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getLearnedWords(): Set<String> {
        val result = mutableSetOf<String>()
        val cursor = readableDatabase.rawQuery("SELECT word FROM $TABLE_USER_WORDS", null)
        while (cursor.moveToNext()) {
            result.add(cursor.getString(0))
        }
        cursor.close()
        return result
    }

    fun removeUserWord(word: String) {
        val lower = word.lowercase().trim()
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_USER_WORDS, "word = ?", arrayOf(lower))
            db.delete(TABLE_CANDIDATES, "word = ?", arrayOf(lower))
            db.delete(TABLE_BIGRAMS, "prev_word = ? OR next_word = ?", arrayOf(lower, lower))

            val cv = ContentValues().apply {
                put("word", lower)
            }
            db.insertWithOnConflict(TABLE_BLOCKED, null, cv, SQLiteDatabase.CONFLICT_REPLACE)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun clearAllLearnedWords() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM $TABLE_USER_WORDS")
            db.execSQL("DELETE FROM $TABLE_CANDIDATES")
            db.execSQL("DELETE FROM $TABLE_BIGRAMS")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
