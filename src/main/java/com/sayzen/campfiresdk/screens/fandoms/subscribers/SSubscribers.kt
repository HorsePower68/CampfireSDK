package com.sayzen.campfiresdk.screens.fandoms.subscribers

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribersGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SSubscribers private constructor(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardAccount, Account>() {

    companion object {

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            Navigator.action(action, SSubscribers(fandomId, languageId))
        }

    }

    init {
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_13)
        setTitle(R.string.app_subscribers)
        setTextEmpty(R.string.fandom_subscribers_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { account -> CardAccount(account) }
                .setBottomLoader { onLoad, cards ->
                    RFandomsSubscribersGetAll(cards.size.toLong(), fandomId, languageId)
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}

