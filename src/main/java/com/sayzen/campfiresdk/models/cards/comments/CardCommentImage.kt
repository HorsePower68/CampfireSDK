package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class CardCommentImage(
        unit: UnitComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(if (miniSize) R.layout.card_comment_image_mini else R.layout.card_comment_image, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    override fun bind(view: View) {
        val unit = xUnit.unit as UnitComment

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vCommentText: TextView = view.findViewById(R.id.vCommentText)

        vCommentText.visibility = if (unit.text.isEmpty()) View.GONE else View.VISIBLE

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener { Navigator.to(SImageView(if (unit.gifId == 0L) unit.imageId else unit.gifId)) }

        ToolsImagesLoader.loadGif(unit.imageId, unit.gifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f)
    }
}