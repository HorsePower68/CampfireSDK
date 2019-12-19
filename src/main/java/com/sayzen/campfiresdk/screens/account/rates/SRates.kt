package com.sayzen.campfiresdk.screens.account.rates

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.accounts.RAccountsRatesGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.screens.rates.CardRate
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler

class SRates(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardRate, Rate>() {

    private val xAccount = XAccount(accountId, accountName){ update() }

    init {
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_21)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) R.string.profile_rates_empty else R.string.profile_rates_empty_another)
        update()
    }

    private fun update(){
        setTitle(ToolsResources.s(R.string.app_rates) + if (ControllerApi.isCurrentAccount(xAccount.accountId)) "" else " " + xAccount.name)

    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardRate, Rate> {
        return RecyclerCardAdapterLoading<CardRate, Rate>(CardRate::class) { ac -> CardRate(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RAccountsRatesGetAll(xAccount.accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.rates) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
