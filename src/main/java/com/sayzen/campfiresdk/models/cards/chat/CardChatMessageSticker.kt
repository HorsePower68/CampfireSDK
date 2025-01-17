package com.sayzen.campfiresdk.models.cards.chat

import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsHTML

class CardChatMessageSticker(
        publication: PublicationChatMessage,
        onClick: ((PublicationChatMessage) -> Boolean)? = null,
        onChange: ((PublicationChatMessage) -> Unit)? = null,
        onQuote: ((PublicationChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_sticker, publication, onClick, onChange, onQuote, onGoTo, onBlocked) {

    init {
        useMessageContainerBackground = false
        changeEnabled = false
        copyEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationChatMessage

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelImageAnswer: TextView = view.findViewById(R.id.vLabelImageAnswer)

        val myName = ControllerApi.account.name
        if (publication.text.startsWith(myName)) vLabelImageAnswer.text =ToolsHTML.font_color(myName, "#ff6d00")
        else vLabelImageAnswer.text = publication.text
        ToolsView.makeTextHtml(vLabelImageAnswer)

        vLabelImageAnswer.visibility = if (publication.text.isEmpty()) GONE else VISIBLE
        vLabelImageAnswer.setOnClickListener {
            if (onGoTo != null) {
                if (publication.quoteId > 0) onGoTo!!.invoke(publication.quoteId)
                else if (publication.parentPublicationId > 0) onGoTo!!.invoke(publication.parentPublicationId)
            }
        }


        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener(null)

        ImageLoader.loadGif(publication.stickerImageId, publication.stickerGifId, publication.imageW, publication.imageH, vImage, vGifProgressBar, 1.7f)
        vImage.setOnClickListener { SStickersView.instanceBySticker(publication.stickerId, Navigator.TO) }

        if (ControllerApi.isCurrentAccount(publication.creatorId)) {
            (vLabelImageAnswer.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.TOP
        } else {
            (vLabelImageAnswer.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.TOP
        }

    }


}