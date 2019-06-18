package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.units.post.PageImage
import com.dzen.campfire.api.models.units.post.UnitPost
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.screens.SImageView

class CardPageImage(
        unit: UnitPost?,
        page: PageImage
) : CardPage(unit, page) {

    override fun getChangeMenuItemText() = R.string.app_crop

    override fun getLayout() = R.layout.card_page_image

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageImage
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)

        if (clickable) vImage.setOnClickListener { onImageClicked() }
        else vImage.setOnClickListener(null)

        vImage.isFocusable = false
        vImage.isClickable = clickable
        vImage.isFocusableInTouchMode = false

        ToolsImagesLoader.loadGif(page.imageId, page.gifId, page.w, page.h, vImage, vGifProgressBar)
    }

    private fun onImageClicked() {

        if(unit != null) {
            val list = ArrayList<Long>()
            var index = 0

            for (p in unit.pages)
                if (p is PageImage) {
                    if (p == page) index = list.size
                    list.add(p.getMainImageId())
                }

            Navigator.to(SImageView(index, list.toTypedArray()))
        } else {
            Navigator.to(SImageView((page as PageImage).getMainImageId()))
        }
    }

    override fun notifyItem() {
        ToolsImagesLoader.load((page as PageImage).imageId).size((page as PageImage).w, (page as PageImage).h).intoCash()
    }

}
