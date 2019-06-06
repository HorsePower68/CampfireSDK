package com.sayzen.campfiresdk.models.cards.chat

import android.view.View
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewImagesSwipe

class CardChatMessageImages(
        unit: UnitChatMessage,
        onClick: ((UnitChatMessage) -> Boolean)? = null,
        onChange: ((UnitChatMessage) -> Unit)? = null,
        onQuote: ((UnitChatMessage) -> Unit)? = null,
        onGoTo: ((Long) -> Unit)?
) : CardChatMessage(unit, onClick, onChange, onQuote, onGoTo) {


    override fun getLayout() = R.layout.card_chat_message_images

    override fun bindView(view: View) {
        super.bindView(view)

        val vImages: ViewImagesSwipe = view.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { view1, x, y -> popup?.asSheetShow() }
        vImages.setOnClickListener { Navigator.to(SImageView(unit.resourceId))  }
        vImages.clear()

        for (i in 0 until unit.imageIdArray.size) {
            vImages.add(unit.imageIdArray[i], unit.imageIdArray[i], unit.imageWArray[i], unit.imageHArray[i],
                    null,
                    { id -> popup?.asSheetShow() }
            )
        }
    }


}