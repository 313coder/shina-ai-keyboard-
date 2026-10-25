package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Provides tactile haptic feedback for key user interactions throughout the Shina AI app:
 * - Successful word and sentence data submissions
 * - Digital badge awards and milestone celebrations
 * - Dictionary search hits and input queries
 * - UI navigation and button clicks
 */
object ShinaHapticManager {

    /**
     * Subtle click feedback for typing and button taps.
     */
    fun performKeyClick(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibrate(context, durationMs = 12, amplitude = 50)
    }

    /**
     * Tactile success feedback for submitting Shina words, verifying sentences, or importing datasets.
     * Dual pulse pattern: quick prep pulse followed by a satisfying confirmation pulse.
     */
    fun performDataSubmissionSuccess(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibratePattern(
            context = context,
            timings = longArrayOf(0, 35, 60, 55),
            amplitudes = intArrayOf(0, 140, 0, 240)
        )
    }

    /**
     * Celebratory tactile pattern for unlocking or tapping gamification digital badges.
     * Triple ascending pulse pattern creating a crisp achievement sensation.
     */
    fun performBadgeAward(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibratePattern(
            context = context,
            timings = longArrayOf(0, 40, 50, 60, 50, 90),
            amplitudes = intArrayOf(0, 160, 0, 210, 0, 255)
        )
    }

    /**
     * Soft crisp feedback when dictionary search results are found or tapped.
     */
    fun performDictionaryResultFound(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibrate(context, durationMs = 18, amplitude = 90)
    }

    /**
     * Warning or error haptic feedback.
     */
    fun performWarning(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        vibratePattern(
            context = context,
            timings = longArrayOf(0, 50, 70, 50),
            amplitudes = intArrayOf(0, 200, 0, 200)
        )
    }

    private fun vibrate(context: Context, durationMs: Long, amplitude: Int) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore devices where vibration permission is denied or silent mode is active
        }
    }

    private fun vibratePattern(context: Context, timings: LongArray, amplitudes: IntArray) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
