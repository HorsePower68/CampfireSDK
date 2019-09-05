package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.wiki.EventWikiChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

class CardWikiItem(
        var wikiItem: WikiTitle
) : Card(R.layout.screen_wiki_card_item), NotifyItem {

    private val eventBus = EventBus
            .subscribe(EventWikiRemove::class) { if (it.item.itemId == wikiItem.itemId) adapter?.remove(this) }
            .subscribe(EventWikiChanged::class) { if (it.item.itemId == wikiItem.itemId) wikiItem = it.item; update(); }

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vName: TextView = view.findViewById(R.id.vName)
        val vSectionIcon: ImageView = view.findViewById(R.id.vSectionIcon)

        ToolsImagesLoader.loadGif(wikiItem.imageId, 0, 0, 0, vImage)
        vName.text = wikiItem.getName(ControllerApi.getLanguageCode())
        vSectionIcon.visibility = if (wikiItem.type == API.WIKI_TYPE_SECION) View.VISIBLE else View.GONE

        view.setOnClickListener {
            if (wikiItem.type == API.WIKI_TYPE_SECION) Navigator.to(SWikiList(wikiItem.fandomId, wikiItem.itemId, wikiItem.name))
            else Navigator.to(SWikiArticlerView(wikiItem, ControllerApi.getLanguageId()))
        }

        view.setOnLongClickListener {
            WidgetMenu()
                    .add(R.string.app_change) { w, i -> Navigator.to(SWikiItemCreate(wikiItem.fandomId, wikiItem.parentItemId, wikiItem)) }
                    .add(R.string.app_remove) { w, i -> removeWikiItem() }.condition(ControllerApi.can(wikiItem.fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT))
                    .asSheetShow()
            true
        }
    }

    private fun removeWikiItem() {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.stickers_remove_confirm, R.string.app_remove, RWikiRemove(wikiItem.itemId)) { r ->
            EventBus.post(EventWikiRemove(wikiItem))
            ToolsToast.show(R.string.app_done)
        }
    }

    override fun notifyItem() {
        ToolsImagesLoader.load(wikiItem.imageId).intoCash()
    }

}