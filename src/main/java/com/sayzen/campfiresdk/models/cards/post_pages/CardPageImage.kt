package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.units.post.PageImage
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.screens.SImageView

class CardPageImage(page: PageImage) : CardPage(page) {

    override fun getChangeMenuItemText(): Int {
        return R.string.app_crop
    }

    override fun getLayout(): Int {
        return R.layout.card_page_image
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageImage
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)

        if (clickable) vImage.setOnClickListener { Navigator.to(SImageView(if (page.gifId == 0L) page.imageId else page.gifId)) }
        else vImage.setOnClickListener(null)

        vImage.isFocusable = false
        vImage.isClickable = clickable
        vImage.isFocusableInTouchMode = false

        ToolsImagesLoader.loadGif(page.imageId, page.gifId, page.w, page.h, vImage, vGifProgressBar)
    }

    override fun notifyItem() {
        ToolsImagesLoader.load((page as PageImage).imageId).size((page as PageImage).w, (page as PageImage).h).intoCash()
    }

}
