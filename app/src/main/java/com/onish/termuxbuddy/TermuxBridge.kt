package com.onish.termuxbuddy.bridge

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Checks if Termux is installed and its RunCommandService is resolvable.
 * This is a quiet preflight check used at startup.
 */
fun isTermuxAvailable(context: Context): Boolean {
    // 1) Termux package present
    val termuxInstalled = try {
        context.packageManager.getPackageInfo("com.termux", 0)
        true
    } catch (_: Exception) {
        false
    }
    if (!termuxInstalled) return false

    // 2) Service resolvable
    val intent = Intent("com.termux.RUN_COMMAND")
        .setClassName("com.termux", "com.termux.app.RunCommandService")
    val resolved = context.packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolved.isNotEmpty()
}

/**
 * Sends a command to Termux via the official RUN_COMMAND intent.
 * Output is returned via TermuxResultReceiver using a PendingIntent.
 */
fun runInTermux(context: Context, command: String) {
    val intent = Intent("com.termux.RUN_COMMAND")
    intent.setClassName("com.termux", "com.termux.app.RunCommandService")

    // Run using bash with -c for full shell parsing
    intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
    intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
    intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")

    // Foreground execution so we get a response immediately
    intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)

    // Optional: set stdin text if needed (leave empty for normal commands)
    intent.putExtra("com.termux.RUN_COMMAND_STDIN", "")

    // Result callback
    val callbackIntent = Intent(context, TermuxResultReceiver::class.java)
    val pi = PendingIntent.getBroadcast(
        context,
        0,
        callbackIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    intent.putExtra("com.termux.RUN_COMMAND_RESULT_PENDING_INTENT", pi)

    context.startService(intent)
}

/**
 * Optional utility: open Termux settings file for enabling external apps.
 * Not auto-called; can be used by a help button.
 */
fun openTermuxSettingsHint(context: Context) {
    // Points users to the Termux settings docs through a browser if needed.
    val url = "https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}