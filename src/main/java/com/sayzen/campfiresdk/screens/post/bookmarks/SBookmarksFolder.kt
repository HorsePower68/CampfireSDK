package com.sayzen.campfiresdk.screens.post.bookmarks

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.BookmarksFolder
import com.dzen.campfire.api.requests.bookmarks.RBookmarksGetAll
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBookmarkChange
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SBookmarksFolder constructor(
        val folder:BookmarksFolder
) : SLoadingRecycler<CardPublication, Publication>() {

    private val eventBus = EventBus
            .subscribe(EventPublicationBookmarkChange::class) { this.onEventUnitBookmarkChange(it) }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(folder.name)
        setTextEmpty(R.string.bookmarks_empty)
        setTextProgress(R.string.bookmarks_loading)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_1)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPublication, Publication> {
        return RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RBookmarksGetAll(cards.size.toLong(), "", ControllerCampfireSDK.ROOT_FANDOM_ID, 0, folder.id, emptyArray())
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
