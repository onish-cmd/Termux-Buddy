package com.onish.termuxbuddy.bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TermuxResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stdout = intent.getStringExtra("com.termux.RUN_COMMAND_RESULT_STDOUT") ?: ""
        val stderr = intent.getStringExtra("com.termux.RUN_COMMAND_RESULT_STDERR") ?: ""
        val code = intent.getIntExtra("com.termux.RUN_COMMAND_RESULT_CODE", -1)

        Log.d("TermuxResultReceiver", "stdout=$stdout, stderr=$stderr, code=$code")

        // Forward result into app via LocalBroadcast
        val local = Intent("TermuxBuddyResult").apply {
            putExtra("stdout", stdout)
            putExtra("stderr", stderr)
            putExtra("code", code)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(local)
    }
}