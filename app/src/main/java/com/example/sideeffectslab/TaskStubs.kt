package com.example.sideeffectslab

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

// Placeholder screens keep the starter project structurally complete.
// Each stub is replaced by the real implementation in its own Git commit.
@Composable fun Task1SplashScreen(onBack: () -> Unit) {
    TaskScreenScaffold("Task 1: Splash screen", onBack) { Text("Task 1 implementation is added in its own commit.", Modifier.padding(it)) }
}
@Composable fun Task2SnackbarScreen(onBack: () -> Unit, onNavigateAway: () -> Unit) {
    TaskScreenScaffold("Task 2: Snackbar", onBack) { Text("Task 2 implementation is added in its own commit.", Modifier.padding(it)) }
}
@Composable fun Task3LifecycleScreen(onBack: () -> Unit) {
    TaskScreenScaffold("Task 3: Lifecycle logger", onBack) { Text("Task 3 implementation is added in its own commit.", Modifier.padding(it)) }
}
@Composable fun Task4SideEffectScreen(onBack: () -> Unit) {
    TaskScreenScaffold("Task 4: SideEffect", onBack) { Text("Task 4 implementation is added in its own commit.", Modifier.padding(it)) }
}
@Composable fun Task5ScrollScreen(onBack: () -> Unit) {
    TaskScreenScaffold("Task 5: Scroll", onBack) { Text("Task 5 implementation is added in its own commit.", Modifier.padding(it)) }
}
@Composable fun Task6ProduceStateScreen(onBack: () -> Unit) {
    TaskScreenScaffold("Task 6: produceState", onBack) { Text("Task 6 implementation is added in its own commit.", Modifier.padding(it)) }
}
