package com.app.polacam

import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.polacam.ui.theme.PolaCamTheme

class MainActivity : ComponentActivity() {



    var count :Int = 5;

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                    .background(Color.Red)
                            )
                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.6f)
                                    .fillMaxWidth()
                                    .background(Color.Blue)
                            )
                            Box(
                                modifier = itemModifier
                                    .fillMaxHeight(0.2f)
                                    .fillMaxWidth()
                                    .background(Color.Magenta)
                            )

                        }


                    }


                }
            }
        }
    }
}


fun increment(activity: MainActivity) :Int?{


    activity.count++;
    return null;
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


        onClick = { increment(MainActivity()) },
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
    Canvas(modifier = Modifier.width(100.dp) , onDraw = {
        drawCircle(color = Color.Red)
    })

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