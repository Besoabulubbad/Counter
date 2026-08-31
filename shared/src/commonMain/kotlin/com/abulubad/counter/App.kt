package com.abulubad.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterTheme
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.LocalCounterDimens
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
    val repo = koinInject<CounterRepository>()
    LaunchedEffect(repo) { repo.seedIfEmpty() }
    val slots by remember(repo) { repo.slots() }.collectAsState(emptyList())
    val reservations by remember(repo) { repo.reservations() }.collectAsState(emptyList())
    val booked = reservations.count { it.status != ReservationStatus.CANCELLED }
    val dimens = LocalCounterDimens.current

    Column(Modifier.fillMaxSize().background(CounterColors.Surface)) {
        Row(
            Modifier.fillMaxWidth()
                .height(dimens.chromeHeight)
                .background(CounterColors.Chrome)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Counter",
                color = CounterColors.OnChrome,
                fontFamily = PlexSans,
                fontSize = CounterType.emphasis,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "slots ${slots.size}   booked $booked",
                color = CounterColors.OnChromeMuted,
                fontFamily = PlexMono,
                fontSize = CounterType.micro,
            )
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            StateSwatches()
        }
    }
}

@Composable
private fun StateSwatches() {
    val states = listOf(
        Triple("Open", CounterColors.Surface, CounterColors.Ink),
        Triple("Held", CounterColors.Held, CounterColors.OnFill),
        Triple("Confirmed", CounterColors.Confirmed, CounterColors.OnFill),
        Triple("Checked in", CounterColors.Confirmed, CounterColors.CheckedInEdge),
        Triple("No show", CounterColors.NoShow, CounterColors.OnFill),
        Triple("Conflict", CounterColors.ConflictWash, CounterColors.Conflict),
    )
    for ((label, fill, ink) in states) {
        SwatchRow(label, fill, ink)
    }
}

@Composable
private fun SwatchRow(label: String, fill: Color, ink: Color) {
    val dimens = LocalCounterDimens.current
    Row(
        Modifier.fillMaxWidth().height(dimens.rowHeight).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.width(dimens.cellWidth).height(dimens.rowHeight - 12.dp)
                .background(fill),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 10.dp),
                color = ink,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "$120.00",
            color = CounterColors.Ink,
            fontFamily = PlexMono,
            fontSize = CounterType.body,
        )
    }
}