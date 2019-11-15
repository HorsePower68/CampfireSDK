package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesContainer

class CardChatMessageImages(
        unit: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage( R.layout.card_chat_message_images, unit, onClick, onChange, onQuote, onGoTo, onBlocked) {

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xPublication.publication as PublicationChatMessage

        val vImages: ViewImagesContainer = view.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { _, _, _ -> showMenu() }
        vImages.setOnClickListener { Navigator.to(SImageView(unit.resourceId))  }
        vImages.clear()

        for (i in 0 until unit.imageIdArray.size) {
            vImages.add(unit.imageIdArray[i], unit.imageIdArray[i], unit.imageWArray[i], unit.imageHArray[i],
                    null,
                    { showMenu() }
            )
        }
    }


}