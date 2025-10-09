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

// Run a command and stream output line by line
suspend fun runCommandStreaming(command: String, onLine: (String) -> Unit): Int {
    return withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "/data/data/com.termux/files/usr/bin/bash", "-c", command
        ).redirectErrorStream(true).start()

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
    val scope = rememberCoroutineScope()   // ✅ use this instead of raw CoroutineScope

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            label = { Text("Enter command") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (command.isNotBlank()) {
                    lines.add("> $command")
                    scope.launch {   // ✅ launch coroutine
                        val exit = runCommandStreaming(command) { line ->
                            lines.add(line)
                        }
                        lines.add("[exit $exit]")
                        listState.scrollToItem(lines.lastIndex) // ✅ inside coroutine
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
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(lines) { line ->
                    Text(line)
                }
            }
        }
    }
}