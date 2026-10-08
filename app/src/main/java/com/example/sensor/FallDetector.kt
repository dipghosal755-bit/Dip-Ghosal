package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class FallDetector(
    context: Context,
    private val onFallDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isListening = false
    private var lastImpactTimestamp: Long = 0
    private var highImpactObserved = false

    // Thresholds: Standard Earth gravity is ~9.8 m/s^2.
    // Freefall/Impact threshold > 24 m/s^2 (~2.5G)
    private val impactThreshold = 24.0f
    private val postImpactStillThreshold = 3.5f

    fun start() {
        if (!isListening && sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stop() {
        if (isListening && sensorManager != null) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val now = System.currentTimeMillis()

            if (magnitude > impactThreshold) {
                highImpactObserved = true
                lastImpactTimestamp = now
            } else if (highImpactObserved && (now - lastImpactTimestamp) in 300..1800) {
                // If device experiences relative stillness/rest shortly after sharp impact spike
                if (magnitude < postImpactStillThreshold) {
                    highImpactObserved = false
                    onFallDetected()
                }
            } else if (now - lastImpactTimestamp > 2500) {
                highImpactObserved = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * For hackathon demo and testing without physically dropping device
     */
    fun triggerSimulatedFall() {
        onFallDetected()
    }
}
