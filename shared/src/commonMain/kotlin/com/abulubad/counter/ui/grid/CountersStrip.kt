package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.LocalPane
import com.abulubad.counter.ui.Pane
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun CountersStrip(counters: Counters, modifier: Modifier = Modifier) {
    val compact = LocalPane.current == Pane.Compact
    Row(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(CounterColors.SurfaceSunk)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(CounterColors.Rule, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 20.dp),
    ) {
        if (!compact) {
            Text(
                "Main",
                color = CounterColors.Ink,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
                fontWeight = FontWeight.SemiBold,
            )
        }
        CounterItem("booked", counters.booked)
        if (compact) {
            CounterItem("open", counters.open)
            CounterItem("unsynced", 0)
        } else {
            CounterItem("held", counters.held)
            CounterItem("paid", counters.paid)
            CounterItem("no shows", counters.noShows)
            CounterItem("open", counters.open)
        }
        Spacer(Modifier.weight(1f))
        if (LocalPane.current == Pane.Expanded) {
            Legend()
        }
    }
}

@Composable
private fun Legend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LegendItem("Open") {
            drawRect(CounterColors.Surface)
            drawRect(CounterColors.Rule, style = Stroke(1.dp.toPx()))
        }
        LegendItem("Held") {
            drawRect(CounterColors.Held)
            val x = 1.5.dp.toPx()
            drawLine(
                CounterColors.OnFill.copy(alpha = 0.8f),
                Offset(x, 0f),
                Offset(x, size.height),
                strokeWidth = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx())),
            )
        }
        LegendItem("Confirmed") { drawRect(CounterColors.Confirmed) }
        LegendItem("Checked in") {
            drawRect(CounterColors.Confirmed)
            drawRect(CounterColors.CheckedInEdge, size = Size(3.dp.toPx(), size.height))
        }
        LegendItem("No show") {
            drawRect(CounterColors.NoShow)
            val step = 3.dp.toPx()
            var x = 0f
            while (x < size.width + size.height) {
                drawLine(
                    CounterColors.OnFill.copy(alpha = 0.3f),
                    Offset(x, size.height),
                    Offset(x - size.height, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
                x += step
            }
        }
        LegendItem("Partial") {
            drawRect(CounterColors.Confirmed)
            drawRect(CounterColors.Held, topLeft = Offset(0f, size.height - 2.dp.toPx()), size = Size(size.width, 2.dp.toPx()))
        }
    }
}

@Composable
private fun LegendItem(label: String, draw: DrawScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(16.dp, 12.dp).drawBehind(draw))
        Text(label, color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro)
    }
}

@Composable
private fun CounterItem(label: String, value: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            label,
            color = CounterColors.InkMuted,
            fontFamily = PlexSans,
            fontSize = CounterType.body,
        )
        Text(
            "$value",
            color = CounterColors.Ink,
            fontFamily = PlexMono,
            fontSize = CounterType.emphasis,
            fontWeight = FontWeight.SemiBold,
        )
    }
}