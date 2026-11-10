package com.lipi.keyboard.data

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

data class ClipItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    var isPinned: Boolean = false
)

class ClipboardHistoryManager(private val context: Context) {

    private val clips = mutableListOf<ClipItem>()
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkAndRecordPrimaryClip()
    }

    fun startListening() {
        try {
            clipboardManager?.addPrimaryClipChangedListener(clipListener)
            checkAndRecordPrimaryClip()
        } catch (_: Exception) {
        }
    }

    fun stopListening() {
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipListener)
        } catch (_: Exception) {
        }
    }

    private fun checkAndRecordPrimaryClip() {
        val manager = clipboardManager ?: return
        val primaryClip = manager.primaryClip ?: return
        val description = manager.primaryClipDescription ?: return

        // Skip sensitive clips on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (description.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE) == true) {
                return
            }
        }

        if (primaryClip.itemCount > 0) {
            val text = primaryClip.getItemAt(0).coerceToText(context).toString().trim()
            if (text.isNotEmpty() && text.length < 5000) {
                addClip(text, false)
            }
        }
    }

    @Synchronized
    fun addClip(text: String, isPinned: Boolean = false) {
        purgeExpiredClips()
        val lowerText = text.trim()
        if (lowerText.isEmpty()) return

        // Remove duplicate if exists
        clips.removeAll { it.text == lowerText }

        val item = ClipItem(
            id = System.currentTimeMillis().toString() + "_" + lowerText.hashCode(),
            text = lowerText,
            timestamp = System.currentTimeMillis(),
            isPinned = isPinned
        )
        clips.add(0, item)

        // Cap history at 50 items
        if (clips.size > 50) {
            val lastUnpinnedIndex = clips.indexOfLast { !it.isPinned }
            if (lastUnpinnedIndex != -1) {
                clips.removeAt(lastUnpinnedIndex)
            }
        }
    }

    @Synchronized
    fun getClips(): List<ClipItem> {
        purgeExpiredClips()
        return clips.toList()
    }

    @Synchronized
    fun deleteClip(id: String) {
        clips.removeAll { it.id == id }
    }

    @Synchronized
    fun togglePin(id: String) {
        val item = clips.find { it.id == id }
        if (item != null) {
            item.isPinned = !item.isPinned
        }
    }

    @Synchronized
    fun clearAll() {
        clips.removeAll { !it.isPinned }
    }

    @Synchronized
    fun purgeExpiredClips(expireMillis: Long = 3600000L) {
        val now = System.currentTimeMillis()
        clips.removeAll { !it.isPinned && (now - it.timestamp > expireMillis) }
    }
}
