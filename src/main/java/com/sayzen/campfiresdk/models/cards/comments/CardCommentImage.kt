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
        onClick: ((UnitComment) -> Boolean)? = null,
        onQuote: ((UnitComment) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardComment(unit, dividers, onClick, onQuote, onGoTo) {

    override fun getLayout() = R.layout.card_comment_image

    override fun bind(view: View) {

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelImage: TextView = view.findViewById(R.id.vLabelImage)
        val vLabel: TextView = view.findViewById(R.id.vLabel)
        val vCommentText: TextView = view.findViewById(R.id.vCommentText)

        vLabelImage.text = vLabel.text

        vLabel.visibility = if(unit.text.isEmpty()) View.GONE else View.VISIBLE
        vCommentText.visibility = if(unit.text.isEmpty()) View.GONE else View.VISIBLE
        vLabelImage.visibility = if(unit.text.isEmpty()) View.VISIBLE else View.GONE


        ToolsView.setOnLongClickCoordinates(vImage) { view1, x, y -> popup?.asSheetShow() }

        vImage.setOnClickListener { Navigator.to(SImageView(if (unit.gifId == 0L) unit.imageId else unit.gifId)) }

        ToolsImagesLoader.loadGif(unit.imageId, unit.gifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f)
    }
}