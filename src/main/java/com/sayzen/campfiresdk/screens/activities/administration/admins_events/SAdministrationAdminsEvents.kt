package com.sayzen.campfiresdk.screens.activities.administration.admins_events

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.events_admins.PublicationEventAdmin
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.events.CardPublicationEventAdmin
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SAdministrationAdminsEvents : SLoadingRecycler<CardPublicationEventAdmin, Publication>() {

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(R.string.administration_admins_events)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublicationEventAdmin, Publication> {
        return RecyclerCardAdapterLoading<CardPublicationEventAdmin, Publication>(CardPublicationEventAdmin::class) { publication -> CardPublicationEventAdmin(publication as PublicationEventAdmin) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsGetAll()
                            .setOffset(cards.size)
                            .setPublicationTypes(API.PUBLICATION_TYPE_EVENT_ADMIN)
                            .setOrder(RUnitsGetAll.ORDER_NEW)
                            .setIncludeZeroLanguages(true)
                            .setIncludeMultilingual(true)
                            .onComplete { rr -> onLoad.invoke(rr.publications) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
                .setRetryMessage(R.string.error_network, R.string.app_retry)
                .setEmptyMessage(R.string.app_empty)
                .setNotifyCount(5)
    }


}
