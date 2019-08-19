package com.sayzen.campfiresdk.screens.fandoms.search


import com.dzen.campfire.api.models.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAllModerated
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading

class SFandomsModeration(
        private val accountId: Long
) : SLoadingRecycler<CardFandom, Fandom>() {


    companion object {

        fun instance(accountId: Long, action: NavigationAction) {
            Navigator.action(action, SFandomsModeration(accountId))
        }
    }

    init {
        setTitle(R.string.app_fandoms)
        setTextEmpty(R.string.fandoms_empty)
    }


    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardFandom, Fandom> {
        return RecyclerCardAdapterLoading<CardFandom, Fandom>(CardFandom::class) { fandom -> CardFandom(fandom).setShowLanguage(true) }
                .setBottomLoader { onLoad, cards ->
                    RFandomsGetAllModerated(accountId, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.fandoms) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)

                }
    }

}
