package com.sayzen.campfiresdk.screens.stickers

import com.dzen.campfire.api.models.units.stickers.UnitStricker
import com.dzen.campfire.api.requests.stickers.RStickersGetAll
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SStickersView(
        val packId: Long,
        val stickerId: Long,
        val packName: String,
        val packAvatar: Long
) : SLoadingRecycler<CardSticker, UnitStricker>() {

    companion object {

        fun instanceBySticker(stickerId: Long, action: NavigationAction) {
            instance(0, stickerId, action)
        }

        fun instance(packId: Long, action: NavigationAction) {
            instance(packId, 0, action)
        }

        fun instance(packId: Long, stickerId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RStickersPacksGetInfo(packId, stickerId)) { r ->
                SStickersView(r.packId, stickerId, r.packName, r.packAvatarId)
            }
        }

    }
    init {
        setTitle(packName)
        setTextEmpty(R.string.stickers_pack_view_empty)
        setBackgroundImage(R.drawable.bg_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardSticker, UnitStricker> {
        return RecyclerCardAdapterLoading<CardSticker, UnitStricker>(CardSticker::class) { CardSticker(it) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RStickersGetAll(packId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.stickers) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}