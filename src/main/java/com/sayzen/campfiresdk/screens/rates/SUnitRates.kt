package com.sayzen.campfiresdk.screens.rates

import com.dzen.campfire.api.models.Rate
import com.dzen.campfire.api.requests.post.RPostRatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SUnitRates(
        val unitId: Long
) : SLoadingRecycler<CardRate, Rate>() {

    init {
        setTitle(R.string.app_rates)
        setTextEmpty(R.string.post_rates_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardRate, Rate> {
        return RecyclerCardAdapterLoading<CardRate, Rate>(CardRate::class) { ac -> CardRate(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RPostRatesGetAll(unitId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.rates) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
