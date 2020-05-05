package com.futurelinegen.maptracer.relam

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class LocationModel(
    @PrimaryKey var mapId : Long = 0,
    var latitude: Double = 0.0,
    var longtitude: Double = 0.0,
    var date: Long = 0,
    var title: String = ""
): RealmObject() {

}