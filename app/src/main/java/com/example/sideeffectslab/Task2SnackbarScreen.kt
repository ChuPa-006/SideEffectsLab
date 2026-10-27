package com.example.sideeffectslab

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Task2SnackbarScreen(onBack: () -> Unit, onNavigateAway: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    TaskScreenScaffold(title = "Task 2: Snackbar", onBack = onBack) { padding ->
        Scaffold(
            modifier = Modifier.padding(padding),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Press the snackbar button twice before the first snackbar disappears to observe the queue.")
                Button(onClick = {
                    // onClick is not composable. Start a coroutine from the remembered scope.
                    scope.launch {
                        Log.d("Task2Snackbar", "showSnackbar coroutine started")
                        snackbarHostState.showSnackbar("Hello from rememberCoroutineScope!")
                        Log.d("Task2Snackbar", "showSnackbar coroutine finished")
                    }
                }) {
                    Text("Show snackbar")
                }
                Button(onClick = {
                    Log.d("Task2Snackbar", "Navigating away; this screen's scope will be cancelled")
                    onNavigateAway()
                }) {
                    Text("Navigate to Lifecycle screen while snackbar is visible")
                }
                Text("Why not LaunchedEffect inside onClick? LaunchedEffect is a composable API and cannot be called from a normal callback.")
                Text("When this screen leaves the Composition, rememberCoroutineScope is cancelled and any active snackbar coroutine is cancelled with it.")
            }
        }
    }
}
