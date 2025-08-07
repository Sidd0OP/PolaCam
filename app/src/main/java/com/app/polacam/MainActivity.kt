package com.app.polacam

import CamOperator
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraXThreads.TAG
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.Log
import com.app.polacam.processing.ImageProcessor
import com.app.polacam.ui.theme.PolaCamTheme
import com.app.polacam.ui.theme.background
import com.app.polacam.ui.theme.shadow
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.graphics.toColorInt
import com.app.polacam.gyro.Rotation


class MainActivity : ComponentActivity() {



    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var cameraController: LifecycleCameraController;

    private val camOperator = CamOperator(this)
    private lateinit var rotationProvider: Rotation






    private fun requestPermissions() {

        camOperator
            .activityResultLauncher
            .launch(REQUIRED_PERMISSIONS)

    }

    fun Modifier.drawColoredShadow(
        color: Color,
        alpha: Float = 0.2f,
        borderRadius: Dp = 0.dp,
        shadowRadius: Dp = 20.dp,
        offsetY: Dp = 0.dp,
        offsetX: Dp = 0.dp
    ) = this.drawBehind {
        val transparentColor = color.copy(alpha = alpha).value.toLong().toColorInt()
        val shadowColor = color.copy(alpha = alpha).value.toLong().toColorInt()
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(
                shadowRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor
            )
            it.drawRoundRect(
                0f,
                0f,
                this.size.width,
                this.size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()

        if (OpenCVLoader.initLocal())
        {

        }else{
            return
        }

        var mat = Mat();

        rotationProvider = Rotation(this)





        setContent {
            PolaCamTheme {


                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val controller = remember {
                    LifecycleCameraController(context).apply {
                        bindToLifecycle(lifecycleOwner)
                    }
                }

                this.cameraController = controller;

                var xRot by remember { mutableStateOf(0.0f) }
                var yRot by remember { mutableStateOf(0.0f) }

                LaunchedEffect(Unit) {
                    rotationProvider.getRotation { x, y, z ->
                        println("X is $x and Y is $y z is $z")
                        val scaleFactor = 20f
                        xRot = y * scaleFactor * -1
                        yRot = x * scaleFactor * -1
                    }
                }

                // DisposableEffect to unregister the listener when the composable leaves the screen
                DisposableEffect(Unit) {
                    onDispose {
                        rotationProvider.onPause()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),


                    ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 0.dp, vertical = 0.dp)
                            .fillMaxSize()
                            .background(background),

                        contentAlignment = Alignment.Center


                    ) {

                        FlowColumn(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            val itemModifier = Modifier.clip(RoundedCornerShape(8.dp))

                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.2f)
                                    .fillMaxWidth()
                                    .background(background),

                                contentAlignment = Alignment.Center
                            ) {}


                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.5f)
                                    .fillMaxWidth(),

                                contentAlignment = Alignment.Center
                            ) {

                                ImageContainer()

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.5f),

                                    contentAlignment = Alignment.Center
                                ) {
                                    cameraPreview(cameraController)
                                }


                            }


                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.3f)
                                    .fillMaxWidth(),

                                contentAlignment = Alignment.Center
                            ) {



                                Button(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(0.dp,0.dp)
                                        .drawColoredShadow(shadow,0.2f,offsetX = xRot.dp,offsetY = yRot.dp,borderRadius = 200.dp,shadowRadius = 10.dp)
                                        .clip(CircleShape),

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    ),


                                    onClick = {

                                        camOperator.takePhoto(
                                            baseContext,
                                            cameraController,
                                            mat,
                                            executor,
                                            {
                                                success -> println("Image status $success")

                                            }


                                        )

                                    }

                                ) {


                                    Text(
                                        text = "X roation is ${xRot}",
                                        color = Color.White

                                    )

                                }


                            }

                        }


                    }


                }
            }
        }
    }




    override fun onPause() {
        super.onPause()
        rotationProvider.onPause()

    }


}


@Composable
fun cameraPreview(cameraController: LifecycleCameraController) {


    AndroidView(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .fillMaxWidth(),

        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                controller = cameraController
            }
        },

        onRelease = {
            cameraController.unbind()
        }

    )

}

@Preview
@Composable
fun ImageContainer(

    shadowColor: Color = Color.Black,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize(),

        onDraw = {
            drawCircle(
                color = Color.Yellow,
                radius = size.minDimension / 2.5f
            )
        }

    )

}


