package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import com.dzen.campfire.api.models.wiki.WikiItem
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SWikiList(
        val fandomId:Long,
        val languageId:Long,
        val itemId: Long
) : SLoadingRecycler<CardWikiItem, WikiItem>() {

    init {
        setTitle(R.string.app_wiki)
        setTextEmpty(R.string.wiki_list_empty)
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { Navigator.to(SWikiItemCreate(fandomId, languageId, itemId)) }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardWikiItem, WikiItem> {
        return RecyclerCardAdapterLoading<CardWikiItem, WikiItem>(CardWikiItem::class) { CardWikiItem(it) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RWikiListGet(fandomId, languageId, itemId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.items) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
