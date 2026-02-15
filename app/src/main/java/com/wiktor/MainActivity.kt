package com.wiktor

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private lateinit var textView: TextView

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

        // initialize textview
        val textView = findViewById<TextView>(R.id.textView)

        // init sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    }
    // register sensor when the app is active
    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    // unregister sensor when the app is in background
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)

            // Convert radians to degrees
            val azimuth = Math.toDegrees(orientationValues[0].toDouble()).roundToInt()
            val pitch = Math.toDegrees(orientationValues[1].toDouble()).roundToInt()
            val roll = Math.toDegrees(orientationValues[2].toDouble()).roundToInt()

            textView.text = "Rotation:\nAzimuth: $azimuth°\nPitch: $pitch°\nRoll: $roll°"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this app
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