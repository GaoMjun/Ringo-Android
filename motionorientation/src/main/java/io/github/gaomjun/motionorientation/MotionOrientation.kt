package io.github.gaomjun.motionorientation

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Created by qq on 11/4/2017.
 */
object MotionOrientation : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    private var R =  FloatArray(9)
    private var orientation = FloatArray(3)

    @JvmStatic val DEVICE_ORIENTATION_UNKNOWN = 0
    @JvmStatic val DEVICE_ORIENTATION_PORTRAIT = 1
    @JvmStatic val DEVICE_ORIENTATION_UPSIDEDOWN = 2
    @JvmStatic val DEVICE_ORIENTATION_LANDSCAPERIGHT = 3
    @JvmStatic val DEVICE_ORIENTATION_LANDSCAPELEFT = 4
    @JvmStatic val DEVICE_ORIENTATION_FACEUP = 5
    @JvmStatic val DEVICE_ORIENTATION_FACEDOWN = 6

    @JvmStatic var DEVICE_ORIENTATION = DEVICE_ORIENTATION_UNKNOWN

    private var lastDeviceOrientation = DEVICE_ORIENTATION_UNKNOWN
    
    private val THRESHOLD = 10
    private val THRESHOLDHARD = 30

    @JvmStatic fun init(context: Context?): MotionOrientation? {
        sensorManager = context?.getSystemService(SENSOR_SERVICE) as SensorManager?
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)

        return this
    }

    fun releaseSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor) {
            accelerometer -> {
                System.arraycopy(event?.values, 0, lastAccelerometer, 0, event?.values!!.size)
                lastAccelerometerSet = true
            }

            magnetometer -> {
                System.arraycopy(event?.values, 0, lastMagnetometer, 0, event?.values!!.size)
                lastMagnetometerSet = true
            }
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            if (SensorManager.getRotationMatrix(R, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(R, orientation)

                val heading = orientation[0] * 180 / Math.PI // heading
                val pitch = orientation[1] * 180 / Math.PI // pitch
                val roll = orientation[2] * 180 / Math.PI // roll
//                println("$x $y $z")
//                return

                if (isPortrait(pitch)) {
//                    if (lastDeviceOrientation == DEVICE_ORIENTATION_FACEUP || lastDeviceOrientation == DEVICE_ORIENTATION_FACEDOWN) {
//                        DEVICE_ORIENTATION = DEVICE_ORIENTATION_PORTRAIT
//                        invokeCallback()
//                        lastDeviceOrientation = DEVICE_ORIENTATION_PORTRAIT
//                        return
//                    } else {
//                        if (lastDeviceOrientation == DEVICE_ORIENTATION_PORTRAIT) {
//                            invokeCallback()
//                            return
//                        } else {
//                            if (isPortraitHard(pitch)) {
//                                DEVICE_ORIENTATION = DEVICE_ORIENTATION_PORTRAIT
//                                invokeCallback()
//                                lastDeviceOrientation = DEVICE_ORIENTATION_PORTRAIT
//                                return
//                            }
//                        }
//                    }

                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_PORTRAIT
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_PORTRAIT
                    return
                }

                if (isUpsideDown(pitch)) {
                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_UPSIDEDOWN
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_UPSIDEDOWN
                    return
                }

                if (isLandscapeRight(roll)) {
                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_LANDSCAPERIGHT
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_LANDSCAPERIGHT
                    return
                }

                if (isLandscapeLeft(roll)) {
                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_LANDSCAPELEFT
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_LANDSCAPELEFT
                }

                if (isFaceUp(roll, pitch)) {
                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_FACEUP
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_FACEUP
                    return
                }

                if (isFaceDown(roll, pitch)) {
                    DEVICE_ORIENTATION = DEVICE_ORIENTATION_FACEDOWN
                    invokeCallback()
                    lastDeviceOrientation = DEVICE_ORIENTATION_FACEDOWN
                    return
                }

            } else {
                DEVICE_ORIENTATION = DEVICE_ORIENTATION_UNKNOWN
                invokeCallback()
                lastDeviceOrientation = DEVICE_ORIENTATION_UNKNOWN
                return
            }
        }
    }

    private fun invokeCallback() {
        deviceOrientationListener?.deviceOrientationChanged(DEVICE_ORIENTATION)
        if (DEVICE_ORIENTATION != lastDeviceOrientation) {
            deviceOrientationListener?.deviceOrientationChangedFromTo(lastDeviceOrientation, DEVICE_ORIENTATION)
        }
    }

    private fun isPortrait(pitch: Double) = pitch < -45-THRESHOLD
    private fun isPortraitHard(pitch: Double) = pitch < -45- THRESHOLDHARD

    private fun isUpsideDown(pitch: Double) = pitch > 45+THRESHOLD
    private fun isUpsideDownHard(pitch: Double) = pitch > 45+ THRESHOLDHARD

    private fun isLandscapeRight(roll: Double) = roll < -45-THRESHOLD && roll > -135+THRESHOLD
    private fun isLandscapeRightHard(roll: Double) = roll < -45-THRESHOLDHARD && roll > -135+ THRESHOLDHARD

    private fun isLandscapeLeft(roll: Double) = roll > 45+THRESHOLD && roll < 135-THRESHOLD
    private fun isLandscapeLeftHard(roll: Double) = roll > 45+THRESHOLDHARD && roll < 135- THRESHOLDHARD

    private fun isFaceUp(roll: Double, pitch: Double) = (roll > -45+THRESHOLD && roll < 45-THRESHOLD) && (pitch < 45-THRESHOLD && pitch > -45+THRESHOLD)
    private fun isFaceUpHard(roll: Double, pitch: Double) = (roll > -45+ THRESHOLDHARD && roll < 45-THRESHOLDHARD) && (pitch < 45-THRESHOLDHARD && pitch > -45+THRESHOLDHARD)

    private fun isFaceDown(roll: Double, pitch: Double) = (roll > 135+THRESHOLD || roll < -135-THRESHOLD) && (pitch > -45+THRESHOLD && pitch < 45-THRESHOLD)
    private fun isFaceDownHard(roll: Double, pitch: Double) = (roll > 135+THRESHOLDHARD || roll < -135-THRESHOLDHARD) && (pitch > -45+THRESHOLDHARD && pitch < 45-THRESHOLDHARD)

    interface DeviceOrientationListener {
        fun deviceOrientationChanged(orientaion: Int)
        fun deviceOrientationChangedFromTo(from: Int, to: Int)
    }
    var deviceOrientationListener: DeviceOrientationListener? = null
}