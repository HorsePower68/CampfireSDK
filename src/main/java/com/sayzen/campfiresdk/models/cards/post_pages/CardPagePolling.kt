package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XPolling
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPolling
import com.sayzen.campfiresdk.models.events.publications.EventPollingChanged
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable

import com.sup.dev.java.libs.eventBus.EventBus

class CardPagePolling(
        pagesContainer: PagesContainer?,
        page: PagePolling
) : CardPage(R.layout.card_page_polling, pagesContainer, page) {

    private val xPolling = XPolling(page, pagesContainer, {editMode}, {postIsDraft}) { update() }

    override fun bindView(view: View) {
        super.bindView(view)
        xPolling.setView(view)
    }

    override fun notifyItem() {

    }
}