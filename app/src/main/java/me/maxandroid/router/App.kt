package me.maxandroid.router

import android.app.Application
import me.maxandroid.router_runtime.Router

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Router.init()
    }
}