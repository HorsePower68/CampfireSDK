package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesContainer

class CardCommentImages(
        publication: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(
        if (miniSize) R.layout.card_comment_images_mini else if (dividers) R.layout.card_comment_images else R.layout.card_comment_images_card,
        publication, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {
        val publication = xPublication.publication as PublicationComment

        val vImages: ViewImagesContainer = view.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { _, _, _ -> showMenu() }
        vImages.setOnClickListener { Navigator.to(SImageView(publication.imageId)) }
        vImages.clear()

        for (i in publication.imageIdArray.indices) {
            vImages.add(publication.imageIdArray[i], publication.imageIdArray[i], publication.imageWArray[i], publication.imageHArray[i],
                    null,
                    { showMenu() }
            )
        }
    }
}