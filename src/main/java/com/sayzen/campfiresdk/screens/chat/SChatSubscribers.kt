package com.sayzen.campfiresdk.screens.chat

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.chat.RChatGetSubscribers
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SChatSubscribers(
        val fandomId:Long,
        val languageId:Long,
        chatName: String
) : SLoadingRecycler<CardAccount, Account>() {

    init {
        setTitle(ToolsResources.s(R.string.app_chat) + " " + chatName)
        setTextEmpty(R.string.app_empty)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardAccount, Account> {
        return RecyclerCardAdapterLoading<CardAccount, Account>(CardAccount::class) { ac -> CardAccount(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RChatGetSubscribers(fandomId, languageId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
