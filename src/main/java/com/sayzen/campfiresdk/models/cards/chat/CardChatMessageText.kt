package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R

class CardChatMessageText(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardChatMessage(unit, onClick, onChange, onQuote, onGoTo) {

    override fun getLayout() = R.layout.card_chat_message_text


    override fun bindView(view: View) {
        super.bindView(view)

    }


}