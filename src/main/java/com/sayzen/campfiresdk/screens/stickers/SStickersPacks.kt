package com.sayzen.campfiresdk.screens.stickers

import android.view.View
import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SStickersPacks(
        val accountId:Long
) : SLoadingRecycler<CardStickersPack, UnitStickersPack>() {

    val eventBus = EventBus.subscribe(EventStickersPackCreate::class){ adapter?.reloadBottom() }

    init {
        setTitle(R.string.app_stickers)
        setTextEmpty(R.string.stickers_packs_empty)
        setBackgroundImage(R.drawable.bg_4)

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener{
            Navigator.to(SStickersPackCreate(null))
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack> {
        return RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack>(CardStickersPack::class) { CardStickersPack(it) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RStickersPacksGetAll(accountId, if(cards.isEmpty()) 0 else cards.last().unit.dateCreate)
                            .onComplete { r -> onLoad.invoke(r.stickersPacks) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
