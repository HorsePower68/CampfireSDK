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
        val publicationId: Long,
        var karmaCount: Long,
        var myKarma: Long,
        val creatorId: Long,
        val publicationStatus: Long,
        var onChanged: () -> Unit
) {

    var anon =  ControllerSettings.anonRates

    private val eventBus = EventBus
            .subscribe(EventPublicationKarmaStateChanged::class) { if (it.publicationId == publicationId) onChanged.invoke() }
            .subscribe(EventPublicationKarmaAdd::class) {
                if (it.publicationId == publicationId) {
                    myKarma = it.myKarma
                    karmaCount += it.myKarma
                    onChanged.invoke()
                }
            }

    constructor(publication: Publication, onChanged: () -> Unit) : this(publication.id, publication.karmaCount, publication.myKarma, publication.creatorId, publication.status, onChanged) {}

    fun setView(view: ViewKarma) {
        view.update(publicationId, karmaCount, myKarma, creatorId, publicationStatus,
                { b -> rate(b) },
                { Navigator.to(SPublicationRates(publicationId, karmaCount, myKarma, creatorId, publicationStatus)) }
        )
    }

    fun rate(up: Boolean) {
        if (ControllerKarma.getStartTime(publicationId) > 0) ControllerKarma.stop(publicationId)
        else ControllerKarma.rate(publicationId, up, anon)
    }


}
