package com.app.polacam.gyro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.media3.common.util.Log

class Rotation(
    context: Context
) {


    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)


    private var sensorEventListener: SensorEventListener? = null
    private var isListenerRegistered = false

    fun onPause() {

        if (sensorEventListener != null && isListenerRegistered)
        {
            sensorManager.unregisterListener(sensorEventListener)
            isListenerRegistered = false
        }
    }

    fun getRotation(

        rotationValues: (x : Float, y : Float, z : Float) -> Unit
    )
    {

        if(sensorEventListener == null)
        {
            sensorEventListener = object : SensorEventListener
            {
                override fun onSensorChanged(event: SensorEvent?) {




                    if(event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR)
                    {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix , event.values)
                        SensorManager.getOrientation( rotationMatrix, orientationAngles)

                        val azimuthInDegrees = orientationAngles[0]
                        val pitchInDegrees = orientationAngles[1]
                        val rollInDegrees = orientationAngles[2]

                        // Adjust azimuth to be 0-360 degrees if desired (optional)
                        // val adjustedAzimuthInDegrees = (azimuthInDegrees + 360) % 360

                        // 4. Pass the angles in DEGREES to your callback
                        // Assuming your rotationValues callback expects x, y, z as azimuth, pitch, roll
                        rotationValues(
                            rollInDegrees,  // Typically what people refer to as 'X' or heading
                            pitchInDegrees,
                            azimuthInDegrees// Typically 'Y'
                                  // Typically 'Z'
                        )
                    }
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {

                }

            }
        }


        if(!isListenerRegistered)
        {
            sensorManager.registerListener(
                sensorEventListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            isListenerRegistered = true
        }

    }
}