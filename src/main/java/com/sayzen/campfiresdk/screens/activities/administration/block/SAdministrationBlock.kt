package com.sayzen.campfiresdk.screens.activities.administration.block

import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.publications.PublicationReview
import com.dzen.campfire.api.models.publications.PublicationBlocked
import com.dzen.campfire.api.requests.units.RUnitsBlockGetAll
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlockedRemove
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SAdministrationBlock : SLoadingRecycler<CardPublication, PublicationBlocked>() {

    private val eventBus = EventBus.subscribe(EventPublicationBlockedRemove::class) {
        var i = 0
        while (i < adapter!!.size()) {
            if (adapter!![i] is CardUnitBlock) {
                if ((adapter!![i] as CardUnitBlock).publication.moderationId == it.moderationId) {
                    adapter!!.remove(i--)
                    adapter!!.remove(i--)
                }
            }
            i++
        }
    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(R.string.app_block_title)
        setTextEmpty(R.string.app_empty)
    }

    override fun reload() {
        super.reload()
        adapter!!.clear()
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, PublicationBlocked> {
        return RecyclerCardAdapterLoading<CardPublication, PublicationBlocked>(CardPublication::class) {
            val c = CardPublication.instance(it.publication, null, false, false)
            c.tag = it
            c
        }
                .setBottomLoader { onLoad, cards ->
                    RUnitsBlockGetAll(cards.size.toLong())
                            .onComplete { r ->
                                for(u in r.publications) if(u.publication is PublicationReview) (u.publication as PublicationReview).text = (u.publication as PublicationReview).removedText
                                onLoad.invoke(r.publications)
                                ToolsThreads.main { afterPackLoaded() }
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    private fun afterPackLoaded() {
        var i = 0
        while (i < adapter!!.size()) {
            if (adapter!![i] is CardPublication) {
                if (i != adapter!!.size() - 1) {
                    if (adapter!![i + 1] is CardPublication) {
                        adapter!!.add(i + 1, CardUnitBlock(adapter!![i].tag as PublicationBlocked))
                        i++
                    }
                } else {
                    adapter!!.add(i + 1, CardUnitBlock((adapter!![i].tag as PublicationBlocked)))
                    i++
                }
            }
            i++
        }
    }

}