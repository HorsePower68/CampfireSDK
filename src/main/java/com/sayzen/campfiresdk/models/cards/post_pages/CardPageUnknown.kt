package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.units.post.Page

import com.dzen.campfire.api.models.units.post.PageText
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.views.views.ViewTextLinkable

class CardPageUnknown(
        unit: UnitPost?,
        page: Page
) : CardPage(R.layout.card_page_unknown, unit, page) {


    override fun bindView(view: View) {
        super.bindView(view)
    }

    override fun notifyItem() {}
}
