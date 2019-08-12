package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import com.dzen.campfire.api.models.units.stickers.UnitStricker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.java.libs.eventBus.EventBus

class CardSticker(
        override val unit: UnitStricker
) : CardUnit(unit) {

    private val eventBus = EventBus

    private val xKarma = XKarma(unit) { update() }

    override fun getLayout() = R.layout.card_sticker

    override fun bindView(view: View) {
        super.bindView(view)

    }

    override fun notifyItem() {

    }


}
