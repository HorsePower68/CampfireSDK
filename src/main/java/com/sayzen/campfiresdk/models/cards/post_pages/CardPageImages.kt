package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageImages
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.views.ViewImagesContainer
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.debug.Debug

class CardPageImages(
        pagesContainer: PagesContainer?,
        page: PageImages
) : CardPage(R.layout.card_page_images, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vTextEmpty: TextView = view.findViewById(R.id.vTextEmpty)
        val vTitle: ViewTextLinkable = view.findViewById(R.id.vTitle)
        val vImagesContainer: ViewImagesContainer = view.findViewById(R.id.vImagesContainer)
        vTitle.setTextIsSelectable(clickable)

        vTitle.text = (page as PageImages).title
        vTitle.visibility = if ((page as PageImages).title.isEmpty()) View.GONE else View.VISIBLE
        vTextEmpty.visibility = if ((page as PageImages).imagesIds.isEmpty()) View.VISIBLE else View.GONE
        vImagesContainer.clear()
        for (i in 0 until (page as PageImages).imagesIds.size) {
            vImagesContainer.add(
                    (page as PageImages).imagesMiniIds[i],
                    (page as PageImages).imagesIds[i],
                    (page as PageImages).imagesMiniSizesW[i],
                    (page as PageImages).imagesMiniSizesH[i], {
                onImageClicked(vImagesContainer, it.a2)
            })
        }

        ControllerLinks.makeLinkable(vTitle)
    }

    private fun onImageClicked(vImagesContainer: ViewImagesContainer, fullId: Long) {
        if (pagesContainer != null) {
            ControllerPost.toImagesScreen(pagesContainer, fullId)
        } else {
            vImagesContainer.toImageView(fullId)
        }
    }


    override fun onDetachView() {
        if (getView() == null) return
        val vImagesSwipe: ViewImagesContainer = getView()!!.findViewById(R.id.vImagesContainer)
        vImagesSwipe.clear()
    }

    override fun notifyItem() {
        for (i in 0 until (page as PageImages).imagesIds.size) {
            ToolsImagesLoader.load((page as PageImages).imagesIds[i]).intoCash()
        }
    }
}
