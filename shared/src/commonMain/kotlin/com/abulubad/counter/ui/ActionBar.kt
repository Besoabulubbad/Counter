package com.abulubad.counter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
fun NavButton(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clickable(onClick = onClick)
            .background(if (active) CounterColors.OnChrome else Color.Transparent)
            .border(1.dp, if (active) CounterColors.OnChrome else CounterColors.ChromeRule)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            label,
            color = if (active) CounterColors.Ink else CounterColors.OnChromeMuted,
            fontFamily = PlexSans,
            fontSize = CounterType.body,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
fun PayButton(total: String, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .clickable(onClick = onClick)
            .background(CounterColors.Confirmed)
            .padding(horizontal = 22.dp, vertical = 11.dp),
    ) {
        Text(
            "Pay $total",
            color = CounterColors.Surface,
            fontFamily = PlexMono,
            fontSize = CounterType.emphasis,
            fontWeight = FontWeight.SemiBold,
        )
    }
}