import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import com.app.polacam.processing.ImageProcessor
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService


class CamOperator(
    activity: ComponentActivity
) {

    private val imagePocessor = ImageProcessor()

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
        context: Context,
        cameraController: CameraController,
        mat: Mat,
        executor: ExecutorService,
        captureSuccess: (Boolean) -> Unit
    ) {


        cameraController.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback()
            {
                override fun onCaptureStarted() {

                }

                override fun onCaptureSuccess(image: ImageProxy)
                {
                    imagePocessor.processImage(
                        context,
                        image,
                        mat,
                        { saved, filePath -> captureSuccess(saved) }
                    )

                    image.close()

                }


            }
        )

    }
}




