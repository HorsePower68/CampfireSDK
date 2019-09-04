package com.sayzen.campfiresdk.screens.fandoms.forums.view

import android.view.View
import com.dzen.campfire.api.models.units.UnitForum
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XComments
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable

class CardInfo(
        private val unit: UnitForum
) : Card(R.layout.screen_forum_card_info) {

    private val xFandom = XFandom(unit, unit.dateCreate) { updateFandom() }
    private val xAccount = XAccount(unit, unit.dateCreate) { updateAccount() }
    private val xKarma = XKarma(unit) { updateKarma() }
    private val xComment = XComments(unit) { updateComments() }

    init {
        xFandom.showLanguage = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)

        vText.text = unit.text

        updateFandom()
        updateAccount()
        updateKarma()
        updateComments()
    }

    private fun updateFandom(){
        if(getView() == null)return
        xFandom.setView(getView()!!.findViewById<ViewAvatar>(R.id.vFandom))
    }

    private fun updateAccount(){
        if(getView() == null)return
        xAccount.setView(getView()!!.findViewById<ViewAvatarTitle>(R.id.vAvatar))
    }

    private fun updateKarma(){
        if(getView() == null)return
        xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    private fun updateComments(){
        if(getView() == null)return
        xComment.setView(getView()!!.findViewById(R.id.vComments))
    }


}