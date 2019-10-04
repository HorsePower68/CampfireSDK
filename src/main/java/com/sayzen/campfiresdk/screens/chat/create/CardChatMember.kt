package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatMember
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardChatMember(
        val chatMember : ChatMember,
        val myLvl:Long
) : Card(R.layout.screen_chat_create_card_user){

    private val xAccount = XAccount(chatMember.accountId, chatMember.accountName, chatMember.accountImageId){update()}

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRemove: View = view.findViewById(R.id.vRemove)

        xAccount.setView(vAvatar)
        vAvatar.isClickable = false
        vAvatar.vAvatar.isClickable = false

        vRemove.visibility = if(chatMember.accountId != ControllerApi.account.id && (myLvl == API.CHAT_MEMBER_LVL_ADMIN
                || (myLvl == API.CHAT_MEMBER_LVL_MODERATOR && chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER)
                || (chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER && chatMember.memberOwner == ControllerApi.account.id))
        ) View.VISIBLE else View.GONE

        vAvatar.setSubtitle("")
        if(chatMember.memberStatus == API.CHAT_MEMBER_STATUS_LEAVE) vAvatar.setSubtitle(ToolsResources.sCap(R.string.chat_member_label_leave, ToolsResources.sex(chatMember.accountSex, R.string.he_leave, R.string.she_leave)))

        vRemove.setOnClickListener {
            adapter?.remove(this)
        }
    }

}