package com.sayzen.campfiresdk.screens.account.stickers


import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.stickers.RStickersGetAllFavorite
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.events.stickers.EventStickerCollectionChanged
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SStickersViewFavorite(
        val accountId:Long
) : SLoadingRecycler<CardSticker, PublicationSticker>() {

    private val eventBus = EventBus
            .subscribe(EventStickerCollectionChanged::class){
                if(adapter != null) {
                    if (it.inCollection) {
                        var found = false
                        for (c in adapter!!.get(CardSticker::class)) if (c.xPublication.publication.id == it.sticker.id) found = true
                        if (!found) adapter!!.add(0, CardSticker(it.sticker))
                        setState(State.NONE)
                    } else {
                        for (c in adapter!!.get(CardSticker::class)) if (c.xPublication.publication.id == it.sticker.id) adapter!!.remove(c)
                    }
                }
            }

    private var loaded = false

    init {
        setTitle(R.string.app_favorites)
        setTextEmpty(R.string.stickers_pack_view_empty)
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)

        val spanCount = if (ToolsAndroid.isScreenPortrait()) 3 else 6
        vRecycler.layoutManager = GridLayoutManager(context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardSticker, PublicationSticker> {
        return RecyclerCardAdapterLoading<CardSticker, PublicationSticker>(CardSticker::class) { CardSticker(it) }
                .setShowLoadingCardBottom(false)
                .setBottomLoader { onLoad, _ ->
                    subscription = RStickersGetAllFavorite(accountId)
                            .onComplete { r ->
                                if (loaded) {
                                    onLoad.invoke(emptyArray())
                                } else {
                                    loaded = true
                                    onLoad.invoke(r.stickers)
                                }
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}