package com.sayzen.campfiresdk.screens.account.karma

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.fandoms.KarmaInFandom
import com.dzen.campfire.api.requests.accounts.RAccountsKarmaInFandomsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler

class ScreenAccountKarma(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardKarma, KarmaInFandom>() {

    private val xAccount = XAccount(accountId, accountName){ update() }

    init {
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_9)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) R.string.profile_karma_empty else R.string.profile_karma_empty_another)
        update()
    }

    private fun update(){
        setTitle(ToolsResources.s(R.string.app_karma) + if (ControllerApi.isCurrentAccount(xAccount.accountId)) "" else " " + xAccount.name)

    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardKarma, KarmaInFandom> {
        return RecyclerCardAdapterLoading<CardKarma, KarmaInFandom>(CardKarma::class) { ac -> CardKarma(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RAccountsKarmaInFandomsGetAll(xAccount.accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.karma) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
