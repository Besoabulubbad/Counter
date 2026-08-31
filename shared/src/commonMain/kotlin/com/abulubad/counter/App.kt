package com.abulubad.counter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.counter.ui.CounterScaffold
import com.abulubad.counter.ui.NavButton
import com.abulubad.counter.ui.PayButton
import com.abulubad.counter.ui.grid.CountersStrip
import com.abulubad.counter.ui.grid.GridViewModel
import com.abulubad.counter.ui.grid.ReservationGrid
import com.abulubad.counter.ui.grid.ReservationList
import com.abulubad.counter.ui.grid.ViewMode
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterTheme
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans
import org.koin.compose.koinInject

@Composable
fun App() {
    CounterTheme {
        CounterShell()
    }
}

@Composable
private fun CounterShell() {
    val viewModel = koinInject<GridViewModel>()
    LaunchedEffect(viewModel) { viewModel.seed() }
    val state by viewModel.state.collectAsState()
    var viewMode by remember { mutableStateOf(ViewMode.Grid) }

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
            Text(
                "Main · front and back nine",
                color = CounterColors.OnChromeMuted,
                fontFamily = PlexMono,
                fontSize = CounterType.body,
            )
        },
        actionBar = {
            NavButton("Order", active = false) {}
            NavButton("Grid", active = viewMode == ViewMode.Grid) { viewMode = ViewMode.Grid }
            NavButton("List", active = viewMode == ViewMode.List) { viewMode = ViewMode.List }
            Spacer(Modifier.weight(1f))
            PayButton("$0.00")
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