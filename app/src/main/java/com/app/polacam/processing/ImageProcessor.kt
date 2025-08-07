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
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import androidx.core.graphics.createBitmap
import org.opencv.core.Core
import org.opencv.core.Core.ROTATE_90_CLOCKWISE
import org.opencv.core.Core.ROTATE_90_COUNTERCLOCKWISE

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
        imageProcessSuccess: (processingSuccess: Boolean,filePath: String) -> Unit)
    {
        loadImage(image, mat)


        Core.rotate(mat , mat, ROTATE_90_CLOCKWISE)
        Imgproc.blur(mat,mat, Size(5.0, 5.0))


        saveImage(
            context,
            mat,
            {

                    saveSuccess, filePath -> imageProcessSuccess(saveSuccess, filePath)
            }
        )

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