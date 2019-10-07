package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class CardChatMessageImage(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_image, unit, onClick, onChange, onQuote, onGoTo, onBlocked) {

    override fun bindView(view: View) {
        super.bindView(view)
        val unit = xUnit.unit as UnitChatMessage

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelRemoved: TextView = view.findViewById(R.id.vImageRemoved)
        val vCommentText: TextView = view.findViewById(R.id.vCommentText)


        vCommentText.visibility = if (unit.text.isEmpty()) GONE else VISIBLE

        ToolsView.setOnLongClickCoordinates(vImage) { _, _, _ -> showMenu() }

        vImage.setOnClickListener(null)

        vLabelRemoved.tag = unit
        vLabelRemoved.visibility = GONE
        ToolsImagesLoader.loadGif(unit.resourceId, unit.gifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f) {
            if (vLabelRemoved.tag == unit && unit.dateCreate < ControllerApi.currentTime() - 1000L * 60 * 60 * 24) vLabelRemoved.visibility = VISIBLE
        }
        vImage.setOnClickListener { Navigator.to(SImageView(if (unit.gifId == 0L) unit.resourceId else unit.gifId)) }

    }


}