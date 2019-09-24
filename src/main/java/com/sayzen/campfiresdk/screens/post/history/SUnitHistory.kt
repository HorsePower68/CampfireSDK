package com.sayzen.campfiresdk.screens.post.history

import com.dzen.campfire.api.models.units.history.HistoryUnit
import com.dzen.campfire.api.requests.units.RUnitsHistoryGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.history.CardHistoryUnit
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SUnitHistory(
        val unitId: Long
) : SLoadingRecycler<CardHistoryUnit, HistoryUnit>() {

    init {
        setTitle(R.string.app_history)
        setTextEmpty(R.string.app_empty)
        setBackgroundImage(R.drawable.bg_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardHistoryUnit, HistoryUnit> {
        return RecyclerCardAdapterLoading<CardHistoryUnit, HistoryUnit>(CardHistoryUnit::class) { CardHistoryUnit(it) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsHistoryGetAll(unitId, cards.size.toLong())
                            .onComplete { r ->
                                onLoad.invoke(r.history)
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
