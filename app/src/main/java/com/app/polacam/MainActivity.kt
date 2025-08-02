package com.app.polacam

import Permission
import android.Manifest
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXThreads.TAG
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.polacam.ui.theme.PolaCamTheme


class MainActivity : ComponentActivity() {



    var count :Int = 5;
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)



    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissions()

        setContent {
            PolaCamTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),


                ) { innerPadding ->

                    Box(
                        modifier =  Modifier
                                   .padding(horizontal = 0.dp, vertical = 0.dp)
                                   .fillMaxSize()
                                   .background(color = Color.White),

                        contentAlignment = Alignment.Center



                    ){
//                        ImageContainer()
//                        Back()

                        FlowColumn (
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.Center,
                        ){
                            val itemModifier = Modifier.clip(RoundedCornerShape(8.dp))

                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.2f)
                                    .fillMaxWidth()
                                    .background(Color.Red),

                                contentAlignment = Alignment.Center
                            ){}


                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.6f)
                                    .fillMaxWidth(),

                                contentAlignment = Alignment.Center



                            ){

                                ImageContainer()
                                cameraPreview()
                            }


                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.2f)
                                    .fillMaxWidth()
                                    .background(Color.Magenta)
                            ){

                            }

                        }


                    }


                }
            }
        }
    }

    @Composable
    fun cameraPreview()
    {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val cameraController = remember {
            LifecycleCameraController(context).apply {
                bindToLifecycle(lifecycleOwner)
            }
        }

        AndroidView(
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize(),

            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    controller = cameraController
                }
            },

            onRelease = {
                cameraController.unbind()
            }

        )

    }






    private fun requestPermissions() {

        Permission(this).
        activityResultLauncher.
        launch(REQUIRED_PERMISSIONS)

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}



@Composable
fun Back()
{
    Button(


        onClick = {  },
        modifier = Modifier
            .width(10.dp)
            .padding(0.dp , 2.dp)
            .background(color = Color.Red),


    ) { }
}

@Preview
@Composable
fun ImageContainer()
{
    Canvas(
        modifier = Modifier
            .background(Color.Green)
            .fillMaxSize() ,
        onDraw = {
                    drawCircle(
                        color = Color.Yellow,
                        radius = size.minDimension / 2.5f
                        )
                }

    )

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PolaCamTheme {
        Greeting("Android")
    }
}


@Preview(showBackground = true)
@Composable
fun ButtonPreview(){

    PolaCamTheme {
        Back()
    }
}