package com.sayzen.campfiresdk.models.cards.chat

import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.screens.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsDate

class CardChatMessageSticker(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardChatMessage(R.layout.card_chat_message_sticker, unit, onClick, onChange, onQuote, onGoTo, onBlocked) {

    init {
        useMessageContainerBackground = false
        changeEnabled = false
        quoteEnabled = false
        copyEnabled = false
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)
        val vLabelImage: TextView = view.findViewById(R.id.vLabelImage)
        val vLabel: TextView = view.findViewById(R.id.vLabel)

        vLabelImage.text = vLabel.text

        vLabel.visibility = if (unit.text.isEmpty()) GONE else VISIBLE
        vLabelImage.visibility = if (unit.text.isEmpty()) VISIBLE else GONE

        ToolsView.setOnLongClickCoordinates(vImage) { view1, x, y -> popup?.asSheetShow() }

        vImage.setOnClickListener(null)

        ToolsImagesLoader.loadGif(unit.stickerImageId, unit.stickerGifId, unit.imageW, unit.imageH, vImage, vGifProgressBar, 1.7f)
        vImage.setOnClickListener { SStickersView.instanceBySticker(unit.stickerId, Navigator.TO) }

        if (ControllerApi.isCurrentAccount(unit.creatorId)) {
            (vLabelImage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.BOTTOM
            vLabelImage.text = ToolsDate.dateToString(unit.dateCreate)
        } else {
            (vLabelImage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.BOTTOM
            vLabelImage.text = xAccount.name + "  " + ToolsDate.dateToString(unit.dateCreate)
        }


    }


}