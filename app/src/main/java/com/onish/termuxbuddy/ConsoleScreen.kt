package com.onish.termuxbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

// Runs a command inside Termux environment and streams lines via callback.
suspend fun runCommandStreaming(
    command: String,
    onLine: suspend (String) -> Unit
): Int {
    return withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "/data/data/com.termux/files/usr/bin/bash", "-c", command
        )
            .redirectErrorStream(true)
            .start()

        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                onLine(line!!)
            }
        }
        process.waitFor()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var command by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll: whenever a new line is added, jump to last item.
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick Termux:API actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { triggerApi(scope, lines, listState, "termux-battery-status") },
                label = { Text("Battery") }
            )
            AssistChip(
                onClick = { triggerApi(scope, lines, listState, "termux-clipboard-get") },
                label = { Text("Clipboard") }
            )
            AssistChip(
                onClick = { triggerApi(scope, lines, listState, "termux-vibrate -d 200") },
                label = { Text("Vibrate") }
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            label = { Text("Enter Termux command") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (command.isNotBlank()) {
                    // Print prompt line
                    lines.add("> $command")
                    scope.launch {
                        val exit = runCommandStreaming(command) { line ->
                            lines.add(line)
                        }
                        lines.add("[exit $exit]")
                    }
                    command = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Run")
        }

        Spacer(Modifier.height(12.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Output", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(lines) { line ->
                        Text(line)
                    }
                }
            }
        }
    }
}

// Helper to trigger a Termux:API command and stream its output to the console.
private fun triggerApi(
    scope: androidx.compose.runtime.CoroutineScope,
    lines: MutableList<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    cmd: String
) {
    lines.add("> $cmd")
    scope.launch {
        val exit = runCommandStreaming(cmd) { line ->
            lines.add(line)
        }
        lines.add("[exit $exit]")
        // Nudge scroll just in case
        listState.scrollToItem(lines.lastIndex)
    }
}