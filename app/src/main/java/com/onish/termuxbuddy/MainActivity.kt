package com.onish.termuxbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onish.termuxbuddy.ui.ConsoleScreen
import com.onish.termuxbuddy.ui.theme.TermuxBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TermuxBuddyTheme {
                ConsoleScreen()
            }
        }
    }
}