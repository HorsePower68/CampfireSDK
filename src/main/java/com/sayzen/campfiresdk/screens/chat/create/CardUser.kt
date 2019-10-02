package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardUser(
        val accountId:Long,
        val accountName:String,
        val accountImageId:Long,
        val removable:Boolean
) : Card(R.layout.screen_chat_create_card_user){

    private val xAccount = XAccount(accountId, accountName, accountImageId){update()}

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRemove: View = view.findViewById(R.id.vRemove)

        xAccount.setView(vAvatar)
        vAvatar.isClickable = false
        vAvatar.vAvatar.isClickable = false
        vRemove.visibility = if(removable) View.VISIBLE else View.GONE
        vRemove.setOnClickListener {
            adapter?.remove(this)
        }
    }

}