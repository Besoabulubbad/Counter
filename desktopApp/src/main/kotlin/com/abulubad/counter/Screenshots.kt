package com.abulubad.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.abulubad.counter.data.buildSeed
import com.abulubad.counter.di.initKoin
import com.abulubad.counter.platform.InputMode
import com.abulubad.counter.ui.LocalPane
import com.abulubad.counter.ui.Pane
import com.abulubad.counter.ui.grid.CountersStrip
import com.abulubad.counter.ui.grid.ReservationList
import com.abulubad.counter.ui.grid.buildGrid
import com.abulubad.counter.ui.order.OrderScreen
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterTheme
import java.io.File
import kotlin.system.exitProcess
import org.jetbrains.skia.EncodedImageFormat

private const val DENSITY = 2f

private fun write(outDir: File, name: String, dpW: Int, dpH: Int, pumps: Int, content: @Composable () -> Unit) {
    val scene = ImageComposeScene(
        width = (dpW * DENSITY).toInt(),
        height = (dpH * DENSITY).toInt(),
        density = Density(DENSITY),
        content = content,
    )
    var frame = 0L
    repeat(pumps) {
        scene.render(frame)
        frame += 16_000_000L
        Thread.sleep(12)
    }
    val image = scene.render(frame)
    File(outDir, "$name.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    scene.close()
    println("wrote $name.png")
}

@Composable
private fun Framed(pane: Pane, content: @Composable () -> Unit) {
    CounterTheme {
        CompositionLocalProvider(LocalPane provides pane) {
            content()
        }
    }
}

fun main() {
    initKoin()
    val outDir = File(System.getenv("SHOT_DIR") ?: "build/screenshots").apply { mkdirs() }

    write(outDir, "grid-desktop", 1440, 900, pumps = 160) { App() }
    write(outDir, "tablet-grid", 840, 1120, pumps = 160) { App(InputMode.Touch) }
    write(outDir, "phone-list", 400, 860, pumps = 160) { App(InputMode.Touch) }

    val bundle = buildSeed()
    val subLabels = bundle.resources.flatMap { it.subResources }.associate { it.id.value to it.name.substringBefore(' ') }
    val state = buildGrid(bundle.slots, bundle.reservations, subLabels)

    write(outDir, "list-desktop", 1280, 860, pumps = 24) {
        Framed(Pane.Expanded) {
            Column(Modifier.fillMaxSize().background(CounterColors.Surface)) {
                CountersStrip(state.counters, 0, Modifier.fillMaxWidth())
                ReservationList(state, { _, _ -> }, Modifier.fillMaxSize())
            }
        }
    }
    write(outDir, "order-desktop", 1280, 860, pumps = 24) {
        Framed(Pane.Expanded) { OrderScreen({}, Modifier.fillMaxSize()) }
    }

    exitProcess(0)
}