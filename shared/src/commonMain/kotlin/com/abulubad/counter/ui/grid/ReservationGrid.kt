package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.LocalCounterDimens
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private data class DragState(val reservation: Reservation, val pointer: Offset)

@Composable
fun ReservationGrid(
    state: GridUiState,
    onSelect: (GridRow, Int) -> Unit,
    selected: Pair<GridRow, Int>?,
    cutId: String?,
    onMove: (Reservation, GridRow, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalCounterDimens.current
    val scrollX = remember { mutableStateOf(0f) }
    val scrollY = remember { mutableStateOf(0f) }

    Column(modifier.fillMaxSize().background(CounterColors.Surface)) {
        Row(Modifier.height(dimens.headerHeight)) {
            Box(
                Modifier.width(dimens.timeColWidth).fillMaxHeight()
                    .background(CounterColors.SurfaceSunk)
                    .border(1.dp, CounterColors.Rule),
            )
            HeaderStrip(state.positions, scrollX, Modifier.weight(1f).fillMaxHeight().clipToBounds())
        }
        Row(Modifier.weight(1f)) {
            TimeColumn(state.rows, scrollY, Modifier.width(dimens.timeColWidth).fillMaxHeight().clipToBounds())
            GridBody(state, onSelect, selected, cutId, onMove, scrollX, scrollY, Modifier.weight(1f).fillMaxHeight().clipToBounds())
        }
    }
}

@Composable
private fun HeaderStrip(positions: Int, scrollX: MutableState<Float>, modifier: Modifier) {
    val dimens = LocalCounterDimens.current
    val density = LocalDensity.current
    BoxWithConstraints(modifier.background(CounterColors.SurfaceSunk)) {
        val cellW = with(density) { dimens.cellWidth.toPx() }
        val width = constraints.maxWidth.toFloat()
        val last = (positions - 1).coerceAtLeast(0)
        val firstCol = (scrollX.value / cellW).toInt().coerceIn(0, last)
        val lastCol = ((scrollX.value + width) / cellW).toInt().coerceIn(0, last)
        for (col in firstCol..lastCol) {
            Box(
                Modifier
                    .offset { IntOffset((col * cellW - scrollX.value).roundToInt(), 0) }
                    .size(dimens.cellWidth, dimens.headerHeight)
                    .border(1.dp, CounterColors.Rule)
                    .padding(horizontal = 9.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    "Position ${col + 1}",
                    color = CounterColors.InkMuted,
                    fontFamily = PlexMono,
                    fontSize = CounterType.micro,
                )
            }
        }
    }
}

@Composable
private fun TimeColumn(rows: List<GridRow>, scrollY: MutableState<Float>, modifier: Modifier) {
    val dimens = LocalCounterDimens.current
    val density = LocalDensity.current
    BoxWithConstraints(modifier.background(CounterColors.SurfaceSunk)) {
        if (rows.isEmpty()) return@BoxWithConstraints
        val rowH = with(density) { dimens.rowHeight.toPx() }
        val height = constraints.maxHeight.toFloat()
        val firstRow = (scrollY.value / rowH).toInt().coerceIn(0, rows.lastIndex)
        val lastRow = ((scrollY.value + height) / rowH).toInt().coerceIn(0, rows.lastIndex)
        for (row in firstRow..lastRow) {
            Column(
                Modifier
                    .offset { IntOffset(0, (row * rowH - scrollY.value).roundToInt()) }
                    .size(dimens.timeColWidth, dimens.rowHeight)
                    .border(1.dp, CounterColors.Rule)
                    .padding(horizontal = 9.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    formatSlotTime(rows[row].slot.startsAt),
                    color = CounterColors.Ink,
                    fontFamily = PlexMono,
                    fontSize = CounterType.body,
                    fontWeight = FontWeight.Medium,
                )
                if (rows[row].subLabel.isNotEmpty()) {
                    Text(
                        rows[row].subLabel,
                        color = CounterColors.InkMuted,
                        fontFamily = PlexSans,
                        fontSize = CounterType.micro,
                    )
                }
            }
        }
    }
}

@Composable
private fun GridBody(
    state: GridUiState,
    onSelect: (GridRow, Int) -> Unit,
    selected: Pair<GridRow, Int>?,
    cutId: String?,
    onMove: (Reservation, GridRow, Int) -> Unit,
    scrollX: MutableState<Float>,
    scrollY: MutableState<Float>,
    modifier: Modifier,
) {
    val dimens = LocalCounterDimens.current
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val rowH = with(density) { dimens.rowHeight.toPx() }
        val cellW = with(density) { dimens.cellWidth.toPx() }
        val bodyW = constraints.maxWidth.toFloat()
        val bodyH = constraints.maxHeight.toFloat()
        val maxX = (state.positions * cellW - bodyW).coerceAtLeast(0f)
        val maxY = (state.rows.size * rowH - bodyH).coerceAtLeast(0f)

        LaunchedEffect(maxX, maxY) {
            scrollX.value = scrollX.value.coerceIn(0f, maxX)
            scrollY.value = scrollY.value.coerceIn(0f, maxY)
        }

        val scrolledToNow = remember { mutableStateOf(false) }
        LaunchedEffect(state.rows.size) {
            if (!scrolledToNow.value && state.rows.isNotEmpty()) {
                val index = state.rows.indexOfFirst { it.slot.startsAt >= Clock.System.now() }
                if (index > 0) scrollY.value = (index * rowH).coerceIn(0f, maxY)
                scrolledToNow.value = true
            }
        }

        LaunchedEffect(selected) {
            val sel = selected ?: return@LaunchedEffect
            val rowIndex = state.rows.indexOf(sel.first)
            if (rowIndex < 0) return@LaunchedEffect
            val cellTop = rowIndex * rowH
            val cellLeft = sel.second * cellW
            if (cellTop < scrollY.value) {
                scrollY.value = cellTop
            } else if (cellTop + rowH > scrollY.value + bodyH) {
                scrollY.value = (cellTop + rowH - bodyH).coerceAtLeast(0f)
            }
            if (cellLeft < scrollX.value) {
                scrollX.value = cellLeft
            } else if (cellLeft + cellW > scrollX.value + bodyW) {
                scrollX.value = (cellLeft + cellW - bodyW).coerceAtLeast(0f)
            }
        }

        val vertical = rememberScrollableState { delta ->
            val old = scrollY.value
            scrollY.value = (old - delta).coerceIn(0f, maxY)
            old - scrollY.value
        }
        val horizontal = rememberScrollableState { delta ->
            val old = scrollX.value
            scrollX.value = (old - delta).coerceIn(0f, maxX)
            old - scrollX.value
        }

        val drag = remember { mutableStateOf<DragState?>(null) }
        val edgeBand = with(density) { 44.dp.toPx() }
        LaunchedEffect(drag.value != null) {
            while (drag.value != null) {
                val pointer = drag.value?.pointer
                if (pointer != null) {
                    if (pointer.y < edgeBand) scrollY.value = (scrollY.value - 14f).coerceIn(0f, maxY)
                    else if (pointer.y > bodyH - edgeBand) scrollY.value = (scrollY.value + 14f).coerceIn(0f, maxY)
                }
                delay(16)
            }
        }

        if (state.rows.isEmpty()) return@BoxWithConstraints
        val sx = scrollX.value.coerceIn(0f, maxX)
        val sy = scrollY.value.coerceIn(0f, maxY)
        val firstRow = (sy / rowH).toInt().coerceIn(0, state.rows.lastIndex)
        val lastRow = ((sy + bodyH) / rowH).toInt().coerceIn(0, state.rows.lastIndex)
        val lastPos = (state.positions - 1).coerceAtLeast(0)
        val firstCol = (sx / cellW).toInt().coerceIn(0, lastPos)
        val lastCol = ((sx + bodyW) / cellW).toInt().coerceIn(0, lastPos)

        Box(
            Modifier.fillMaxSize()
                .scrollable(vertical, Orientation.Vertical)
                .scrollable(horizontal, Orientation.Horizontal)
                .pointerInput(state.rows, dimens.dragNeedsHold) {
                    val reservationAt: (Offset) -> Reservation? = { offset ->
                        val col = ((offset.x + scrollX.value) / cellW).toInt()
                        val rowIndex = ((offset.y + scrollY.value) / rowH).toInt()
                        state.rows.getOrNull(rowIndex)?.cells?.getOrNull(col)
                    }
                    val drop: () -> Unit = {
                        val current = drag.value
                        if (current != null) {
                            val col = ((current.pointer.x + scrollX.value) / cellW).toInt()
                            val rowIndex = ((current.pointer.y + scrollY.value) / rowH).toInt()
                            val target = state.rows.getOrNull(rowIndex)
                            if (target != null && canMoveTo(target, col, Clock.System.now())) {
                                onMove(current.reservation, target, col)
                            }
                        }
                        drag.value = null
                    }
                    if (dimens.dragNeedsHold) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset -> reservationAt(offset)?.let { drag.value = DragState(it, offset) } },
                            onDrag = { change, _ ->
                                if (drag.value != null) {
                                    drag.value = drag.value?.copy(pointer = change.position)
                                    change.consume()
                                }
                            },
                            onDragEnd = drop,
                            onDragCancel = { drag.value = null },
                        )
                    } else {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val reservation = reservationAt(down.position) ?: return@awaitEachGesture
                            val slop = viewConfiguration.touchSlop
                            var started = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                if (!started && (change.position - down.position).getDistance() > slop) {
                                    started = true
                                    drag.value = DragState(reservation, change.position)
                                }
                                if (started) {
                                    drag.value = drag.value?.copy(pointer = change.position)
                                    change.consume()
                                }
                            }
                            if (started) drop() else drag.value = null
                        }
                    }
                },
        ) {
            for (row in firstRow..lastRow) {
                val gridRow = state.rows[row]
                for (col in firstCol..lastCol) {
                    GridCell(
                        reservation = gridRow.cells.getOrNull(col),
                        currency = gridRow.slot.rate.currency,
                        selected = selected?.let { it.first.slot.id == gridRow.slot.id && it.second == col } ?: false,
                        cut = cutId != null && gridRow.cells.getOrNull(col)?.id?.value == cutId,
                        onClick = { onSelect(gridRow, col) },
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (col * cellW - scrollX.value).roundToInt(),
                                    (row * rowH - scrollY.value).roundToInt(),
                                )
                            }
                            .size(dimens.cellWidth, dimens.rowHeight),
                    )
                }
            }
            val active = drag.value
            if (active != null) {
                val targetCol = ((active.pointer.x + scrollX.value) / cellW).toInt()
                val targetRowIndex = ((active.pointer.y + scrollY.value) / rowH).toInt()
                val targetRow = state.rows.getOrNull(targetRowIndex)
                val valid = targetRow != null && canMoveTo(targetRow, targetCol, Clock.System.now())
                if (targetRow != null && targetCol in 0..lastPos) {
                    Box(
                        Modifier
                            .offset { IntOffset((targetCol * cellW - scrollX.value).roundToInt(), (targetRowIndex * rowH - scrollY.value).roundToInt()) }
                            .size(dimens.cellWidth, dimens.rowHeight)
                            .border(2.dp, if (valid) CounterColors.Confirmed else CounterColors.Conflict),
                    )
                }
                Box(
                    Modifier
                        .offset { IntOffset((active.pointer.x - cellW / 2f).roundToInt(), (active.pointer.y - rowH / 2f).roundToInt()) }
                        .size(dimens.cellWidth, dimens.rowHeight)
                        .background(chipColor(active.reservation.status))
                        .border(1.dp, CounterColors.Ink)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        active.reservation.holderName,
                        color = CounterColors.OnFill,
                        fontFamily = PlexSans,
                        fontSize = CounterType.body,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

internal fun formatSlotTime(instant: Instant): String {
    val time = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return time.hour.toString().padStart(2, '0') + ":" + time.minute.toString().padStart(2, '0')
}