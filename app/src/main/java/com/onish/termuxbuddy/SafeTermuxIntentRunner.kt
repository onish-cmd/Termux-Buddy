package com.onish.termuxbuddy.bridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast

object SafeTermuxIntentRunner {
    private const val TERMUX_API_PACKAGE = "com.termux.api"
    private const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    fun runCommand(
        context: Context,
        command: String,
        args: Array<String>? = null,
        workdir: String? = null
    ) {
        try {
            context.packageManager.getPackageInfo(TERMUX_API_PACKAGE, 0)

            val granted = if (Build.VERSION.SDK_INT >= 23) {
                context.checkSelfPermission(RUN_COMMAND_PERMISSION) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!granted) {
                Toast.makeText(context, "Grant Run Command permission", Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setPackage(TERMUX_API_PACKAGE)
                putExtra("com.termux.RUN_COMMAND_PATH", command)
                if (args != null) putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args)
                if (workdir != null) putExtra("com.termux.RUN_COMMAND_WORKDIR", workdir)

                // Ask Termux to send results back
                putExtra("com.termux.RUN_COMMAND_RESULT_STDOUT", true)
                putExtra("com.termux.RUN_COMMAND_RESULT_STDERR", true)
                putExtra("com.termux.RUN_COMMAND_RESULT_CODE", true)
                putExtra("com.termux.RUN_COMMAND_RESULT_BROADCAST_NAME",
                    "com.onish.termuxbuddy.RESULT")
            }

            context.startService(intent)
            Toast.makeText(context, "Command sent to Termux", Toast.LENGTH_SHORT).show()

        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Install Termux:API first", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }
}