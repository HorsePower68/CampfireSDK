package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.project.EventApiVersionChanged
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardUpdate : Card(R.layout.screen_fandom_card_update) {

    private val eventBus = EventBus.subscribe(EventApiVersionChanged::class) { update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vContainer:View = view.findViewById(R.id.vContainer)
        val vUpdate: TextView = view.findViewById(R.id.vUpdate)

        vContainer.visibility = if (ControllerApi.isOldVersion()) View.VISIBLE else View.GONE
        vUpdate.setOnClickListener { ToolsIntent.startPlayMarket(SupAndroid.appId) }


    }

}
