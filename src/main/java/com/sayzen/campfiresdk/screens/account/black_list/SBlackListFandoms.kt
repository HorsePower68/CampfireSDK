package com.sayzen.campfiresdk.screens.account.black_list

import com.dzen.campfire.api.models.Fandom
import com.dzen.campfire.api.requests.accounts.RAccountsGetIgnoredFandoms
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllById
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SBlackListFandoms(
        val accountId: Long,
        val accountName: String,
        val feedIgnoreFandoms: Array<Long>
) : SLoadingRecycler<CardFandom, Fandom>() {

    companion object {

        fun instance(accountId: Long, accountName: String, action: NavigationAction) {
            if (accountId == ControllerApi.account.id)
                Navigator.action(action, SBlackListFandoms(accountId, accountName, ControllerSettings.feedIgnoreFandoms))
            else
                ApiRequestsSupporter.executeInterstitial(action, RAccountsGetIgnoredFandoms(accountId)) { r -> SBlackListFandoms(accountId, accountName, r.fandomsIds) }
        }

    }

    val eventBud = EventBus
            .subscribe(EventFandomBlackListChange::class) {
                if (!it.inBlackList && adapter != null)
                    for (c in adapter!!.get(CardFandom::class)) if (c.fandom.id == it.fandomId) adapter?.remove(c)
            }

    private var loaded = false

    init {
        setTitle(R.string.settings_black_list_fandoms)
        setTextEmpty(R.string.settings_black_list_empty)
        setBackgroundImage(R.drawable.bg_22)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardFandom, Fandom> {
        return RecyclerCardAdapterLoading<CardFandom, Fandom>(CardFandom::class) { fandom ->
            val c = CardFandom(fandom)
            if (accountId == ControllerApi.account.id) c.onClick = { ControllerCampfireSDK.removeFromBlackListFandom(fandom.id) }
            c.setShowLanguage(false).setSubscribed(false)
            c
        }
                .setBottomLoader { onLoad, _ ->
                    if (loaded) {
                        onLoad.invoke(emptyArray())
                    } else {
                        subscription = RFandomsGetAllById(feedIgnoreFandoms)
                                .onComplete { r ->
                                    loaded = true
                                    onLoad.invoke(r.fandoms)
                                }
                                .onNetworkError { onLoad.invoke(null) }
                                .send(api)
                    }
                }
    }

}
