package com.sayzen.campfiresdk.screens.account.stickers

import com.dzen.campfire.api.models.units.stickers.UnitStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersSearch
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SStickersPacksSearch() : SLoadingRecycler<CardStickersPack, UnitStickersPack>() {

    init {
        setTitle(R.string.app_search)
        setTextEmpty(R.string.stickers_packs_empty)
        setBackgroundImage(R.drawable.bg_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack> {
        return RecyclerCardAdapterLoading<CardStickersPack, UnitStickersPack>(CardStickersPack::class) { CardStickersPack(it) }
                .setBottomLoader { onLoad, cards ->

                    subscription = RStickersSearch(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.stickersPacks) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
