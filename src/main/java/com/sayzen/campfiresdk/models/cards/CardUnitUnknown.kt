package com.sayzen.campfiresdk.models.cards

import android.view.View
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R

class CardUnitUnknown(
        unit: Publication
) : CardUnit(R.layout.card_unit_unknown, unit) {

    override fun bindView(view: View) {
        super.bindView(view)
    }

    override fun notifyItem() {

    }

    override fun updateComments() {
    }

    override fun updateFandom() {
    }

    override fun updateAccount() {
    }

    override fun updateKarma() {
    }

    override fun updateReports() {

    }

}
