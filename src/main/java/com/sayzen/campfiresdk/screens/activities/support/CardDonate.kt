package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.project.Donate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsText

class CardDonate(
        val donate: Donate
) : Card(R.layout.screen_donates_card){

    private val xAccount = XAccount(donate.account, 0L, 0L, 0L) {update()}

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vCounter:TextView = view.findViewById(R.id.vCounter)

        xAccount.setView(vAvatar)
        vAvatar.setSubtitle(donate.comment)
        ControllerLinks.makeLinkable(vAvatar.vSubtitle)
        vCounter.text = "${ToolsText.numToStringRoundAndTrim(donate.sum/100.0, 2)} \u20BD"
    }


}