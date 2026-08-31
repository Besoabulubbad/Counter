package com.abulubad.counter.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.ui.theme.CounterColors
import com.abulubad.counter.ui.theme.CounterType
import com.abulubad.counter.ui.theme.PlexSans

@Composable
internal fun GridCell(reservation: Reservation?, modifier: Modifier) {
    Box(
        modifier
            .background(fillFor(reservation))
            .drawWithContent {
                if (reservation != null) drawCellCues(reservation)
                drawContent()
            }
            .border(1.dp, CounterColors.Rule),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (reservation != null) {
            val cancelled = reservation.status == ReservationStatus.CANCELLED
            Text(
                reservation.holderName,
                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                color = if (cancelled) CounterColors.InkMuted else CounterColors.OnFill,
                fontFamily = PlexSans,
                fontSize = CounterType.body,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (cancelled) TextDecoration.LineThrough else null,
            )
        }
    }
}

private fun fillFor(reservation: Reservation?): Color = when (reservation?.status) {
    null, ReservationStatus.CANCELLED -> CounterColors.Surface
    ReservationStatus.HELD -> CounterColors.Held
    ReservationStatus.NO_SHOW -> CounterColors.NoShow
    ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN -> CounterColors.Confirmed
}

private fun DrawScope.drawCellCues(reservation: Reservation) {
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
            drawRect(
                color = CounterColors.CheckedInEdge,
                size = Size(4.dp.toPx(), size.height),
            )
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