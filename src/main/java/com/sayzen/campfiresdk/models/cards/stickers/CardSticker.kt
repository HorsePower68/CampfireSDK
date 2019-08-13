package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.units.stickers.UnitSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.java.libs.eventBus.EventBus

class CardSticker(
        override val unit: UnitSticker
) : CardUnit(unit) {

    private val eventBus = EventBus

    private val xKarma = XKarma(unit) { update() }

    override fun getLayout() = R.layout.card_sticker

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage:ImageView = view.findViewById(R.id.vImage)
        val vProgress:View = view.findViewById(R.id.vProgress)

        ToolsImagesLoader.loadGif(unit.imageId, unit.gifId, 0, 0, vImage, vProgress)
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(unit.imageId).intoCash()
    }


}
