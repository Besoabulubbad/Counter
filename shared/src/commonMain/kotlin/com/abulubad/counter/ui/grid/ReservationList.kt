package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.platform.formatCurrency
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.LocalCounterDimens
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

enum class ViewMode { Grid, List }

private data class ListEntry(val row: GridRow, val position: Int, val reservation: Reservation)

private val ColTime = 92.dp
private val ColCourse = 72.dp
private val ColPos = 44.dp
private val ColParty = 56.dp
private val ColHoles = 64.dp
private val ColRate = 80.dp
private val ColStatus = 120.dp
private val ColPrice = 96.dp
private val ColBalance = 120.dp

@Composable
fun ReservationList(state: GridUiState, modifier: Modifier = Modifier) {
    val entries = remember(state) {
        state.rows.flatMap { row ->
            row.cells.mapIndexedNotNull { pos, res -> res?.let { ListEntry(row, pos, it) } }
        }
    }
    val dimens = LocalCounterDimens.current
    Column(modifier.fillMaxWidth().background(CounterColors.Surface)) {
        ListHeader()
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(entries, key = { it.reservation.id.value }) { entry ->
                ListRow(entry, dimens.rowHeight)
            }
        }
    }
}

@Composable
private fun ListHeader() {
    Row(
        Modifier.fillMaxWidth().height(34.dp).background(CounterColors.SurfaceSunk)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(CounterColors.Rule, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderLabel("Time", Modifier.width(ColTime).padding(start = 12.dp))
        HeaderLabel("Course", Modifier.width(ColCourse))
        HeaderLabel("Pos", Modifier.width(ColPos))
        HeaderLabel("Holder", Modifier.weight(1f))
        HeaderLabel("Party", Modifier.width(ColParty))
        HeaderLabel("Holes", Modifier.width(ColHoles))
        HeaderLabel("Rate", Modifier.width(ColRate))
        HeaderLabel("Status", Modifier.width(ColStatus))
        HeaderLabel("Price", Modifier.width(ColPrice), TextAlign.End)
        HeaderLabel("Balance", Modifier.width(ColBalance).padding(end = 16.dp), TextAlign.End)
    }
}

@Composable
private fun HeaderLabel(text: String, modifier: Modifier, align: TextAlign = TextAlign.Start) {
    Text(text, modifier = modifier, color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro, textAlign = align)
}

@Composable
private fun ListRow(entry: ListEntry, rowHeight: Dp) {
    val r = entry.reservation
    val balance = r.priceMinorUnits - r.paidMinorUnits
    val currency = entry.row.slot.rate.currency
    Row(
        Modifier.fillMaxWidth().height(rowHeight)
            .drawBehind {
                val y = size.height - 1.dp.toPx()
                drawLine(CounterColors.RuleFaint, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Mono(formatSlotTime(entry.row.slot.startsAt), Modifier.width(ColTime).padding(start = 12.dp))
        Sans(entry.row.subLabel, Modifier.width(ColCourse), CounterColors.InkMuted)
        Mono("${entry.position + 1}", Modifier.width(ColPos), CounterColors.InkMuted)
        Text(
            r.holderName,
            Modifier.weight(1f).padding(end = 8.dp),
            color = CounterColors.Ink,
            fontFamily = PlexSans,
            fontSize = CounterType.body,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Mono("${r.partySize}", Modifier.width(ColParty))
        Mono(durationLabel(r.duration), Modifier.width(ColHoles), CounterColors.InkMuted)
        Mono(r.rateCode, Modifier.width(ColRate), CounterColors.InkMuted)
        StatusCell(r.status, Modifier.width(ColStatus))
        Mono(formatCurrency(r.priceMinorUnits, currency), Modifier.width(ColPrice), align = TextAlign.End)
        Mono(
            if (balance == 0L) "paid" else formatCurrency(balance, currency),
            Modifier.width(ColBalance).padding(end = 16.dp),
            if (balance == 0L) CounterColors.InkMuted else CounterColors.Held,
            TextAlign.End,
        )
    }
}

@Composable
private fun StatusCell(status: ReservationStatus, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.size(10.dp).background(chipColor(status), CircleShape))
        Text(statusLabel(status), color = CounterColors.Ink, fontFamily = PlexSans, fontSize = CounterType.body)
    }
}

@Composable
private fun RowScope.Mono(text: String, modifier: Modifier, color: Color = CounterColors.Ink, align: TextAlign = TextAlign.Start) {
    Text(text, modifier = modifier, color = color, fontFamily = PlexMono, fontSize = CounterType.body, textAlign = align)
}

@Composable
private fun Sans(text: String, modifier: Modifier, color: Color) {
    Text(text, modifier = modifier, color = color, fontFamily = PlexSans, fontSize = CounterType.body)
}

private fun formatSlotTimeFor(row: GridRow): String = formatSlotTime(row.slot.startsAt)