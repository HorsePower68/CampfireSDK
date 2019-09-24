package com.sayzen.campfiresdk.models.cards.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.UnitComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsHTML

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
        val vLabelImageAnswer: TextView = view.findViewById(R.id.vLabelImageAnswer)

        val myName = ControllerApi.account.name
        if (unit.text.startsWith(myName)) vLabelImageAnswer.text = ToolsHTML.font_color(myName, "#ff6d00")
        else vLabelImageAnswer.text = unit.text
        ToolsView.makeTextHtml(vLabelImageAnswer)

        vLabelImageAnswer.visibility = if (unit.text.isEmpty()) View.GONE else View.VISIBLE
        vLabelImageAnswer.setOnClickListener {
            if (onGoTo != null) {
                if (unit.quoteId > 0) onGoTo!!.invoke(unit.quoteId)
                else if (unit.parentUnitId > 0) onGoTo!!.invoke(unit.parentUnitId)
            }
        }

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener { SStickersView.instanceBySticker(unit.stickerId, Navigator.TO) }

        ToolsImagesLoader.loadGif(unit.stickerImageId, unit.stickerGifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f)
    }
}