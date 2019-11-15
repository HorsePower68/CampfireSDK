package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R

class CardChatMessageText(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_text, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    override fun bindView(view: View) {
        super.bindView(view)

    }


}