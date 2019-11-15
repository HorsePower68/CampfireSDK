package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.reports

import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.units.RUnitsReportedGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace
import com.sup.dev.java.libs.eventBus.EventBus

class SReports(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardPublication, Publication>() {

    private val eventBus = EventBus.subscribe(EventPublicationReportsClear::class) {
        if (adapter != null) for (c in adapter!!.get(CardPublication::class)) if (c.xPublication.publication.id == it.publicationId) adapter?.remove(c)
    }

    init {
        setBackgroundImage(R.drawable.bg_15)
        setTitle(R.string.moderation_screen_reports)
        setTextEmpty(R.string.moderation_screen_reports_empty)

        vRecycler.addItemDecoration(DecoratorVerticalSpace())
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { CardPublication.instance(it, null, false, false) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsReportedGetAll(fandomId, arrayOf(languageId), cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}