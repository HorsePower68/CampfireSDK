package com.sayzen.campfiresdk.screens.post.pending

import android.view.View
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.requests.post.RPostPendingGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace

class SPending : SLoadingRecycler<CardPost, UnitPost>() {

    init {
        setScreenColorBackground()
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
        setTitle(R.string.app_pending)
        setTextEmpty(R.string.post_drafts_empty_text)
        setTextProgress(R.string.post_drafts_loading)
        setBackgroundImage(R.drawable.bg_2)

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            SFandomsSearch.instance(Navigator.TO, true) { fandom ->
                SPostCreate.instance(fandom.id, fandom.languageId, fandom.name, fandom.imageId, emptyArray(), Navigator.TO)
            }
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPost, UnitPost> {
        return RecyclerCardAdapterLoading<CardPost, UnitPost>(CardPost::class) { unit ->
            val card = CardPost(vRecycler, unit)
            card.showFandom = true
            card
        }
                .setBottomLoader { onLoad, cards ->
                    val r = RPostPendingGetAll(cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                    r.tokenRequired = true
                    r.send(api)
                }
    }

}
