package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageLink
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewTextLinkable

class CardPageLink(
        pagesContainer: PagesContainer?,
        page: PageLink
) : CardPage(R.layout.card_page_link, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)


        val vName: ViewTextLinkable = view.findViewById(R.id.vName)
        val vLink: ViewTextLinkable = view.findViewById(R.id.vLink)
        val vTouch: View = view.findViewById(R.id.vTouch)

        ControllerApi.makeLinkable(vName)

        vTouch.visibility = if (clickable) View.VISIBLE else View.GONE
        vTouch.setOnClickListener { ControllerCampfireSDK.openLink((page as PageLink).link) }
        vTouch.setOnLongClickListener {
            ToolsAndroid.setToClipboard((page as PageLink).link)
            ToolsToast.show(R.string.app_copied)
            true
        }

        vLink.text = (page as PageLink).link
        vName.text = (page as PageLink).name

        ControllerApi.makeLinkable(vLink)
    }


    override fun notifyItem() {}
}