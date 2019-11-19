package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.Page

import com.sayzen.campfiresdk.R

class CardPageUnknown(
        pagesContainer: PagesContainer?,
        page: Page
) : CardPage(R.layout.card_page_unknown, pagesContainer, page) {


    override fun bindView(view: View) {
        super.bindView(view)
    }

    override fun notifyItem() {}
}
