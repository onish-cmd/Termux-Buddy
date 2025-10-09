package com.onish.termuxbuddy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onish.termuxbuddy.bridge.SafeTermuxIntentRunner

class MainActivity : ComponentActivity() {

    // Runtime permission requester for Termux RUN_COMMAND
    private val requestRunCommandPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op; UI will re-check */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val hasPermission = remember { mutableStateOf(checkRunCommandPermission()) }

                    LaunchedEffect(Unit) {
                        if (!hasPermission.value && Build.VERSION.SDK_INT >= 23) {
                            requestRunCommandPermission.launch("com.termux.permission.RUN_COMMAND")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Termux Buddy", style = MaterialTheme.typography.headlineMedium)

                        Button(onClick = {
                            SafeTermuxIntentRunner.runCommand(
                                context = this@MainActivity,
                                command = "bash",
                                args = arrayOf("-lc", "echo Hello from Termux; uname -a"),
                                workdir = "/data/data/com.termux/files/home",
                                background = true
                            )
                        }) {
                            Text("Run test command")
                        }

                        Button(onClick = {
                            // Re-check after user potentially granted permission
                            hasPermission.value = checkRunCommandPermission()
                        }) {
                            Text("Check permission")
                        }
                    }
                }
            }
        }
    }

    private fun checkRunCommandPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            checkSelfPermission("com.termux.permission.RUN_COMMAND") ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
}
