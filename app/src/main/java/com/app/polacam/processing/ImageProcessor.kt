package com.app.polacam.processing

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.size
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import androidx.core.graphics.createBitmap
import androidx.media3.common.util.Log
import org.opencv.core.Core
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.ROTATE_90_COUNTERCLOCKWISE
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Scalar
import java.util.Collections

class ImageProcessor {


    private fun loadImage(image: ImageProxy, mat: Mat)
    {
        val map = image.toBitmap();
        Utils.bitmapToMat(map, mat);
    }




    fun processImage(
        context: Context,
        image: ImageProxy ,
        mat: Mat,
        imageProcessSuccess: (processingSuccess: Boolean,filePath: String) -> Unit
    )
    {
        loadImage(image, mat)

//        Core.rotate(mat , mat, ROTATE_90_COUNTERCLOCKWISE)



//        val (hist, rows, cols) = calculateHistogram(mat)
//        val(lowerBound, upperBound) = analyzeContrast( hist, rows)

        mat.convertTo(mat , -1,1.0, -40.0)
        val colouredMat = tintColor(increaseSaturation(mat,5.0),0.2)
        Imgproc.blur(colouredMat,colouredMat, Size(8.0, 8.0))


//        var alpha = 1.8
//        var beta = 8.0
//
//        if (upperBound > lowerBound) {
//            alpha = 255.0 / (upperBound - lowerBound)
//            beta = -lowerBound * alpha
//            println("Alpha ${alpha} Beta ${beta}")
//        }
//
//        if(alpha < 2)
//        {
//            val contrastBoostFactor = 1.4
//            alpha *= contrastBoostFactor
//        }
//
//
//        mat.convertTo(mat , -1,alpha, beta * 0.2)
//
//        val brightness = getMeanIntensityFromHistogram(hist, rows)
//
//        if(brightness > 0)
//        {
//            when(brightness)
//            {
//                in 10f..50f -> mat.convertTo(mat , -1,1.0, brightness * 1.5)
//                in 51f..90f -> mat.convertTo(mat , -1,1.0, brightness / 3)
//                in 91f..150f -> mat.convertTo(mat , -1,1.0, 0.1)
//                in 150f..200f -> mat.convertTo(mat , -1,1.0, 0.05)
//
//            }
//
//
//        }
//
//        val colouredMat = tintMatPink(mat,0.4)


        saveImage(
            context,
            colouredMat,
            {

                    saveSuccess, filePath -> imageProcessSuccess(saveSuccess, filePath)
            }
        )

    }


    fun calculateHistogram(imageMat: Mat): Triple<Mat,Int,Int>
    {


        val grayMat = Mat()
        if (imageMat.channels() > 1) {
            Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        } else {
            imageMat.copyTo(grayMat)
        }

        val hist = Mat()
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)
        val channels = MatOfInt(0)

        Imgproc.calcHist(
            Collections.singletonList(grayMat),
            channels,
            Mat(),
            hist,
            histSize,
            ranges
        )



        grayMat.release()
        return Triple(hist,imageMat.rows(),imageMat.cols())
    }



    fun analyzeContrast(hist: Mat, totalPixels: Int): Pair<Int, Int> {


        val thresholdPercentage = 0.05 // 5% of pixels
        var lowerBound = 0
        var upperBound = 255
        var cumulativeCount = 0f


        for (i in 0 until hist.rows()) {
            cumulativeCount += hist.get(i, 0)[0].toFloat()
            if (cumulativeCount / totalPixels > thresholdPercentage)
            {
                lowerBound = i
                break
            }
        }

        cumulativeCount = 0f

        for (i in (hist.rows() - 1) downTo 0)
        {
            cumulativeCount += hist.get(i, 0)[0].toFloat()
            if (cumulativeCount / totalPixels > thresholdPercentage)
            {
                upperBound = i
                break
            }
        }

        return Pair(lowerBound, upperBound)
    }

    fun getMeanIntensityFromHistogram(hist: Mat, totalPixels: Int): Double
    {
        if (hist.empty() || hist.rows() != 256 || totalPixels <= 0)
        {
            return -1.0
        }

        var sumOfIntensities = 0.0
        for (i in 0 until hist.rows()) {
            val count = hist.get(i, 0)[0]
            sumOfIntensities += (i * count)
        }
        return sumOfIntensities / totalPixels
    }


    fun increaseSaturation(
        mat: Mat, saturationAmount: Double = 0.2
    ): Mat
    {

        // Check if the input Mat has 3 channels (RGB)
        if (mat.channels() < 3) {
            return mat
        }

        // Convert the image from BGR to HSV color space
        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_BGR2HSV)

        // Split the HSV image into separate channels
        val channels = ArrayList<Mat>()
        Core.split(hsvMat, channels)

        val hChannel = channels[0] // Hue
        val sChannel = channels[1] // Saturation
        val vChannel = channels[2] // Value (Brightness)

        // Increase the saturation channel by a scaling factor
        // To prevent values from exceeding 255, we use Core.addWeighted
        Core.addWeighted(sChannel, 1.0, sChannel, saturationAmount, 0.0, sChannel)

        // Merge the channels back together
        Core.merge(channels, hsvMat)

        // Convert the image back from HSV to BGR
        val saturatedMat = Mat()
        Imgproc.cvtColor(hsvMat, saturatedMat, Imgproc.COLOR_HSV2BGR)

        // Release temporary Mats to free up memory
        hsvMat.release()
        hChannel.release()
        sChannel.release()
        vChannel.release()
        channels.forEach { it.release() }

        return saturatedMat

    }


    fun tintColor(mat: Mat, tintIntensity: Double = 0.2): Mat {
        if (mat.channels() < 3) {

            return mat
        }

        val channels = ArrayList<Mat>()
        Core.split(mat, channels)

        val bChannel = channels[0]
        val gChannel = channels[1]
        val rChannel = channels[2]


        val redBoostAmount = 255.0 * tintIntensity

        val redBoostMat = Mat(rChannel.size(), rChannel.type(), Scalar(redBoostAmount))
        Core.add(rChannel, redBoostMat, rChannel)
        redBoostMat.release()

        val blueBoostAmount = 255.0 * tintIntensity

        val blueBoostMat = Mat(bChannel.size(), bChannel.type(), Scalar(blueBoostAmount * 0.6))
        Core.add(bChannel, blueBoostMat, bChannel)
        blueBoostMat.release()



        val tintedMat = Mat()
        Core.merge(channels, tintedMat)

        bChannel.release()
        gChannel.release()
        rChannel.release()


        return tintedMat
    }





    @SuppressLint("SimpleDateFormat")
    private fun photoFileName(): String {
        return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
            .format(System.currentTimeMillis()) + ".jpg"
    }

    private fun saveImage(
        context: Context,
        mat: Mat,
        isImageSaved: (saveSuccess: Boolean, filePath: String) -> Unit
        )
    {

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

        }

        val contentResolver = context.contentResolver


        var contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val imageUri = contentResolver.insert(
            contentUri,
            contentValues
        )

        if (imageUri == null)
        {
            throw Exception("Could not create URI")
        }

        contentResolver.openOutputStream(imageUri)?.use { stream ->

            if(stream == null)
            {
                isImageSaved(false, imageUri.path.toString())
                return
            }

            val bitmap = createBitmap(mat.cols(), mat.rows())
            Utils.matToBitmap(mat, bitmap)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            stream.flush()

        }


    }











}