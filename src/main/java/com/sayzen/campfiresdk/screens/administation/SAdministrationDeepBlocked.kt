package com.sayzen.campfiresdk.screens.administation

import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsGetAllDeepBlocked
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SAdministrationDeepBlocked(val accountId: Long) : SLoadingRecycler<CardPublication, Publication>() {

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle("")
        setTextEmpty("Нет заблокированных публикаций")
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler, true, false, true, true) }
                .setBottomLoader { onLoad, cards ->
                    RPublicationsGetAllDeepBlocked(accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.publications) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
