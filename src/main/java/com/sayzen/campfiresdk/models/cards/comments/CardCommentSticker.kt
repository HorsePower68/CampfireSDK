package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView

class CardCommentSticker(
        unit: UnitComment,
        dividers: Boolean,
        miniSize: Boolean,
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment( if (miniSize) R.layout.card_comment_sticker_mini else R.layout.card_comment_sticker, unit, dividers, miniSize, onClick, onQuote, onGoTo) {

    init {
        changeEnabled = false
        copyEnabled = false
    }

    override fun bind(view: View) {
        val unit = xUnit.unit as UnitComment

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener { SStickersView.instanceBySticker(unit.stickerId, Navigator.TO) }

        ToolsImagesLoader.loadGif(unit.stickerImageId, unit.stickerGifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f)
    }
}