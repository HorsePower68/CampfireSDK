package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import com.dzen.campfire.api.models.PublicationComment
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesContainer

class CardCommentImages(
        unit: PublicationComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((PublicationComment) -> Boolean)? = null,
        onQuote: ((PublicationComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(if (miniSize) R.layout.card_comment_images_mini else R.layout.card_comment_images, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {
        val unit = xUnit.unit as PublicationComment

        val vImages: ViewImagesContainer = view.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { _, _, _ -> showMenu() }
        vImages.setOnClickListener { Navigator.to(SImageView(unit.imageId)) }
        vImages.clear()

        for (i in unit.imageIdArray.indices) {
            vImages.add(unit.imageIdArray[i], unit.imageIdArray[i], unit.imageWArray[i], unit.imageHArray[i],
                    null,
                    { showMenu() }
            )
        }
    }
}