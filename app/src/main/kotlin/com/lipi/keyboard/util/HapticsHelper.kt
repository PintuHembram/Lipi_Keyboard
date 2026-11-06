package com.lipi.keyboard.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.lipi.keyboard.data.SettingsManager

object HapticsHelper {

    fun performKeyTick(view: View) {
        val context = view.context

        // 1. Haptic Vibration
        if (SettingsManager.isHapticEnabled(context)) {
            val performed = view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
            if (!performed) {
                vibrate(context, 10L)
            }
        }

        // 2. Sound Effect
        if (SettingsManager.isSoundEnabled(context)) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
            } catch (_: Exception) {
                // Ignore audio exception
            }
        }
    }

    private fun vibrate(context: Context, milliseconds: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(milliseconds)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration exception
        }
    }
}
