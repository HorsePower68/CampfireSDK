package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewTextLinkable

class CardChatMessageSystem(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_moderation, unit, onClick, onChange, onQuote, onGoTo, onBlocked) {

    init {
        quoteEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitChatMessage

        val vSystemMessage: ViewTextLinkable = view.findViewById(R.id.vSystemMessage)
        val vTouchModeration: ViewGroup = view.findViewById(R.id.vTouchModeration)

        vTouchModeration.setOnClickListener {
            ControllerCampfireSDK.onToModerationClicked(unit.blockModerationEventId, 0, Navigator.TO)
        }

        vSystemMessage.visibility = View.VISIBLE

        if(unit.blockDate > 0) {
            vSystemMessage.text = "${ToolsResources.s(R.string.chat_block_message, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(unit.systemTargetName))} " +
                    "\n ${ToolsResources.s(R.string.app_comment)}: ${unit.systemComment}"
        }else{
            vSystemMessage.text = "${ToolsResources.s(R.string.chat_warn_message, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(unit.systemTargetName))} " +
                    "\n ${ToolsResources.s(R.string.app_comment)}: ${unit.systemComment}"
        }

        ControllerApi.makeLinkable(vSystemMessage)
    }

}
