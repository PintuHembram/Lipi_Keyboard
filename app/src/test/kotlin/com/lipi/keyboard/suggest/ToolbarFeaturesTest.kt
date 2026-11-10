package com.lipi.keyboard.suggest

import com.lipi.keyboard.data.ClipItem
import com.lipi.keyboard.data.SettingsManager
import com.lipi.keyboard.layout.LayoutParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolbarFeaturesTest {

    @Test
    fun testJsonLayoutParsing() {
        val menuGridJson = """
            {
              "id": "menu_grid",
              "name": "Menu",
              "language": "all",
              "rows": [
                {
                  "keys": [
                    { "type": "emoji", "label": "😊 Emoji", "targetLayer": "emojis" },
                    { "type": "symbol", "label": "🏷️ Sticker", "targetLayer": "stickers" }
                  ]
                }
              ]
            }
        """.trimIndent()

        val layout = LayoutParser.parse(menuGridJson)
        assertEquals("menu_grid", layout.id)
        assertEquals(1, layout.rows.size)
        assertEquals(2, layout.rows[0].keys.size)
        assertEquals("😊 Emoji", layout.rows[0].keys[0].label)
    }

    @Test
    fun testClipboardExpiry() {
        val now = System.currentTimeMillis()
        val oldClip = ClipItem("1", "old text", now - 4000000L, isPinned = false)
        val pinnedClip = ClipItem("2", "pinned text", now - 4000000L, isPinned = true)
        val newClip = ClipItem("3", "new text", now - 1000L, isPinned = false)

        val clips = mutableListOf(oldClip, pinnedClip, newClip)
        val expireMillis = 3600000L

        clips.removeAll { !it.isPinned && (now - it.timestamp > expireMillis) }

        assertEquals(2, clips.size)
        assertFalse(clips.contains(oldClip))
        assertTrue(clips.contains(pinnedClip))
        assertTrue(clips.contains(newClip))
    }

    @Test
    fun testThemeColors() {
        val amoledColors = SettingsManager.getThemeColors("Dark AMOLED")
        assertNotNull(amoledColors)

        val greenColors = SettingsManager.getThemeColors("Emerald Green")
        assertNotNull(greenColors)

        val lightColors = SettingsManager.getThemeColors("Classic Light")
        assertNotNull(lightColors)
    }
}
