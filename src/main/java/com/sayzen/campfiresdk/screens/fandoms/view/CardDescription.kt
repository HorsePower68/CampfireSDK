package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationDescriptionChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.fandom.EventFandomDescriptionChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomNamesChanged
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardDescription(
        private val xFandom: XFandom,
        private var description: String,
        var names: Array<String>
) : Card(R.layout.screen_fandom_card_description) {

    private val eventBus = EventBus
            .subscribe(EventFandomDescriptionChanged::class) { this.onEventFandomDescriptionChanged(it) }
            .subscribe(EventFandomNamesChanged::class) { this.onEventFandomNamesChanged(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vDescription: TextView = view.findViewById(R.id.vDescription)
        val vNames: TextView = view.findViewById(R.id.vNames)

        vDescription.text = if (description.isEmpty()) ToolsResources.s(R.string.fandom_info_description_empty) else description
        if (ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_DESCRIPTION)) vDescription.setOnClickListener { changeDescription() }

        var namesS = if (names.isNotEmpty()) names[0] else ""
        for (i in 1 until names.size) namesS += ", ${names[i]}"
        vNames.text = if (namesS.isEmpty()) ToolsResources.s(R.string.fandom_info_names_empty) else namesS
        if (ControllerApi.can(xFandom.fandomId, xFandom.languageId, API.LVL_MODERATOR_NAMES)) vNames.setOnClickListener { WidgetNames(xFandom.fandomId, xFandom.languageId, names).asSheetShow() }
    }


    private fun changeDescription() {
        WidgetDescription(description) { description, comment ->
            ApiRequestsSupporter.executeProgressDialog(RFandomsModerationDescriptionChange(xFandom.fandomId, xFandom.languageId, description, comment)) { _ ->
                EventBus.post(EventFandomDescriptionChanged(xFandom.fandomId, xFandom.languageId, description))
                ToolsToast.show(R.string.app_done)
            }
        }.asSheetShow()
    }

    //
    //  EventBus
    //

    private fun onEventFandomDescriptionChanged(e: EventFandomDescriptionChanged) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId) {
            this.description = e.description
            update()
        }
    }
    private fun onEventFandomNamesChanged(e: EventFandomNamesChanged) {
        if (e.fandomId == xFandom.fandomId && e.languageId == xFandom.languageId) {
            this.names = e.names
            update()
        }
    }


}
