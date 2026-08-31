package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.platform.formatCurrency
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun DetailPanel(row: GridRow?, position: Int, modifier: Modifier = Modifier) {
    Column(modifier.background(CounterColors.SurfaceSunk).verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            if (row == null) {
                Text("Select a slot", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
            } else {
                SelectedDetail(row, position)
            }
        }
        DebugSection()
    }
}

@Composable
private fun SelectedDetail(row: GridRow, position: Int) {
    val reservation = row.cells.getOrNull(position)
    val currency = row.slot.rate.currency
    Text("Selected", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro)
    Spacer(Modifier.height(7.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(formatSlotTime(row.slot.startsAt), color = CounterColors.Ink, fontFamily = PlexMono, fontSize = CounterType.display, fontWeight = FontWeight.SemiBold)
        Text("${row.subLabel} · position ${position + 1}", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
    }
    Spacer(Modifier.height(9.dp))
    Text(
        reservation?.holderName ?: "Open position",
        color = CounterColors.Ink,
        fontFamily = PlexSans,
        fontSize = CounterType.emphasis,
        fontWeight = FontWeight.Medium,
    )
    Spacer(Modifier.height(11.dp))
    Field("Status", if (reservation == null) "Open" else statusLabel(reservation.status))
    Field("Party", reservation?.partySize?.toString() ?: "—")
    Field("Holes", reservation?.let { durationLabel(it.duration) } ?: "—")
    Field("Rate", reservation?.rateCode ?: "—")
    Field("Price", reservation?.let { formatCurrency(it.priceMinorUnits, currency) } ?: "—")
    Field("Paid", reservation?.let { formatCurrency(it.paidMinorUnits, currency) } ?: "—")
    Field("Source", reservation?.let { if (it.bookedOnline) "Online booking" else "Counter" } ?: "—")
    Field("Version", reservation?.version?.toString() ?: "—")
    Spacer(Modifier.height(13.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionChip("Check in", primary = true, Modifier.weight(1f))
        ActionChip("Add to ticket", primary = false, Modifier.weight(1f))
    }
}

@Composable
private fun Field(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.width(96.dp), color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
        Text(value, color = CounterColors.Ink, fontFamily = PlexMono, fontSize = CounterType.body)
    }
}

@Composable
private fun ActionChip(label: String, primary: Boolean, modifier: Modifier) {
    Box(
        modifier
            .clickable {}
            .background(if (primary) CounterColors.Confirmed else CounterColors.Surface)
            .border(1.dp, if (primary) CounterColors.Confirmed else CounterColors.Rule)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (primary) CounterColors.Surface else CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DebugSection() {
    Column(
        Modifier.fillMaxWidth().topRule(CounterColors.Rule).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Debug · fake backend", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DebugChip("Offline", Modifier.weight(1f))
            DebugChip("Force conflict", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("outbox 0", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
            Text("latency n/a", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
            Text("recomp ${RecompositionStats.live}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
        }
    }
}

@Composable
private fun DebugChip(label: String, modifier: Modifier) {
    Box(
        modifier
            .clickable {}
            .background(CounterColors.Surface)
            .border(1.dp, CounterColors.Rule)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body)
    }
}

private fun Modifier.topRule(color: Color): Modifier = drawBehind {
    drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
}