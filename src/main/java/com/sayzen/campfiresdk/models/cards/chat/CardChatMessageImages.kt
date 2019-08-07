package com.sayzen.campfiresdk.models.cards.chat

import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.TextView
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesContainer
import com.sup.dev.android.views.views.ViewImagesSwipe
import com.sup.dev.java.tools.ToolsDate

class CardChatMessageImages(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?,
        onBlocked: ((UnitChatMessage) -> Unit)? = null
) : CardChatMessage(unit, onClick, onChange, onQuote, onGoTo, onBlocked) {


    override fun getLayout() = R.layout.card_chat_message_images

    override fun bindView(view: View) {
        super.bindView(view)

        val vImages: ViewImagesContainer = view.findViewById(R.id.vImages)
        val vLabelImage: TextView = view.findViewById(R.id.vLabelImage)
        val vLabel: TextView = view.findViewById(R.id.vLabel)

        vLabelImage.text = vLabel.text

        vLabel.visibility = if (unit.text.isEmpty()) GONE else VISIBLE
        vLabelImage.visibility = if (unit.text.isEmpty()) VISIBLE else GONE

        ToolsView.setOnLongClickCoordinates(vImages) { view1, x, y -> popup?.asSheetShow() }
        vImages.setOnClickListener { Navigator.to(SImageView(unit.resourceId))  }
        vImages.clear()


        if (ControllerApi.isCurrentAccount(unit.creatorId)) {
            (vLabelImage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.BOTTOM
            vLabelImage.text = ToolsDate.dateToString(unit.dateCreate)
        } else {
            (vLabelImage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.BOTTOM
            vLabelImage.text = xAccount.name + "  " + ToolsDate.dateToString(unit.dateCreate)
        }

        for (i in 0 until unit.imageIdArray.size) {
            vImages.add(unit.imageIdArray[i], unit.imageIdArray[i], unit.imageWArray[i], unit.imageHArray[i],
                    null,
                    { id -> popup?.asSheetShow() }
            )
        }
    }


}