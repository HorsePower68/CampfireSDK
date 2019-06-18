package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.units.post.PageVideo
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerYoutube
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewIcon

class CardPageVideo(
        unit: UnitPost?,
        page: PageVideo
) : CardPage(unit, page) {

    override fun getLayout() = R.layout.card_page_video

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageVideo
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vPlay: ViewIcon = view.findViewById(R.id.vPlay)

        if (clickable) vImage.setOnClickListener { ControllerYoutube.play(page.videoId) }
        else vImage.setOnClickListener(null)
        vPlay.setOnClickListener { ControllerYoutube.play(page.videoId) }

        vImage.isFocusable = false
        vImage.isClickable = clickable
        vImage.isFocusableInTouchMode = false

        ToolsImagesLoader.load(page.imageId).size(page.w, page.h).into(vImage)
    }

    override fun notifyItem() {
        val page = this.page as PageVideo
        ToolsImagesLoader.load(page.imageId).size(page.w, page.h).intoCash()
    }

}
