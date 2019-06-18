package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.TextView

import com.dzen.campfire.api.models.units.post.PageImages
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewImagesContainer
import com.sup.dev.android.views.views.ViewTextLinkable

class CardPageImages(
        unit: UnitPost?,
        page: PageImages
) : CardPage(unit, page) {

    override fun getLayout() = R.layout.card_page_images

    override fun bindView(view: View) {
        super.bindView(view)
        val vTextEmpty: TextView = view.findViewById(R.id.vTextEmpty)
        val vTitle: ViewTextLinkable = view.findViewById(R.id.vTitle)
        val vImagesSwipe: ViewImagesContainer = view.findViewById(R.id.vImagesSwipe)
        vTitle.setTextIsSelectable(clickable)

        vTitle.text = (page as PageImages).title
        vTitle.visibility = if ((page as PageImages).title.isEmpty()) View.GONE else View.VISIBLE
        vTextEmpty.visibility = if ((page as PageImages).imagesIds.isEmpty()) View.VISIBLE else View.GONE
        vImagesSwipe.clear()
        for (i in 0 until (page as PageImages).imagesIds.size) {
            vImagesSwipe.add((page as PageImages).imagesMiniIds[i], (page as PageImages).imagesIds[i], (page as PageImages).imagesMiniSizesW[i],(page as PageImages).imagesMiniSizesH[i])
        }

        ControllerApi.makeLinkable(vTitle)
    }

    override fun notifyItem() {
        for (i in 0 until (page as PageImages).imagesIds.size) {
            ToolsImagesLoader.load((page as PageImages).imagesIds[i]).intoCash()
        }
    }
}
