package com.sayzen.campfiresdk.models.cards

import android.view.View
import com.dzen.campfire.api.models.units.Unit
import com.sayzen.campfiresdk.R

class CardUnitUnknown(
        override val unit: Unit
) : CardUnit(R.layout.card_unit_unknown, unit) {


    override fun bindView(view: View) {
        super.bindView(view)

    }

    override fun notifyItem() {

    }

}
