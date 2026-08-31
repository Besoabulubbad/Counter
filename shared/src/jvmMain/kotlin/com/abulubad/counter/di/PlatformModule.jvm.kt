package com.abulubad.counter.di

import com.abulubad.counter.data.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory() }
}