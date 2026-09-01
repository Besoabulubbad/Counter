package com.abulubad.counter.domain

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.jvm.JvmInline
import kotlin.time.Clock

@JvmInline value class ResourceId(val value: String)
@JvmInline value class SubResourceId(val value: String)
@JvmInline value class SlotId(val value: String)
@JvmInline value class ReservationId(val value: String)

@OptIn(ExperimentalAtomicApi::class)
private val reservationSeq = AtomicLong(0)

@OptIn(ExperimentalAtomicApi::class)
fun newReservationId(): ReservationId =
    ReservationId("r-${Clock.System.now().toEpochMilliseconds()}-${reservationSeq.addAndFetch(1L)}")