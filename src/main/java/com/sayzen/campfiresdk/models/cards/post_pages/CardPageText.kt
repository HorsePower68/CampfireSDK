package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.dzen.campfire.api.models.PagesContainer

import com.dzen.campfire.api.models.units.post.PageText
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable

class CardPageText(
        pagesContainer: PagesContainer?,
        page: PageText
) : CardPage(R.layout.card_page_text, pagesContainer, page) {

    var maxLines = Integer.MAX_VALUE

    override fun bindView(view: View) {
        super.bindView(view)
        val page = page as PageText
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vTextIcon: ImageView = view.findViewById(R.id.vTextIcon)
        vText.setTextIsSelectable(clickable)
        vText.maxLines = maxLines;

        vText.text = page.text
        vText.textSize = (if (page.size == PageText.SIZE_0) 14 else 21).toFloat()
        ControllerApi.makeLinkable(vText)

        if(page.icon > 0 && page.icon < CampfireConstants.TEXT_ICONS.size){
            vTextIcon.setImageDrawable(ToolsResources.getDrawableAttr(CampfireConstants.TEXT_ICONS[page.icon]))
            vTextIcon.visibility = View.VISIBLE
            (vText.layoutParams as LinearLayout.LayoutParams).leftMargin = ToolsView.dpToPx(4).toInt()
        }else{
            vTextIcon.setImageDrawable(null)
            vTextIcon.visibility = View.GONE
            (vText.layoutParams as LinearLayout.LayoutParams).leftMargin = ToolsView.dpToPx(0).toInt()
        }
    }

    override fun notifyItem() {}
}
