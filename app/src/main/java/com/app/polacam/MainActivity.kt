package com.app.polacam

import CamOperator
import android.Manifest
import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraXThreads.TAG
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.app.polacam.ui.theme.innerShadow
import kotlin.math.asin


class MainActivity : ComponentActivity() {


    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    val executor: ExecutorService = Executors.newSingleThreadExecutor()


    var mediaPlayer: MediaPlayer? = null;
    
    private lateinit var cameraController: LifecycleCameraController;

    private val camOperator = CamOperator(this)
    private lateinit var rotationProvider: Rotation


    private fun requestPermissions() {

        camOperator
            .activityResultLauncher
            .launch(REQUIRED_PERMISSIONS)

    }

    fun Modifier.innerShadow(
        color: Color = Color.Black,
        cornersRadius: Dp = 0.dp,
        spread: Dp = 0.dp,
        blur: Dp = 0.dp,
        offsetY: Dp = 0.dp,
        offsetX: Dp = 0.dp
    ) = this.drawWithContent {

        drawContent()

        val rect = Rect(Offset.Zero, size)
        val paint = Paint()

        drawIntoCanvas {

            paint.color = color
            paint.isAntiAlias = true
            it.saveLayer(rect, paint)
            it.drawRoundRect(
                left = rect.left,
                top = rect.top,
                right = rect.right,
                bottom = rect.bottom,
                cornersRadius.toPx(),
                cornersRadius.toPx(),
                paint
            )
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (blur.toPx() > 0) {
                frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            val left = if (offsetX > 0.dp) {
                rect.left + offsetX.toPx()
            } else {
                rect.left
            }
            val top = if (offsetY > 0.dp) {
                rect.top + offsetY.toPx()
            } else {
                rect.top
            }
            val right = if (offsetX < 0.dp) {
                rect.right + offsetX.toPx()
            } else {
                rect.right
            }
            val bottom = if (offsetY < 0.dp) {
                rect.bottom + offsetY.toPx()
            } else {
                rect.bottom
            }
            paint.color = Color.Black
            it.drawRoundRect(
                left = left + spread.toPx() / 2,
                top = top + spread.toPx() / 2,
                right = right - spread.toPx() / 2,
                bottom = bottom - spread.toPx() / 2,
                cornersRadius.toPx(),
                cornersRadius.toPx(),
                paint
            )
            frameworkPaint.xfermode = null
            frameworkPaint.maskFilter = null
        }
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

        mediaPlayer = MediaPlayer.create(this, R.raw.click_efect)

        if (OpenCVLoader.initLocal()) {

        } else {
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
                var zRot by remember { mutableStateOf(0.0f) }

                val dragFactor = 5.0f

                LaunchedEffect(Unit) {
                    rotationProvider.getRotation { x, y, z ->

                        xRot = (x) * dragFactor
                        yRot = -1 * (y) * dragFactor
                        zRot = (z) * dragFactor

                    }
                }


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
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(0.5f)
                                        .drawColoredShadow(
                                            shadow,
                                            0.6f,
                                            offsetX = xRot.dp,
                                            offsetY = yRot.dp,
                                            borderRadius = 10.dp,
                                            shadowRadius = 25.dp
                                        )
                                        .innerShadow(
                                            innerShadow,
                                            cornersRadius = 10.dp,
                                            offsetX = xRot.dp,
                                            offsetY = yRot.dp,
                                            spread = 0.2.dp,
                                            blur = 12.dp
                                        )
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center

                                )
                                {
                                    Text(
                                        text = "PolaCam",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Cursive,
                                    )
                                }


                            }


                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.5f)
                                    .fillMaxWidth(),

                                contentAlignment = Alignment.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                )
                                {
                                    Image(
                                        painter = painterResource(R.drawable.camera_lines),
                                        contentDescription = "SVG Image",
                                        modifier = Modifier
                                            .rotate(90f)
                                            .width(100.dp)
                                            .fillMaxHeight(),
                                        contentScale = ContentScale.FillBounds
                                    )
                                }

                                ImageContainer(xRot,yRot)

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

                                val hapticFeedback = LocalHapticFeedback.current

                                Button(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(0.dp, 0.dp)
                                        .drawColoredShadow(
                                            shadow,
                                            0.4f,
                                            offsetX = xRot.dp,
                                            offsetY = yRot.dp,
                                            borderRadius = 200.dp,
                                            shadowRadius = 15.dp
                                        )
                                        .innerShadow(
                                            innerShadow,
                                            cornersRadius = 200.dp,
                                            offsetX = xRot.dp,
                                            offsetY = yRot.dp,
                                            spread = 0.5.dp,
                                            blur = 10.dp
                                        )
                                        .clip(CircleShape),

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    ),


                                    onClick = {

                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                        camOperator.takePhoto(
                                            baseContext,
                                            cameraController,
                                            mat,
                                            executor,
                                            { success ->
                                                println("Image status $success")

                                            }


                                        )

                                        mediaPlayer?.start()

                                    }


                                ) {


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


    @Composable
    fun cameraPreview(cameraController: LifecycleCameraController) {


        AndroidView(
            modifier = Modifier
                .aspectRatio(1f)
                .innerShadow(
                    shadow,
                    cornersRadius = 200.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 4.dp,
                    blur = 20.dp
                )
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

        xRotation: Float = 0.0f,
        yRotation: Float = 0.0f,

        ) {

        //outer box static
        //image with dials to be fixed

        Box(

            modifier = Modifier
                .drawColoredShadow(
                    shadow,
                    0.4f,
                    offsetX = xRotation.dp,
                    offsetY = yRotation.dp,
                    borderRadius = 150.dp,
                    shadowRadius =  20.dp
                )
                .innerShadow(
                    innerShadow,
                    cornersRadius = 200.dp,
                    offsetX = xRotation.dp,
                    offsetY = yRotation.dp,
                    spread = 1.dp,
                    blur = 15.dp
                )
                .clip(CircleShape)
                .size(300.dp)
                .background(Color.Red),

            contentAlignment = Alignment.Center

        ){

            Box(

                modifier = Modifier
                    .clip(CircleShape)
                    .size(250.dp)
                    .background(Color.Black),

                contentAlignment = Alignment.Center


            ){


            }

        }

    }














}







