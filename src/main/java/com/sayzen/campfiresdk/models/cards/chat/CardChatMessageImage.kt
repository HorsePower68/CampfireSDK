package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class CardChatMessageImage(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_image, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationChatMessage

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelRemoved: TextView = view.findViewById(R.id.vImageRemoved)
        val vCommentText: TextView = view.findViewById(R.id.vCommentText)


        vCommentText.visibility = if (publication.text.isEmpty()) GONE else VISIBLE

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener(null)

        vLabelRemoved.tag = publication
        vLabelRemoved.visibility = GONE
        ImageLoader.loadGif(publication.resourceId, publication.gifId, publication.imageW, publication.imageH, vImage, vGifProgressBar, 1.7f) {
            if (vLabelRemoved.tag == publication && publication.dateCreate < ControllerApi.currentTime() - 1000L * 60 * 60 * 24) vLabelRemoved.visibility = VISIBLE
        }
        vImage.setOnClickListener { Navigator.to(SImageView(if (publication.gifId == 0L) publication.resourceId else publication.gifId)) }

    }


}