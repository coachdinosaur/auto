package com.coachdinosaur.auto

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val preferences by lazy {
        getSharedPreferences("auto_clicker_settings", MODE_PRIVATE)
    }

    private lateinit var xInput: EditText
    private lateinit var yInput: EditText
    private lateinit var intervalInput: EditText
    private lateinit var delayInput: EditText
    private lateinit var countInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        xInput = findViewById(R.id.xInput)
        yInput = findViewById(R.id.yInput)
        intervalInput = findViewById(R.id.intervalInput)
        delayInput = findViewById(R.id.delayInput)
        countInput = findViewById(R.id.countInput)
        statusText = findViewById(R.id.statusText)

        restoreSettings()

        findViewById<Button>(R.id.accessibilityButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startClicking()
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            val stopped = AutoClickService.stop()
            statusText.text = if (stopped) {
                getString(R.string.status_stopped)
            } else {
                getString(R.string.status_enable_service)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        statusText.text = if (AutoClickService.isConnected()) {
            getString(R.string.status_ready)
        } else {
            getString(R.string.status_enable_service)
        }
    }

    private fun startClicking() {
        val x = xInput.text.toString().toIntOrNull()
        val y = yInput.text.toString().toIntOrNull()
        val interval = intervalInput.text.toString().toLongOrNull()
        val delay = delayInput.text.toString().toLongOrNull()
        val count = countInput.text.toString().toIntOrNull()

        if (x == null || y == null || interval == null || delay == null || count == null) {
            statusText.text = getString(R.string.status_invalid_input)
            return
        }

        if (x < 0 || y < 0 || interval < 100 || delay < 0 || count < 0) {
            statusText.text = getString(R.string.status_invalid_range)
            return
        }

        saveSettings(x, y, interval, delay, count)

        val started = AutoClickService.start(
            AutoClickService.ClickConfig(
                x = x,
                y = y,
                intervalMs = interval,
                startDelayMs = delay,
                tapCount = count
            )
        )

        statusText.text = if (started) {
            getString(R.string.status_running)
        } else {
            getString(R.string.status_enable_service)
        }
    }

    private fun restoreSettings() {
        xInput.setText(preferences.getInt("x", 500).toString())
        yInput.setText(preferences.getInt("y", 1000).toString())
        intervalInput.setText(preferences.getLong("interval", 1000L).toString())
        delayInput.setText(preferences.getLong("delay", 3000L).toString())
        countInput.setText(preferences.getInt("count", 0).toString())
    }

    private fun saveSettings(x: Int, y: Int, interval: Long, delay: Long, count: Int) {
        preferences.edit()
            .putInt("x", x)
            .putInt("y", y)
            .putLong("interval", interval)
            .putLong("delay", delay)
            .putInt("count", count)
            .apply()
    }
}
