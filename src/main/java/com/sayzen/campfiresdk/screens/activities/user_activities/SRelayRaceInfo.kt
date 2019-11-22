package com.sayzen.campfiresdk.screens.activities.user_activities

import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.activities.RActivitiesGet
import com.dzen.campfire.api.requests.activities.RActivitiesGetPosts
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SRelayRaceInfo(
        val userActivity: UserActivity
) : SLoadingRecycler<CardPublication, Publication>() {

    companion object {

        fun instance(userActivityId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RActivitiesGet(userActivityId)
            ) { r -> SRelayRaceInfo(r.userActivity) }
        }

    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(userActivity.name)
        setTextEmpty(R.string.app_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RActivitiesGetPosts(userActivity.id, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.posts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
