package com.onish.termuxbuddy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.onish.termuxbuddy.bridge.SafeTermuxIntentRunner

class MainActivity : ComponentActivity() {

    private lateinit var resultReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stdoutState = mutableStateOf("")
        val stderrState = mutableStateOf("")
        val codeState = mutableStateOf(-1)

        // Listen for results from TermuxResultReceiver
        resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                stdoutState.value = intent?.getStringExtra("stdout") ?: ""
                stderrState.value = intent?.getStringExtra("stderr") ?: ""
                codeState.value = intent?.getIntExtra("code", -1) ?: -1
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(resultReceiver, IntentFilter("TermuxBuddyResult"))

        setContent {
            MaterialTheme {
                Surface {
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
                                workdir = "/data/data/com.termux/files/home"
                            )
                        }) {
                            Text("Run test command")
                        }

                        Text("Exit code: ${codeState.value}")
                        Text("Stdout:\n${stdoutState.value}")
                        Text("Stderr:\n${stderrState.value}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver)
    }
}