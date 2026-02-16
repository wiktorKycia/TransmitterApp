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
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    // --- CONFIGURATION ---
    private val DEST_IP = "192.168.1.74" // REPLACE WITH YOUR LINUX IP
    private val DEST_PORT = 5005
    // ---------------------

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private lateinit var textView: TextView

    private var udpSocket: DatagramSocket? = null
    private var leftPressed = 0
    private var rightPressed = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUI()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textView = findViewById<TextView>(R.id.textView)
        val btnLeft = findViewById<FrameLayout>(R.id.btnLeft)
        val btnRight = findViewById<FrameLayout>(R.id.btnRight)

        btnLeft.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> leftPressed = 1
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> leftPressed = 0
            }
            handleTouch(view, event, "#4CAF50".toColorInt())
            true
        }

        btnRight.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> rightPressed = 1
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> rightPressed = 0
            }
            handleTouch(view, event, "#2196F3".toColorInt())
            true
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // Initialize UDP Socket in a background thread
        thread {
            try {
                udpSocket = DatagramSocket()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) // <- fastest, UI: 60Hz, NORMAL:5-10Hz
        }
    }

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

            val azimuth = Math.toDegrees(orientationValues[0].toDouble()).roundToInt()
            val pitch = Math.toDegrees(orientationValues[1].toDouble()).roundToInt()
            val roll = Math.toDegrees(orientationValues[2].toDouble()).roundToInt()

            textView.text = "Azimuth: $azimuth°\nPitch: $pitch°\nRoll: $roll°\nL: $leftPressed R: $rightPressed"

            // Send data via UDP
            sendUdpData("$azimuth $pitch $roll $leftPressed $rightPressed")
        }
    }

    private fun sendUdpData(message: String) {
        thread {
            try {
                val address = InetAddress.getByName(DEST_IP)
                val buf = message.toByteArray()
                val packet = DatagramPacket(buf, buf.size, address, DEST_PORT)
                udpSocket?.send(packet)
            } catch (e: Exception) {
                // Ignore network errors to prevent UI lag
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun handleTouch(view: View, event: MotionEvent, activeColor: Int) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> view.setBackgroundColor(activeColor)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.setBackgroundColor("#222222".toColorInt())
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}