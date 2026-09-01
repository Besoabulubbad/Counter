package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun ConflictBanner(
    row: GridRow,
    position: Int,
    serverVersion: Long,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reservation = row.cells.getOrNull(position)
    Row(
        modifier.fillMaxWidth().background(CounterColors.ConflictWash).border(1.dp, CounterColors.Conflict).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.width(3.dp).height(40.dp).background(CounterColors.Conflict))
        Column(Modifier.weight(1f)) {
            Text(
                "${formatSlotTime(row.slot.startsAt)} · ${row.subLabel}, position ${position + 1} — sync conflict",
                color = CounterColors.Conflict,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${reservation?.holderName ?: "This booking"} was changed on another terminal, now on version $serverVersion, while you were offline. Your change is held locally, not dropped.",
                color = CounterColors.InkMuted,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
            )
        }
        BannerButton("Keep mine, retry", primary = true, onRetry)
        BannerButton("Discard", primary = false, onDiscard)
    }
}

@Composable
private fun BannerButton(label: String, primary: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clickable(onClick = onClick)
            .background(if (primary) CounterColors.Confirmed else CounterColors.Surface)
            .border(1.dp, if (primary) CounterColors.Confirmed else CounterColors.Conflict)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (primary) CounterColors.Surface else CounterColors.Conflict,
            fontFamily = PlexSans,
            fontSize = CounterType.body,
            fontWeight = FontWeight.Medium,
        )
    }
}