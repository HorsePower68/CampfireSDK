package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageSpoiler
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.views.ViewTextLinkable
import java.util.ArrayList

class CardPageSpoiler(
        pagesContainer: PagesContainer?,
        page: PageSpoiler
) : CardPage(R.layout.card_page_spoiler, pagesContainer, page) {

    var pages:ArrayList<CardPage>? = null
    var onClick:()->Unit = {}

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vIcon:ImageView = view.findViewById(R.id.vIcon)
        val vTouch:View = view.findViewById(R.id.vTouch)

        ControllerApi.makeLinkable(vText)

        vTouch.visibility = if (clickable) View.VISIBLE else View.GONE
        vTouch.setOnClickListener { onClicked() }

        vIcon.setImageResource(ToolsResources.getDrawableAttrId(if ((page as PageSpoiler).isOpen) R.attr.ic_keyboard_arrow_up_24dp else R.attr.ic_keyboard_arrow_down_24dp))
        vText.text = (page as PageSpoiler).name + " (" + (page as PageSpoiler).count + " " + ToolsResources.getPlural(R.plurals.pages_count, (page as PageSpoiler).count) + ")"

    }


    private fun onClicked() {
        (page as PageSpoiler).isOpen = !(page as PageSpoiler).isOpen
        update()
        if (adapter != null && adapter is RecyclerCardAdapter) ControllerPost.updateSpoilers((adapter as RecyclerCardAdapter?)!!)
        if (pages != null) ControllerPost.updateSpoilers(pages!!)
        onClick.invoke()
    }

    override fun notifyItem() {}


}