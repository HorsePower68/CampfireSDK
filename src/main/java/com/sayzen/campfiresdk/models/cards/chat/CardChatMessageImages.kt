package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesContainer

class CardChatMessageImages(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage( R.layout.card_chat_message_images, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationChatMessage

        val vImages: ViewImagesContainer = view.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { _, _, _ -> showMenu() }
        vImages.setOnClickListener { Navigator.to(SImageView(publication.resourceId))  }
        vImages.clear()

        for (i in 0 until publication.imageIdArray.size) {
            vImages.add(publication.imageIdArray[i], publication.imageIdArray[i], publication.imageWArray[i], publication.imageHArray[i],
                    null,
                    { showMenu() }
            )
        }
    }


}