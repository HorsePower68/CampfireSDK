package com.sayzen.campfiresdk.adapters

import com.dzen.campfire.api.models.units.Unit
import com.sayzen.campfiresdk.controllers.ControllerKarma
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitKarmaStateChanged
import com.sayzen.campfiresdk.screens.rates.SUnitRates
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus

class XKarma(
        val unitId: Long,
        var karmaCount: Long,
        var myKarma: Long,
        val creatorId: Long,
        val unitStatus: Long,
        var onChanged: () -> kotlin.Unit
) {

    var anon =  ControllerSettings.anonRates

    private val eventBus = EventBus
            .subscribe(EventUnitKarmaStateChanged::class) { if (it.unitId == unitId) onChanged.invoke() }
            .subscribe(EventUnitKarmaAdd::class) {
                if (it.unitId == unitId) {
                    myKarma = it.myKarma
                    karmaCount += it.myKarma
                    onChanged.invoke()
                }
            }

    constructor(unit: Unit, onChanged: () -> kotlin.Unit) : this(unit.id, unit.karmaCount, unit.myKarma, unit.creatorId, unit.status, onChanged) {}

    fun setView(view: ViewKarma) {
        view.update(unitId, karmaCount, myKarma, creatorId, unitStatus,
                { b -> rate(b) },
                { Navigator.to(SUnitRates(unitId, karmaCount, myKarma, creatorId, unitStatus)) }
        )
    }

    fun rate(up: Boolean) {
        if (ControllerKarma.getStartTime(unitId) > 0) ControllerKarma.stop(unitId)
        else ControllerKarma.rate(unitId, up, anon)
    }


}
