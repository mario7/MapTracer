package com.futurelinegen.maptracer

import android.app.Application
import io.realm.Realm

class MapTracerApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}