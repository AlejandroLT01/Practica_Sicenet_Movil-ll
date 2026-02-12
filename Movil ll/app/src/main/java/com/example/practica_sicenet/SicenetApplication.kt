package com.example.practica_sicenet

import android.app.Application
import com.example.practica_sicenet.data.AppContainer
import com.example.practica_sicenet.data.DefaultAppContainer

class SicenetApplication : Application() {
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}