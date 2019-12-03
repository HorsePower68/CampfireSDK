package com.sayzen.campfiresdk.screens.activities.user_activities.relay_race

import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.activities.RActivitiesGetRelayRaceFullInfo
import com.dzen.campfire.api.requests.activities.RActivitiesGetPosts
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.activities.user_activities.CardUserActivity
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SRelayRaceInfo(
        val userActivity: UserActivity,
        val postsCount:Long,
        val waitMembersCount:Long,
        val rejectedMembersCount:Long
) : SLoadingRecycler<CardPublication, Publication>() {

    companion object {

        fun instance(userActivityId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RActivitiesGetRelayRaceFullInfo(userActivityId)
            ) { r -> SRelayRaceInfo(r.userActivity, r.postsCount, r.waitMembersCount, r.rejectedMembersCount) }
        }

    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(userActivity.name)
        setTextEmpty("")
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        val adapterX =  RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RActivitiesGetPosts(userActivity.id, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.posts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }

        adapterX.add(CardUserActivity(userActivity))
        adapterX.add(CardRelayRaceButtons( userActivity.id, postsCount, waitMembersCount, rejectedMembersCount))

        return adapterX
    }

}
