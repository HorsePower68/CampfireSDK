package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerWiki
import com.sayzen.campfiresdk.models.events.wiki.EventWikiChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.java.libs.eventBus.EventBus

class CardWikiItem(
        var wikiItem: WikiTitle
) : Card(R.layout.screen_wiki_card_item), NotifyItem {

    private val eventBus = EventBus
            .subscribe(EventWikiRemove::class) { if (it.itemId == wikiItem.itemId) adapter?.remove(this) }
            .subscribe(EventWikiChanged::class) { if (it.item.itemId == wikiItem.itemId) wikiItem = it.item; update(); }

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vName: TextView = view.findViewById(R.id.vName)
        val vSectionIcon: ImageView = view.findViewById(R.id.vSectionIcon)

        ToolsImagesLoader.loadGif(wikiItem.imageId, 0, 0, 0, vImage)
        vName.text = wikiItem.getName(ControllerApi.getLanguageCode())
        vSectionIcon.visibility = if (wikiItem.itemType == API.WIKI_TYPE_SECION) View.VISIBLE else View.GONE

        view.setOnClickListener {
            if (wikiItem.itemType == API.WIKI_TYPE_SECION) Navigator.to(SWikiList(wikiItem.fandomId, wikiItem.itemId, wikiItem.getName(ControllerApi.getLanguageCode())))
            else Navigator.to(SWikiArticleView(wikiItem, ControllerApi.getLanguageId()))
        }

        view.setOnLongClickListener {
            ControllerWiki.showMenu(wikiItem, ControllerApi.getLanguageId())
            true
        }
    }


    override fun notifyItem() {
        ToolsImagesLoader.load(wikiItem.imageId).intoCash()
    }

}