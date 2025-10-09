package com.onish.termuxbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.onish.termuxbuddy.exec.runCommandStreaming

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var command by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()   // ✅ correct way

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
                val cmd = command.trim()
                if (cmd.isNotBlank()) {
                    lines.add("> $cmd")
                    scope.launch {   // ✅ wrap suspend calls
                        val exit = runCommandStreaming(cmd) { line ->
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