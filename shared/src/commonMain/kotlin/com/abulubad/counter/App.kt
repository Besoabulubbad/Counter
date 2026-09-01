package com.abulubad.counter

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.ui.CounterScaffold
import com.abulubad.counter.ui.LocalPane
import com.abulubad.counter.ui.NavButton
import com.abulubad.counter.ui.Pane
import com.abulubad.counter.ui.PayButton
import com.abulubad.counter.ui.grid.ConflictBanner
import com.abulubad.counter.ui.grid.CountersStrip
import com.abulubad.counter.ui.grid.DetailPanel
import com.abulubad.counter.ui.grid.GridRow
import com.abulubad.counter.ui.grid.GridUiState
import com.abulubad.counter.ui.grid.GridViewModel
import com.abulubad.counter.ui.grid.ReservationGrid
import com.abulubad.counter.ui.grid.ReservationList
import com.abulubad.counter.ui.grid.SlotSheet
import com.abulubad.counter.ui.grid.ViewMode
import com.abulubad.counter.ui.paneFor
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterTheme
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexSans
import org.koin.compose.koinInject

@Composable
fun App() {
    CounterTheme {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalPane provides paneFor(maxWidth)) {
                CounterShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CounterShell() {
    val pane = LocalPane.current
    val compact = pane == Pane.Compact
    val expanded = pane == Pane.Expanded
    val viewModel = koinInject<GridViewModel>()
    LaunchedEffect(viewModel) { viewModel.seed() }
    val state by viewModel.state.collectAsState()
    val offline by viewModel.offline.collectAsState()
    val outboxDepth by viewModel.outboxDepth.collectAsState()
    val forceConflict by viewModel.forceConflict.collectAsState()
    val conflict by viewModel.conflict.collectAsState()
    var viewMode by remember(compact) { mutableStateOf(if (compact) ViewMode.List else ViewMode.Grid) }
    var selectedKey by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val onSelect: (GridRow, Int) -> Unit = { row, position -> selectedKey = row.slot.id.value to position }
    val onAdvance: (Reservation) -> Unit = { viewModel.advance(it) }
    val selectedRow = selectedKey?.let { key -> state.rows.find { it.slot.id.value == key.first } }
    val selectedPos = selectedKey?.second ?: 0
    val cursor = selectedRow?.let { it to selectedPos }
    val conflictLocation = conflict?.let { c ->
        state.rows.firstNotNullOfOrNull { row ->
            val index = row.cells.indexOfFirst { it?.id?.value == c.reservationId }
            if (index >= 0) Triple(row, index, c.serverVersion) else null
        }
    }

    val toggle: @Composable RowScope.() -> Unit = {
        NavButton("Grid", active = viewMode == ViewMode.Grid) { viewMode = ViewMode.Grid }
        NavButton("List", active = viewMode == ViewMode.List) { viewMode = ViewMode.List }
    }

    CounterScaffold(
        chrome = {
            Text(
                "Counter",
                color = CounterColors.OnChrome,
                fontFamily = PlexSans,
                fontSize = CounterType.emphasis,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            if (!compact) {
                toggle()
                Spacer(Modifier.width(12.dp))
                PayButton(null)
            }
        },
        actionBar = if (compact) {
            {
                toggle()
                Spacer(Modifier.weight(1f))
                PayButton(null)
            }
        } else {
            null
        },
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        Box(
            Modifier.fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event -> handleGridKey(event, state, viewMode, selectedKey, onAdvance) { selectedKey = it } },
        ) {
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                GridArea(state, viewMode, onSelect, cursor, conflictLocation, viewModel::resolveRetry, viewModel::resolveDiscard, Modifier.weight(1f).fillMaxHeight())
                DetailPanel(selectedRow, selectedPos, onAdvance, offline, outboxDepth, viewModel::toggleOffline, forceConflict, viewModel::toggleForceConflict, Modifier.width(340.dp).fillMaxHeight())
            }
        } else {
            GridArea(state, viewMode, onSelect, cursor, conflictLocation, viewModel::resolveRetry, viewModel::resolveDiscard, Modifier.fillMaxSize())
            if (selectedRow != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedKey = null },
                    shape = RectangleShape,
                    containerColor = CounterColors.Surface,
                    scrimColor = Color.Black.copy(alpha = 0.45f),
                    dragHandle = null,
                ) {
                    SlotSheet(selectedRow, selectedPos, onAdvance, Modifier.fillMaxWidth())
                }
            }
        }
        }
    }
}

private fun handleGridKey(
    event: KeyEvent,
    state: GridUiState,
    viewMode: ViewMode,
    selectedKey: Pair<String, Int>?,
    onAdvance: (Reservation) -> Unit,
    setSelectedKey: (Pair<String, Int>?) -> Unit,
): Boolean {
    if (event.type != KeyEventType.KeyDown || viewMode != ViewMode.Grid || state.rows.isEmpty()) return false
    val rows = state.rows
    val lastCol = (state.positions - 1).coerceAtLeast(0)
    val curRow = selectedKey?.first
        ?.let { id -> rows.indexOfFirst { it.slot.id.value == id } }
        ?.takeIf { it >= 0 } ?: 0
    val curCol = selectedKey?.second ?: 0
    fun move(row: Int, col: Int): Boolean {
        setSelectedKey(rows[row.coerceIn(0, rows.lastIndex)].slot.id.value to col.coerceIn(0, lastCol))
        return true
    }
    return when (event.key) {
        Key.DirectionUp -> move(curRow - 1, curCol)
        Key.DirectionDown -> move(curRow + 1, curCol)
        Key.DirectionLeft -> move(curRow, curCol - 1)
        Key.DirectionRight -> move(curRow, curCol + 1)
        Key.Enter -> {
            val reservation = if (selectedKey != null) rows.getOrNull(curRow)?.cells?.getOrNull(curCol) else null
            if (reservation != null) {
                onAdvance(reservation)
                true
            } else {
                false
            }
        }
        Key.Escape -> {
            setSelectedKey(null)
            true
        }
        else -> false
    }
}

@Composable
private fun GridArea(
    state: GridUiState,
    viewMode: ViewMode,
    onSelect: (GridRow, Int) -> Unit,
    selected: Pair<GridRow, Int>?,
    conflict: Triple<GridRow, Int, Long>?,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        CountersStrip(state.counters, Modifier.fillMaxWidth())
        if (conflict != null) {
            ConflictBanner(conflict.first, conflict.second, conflict.third, onRetry, onDiscard)
        }
        when (viewMode) {
            ViewMode.Grid -> ReservationGrid(state, onSelect, selected, Modifier.weight(1f).fillMaxWidth())
            ViewMode.List -> ReservationList(state, onSelect, Modifier.weight(1f).fillMaxWidth())
        }
    }
}