package com.goodwy.dialer.activities

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class OverlayCallActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        val number = intent.getStringExtra("caller_number") ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER
        }

        val labelText = TextView(this).apply {
            text = "RikkY Caller ID"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 14f
            gravity = Gravity.CENTER
        }

        val nameText = TextView(this).apply {
            text = number
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        }

        container.addView(labelText)
        container.addView(nameText)
        setContentView(container)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.TOP)
    }
}
