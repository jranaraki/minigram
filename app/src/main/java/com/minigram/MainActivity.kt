package com.minigram

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val setupButton = findViewById<Button>(R.id.setupButton)
        val statusText = findViewById<TextView>(R.id.statusText)

        setupButton.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
            } else {
                openAccessibilitySettings()
                statusText.text = "Enable MiniGram in Accessibility settings"
            }
        }

        updateStatus()
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun updateStatus() {
        val statusText = findViewById<TextView>(R.id.statusText)
        statusText.text = if (hasOverlayPermission()) {
            "Overlay permission granted. Tap to enable accessibility."
        } else {
            "Grant overlay permission first."
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}