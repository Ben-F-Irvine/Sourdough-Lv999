package com.sourdough.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sourdough.companion.ui.SourdoughNavHost
import com.sourdough.companion.ui.theme.SourdoughTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SourdoughTheme {
                SourdoughNavHost()
            }
        }
    }
}
