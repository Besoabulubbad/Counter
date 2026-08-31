package com.abulubad.counter.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class Pane { Compact, Medium, Expanded }

fun paneFor(width: Dp): Pane = when {
    width < 600.dp -> Pane.Compact
    width < 1024.dp -> Pane.Medium
    else -> Pane.Expanded
}

val LocalPane = staticCompositionLocalOf { Pane.Expanded }