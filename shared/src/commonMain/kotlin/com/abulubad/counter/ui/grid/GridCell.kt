package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.platform.formatCurrency
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.LocalCounterDimens
import com.abulubad.counter.ui.theme.PlexMono
import com.abulubad.counter.ui.theme.PlexSans

@Composable
internal fun GridCell(
    reservation: Reservation?,
    currency: String,
    selected: Boolean,
    cut: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val dimens = LocalCounterDimens.current
    DisposableEffect(Unit) {
        RecompositionStats.enter()
        onDispose { RecompositionStats.leave() }
    }
    Box(
        modifier
            .background(fillFor(reservation))
            .clickable(onClick = onClick)
            .drawWithContent {
                if (reservation != null) drawCellCues(reservation, dimens.cueWidth)
                drawContent()
                drawSeams(dimens.seamWidth)
                if (cut) drawCutMarker()
                if (selected) drawCursor()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        when {
            reservation == null -> Unit
            reservation.status == ReservationStatus.CANCELLED -> CancelledContent(reservation)
            else -> CellContent(reservation, currency)
        }
    }
}

@Composable
private fun CellContent(reservation: Reservation, currency: String) {
    val dimens = LocalCounterDimens.current
    Column(
        Modifier.fillMaxSize().padding(horizontal = dimens.cellPadding),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "${reservation.partySize}",
                color = CounterColors.OnFill,
                fontFamily = PlexMono,
                fontSize = CounterType.micro,
            )
            Text(
                reservation.holderName,
                modifier = Modifier.weight(1f, fill = false),
                color = CounterColors.OnFill,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (reservation.bookedOnline) {
                Box(Modifier.size(5.dp).background(CounterColors.OnFill, CircleShape))
            }
        }
        Spacer(Modifier.size(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(durationLabel(reservation.duration), color = CounterColors.OnFill, fontFamily = PlexMono, fontSize = CounterType.micro)
            Text(reservation.rateCode, color = CounterColors.OnFill, fontFamily = PlexMono, fontSize = CounterType.micro)
            if (reservation.hasCart) {
                Text("cart", color = CounterColors.OnFill, fontFamily = PlexMono, fontSize = CounterType.micro)
            }
            Spacer(Modifier.weight(1f))
            Text(
                formatCurrency(reservation.priceMinorUnits, currency),
                color = CounterColors.OnFill,
                fontFamily = PlexMono,
                fontSize = CounterType.body,
            )
        }
    }
}

@Composable
private fun CancelledContent(reservation: Reservation) {
    Text(
        reservation.holderName,
        modifier = Modifier.padding(horizontal = LocalCounterDimens.current.cellPadding),
        color = CounterColors.InkMuted,
        fontFamily = PlexSans,
        fontSize = CounterType.body,
        textDecoration = TextDecoration.LineThrough,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

internal fun durationLabel(duration: DurationCode): String = when (duration) {
    DurationCode.NINE -> "9H"
    DurationCode.EIGHTEEN -> "18H"
}

internal fun statusLabel(status: ReservationStatus): String = when (status) {
    ReservationStatus.HELD -> "Held"
    ReservationStatus.CONFIRMED -> "Confirmed"
    ReservationStatus.CHECKED_IN -> "Checked in"
    ReservationStatus.NO_SHOW -> "No show"
    ReservationStatus.CANCELLED -> "Cancelled"
}

internal fun chipColor(status: ReservationStatus): Color = when (status) {
    ReservationStatus.HELD -> CounterColors.Held
    ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN -> CounterColors.Confirmed
    ReservationStatus.NO_SHOW -> CounterColors.NoShow
    ReservationStatus.CANCELLED -> CounterColors.InkMuted
}

internal fun primaryActionLabel(status: ReservationStatus): String = when (status) {
    ReservationStatus.HELD -> "Confirm"
    ReservationStatus.CONFIRMED -> "Check in"
    ReservationStatus.CHECKED_IN -> "Add to ticket"
    ReservationStatus.NO_SHOW, ReservationStatus.CANCELLED -> "Rebook"
}

private fun fillFor(reservation: Reservation?): Color = when (reservation?.status) {
    null, ReservationStatus.CANCELLED -> CounterColors.Surface
    ReservationStatus.HELD -> CounterColors.Held
    ReservationStatus.NO_SHOW -> CounterColors.NoShow
    ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN -> CounterColors.Confirmed
}

private fun DrawScope.drawCursor() {
    val w = 2.dp.toPx()
    drawRect(
        color = CounterColors.Ink,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        style = Stroke(width = w),
    )
}

private fun DrawScope.drawCutMarker() {
    drawRect(CounterColors.Surface.copy(alpha = 0.45f))
    val w = 2.dp.toPx()
    drawRect(
        color = CounterColors.Ink,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
    )
}

private fun DrawScope.drawSeams(seamWidth: Dp) {
    val w = seamWidth.toPx()
    drawLine(CounterColors.Rule, Offset(size.width - w / 2, 0f), Offset(size.width - w / 2, size.height), strokeWidth = w)
    drawLine(CounterColors.Rule, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), strokeWidth = w)
}

private fun DrawScope.drawCellCues(reservation: Reservation, cueWidth: Dp) {
    when (reservation.status) {
        ReservationStatus.HELD -> {
            val x = 2.dp.toPx()
            drawLine(
                color = CounterColors.OnFill.copy(alpha = 0.75f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
            )
        }
        ReservationStatus.CHECKED_IN -> {
            drawRect(color = CounterColors.CheckedInEdge, size = Size(cueWidth.toPx(), size.height))
        }
        ReservationStatus.NO_SHOW -> {
            clipRect {
                val step = 5.dp.toPx()
                val stroke = 1.dp.toPx()
                val color = CounterColors.OnFill.copy(alpha = 0.30f)
                var x = -size.height
                while (x < size.width) {
                    drawLine(color, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = stroke)
                    x += step
                }
            }
        }
        else -> Unit
    }
    if (reservation.isPartiallyPaid && reservation.priceMinorUnits > 0L) {
        val fraction = (reservation.paidMinorUnits.toFloat() / reservation.priceMinorUnits).coerceIn(0f, 1f)
        val ruleHeight = 3.dp.toPx()
        drawRect(
            color = CounterColors.Held,
            topLeft = Offset(0f, size.height - ruleHeight),
            size = Size(size.width * fraction, ruleHeight),
        )
    }
}