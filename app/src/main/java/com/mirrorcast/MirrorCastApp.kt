package com.mirrorcast

import android.app.Application
import com.mirrorcast.di.AppContainer
import com.mirrorcast.di.DefaultAppContainer

class MirrorCastApp : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
