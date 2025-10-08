package com.onish.termuxbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) {
                dynamicDarkColorScheme(this)
            } else {
                dynamicLightColorScheme(this)
            }

            MaterialTheme(colorScheme = colorScheme) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var text by remember { mutableStateOf("") }
    var sliderValue by remember { mutableStateOf(0.5f) }
    var checked by remember { mutableStateOf(false) }
    var switchOn by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Settings", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.Settings, Icons.Default.Person)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material You Showcase") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Typography Example", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = {}) { Text("Filled Button") }
            OutlinedButton(onClick = {}) { Text("Outlined Button") }
            ElevatedButton(onClick = {}) { Text("Elevated Button") }
            TextButton(onClick = {}) { Text("Text Button") }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter text") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                Text("Checkbox")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = switchOn, onCheckedChange = { switchOn = it })
                Text("Switch")
            }

            Slider(value = sliderValue, onValueChange = { sliderValue = it })

            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("This is a Material 3 Card")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {}) { Text("Inside Card") }
                }
            }

            AssistChip(
                onClick = {},
                label = { Text("Assist Chip") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MaterialTheme {
        MainScreen()
    }
}
