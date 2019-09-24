package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.wiki.EventWikiCreated
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiList(
        val fandomId: Long,
        val itemId: Long,
        val itemName: String
) : SLoadingRecycler<CardWikiItem, WikiTitle>() {

    companion object {

        fun instanceFandomId(fandomId: Long, action: NavigationAction) {
            Navigator.action(action, SWikiList(fandomId, 0, ""))
        }

        fun instanceItemId(wikiItemId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RWikiItemGet(wikiItemId)) { r ->
                SWikiList(r.wikiTitle.fandomId, r.wikiTitle.itemId, r.wikiTitle.getName(ControllerApi.getLanguageCode()))
            }
        }


    }

    private val eventBus = EventBus.subscribe(EventWikiCreated::class) { if (it.item.fandomId == fandomId && it.item.parentItemId == itemId) adapter?.reloadBottom() }

    init {

        addToolbarIcon(ToolsResources.getDrawableAttr(R.attr.ic_insert_link_24dp)!!) {
            if (itemId > 0) ToolsAndroid.setToClipboard(ControllerApi.linkToWikiItemId(itemId))
            else ToolsAndroid.setToClipboard(ControllerApi.linkToWikiFandomId(fandomId))
            ToolsToast.show(R.string.app_copied)
        }

        if (itemName.isEmpty()) setTitle(R.string.app_wiki) else setTitle(itemName)
        setTextEmpty(R.string.wiki_list_empty)
        (vFab as View).visibility = if (ControllerApi.can(fandomId, ControllerApi.getLanguage("en").id, API.LVL_MODERATOR_WIKI_EDIT)) View.VISIBLE else View.GONE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { Navigator.to(SWikiItemCreate(fandomId, itemId)) }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardWikiItem, WikiTitle> {
        return RecyclerCardAdapterLoading<CardWikiItem, WikiTitle>(CardWikiItem::class) { CardWikiItem(it) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RWikiListGet(fandomId, itemId, cards.size.toLong())
                            .onComplete { r ->
                                onLoad.invoke(r.items)
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
