package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerChats
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewTextLinkable

class CardChatMessageSystem(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_moderation, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    init {
        quoteEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationChatMessage

        val vSystemMessage: ViewTextLinkable = view.findViewById(R.id.vSystemMessage)
        val vTouchModeration: ViewGroup = view.findViewById(R.id.vTouchModeration)

        vTouchModeration.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(publication.blockModerationEventId, 0, Navigator.TO) }
        vTouchModeration.isClickable = publication.blockModerationEventId != 0L

        vSystemMessage.visibility = View.VISIBLE
        vSystemMessage.setTextColor(ToolsResources.getColorAttr(R.attr.toolbar_content_color_secondary))

        if (publication.systemType == PublicationChatMessage.SYSTEM_TYPE_BLOCK) vSystemMessage.setTextColor(ToolsResources.getColor(R.color.red_600))


        vSystemMessage.text = ControllerChats.getSystemText(publication)

        ControllerLinks.makeLinkable(vSystemMessage)
    }

}
