package com.sayzen.campfiresdk.screens.post.bookmarks

import com.dzen.campfire.api.requests.units.RUnitsBookmarksGetAll
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBookmarkChange
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace
import com.sup.dev.java.libs.eventBus.EventBus

class SBookmarks private constructor() : SLoadingRecycler<CardPublication, Publication>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SBookmarks())
        }
    }

    private val eventBus = EventBus
            .subscribe(EventPublicationBookmarkChange::class) { this.onEventUnitBookmarkChange(it) }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(R.string.app_bookmarks)
        setTextEmpty(R.string.bookmarks_empty)
        setTextProgress(R.string.bookmarks_loading)
        setBackgroundImage(R.drawable.bg_1)
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RUnitsBookmarksGetAll(cards.size.toLong(), "", ControllerCampfireSDK.ROOT_FANDOM_ID, 0)
                            .onComplete { r -> onLoad.invoke(r.publications) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

    //
    //  EventBus
    //

    private fun onEventUnitBookmarkChange(e: EventPublicationBookmarkChange) {
        if(adapter == null) return
        for(c in adapter!!.get(CardPublication::class)) if(c.xPublication.publication.id == e.publicationId) adapter!!.remove(c)
    }
}
