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

                        rotationValues(rotationMatrix[0], rotationMatrix[1], rotationMatrix[2])
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