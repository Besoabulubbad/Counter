package com.abulubad.counter.ui.grid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

enum class ViewMode { Grid, List, Order }

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
fun ReservationList(
    state: GridUiState,
    onSelect: (GridRow, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth >= 760.dp) {
            WideList(state, onSelect, Modifier.fillMaxSize())
        } else {
            PhoneList(state, onSelect, Modifier.fillMaxSize())
        }
    }
}

private fun flatten(state: GridUiState): List<ListEntry> =
    state.rows.flatMap { row ->
        row.cells.mapIndexedNotNull { pos, res -> res?.let { ListEntry(row, pos, it) } }
    }

private fun booked(row: GridRow): List<Pair<Int, Reservation>> =
    row.cells.mapIndexedNotNull { pos, res -> res?.let { pos to it } }

// -- Phone: grouped by slot, sticky subheader per slot --

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhoneList(state: GridUiState, onSelect: (GridRow, Int) -> Unit, modifier: Modifier) {
    val groups = remember(state) { state.rows.map { it to booked(it) }.filter { it.second.isNotEmpty() } }
    val dimens = LocalCounterDimens.current
    val rowHeight = dimens.rowHeight
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth().background(CounterColors.Surface).dragToScroll(listState, dimens.pointerDragScroll),
    ) {
        groups.forEach { (row, reservations) ->
            stickyHeader(key = "h-${row.slot.id.value}") {
                SlotHeader(row, reservations.size)
            }
            items(reservations, key = { it.second.id.value }) { (pos, res) ->
                PhoneRow(row, pos, res, rowHeight, onSelect)
            }
        }
    }
}

@Composable
private fun SlotHeader(row: GridRow, bookedCount: Int) {
    Row(
        Modifier.fillMaxWidth().height(28.dp).background(CounterColors.SurfaceSunk)
            .bottomRule(CounterColors.Rule).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(formatSlotTime(row.slot.startsAt), color = CounterColors.Ink, fontFamily = PlexMono, fontSize = CounterType.body, fontWeight = FontWeight.SemiBold)
        Text("· ${row.subLabel}", color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro)
        Spacer(Modifier.weight(1f))
        Text("$bookedCount of ${row.slot.capacity}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
    }
}

@Composable
private fun PhoneRow(row: GridRow, position: Int, r: Reservation, rowHeight: Dp, onSelect: (GridRow, Int) -> Unit) {
    val balance = r.priceMinorUnits - r.paidMinorUnits
    val currency = row.slot.rate.currency
    Row(
        Modifier.fillMaxWidth().height(rowHeight).bottomRule(CounterColors.RuleFaint)
            .clickable { onSelect(row, position) }
            .padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(chipColor(r.status)))
        Column(
            Modifier.weight(1f).padding(start = 10.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    r.holderName,
                    modifier = Modifier.weight(1f),
                    color = CounterColors.Ink,
                    fontFamily = PlexSans,
                    fontSize = CounterType.body,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                statusWord(r.status)?.let {
                    Text(it, color = CounterColors.InkMuted, fontFamily = PlexSans, fontSize = CounterType.micro)
                }
                Text(
                    moneyLabel(balance, currency),
                    color = if (balance == 0L) CounterColors.InkMuted else CounterColors.Ink,
                    fontFamily = PlexMono,
                    fontSize = CounterType.body,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(metaLine(r), color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
                Spacer(Modifier.weight(1f))
                Text("pos ${position + 1}", color = CounterColors.InkMuted, fontFamily = PlexMono, fontSize = CounterType.micro)
            }
        }
    }
}

internal fun statusWord(status: ReservationStatus): String? = when (status) {
    ReservationStatus.CONFIRMED -> null
    else -> statusLabel(status)
}

private fun moneyLabel(balance: Long, currency: String): String =
    if (balance == 0L) "paid" else "due ${formatCurrency(balance, currency)}"

internal fun metaLine(r: Reservation): String {
    val parts = mutableListOf("${r.partySize} up", durationLabel(r.duration), r.rateCode)
    if (r.hasCart) parts.add("cart")
    return parts.joinToString(" · ")
}

// -- Wide: columnar table --

@Composable
private fun WideList(state: GridUiState, onSelect: (GridRow, Int) -> Unit, modifier: Modifier) {
    val entries = remember(state) { flatten(state) }
    val dimens = LocalCounterDimens.current
    val listState = rememberLazyListState()
    Column(modifier.fillMaxWidth().background(CounterColors.Surface)) {
        WideHeader()
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().dragToScroll(listState, dimens.pointerDragScroll),
        ) {
            items(entries, key = { it.reservation.id.value }) { entry ->
                WideRow(entry, dimens.rowHeight, onSelect)
            }
        }
    }
}

@Composable
private fun WideHeader() {
    Row(
        Modifier.fillMaxWidth().height(34.dp).background(CounterColors.SurfaceSunk).bottomRule(CounterColors.Rule),
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
private fun WideRow(entry: ListEntry, rowHeight: Dp, onSelect: (GridRow, Int) -> Unit) {
    val r = entry.reservation
    val balance = r.priceMinorUnits - r.paidMinorUnits
    val currency = entry.row.slot.rate.currency
    Row(
        Modifier.fillMaxWidth().height(rowHeight).bottomRule(CounterColors.RuleFaint)
            .clickable { onSelect(entry.row, entry.position) },
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
            if (balance == 0L) CounterColors.InkMuted else CounterColors.Ink,
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
private fun Mono(text: String, modifier: Modifier, color: Color = CounterColors.Ink, align: TextAlign = TextAlign.Start) {
    Text(text, modifier = modifier, color = color, fontFamily = PlexMono, fontSize = CounterType.body, textAlign = align)
}

@Composable
private fun Sans(text: String, modifier: Modifier, color: Color) {
    Text(text, modifier = modifier, color = color, fontFamily = PlexSans, fontSize = CounterType.body)
}

internal fun Modifier.bottomRule(color: Color): Modifier = drawBehind {
    val y = size.height - 1.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
}

private fun Modifier.dragToScroll(state: LazyListState, enabled: Boolean): Modifier =
    if (!enabled) {
        this
    } else {
        pointerInput(Unit) {
            detectVerticalDragGestures { change, delta ->
                change.consume()
                state.dispatchRawDelta(-delta)
            }
        }
    }