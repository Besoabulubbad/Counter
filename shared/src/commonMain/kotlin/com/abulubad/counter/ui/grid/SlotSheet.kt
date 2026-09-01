package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.platform.formatCurrency
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun SlotSheet(row: GridRow, focusedPosition: Int, onAdvance: (Reservation) -> Unit, onBook: (Slot, Int) -> Unit, modifier: Modifier = Modifier) {
    val bookedCount = row.cells.count { it != null }
    Column(modifier.background(CounterColors.Surface).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatSlotTime(row.slot.startsAt), color = CounterColors.Ink, fontFamily = PlexMono, fontSize = CounterType.hero, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Text("${row.subLabel} nine", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
            Spacer(Modifier.weight(1f))
            Text("$bookedCount of ${row.slot.capacity}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.body)
        }
        Spacer(Modifier.height(14.dp))
        for (pos in 0 until row.slot.capacity) {
            PositionRow(row.cells.getOrNull(pos), pos, row.slot.rate.currency, focused = pos == focusedPosition, onAdvance = onAdvance, onBook = { onBook(row.slot, pos) })
        }
    }
}

@Composable
private fun PositionRow(reservation: Reservation?, position: Int, currency: String, focused: Boolean, onAdvance: (Reservation) -> Unit, onBook: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(if (focused) CounterColors.SurfaceSunk else CounterColors.Surface)
            .bottomRule(CounterColors.RuleFaint),
    ) {
        Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(reservation?.let { chipColor(it.status) } ?: CounterColors.Rule))
            Spacer(Modifier.width(12.dp))
            if (reservation == null) {
                Text("Open", modifier = Modifier.weight(1f), color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
            } else {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(reservation.holderName, modifier = Modifier.weight(1f, fill = false), color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.emphasis, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        statusWord(reservation.status)?.let { Text(it, color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro) }
                    }
                    Text(metaLine(reservation), color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
                }
            }
            Text("pos ${position + 1}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro, modifier = Modifier.padding(end = 12.dp))
        }
        if (focused && reservation != null) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(moneyLine(reservation, currency), color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body, fontWeight = FontWeight.Medium)
                    Text("${sourceLabel(reservation)} · v${reservation.version}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
                }
                PrimaryButton(primaryActionLabel(reservation.status)) { onAdvance(reservation) }
            }
        } else if (focused) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Open · add a walk-in", modifier = Modifier.weight(1f), color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.body)
                PrimaryButton("Book walk-in") { onBook() }
            }
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier.clickable(onClick = onClick).background(CounterColors.Confirmed).padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(label, color = CounterColors.Surface, fontFamily = PlexSans, fontSize = CounterType.body, fontWeight = FontWeight.Medium)
    }
}

private fun moneyLine(reservation: Reservation, currency: String): String {
    val balance = reservation.priceMinorUnits - reservation.paidMinorUnits
    return if (balance == 0L) "Paid in full" else "Balance due ${formatCurrency(balance, currency)}"
}

private fun sourceLabel(reservation: Reservation): String = if (reservation.bookedOnline) "Online" else "Counter"