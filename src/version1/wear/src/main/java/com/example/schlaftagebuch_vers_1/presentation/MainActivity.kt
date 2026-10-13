package com.example.schlaftagebuch_vers_1.presentation

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import androidx.localbroadcastmanager.content.LocalBroadcastManager // Wichtig, auch wenn veraltet

class MainActivity : Activity() {

    // Variable im Layout
    private lateinit var statusText: TextView

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val status = intent?.getStringExtra("status")

            // Statusprüfung der Daten
            if (status == "running") {
                statusText.text = "AUFNAHME\nLÄUFT"
                statusText.setTextColor(Color.RED)
            } else {
                statusText.text = "BEREIT"
                statusText.setTextColor(Color.GREEN)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Berechtigungen
        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.BODY_SENSORS,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ), 1
            )
        }

        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(Color.BLACK)
        scrollView.isFillViewport = true

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(10, 10, 10, 10)

        val title = TextView(this)
        title.text = "Schlaftagebuch"
        title.setTextColor(Color.parseColor("#00FF00"))
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        title.gravity = Gravity.CENTER

        statusText = TextView(this)
        statusText.text = "BEREIT"
        statusText.setTextColor(Color.WHITE)
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        statusText.typeface = Typeface.DEFAULT_BOLD
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 4, 0, 4)

        val info = TextView(this)
        info.text = "App läuft.\nBitte Handy nutzen."
        info.setTextColor(Color.LTGRAY)
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        info.gravity = Gravity.CENTER
        info.setLineSpacing(0f, 0.8f)

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(info)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            statusReceiver,
            IntentFilter("com.example.schlaftagebuch.STATUS_UPDATE")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver)
    }
}