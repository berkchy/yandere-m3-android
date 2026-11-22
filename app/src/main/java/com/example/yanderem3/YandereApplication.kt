package com.example.yanderem3

import android.app.Application
import com.example.yanderem3.util.AppContainer

class YandereApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
