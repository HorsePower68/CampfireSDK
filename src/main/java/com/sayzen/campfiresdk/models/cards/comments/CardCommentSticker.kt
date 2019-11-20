package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsHTML

class CardCommentSticker(
        publication: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(
        if (miniSize) R.layout.card_comment_sticker_mini else if (dividers) R.layout.card_comment_sticker else R.layout.card_comment_sticker_card,
        publication, dividers, miniSize, onClick, onQuote, onGoTo) {

    init {
        changeEnabled = false
        copyEnabled = false
    }

    override fun bind(view: View) {
        val publication = xPublication.publication as PublicationComment

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelImageAnswer: TextView = view.findViewById(R.id.vLabelImageAnswer)

        val myName = ControllerApi.account.name
        if (publication.text.startsWith(myName)) vLabelImageAnswer.text = ToolsHTML.font_color(myName, "#ff6d00")
        else vLabelImageAnswer.text = publication.text
        ToolsView.makeTextHtml(vLabelImageAnswer)

        vLabelImageAnswer.visibility = if (publication.text.isEmpty()) View.GONE else View.VISIBLE
        vLabelImageAnswer.setOnClickListener {
            if (onGoTo != null) {
                if (publication.quoteId > 0) onGoTo!!.invoke(publication.quoteId)
                else if (publication.parentPublicationId > 0) onGoTo!!.invoke(publication.parentPublicationId)
            }
        }

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener { SStickersView.instanceBySticker(publication.stickerId, Navigator.TO) }

        ToolsImagesLoader.loadGif(publication.stickerImageId, publication.stickerGifId, publication.imageW, publication.imageH, vImage, vGifProgressBar, 1.7f)
    }
}