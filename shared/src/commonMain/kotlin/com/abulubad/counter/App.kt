package com.abulubad.counter

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.CounterScaffold
import com.abulubad.counter.ui.LocalPane
import com.abulubad.counter.ui.NavButton
import com.abulubad.counter.ui.Pane
import com.abulubad.counter.ui.PayButton
import com.abulubad.counter.ui.paneFor
import com.abulubad.counter.ui.grid.CountersStrip
import com.abulubad.counter.ui.grid.GridViewModel
import com.abulubad.counter.ui.grid.ReservationGrid
import com.abulubad.counter.ui.grid.ReservationList
import com.abulubad.counter.ui.grid.ViewMode
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

@Composable
private fun CounterShell() {
    val compact = LocalPane.current == Pane.Compact
    val viewModel = koinInject<GridViewModel>()
    LaunchedEffect(viewModel) { viewModel.seed() }
    val state by viewModel.state.collectAsState()
    var viewMode by remember(compact) { mutableStateOf(if (compact) ViewMode.List else ViewMode.Grid) }

    val toggle: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
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
        Column(Modifier.fillMaxSize()) {
            CountersStrip(state.counters, Modifier.fillMaxWidth())
            when (viewMode) {
                ViewMode.Grid -> ReservationGrid(state, Modifier.weight(1f).fillMaxWidth())
                ViewMode.List -> ReservationList(state, Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}