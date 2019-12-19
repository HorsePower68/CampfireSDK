package com.sayzen.campfiresdk.screens.account.followers

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsGetAll
import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SFollows(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardAccount, Account>() {

    private val eventBus = EventBus.subscribe(EventAccountsFollowsChange::class) {
        if (!it.isFollow && adapter != null)
            for (c in adapter!!.get(CardAccount::class)) if (c.xAccount.accountId == it.accountId) adapter?.remove(c)
    }

    private val xAccount = XAccount(accountId, accountName) { update() }

    init {
        addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_search_24dp)) { Navigator.to(SAccountSearch { SProfile.instance(it.name, Navigator.TO) }) }
        setTextEmpty(if (ControllerApi.isCurrentAccount(xAccount.accountId)) R.string.profile_follows_empty else R.string.profile_follows_empty_another)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
        update()
    }

    private fun update() {
        setTitle(ToolsResources.s(R.string.app_follows) + if (ControllerApi.isCurrentAccount(xAccount.accountId)) "" else " " + xAccount.name)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { ac -> CardAccount(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RAccountsFollowsGetAll(xAccount.accountId, cards.size.toLong(), false)
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
