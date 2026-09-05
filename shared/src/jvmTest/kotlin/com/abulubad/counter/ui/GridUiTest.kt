package com.abulubad.counter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.abulubad.counter.domain.DurationCode
import com.abulubad.counter.domain.Rate
import com.abulubad.counter.domain.Reservation
import com.abulubad.counter.domain.ReservationId
import com.abulubad.counter.domain.ReservationStatus
import com.abulubad.counter.domain.ResourceId
import com.abulubad.counter.domain.Slot
import com.abulubad.counter.domain.SlotId
import com.abulubad.counter.ui.grid.GridUiState
import com.abulubad.counter.ui.grid.ReservationList
import com.abulubad.counter.ui.grid.buildGrid
import com.abulubad.counter.ui.order.OrderItem
import com.abulubad.counter.ui.order.OrderScreen
import com.abulubad.counter.ui.theme.LocalCounterDimens
import com.abulubad.counter.ui.theme.PointerDimens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

@OptIn(ExperimentalTestApi::class)
class GridUiTest {

    @Test
    fun listShowsReservationAndReportsRowSelection() = runComposeUiTest {
        val state = singleReservationState("Testerson")
        var selected: Pair<String, Int>? = null
        setContent {
            Framed {
                Box(Modifier.requiredSize(1280.dp, 800.dp)) {
                    ReservationList(state, onSelect = { row, pos -> selected = row.slot.id.value to pos })
                }
            }
        }

        onNodeWithText("Testerson").assertIsDisplayed()
        onNodeWithText("Testerson").performClick()
        assertEquals("slot-1" to 0, selected)
    }

    @Test
    fun orderTileAddsToTicketAndCategorySwitchWorks() = runComposeUiTest {
        var added: OrderItem? = null
        setContent {
            Framed {
                Box(Modifier.requiredSize(1000.dp, 800.dp)) {
                    OrderScreen(onAddItem = { added = it })
                }
            }
        }

        onNodeWithText("Course polo, navy").assertIsDisplayed()
        onNodeWithText("Course polo, navy").performClick()
        assertEquals("AP-1042", added?.sku)

        onNodeWithText("Balls").performClick()
        onNodeWithText("Sleeve of three").assertIsDisplayed()
    }

    @Test
    fun payButtonReflectsTicketStateAndFires() = runComposeUiTest {
        var paid = 0
        setContent {
            Framed {
                PayButton("$100.00") { paid++ }
            }
        }

        onNodeWithText("Pay $100.00").assertIsDisplayed()
        onNodeWithText("Pay $100.00").performClick()
        assertEquals(1, paid)
    }

    @Composable
    private fun Framed(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalCounterDimens provides PointerDimens) {
            content()
        }
    }

    private fun singleReservationState(holder: String): GridUiState {
        val slot = Slot(
            id = SlotId("slot-1"),
            resourceId = ResourceId("r1"),
            subResourceId = null,
            startsAt = Clock.System.now(),
            capacity = 6,
            rate = Rate("WD", "Weekday", 6500, "USD"),
        )
        val reservation = Reservation(
            id = ReservationId("resv-1"),
            slotId = slot.id,
            position = 0,
            partySize = 2,
            holderName = holder,
            isGuest = false,
            duration = DurationCode.EIGHTEEN,
            rateCode = "WD",
            priceMinorUnits = 6500,
            paidMinorUnits = 0,
            bookedOnline = false,
            hasCart = false,
            status = ReservationStatus.CONFIRMED,
            version = 1,
        )
        return buildGrid(listOf(slot), listOf(reservation))
    }
}