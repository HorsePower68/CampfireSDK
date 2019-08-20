package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.units.post.PageCampfireObject
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardPageCampfireObject(
        unit: UnitPost?,
        page: PageCampfireObject
) : CardPage(R.layout.card_page_campfire_object, unit, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageCampfireObject

        val vAvatar:ViewAvatarTitle = view.findViewById(R.id.vAvatarTitle)

        vAvatar.setTitle("Title [${page.type}]")
        vAvatar.setSubtitle(page.link)
    }

    override fun notifyItem() {

    }

}
