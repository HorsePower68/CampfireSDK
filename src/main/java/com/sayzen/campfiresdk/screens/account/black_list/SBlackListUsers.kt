package com.sayzen.campfiresdk.screens.account.black_list

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SBlackListUsers(
        val accountId:Long,
        val accountName: String
) : SLoadingRecycler<CardAccount, Account>() {

    val eventBud = EventBus
            .subscribe(EventAccountAddToBlackList::class) { reload() }
            .subscribe(EventAccountRemoveFromBlackList::class) { onEventAccountRemoveFromBlackList(it) }

    init {
        setTitle(R.string.settings_black_list_users)
        setTextEmpty(R.string.settings_black_list_empty)
        setBackgroundImage(R.drawable.bg_22)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { ac ->
            val c = CardAccount(ac)
            if(accountId == ControllerApi.account.id) c.setOnClick { ControllerCampfireSDK.removeFromBlackListUser(ac.id) }
            c
        }
                .setBottomLoader { onLoad, cards ->
                    subscription = RAccountsBlackListGetAll(accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    //
    //  EventBus
    //

    private fun onEventAccountRemoveFromBlackList(e: EventAccountRemoveFromBlackList) {
        if (adapter == null) return
        val cards = adapter!!.get(CardAccount::class)
        for(c in cards) if(c.xAccount.accountId == e.accountId) adapter!!.remove(c)
    }


}
