package com.sayzen.campfiresdk.adapters

import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.controllers.ControllerKarma
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationKarmaStateChanged
import com.sayzen.campfiresdk.screens.rates.SPublicationRates
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
            .subscribe(EventPublicationKarmaStateChanged::class) { if (it.publicationId == unitId) onChanged.invoke() }
            .subscribe(EventPublicationKarmaAdd::class) {
                if (it.publicationId == unitId) {
                    myKarma = it.myKarma
                    karmaCount += it.myKarma
                    onChanged.invoke()
                }
            }

    constructor(unit: Publication, onChanged: () -> kotlin.Unit) : this(unit.id, unit.karmaCount, unit.myKarma, unit.creatorId, unit.status, onChanged) {}

    fun setView(view: ViewKarma) {
        view.update(unitId, karmaCount, myKarma, creatorId, unitStatus,
                { b -> rate(b) },
                { Navigator.to(SPublicationRates(unitId, karmaCount, myKarma, creatorId, unitStatus)) }
        )
    }

    fun rate(up: Boolean) {
        if (ControllerKarma.getStartTime(unitId) > 0) ControllerKarma.stop(unitId)
        else ControllerKarma.rate(unitId, up, anon)
    }


}
