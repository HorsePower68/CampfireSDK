package com.sayzen.campfiresdk.screens.administation

import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.requests.post.RPostGetAllByTag
import com.dzen.campfire.api.requests.units.RUnitsGetAllDeepBlocked
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace

class SAdministrationDeepBlocked(val accountId: Long) : SLoadingRecycler<CardUnit, Unit>() {

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle("")
        setTextEmpty("Нет заблокированных публикаций")
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUnit, Unit> {
        return RecyclerCardAdapterLoading<CardUnit, Unit>(CardUnit::class) { unit -> CardUnit.instance(unit, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsGetAllDeepBlocked(accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
