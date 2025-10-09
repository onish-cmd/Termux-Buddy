package com.onish.termuxbuddy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.onish.termuxbuddy.bridge.TermuxOutput
import com.onish.termuxbuddy.bridge.isTermuxAvailable
import com.onish.termuxbuddy.bridge.runInTermux
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val context = LocalContext.current
    var command by remember { mutableStateOf("") }
    val lines = TermuxOutput.lines
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Startup check: invisible unless it fails.
    var termuxReady by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        termuxReady = isTermuxAvailable(context)
        if (!termuxReady) {
            lines.add("Error: Termux not found or external access disabled.")
            lines.add("Enable allow-external-apps in ~/.termux/termux.properties and restart Termux.")
        } else {
            // Silent sanity probe: runs 'true' with no output, relies on receiver for errors.
            // We don't add any lines here; remains invisible on success.
            runInTermux(context, "true")
        }
    }

    // Auto-scroll on new lines
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.scrollToItem(lines.lastIndex)
        }
    }

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
            singleLine = true,
            enabled = termuxReady
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val cmd = command.trim()
                if (cmd.isNotBlank()) {
                    lines.add("> $cmd")
                    // Send to Termux; output will arrive via receiver.
                    runInTermux(context, cmd)
                    command = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = termuxReady
        ) {
            Text(if (termuxReady) "Run" else "Termux required")
        }

        Spacer(Modifier.height(12.dp))

        // Quick actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {
                    val cmd = "pwd"
                    lines.add("> $cmd")
                    runInTermux(context, cmd)
                },
                label = { Text("pwd") },
                enabled = termuxReady
            )
            AssistChip(
                onClick = {
                    val cmd = "ls -la"
                    lines.add("> $cmd")
                    runInTermux(context, cmd)
                },
                label = { Text("ls -la") },
                enabled = termuxReady
            )
            AssistChip(
                onClick = {
                    val cmd = "termux-battery-status"
                    lines.add("> $cmd")
                    runInTermux(context, cmd)
                },
                label = { Text("Battery") },
                enabled = termuxReady
            )
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