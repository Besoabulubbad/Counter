package com.abulubad.counter

import android.app.Application
import com.abulubad.counter.di.initKoinAndroid

class CounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}