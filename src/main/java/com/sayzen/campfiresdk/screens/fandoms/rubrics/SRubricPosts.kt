package com.sayzen.campfiresdk.screens.fandoms.rubrics

import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.requests.post.RPostGetAllByRubric
import com.dzen.campfire.api.requests.rubrics.RRubricGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace

class SRubricPosts(
        val rubricId: Long,
        val rubricName: String
) : SLoadingRecycler<CardUnit, Unit>() {

    companion object{

        fun instance(rubricId:Long, action: NavigationAction){
            ApiRequestsSupporter.executeInterstitial(action, RRubricGet(rubricId)) { r ->
                SRubricPosts(rubricId, r.rubric.name)
            }
        }

    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(rubricName)
        setTextEmpty(R.string.rubric_posts_empty)
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUnit, Unit> {
        return RecyclerCardAdapterLoading<CardUnit, Unit>(CardUnit::class) { unit -> CardUnit.instance(unit, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RPostGetAllByRubric(rubricId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
