package com.razorquake.morselens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.razorquake.morselens.ui.theme.MorseLensTheme
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!OpenCVLoader.initLocal()) {
            Log.e("OpenCV", "Initialization failed")
        } else {
            Log.d("OpenCV", "Initialization succeeded")
        }
        enableEdgeToEdge()
        setContent {
            MorseLensTheme {
                    Navigator()
            }
        }
    }
}
