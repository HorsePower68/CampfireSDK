package com.sayzen.campfiresdk.screens.activities.support

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardSupportUser(
        val account: Account,
        val count:Long
) : Card(R.layout.screen_support_card_user){

    private val xAccount = XAccount(account, 0L, 0L, 0L) {update()}

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vCounter:TextView = view.findViewById(R.id.vCounter)

        xAccount.setView(vAvatar)
        vCounter.text = "$count"
    }


}