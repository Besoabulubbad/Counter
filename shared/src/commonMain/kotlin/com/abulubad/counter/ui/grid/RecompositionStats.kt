package com.abulubad.counter.ui.grid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object RecompositionStats {
    var live by mutableIntStateOf(0)
        private set
    var total by mutableIntStateOf(0)
        private set

    fun enter() {
        live += 1
        total += 1
    }

    fun leave() {
        live -= 1
    }
}