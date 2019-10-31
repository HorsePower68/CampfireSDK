package com.sayzen.campfiresdk.screens.account.fandoms

import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllSubscribed
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SAcounFandoms(
        val accountId: Long
) : SLoadingRecycler<CardFandom, Fandom>() {

    init {
        setTitle(R.string.app_fandoms)
        setTextEmpty(R.string.fandoms_empty)
        setBackgroundImage(R.drawable.bg_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardFandom, Fandom> {
        return RecyclerCardAdapterLoading<CardFandom, Fandom>(CardFandom::class) { CardFandom(it) }
                .setBottomLoader { onLoad, cards ->
                    RFandomsGetAllSubscribed(accountId, cards.size.toLong())
                            .onComplete { r ->
                                onLoad.invoke(r.fandoms)
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
