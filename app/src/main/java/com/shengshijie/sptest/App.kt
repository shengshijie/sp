package com.shengshijie.sptest

import android.app.Application

class App: Application() {

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    companion object {
        @get:Synchronized
        var instance: App? = null
            private set
    }

}