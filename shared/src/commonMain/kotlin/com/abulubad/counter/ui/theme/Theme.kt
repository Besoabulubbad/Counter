package com.abulubad.counter.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.abulubad.counter.platform.InputMode
import com.abulubad.counter.platform.inputMode as platformInputMode

@Composable
fun CounterTheme(
    inputMode: InputMode = platformInputMode,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCounterDimens provides dimensFor(inputMode),
    ) {
        content()
    }
}