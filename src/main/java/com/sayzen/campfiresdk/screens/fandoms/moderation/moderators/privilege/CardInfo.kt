package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.privilege

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R

import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.views.cards.Card

class CardInfo(
        val fandomId: Long, 
        val languageId: Long
) : Card(R.layout.screen_fandom_moderators_card_info) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vForce = view.findViewById<TextView>(R.id.vTextForce)

        vForce.text = "${ControllerApi.getKarmaCount(fandomId, languageId) / 100}"
    }
}
