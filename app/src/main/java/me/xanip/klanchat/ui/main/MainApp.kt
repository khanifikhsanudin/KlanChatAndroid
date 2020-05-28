package me.xanip.klanchat.ui.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.multidex.MultiDexApplication

class MainApp: MultiDexApplication(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isInBackground = true
        isInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isInForeground = true
        isInBackground = false
    }

    companion object {
        lateinit var instance: MainApp
        var isInBackground: Boolean? = null
        var isInForeground: Boolean? = null
    }
}