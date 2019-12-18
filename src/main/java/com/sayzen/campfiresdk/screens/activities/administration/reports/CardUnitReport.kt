package com.sayzen.campfiresdk.screens.activities.administration.reports

import android.view.View
import android.widget.Button
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardUnitReport(
        val cardPublication: CardPublication
) : Card(R.layout.screen_administration_unit_reports_card) {

    private val eventBus = EventBus
            .subscribe(EventPublicationRemove::class) { if (cardPublication.xPublication.publication.id == it.publicationId) remove() }
            .subscribe(EventPublicationReportsClear::class) { if (cardPublication.xPublication.publication.id == it.publicationId) remove() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vBlock: Button = view.findViewById(R.id.vBlock)
        val vClear: Button = view.findViewById(R.id.vClear)

        vBlock.setOnClickListener {
            ControllerPublications.block(cardPublication.xPublication.publication){
                remove()
                cardPublication.remove()
            }
        }

        vClear.setOnClickListener {
            remove()
            cardPublication.remove()
            ControllerApi.clearReportsPublicationNow(cardPublication.xPublication.publication.id)
        }

    }

}