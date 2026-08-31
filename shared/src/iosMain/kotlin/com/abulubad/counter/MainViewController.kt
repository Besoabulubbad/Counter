package com.abulubad.counter

import androidx.compose.ui.window.ComposeUIViewController
import com.abulubad.counter.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}