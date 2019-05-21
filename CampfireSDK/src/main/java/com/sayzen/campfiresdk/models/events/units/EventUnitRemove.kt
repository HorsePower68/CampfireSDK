package com.sayzen.campfiresdk.models.events.units

class EventUnitRemove {

    val parentUnitId: Long
    val unitId: Long

    constructor(unitId: Long) {
        this.unitId = unitId
        this.parentUnitId = 0
    }

    constructor(unitId: Long, parentUnitId: Long) {
        this.unitId = unitId
        this.parentUnitId = parentUnitId
    }


}