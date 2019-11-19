package com.sayzen.campfiresdk.screens.activities.administration.reports

import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.account.AccountReports
import com.dzen.campfire.api.requests.accounts.RAccountsReportsGetAll
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SAdministrationUserReports(
) : SLoadingRecycler<CardUserReport, AccountReports>() {

    init {
        setBackgroundImage(R.drawable.bg_15)
        setTitle(R.string.administration_user_reports)
        setTextEmpty(R.string.administration_user_reports_empty)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUserReport, AccountReports> {
        return RecyclerCardAdapterLoading<CardUserReport, AccountReports>(CardUserReport::class) { CardUserReport(it) }
                .setBottomLoader { onLoad, cards ->
                    RAccountsReportsGetAll(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.accounts) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}