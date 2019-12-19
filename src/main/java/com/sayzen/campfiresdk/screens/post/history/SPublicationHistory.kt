package com.sayzen.campfiresdk.screens.post.history

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.publications.history.HistoryPublication
import com.dzen.campfire.api.requests.publications.RPublicationsHistoryGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.history.CardHistoryUnit
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SPublicationHistory(
        val publicationId: Long
) : SLoadingRecycler<CardHistoryUnit, HistoryPublication>() {

    init {
        setTitle(R.string.app_history)
        setTextEmpty(R.string.app_empty)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardHistoryUnit, HistoryPublication> {
        return RecyclerCardAdapterLoading<CardHistoryUnit, HistoryPublication>(CardHistoryUnit::class) { CardHistoryUnit(it) }
                .setBottomLoader { onLoad, cards ->
                    RPublicationsHistoryGetAll(publicationId, cards.size.toLong())
                            .onComplete { r ->
                                onLoad.invoke(r.history)
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
