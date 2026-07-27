package com.coachdinosaur.auto

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class AutoClickService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var tapX = 0f
    private var tapY = 0f
    private var intervalMs = 1000L
    private var remainingTaps = 0
    private var finiteTapCount = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        stopClickingInternal()
    }

    override fun onDestroy() {
        stopClickingInternal()
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun startClickingInternal(config: ClickConfig) {
        stopClickingInternal()

        tapX = config.x.toFloat()
        tapY = config.y.toFloat()
        intervalMs = config.intervalMs.coerceAtLeast(MIN_INTERVAL_MS)
        remainingTaps = config.tapCount.coerceAtLeast(0)
        finiteTapCount = config.tapCount > 0
        running = true

        handler.postDelayed(::performTap, config.startDelayMs.coerceAtLeast(0L))
    }

    private fun stopClickingInternal() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun performTap() {
        if (!running) return
        if (finiteTapCount && remainingTaps <= 0) {
            stopClickingInternal()
            return
        }

        val path = Path().apply { moveTo(tapX, tapY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS))
            .build()

        val dispatched = dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (finiteTapCount) remainingTaps--
                    scheduleNextTap()
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    scheduleNextTap()
                }
            },
            null
        )

        if (!dispatched) scheduleNextTap()
    }

    private fun scheduleNextTap() {
        if (running) handler.postDelayed(::performTap, intervalMs)
    }

    data class ClickConfig(
        val x: Int,
        val y: Int,
        val intervalMs: Long,
        val startDelayMs: Long,
        val tapCount: Int
    )

    companion object {
        private const val MIN_INTERVAL_MS = 100L
        private const val TAP_DURATION_MS = 50L

        @Volatile
        private var instance: AutoClickService? = null

        fun isConnected(): Boolean = instance != null

        fun start(config: ClickConfig): Boolean {
            val service = instance ?: return false
            service.startClickingInternal(config)
            return true
        }

        fun stop(): Boolean {
            val service = instance ?: return false
            service.stopClickingInternal()
            return true
        }
    }
}
