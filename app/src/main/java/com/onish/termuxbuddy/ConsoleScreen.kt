package com.onish.termuxbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Helper to run shell commands inside Termux environment
fun runCommand(command: String): String {
    return try {
        val process = ProcessBuilder(
            "/data/data/com.termux/files/usr/bin/bash", "-c", command
        )
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor()
        output.ifBlank { "(no output)" }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen() {
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val history = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TermuxBuddy Console") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text("Enter command") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (command.isNotBlank()) {
                        val result = runCommand(command)
                        history.add("> $command\n$result\n")
                        output = history.joinToString("\n")
                        command = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run")
            }

            Spacer(Modifier.height(16.dp))

            Text("Console Output:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(
                text = output,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            )
        }
    }
}