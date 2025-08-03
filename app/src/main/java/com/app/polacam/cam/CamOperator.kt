import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.media3.common.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService


class CamOperator(
    activity: ComponentActivity
) {

    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


    val activityResultLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    activity.baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    fun takePhoto(
        cameraController: CameraController,
        executor: ExecutorService,
        context: Context,
        onImageCaptured: (Uri) -> Unit
    ) {


        val outPutFile = File(context.filesDir, "image.jpg").apply { parentFile?.mkdirs() }

        fun photoFileName(): String {
            return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
                .format(System.currentTimeMillis()) + ".jpg"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, photoFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures")

        }


        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        cameraController.takePicture(
            executor,
            object :
                ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureStarted() {
                    println("Capture Started")

                }


                override fun onCaptureSuccess(image: ImageProxy) {

                    println("Image captured ${image.format}")

                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }

            }


        )

        cameraController.takePicture(


            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    Uri.fromFile(outPutFile).let(onImageCaptured)

                }

                override fun onError(exception: ImageCaptureException) {

                    println(exception.message)

                }
            }


        )


    }
}




