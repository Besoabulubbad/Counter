package com.abulubad.counter

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.abulubad.counter.ui.CounterScaffold
import com.abulubad.counter.ui.grid.GridViewModel
import com.abulubad.counter.ui.grid.ReservationGrid
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
                "booked ${state.counters.booked}   open ${state.counters.available}",
                color = CounterColors.OnChromeMuted,
                fontFamily = PlexMono,
                fontSize = CounterType.micro,
            )
        },
    ) {
        ReservationGrid(state, Modifier.fillMaxSize())
    }
}