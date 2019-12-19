package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class CardCommentImage(
        publication: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(
        if (miniSize) R.layout.card_comment_image_mini else if (dividers) R.layout.card_comment_image else R.layout.card_comment_image_card,
        publication, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {
        val publication = xPublication.publication as PublicationComment

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vCommentText: TextView = view.findViewById(R.id.vCommentText)

        vCommentText.visibility = if (publication.text.isEmpty()) View.GONE else View.VISIBLE

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener { Navigator.to(SImageView(if (publication.gifId == 0L) publication.imageId else publication.gifId)) }

        ImageLoader.loadGif(publication.imageId, publication.gifId, publication.imageW, publication.imageH, vImage, vGifProgressBar, 1.7f)
    }
}