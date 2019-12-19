package com.sayzen.campfiresdk.screens.account.stickers

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersSearch
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SStickersPacksSearch() : SLoadingRecycler<CardStickersPack, PublicationStickersPack>() {

    init {
        setTitle(R.string.app_search)
        setTextEmpty(R.string.stickers_packs_empty)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardStickersPack, PublicationStickersPack> {
        return RecyclerCardAdapterLoading<CardStickersPack, PublicationStickersPack>(CardStickersPack::class) { CardStickersPack(it) }
                .setBottomLoader { onLoad, cards ->

                    subscription = RStickersSearch(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.stickersPacks) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
