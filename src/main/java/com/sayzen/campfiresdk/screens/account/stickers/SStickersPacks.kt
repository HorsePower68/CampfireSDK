package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetAllByAccount
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCollectionChanged
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SStickersPacks(
        val accountId: Long
) : SLoadingRecycler<CardStickersPack, UnitStickersPack>() {

    val eventBus = EventBus
            .subscribe(EventStickersPackCreate::class) { reload() }
            .subscribe(EventStickersPackCollectionChanged::class) { reload() }
    var loaded = false

    init {
        setTitle(R.string.app_stickers)
        setTextEmpty(R.string.stickers_packs_empty)
        setBackgroundImage(R.drawable.bg_4)
        if (accountId == ControllerApi.account.id) {
            if(ControllerApi.can(API.LVL_CREATE_STICKERS)) {
                addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_add_24dp)) {
                    Navigator.to(SStickersPackCreate(null))
                }
            }
            (vFab as View).visibility = View.VISIBLE
            vFab.setImageResource(R.drawable.ic_search_white_24dp)
            vFab.setOnClickListener {
                Navigator.to(SStickersPacksSearch())
            }
        }
    }

    override fun reload() {
        loaded = false
        super.reload()
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack> {
        val adapter = RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack>(CardStickersPack::class) { CardStickersPack(it) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RStickersPacksGetAllByAccount(accountId, if(cards.isEmpty()) Long.MAX_VALUE else cards.get(cards.size-1).xUnit.unit.dateCreate)
                            .onComplete { r ->
                                if (loaded) {
                                    onLoad.invoke(emptyArray())
                                } else {
                                    loaded = true
                                    onLoad.invoke(r.stickersPacks)
                                }
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }

        adapter.add(CardFavorites(accountId))

        return adapter
    }

}
