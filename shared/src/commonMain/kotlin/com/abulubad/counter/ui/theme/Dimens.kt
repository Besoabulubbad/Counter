package com.abulubad.counter.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.counter.platform.InputMode

data class CounterDimens(
    val rowHeight: Dp,
    val headerHeight: Dp,
    val cellWidth: Dp,
    val timeColWidth: Dp,
    val minHitTarget: Dp,
    val gutter: Dp,
    val chromeHeight: Dp,
    val actionBarHeight: Dp,
    val dragNeedsHold: Boolean,
)

val PointerDimens = CounterDimens(
    rowHeight = 56.dp,
    headerHeight = 34.dp,
    cellWidth = 184.dp,
    timeColWidth = 100.dp,
    minHitTarget = 32.dp,
    gutter = 14.dp,
    chromeHeight = 56.dp,
    actionBarHeight = 60.dp,
    dragNeedsHold = false,
)

val TouchDimens = CounterDimens(
    rowHeight = 64.dp,
    headerHeight = 34.dp,
    cellWidth = 168.dp,
    timeColWidth = 84.dp,
    minHitTarget = 48.dp,
    gutter = 12.dp,
    chromeHeight = 56.dp,
    actionBarHeight = 68.dp,
    dragNeedsHold = true,
)

fun dimensFor(inputMode: InputMode): CounterDimens = when (inputMode) {
    InputMode.Pointer -> PointerDimens
    InputMode.Touch -> TouchDimens
}

val LocalCounterDimens = staticCompositionLocalOf<CounterDimens> {
    error("CounterDimens not provided — wrap content in CounterTheme")
}