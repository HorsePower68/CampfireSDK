package com.sayzen.campfiresdk.screens.punishments

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPunishments(
        accountId: Long,
        accountName: String
) : SLoadingRecycler<CardPunishment, AccountPunishment>() {

    private val xAccount = XAccount(accountId, accountName){ update() }

    init {
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_8)
        setTextEmpty(if (ControllerApi.isCurrentAccount(accountId)) R.string.profile_punishments_empty else R.string.profile_punishments_empty_another)
        update()
    }

    private fun update(){
        setTitle(ToolsResources.s(R.string.app_punishments) + if (ControllerApi.isCurrentAccount(xAccount.accountId)) "" else " " + xAccount.name)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPunishment, AccountPunishment> {
        return RecyclerCardAdapterLoading<CardPunishment, AccountPunishment>(CardPunishment::class) { ac -> CardPunishment(ac) }
                .setBottomLoader { onLoad, cards ->
                    subscription = RAccountsPunishmentsGetAll(xAccount.accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.punishments) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
