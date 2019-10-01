package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card

class CardCreateTitle : Card(R.layout.screen_chat_create_card_title) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vUsers: TextView = view.findViewById(R.id.vUsers)
        vUsers.text = ToolsResources.s(R.string.app_users) + ":"
    }

}