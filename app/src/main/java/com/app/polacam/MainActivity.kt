package com.app.polacam

import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.app.polacam.ui.theme.PolaCamTheme

class MainActivity : ComponentActivity() {



    var count :Int = 5;

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolaCamTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(

                            title = {
                                Text("Small Top App Bar")
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            )
                        )
                    },
//                    bottomBar = {
//                        BottomAppBar(
//                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
//                            contentColor = MaterialTheme.colorScheme.tertiary,
//                        )
//                    }

                ) { innerPadding ->

                    Box(
                        modifier =  Modifier
                                   .padding(innerPadding)
                                   .fillMaxSize()
                                   .background(color = MaterialTheme.colorScheme.background),

                        contentAlignment = Alignment.Center


                    ){
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        );
                        Back()
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
        modifier =  Modifier
                    .fillMaxSize()
                    .background(color = Color.Red),


    ) { }
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