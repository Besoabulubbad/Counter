package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun CountersStrip(counters: Counters, modifier: Modifier = Modifier) {
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
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Main",
            color = CounterColors.Ink,
            fontFamily = PlexSans,
            fontSize = CounterType.body,
            fontWeight = FontWeight.SemiBold,
        )
        CounterItem("slots", counters.slots)
        CounterItem("booked", counters.booked)
        CounterItem("held", counters.held)
        CounterItem("paid", counters.paid)
        CounterItem("no shows", counters.noShows)
        CounterItem("open", counters.open)
        Spacer(Modifier.weight(1f))
        Text(
            "recomp ${RecompositionStats.live}",
            color = CounterColors.InkMuted,
            fontFamily = PlexMono,
            fontSize = CounterType.micro,
        )
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