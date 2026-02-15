package com.wiktor

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize buttons
        val btnLeft = findViewById<FrameLayout>(R.id.btnLeft)
        val btnRight = findViewById<FrameLayout>(R.id.btnRight)

        btnLeft.setOnTouchListener { view, event ->
            handleTouch(view, event, "#4CAF50".toColorInt()) // Green when pressed
            true
        }
        btnRight.setOnTouchListener { view, event ->
            handleTouch(view, event, "#2196F3".toColorInt()) // Blue when pressed
            true
        }
    }

    private fun handleTouch(view: View, event: MotionEvent, activeColor: Int) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Pressed
                view.setBackgroundColor(activeColor)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Released
                view.setBackgroundColor("#222222".toColorInt())
            }
        }
    }
}