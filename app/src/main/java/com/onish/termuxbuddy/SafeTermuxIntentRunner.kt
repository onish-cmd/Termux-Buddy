package com.onish.termuxbuddy.bridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast

object SafeTermuxIntentRunner {

    private const val TERMUX_API_PACKAGE = "com.termux.api"
    private const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    /**
     * Fire-and-forget Termux command using RUN_COMMAND intent.
     *
     * @param command Absolute path or command in $PATH (e.g., "bash")
     * @param args Optional arguments (e.g., arrayOf("-lc", "echo Hi"))
     * @param workdir Optional working directory path inside Termux home
     */
    fun runCommand(
        context: Context,
        command: String,
        args: Array<String>? = null,
        workdir: String? = null,
        background: Boolean = true
    ) {
        try {
            // Ensure Termux:API is installed
            context.packageManager.getPackageInfo(TERMUX_API_PACKAGE, 0)

            // Check runtime permission
            val granted = if (Build.VERSION.SDK_INT >= 23) {
                context.checkSelfPermission(RUN_COMMAND_PERMISSION) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!granted) {
                Toast.makeText(context, "Grant Run Command permission to Termux Buddy", Toast.LENGTH_LONG).show()
                return
            }

            // Build explicit intent
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setPackage(TERMUX_API_PACKAGE)
                putExtra("com.termux.RUN_COMMAND_PATH", command)
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", background)
                if (args != null) putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args)
                if (workdir != null) putExtra("com.termux.RUN_COMMAND_WORKDIR", workdir)
                // Optional extras you can enable when wiring result handling:
                // putExtra("com.termux.RUN_COMMAND_STDOUT", true)
                // putExtra("com.termux.RUN_COMMAND_STDERR", true)
            }

            // Start service (Termux:API runs it as a foreground/background service)
            context.startService(intent)

            Toast.makeText(context, "Command sent to Termux", Toast.LENGTH_SHORT).show()

        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Install Termux:API to run commands", Toast.LENGTH_LONG).show()
        } catch (se: SecurityException) {
            Toast.makeText(context, "Permission denied: $se", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }
}
