package com.abulubad.counter.di

import com.abulubad.counter.data.CounterRepository
import com.abulubad.counter.data.createCounterDatabase
import com.abulubad.counter.session.Ticket
import com.abulubad.counter.sync.FakeBackend
import com.abulubad.counter.sync.SyncEngine
import com.abulubad.counter.ui.grid.GridViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

val dataModule = module {
    single { createCounterDatabase(get()) }
    single { CounterRepository(get()) }
    single { FakeBackend() }
    single { SyncEngine(get(), get()) }
    single { Ticket() }
}

val uiModule = module {
    single { GridViewModel(get(), get(), get()) }
}

private var started = false

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (started) return
    started = true
    startKoin {
        appDeclaration()
        modules(platformModule(), dataModule, uiModule)
    }
}