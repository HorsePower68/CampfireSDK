package com.sayzen.campfiresdk.screens.activities.user_activities.relay_race

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.activities.RActivitiesRelayGetMembers
import com.dzen.campfire.api.requests.activities.RActivitiesRelayGetRejected
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SRelayRaceInfoRejected(
        val userActivityId: Long
) : SLoadingRecycler<CardAccount, Account>() {

    init {
        setTitle(R.string.activities_relay_race_rejected)
        setTextEmpty(R.string.app_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { ac -> CardAccount(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RActivitiesRelayGetRejected(userActivityId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}
