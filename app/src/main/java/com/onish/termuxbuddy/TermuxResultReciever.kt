package com.onish.termuxbuddy.bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf

/**
 * Shared output buffer observed by Compose.
 */
object TermuxOutput {
    val lines = mutableStateListOf<String>()
}

/**
 * Receives stdout/stderr/exit code after Termux executes a command.
 */
class TermuxResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stdout = intent.getStringExtra("com.termux.RUN_COMMAND_STDOUT") ?: ""
        val stderr = intent.getStringExtra("com.termux.RUN_COMMAND_STDERR") ?: ""
        val exitCode = intent.getIntExtra("com.termux.RUN_COMMAND_EXIT_CODE", -1)

        if (stdout.isNotBlank()) {
            // Split into lines for nicer display
            stdout.split("\n").forEach { line ->
                if (line.isNotEmpty()) TermuxOutput.lines.add(line)
            }
        }
        if (stderr.isNotBlank()) {
            stderr.split("\n").forEach { line ->
                if (line.isNotEmpty()) TermuxOutput.lines.add("ERR: $line")
            }
        }
        TermuxOutput.lines.add("[exit $exitCode]")
    }
}