package com.abulubad.counter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.abulubad.counter.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Counter",
        ) {
            App()
        }
    }
}