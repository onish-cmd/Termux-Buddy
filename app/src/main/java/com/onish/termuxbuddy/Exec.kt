package com.onish.termuxbuddy.exec

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TERMUX_BASH = "/data/data/com.termux/files/usr/bin/bash"

suspend fun runCommandStreaming(command: String, onLine: (String) -> Unit): Int {
    return withContext(Dispatchers.IO) {
        try {
            val pb = ProcessBuilder(TERMUX_BASH, "-c", command)
                .redirectErrorStream(true)

            // Ensure PATH includes Termux binaries
            val env = pb.environment()
            val termuxBase = "/data/data/com.termux/files"
            env["PATH"] = listOf(
                "$termuxBase/usr/bin",
                "$termuxBase/usr/sbin",
                "$termuxBase/usr/libexec"
            ).joinToString(":")

            val process = pb.start()

            BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
                lines.forEach { line -> onLine(line) }
            }
            process.waitFor()
        } catch (e: Exception) {
            onLine("Error: Termux not found or command failed → ${e.message}")
            -1
        }
    }
}