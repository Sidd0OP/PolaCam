import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts




class Permission(
    activity: ComponentActivity
)
{

    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


    val activityResultLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(activity.baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            }
        }
}




