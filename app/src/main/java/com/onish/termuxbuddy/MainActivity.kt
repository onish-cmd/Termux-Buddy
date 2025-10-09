package com.onish.termuxbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.onish.termuxbuddy.ui.ConsoleScreen
import com.onish.termuxbuddy.ui.theme.TermuxBuddyTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TermuxBuddyTheme {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeContent.union(WindowInsets.ime),
                    topBar = { TopAppBar(title = { Text("TermuxBuddy Console") }) }
                ) { padding ->
                    ConsoleScreen(contentPadding = padding)
                }
            }
        }
    }
}