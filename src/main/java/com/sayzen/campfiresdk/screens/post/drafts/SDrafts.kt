package com.sayzen.campfiresdk.screens.post.drafts

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.events.units.EventPostPublishedChange
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.screens.post.pending.SPending
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.libs.eventBus.EventBus

class SDrafts constructor(
        val onSelect: ((Unit) -> kotlin.Unit)? = null
) : SLoadingRecycler<CardPost, Unit>() {

    private val eventBus = EventBus
            .subscribe(EventPostPublishedChange::class) { e: EventPostPublishedChange -> this.onPostPublicationChange(e) }

    init {
        setScreenColorBackground()
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
        setTitle(R.string.app_drafts)
        setTextEmpty(R.string.post_drafts_empty_text)
        setTextProgress(R.string.post_drafts_loading)
        setBackgroundImage(R.drawable.bg_2)

        addToolbarIcon(ToolsResources.getDrawableAttr(R.attr.ic_more_vert_24dp)!!) { view ->
            WidgetMenu()
                    .add(R.string.app_pending) { w, i -> Navigator.to(SPending()) }
                    .asPopupShow(view)
        }

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { v ->
            SFandomsSearch.instance(Navigator.TO, true) { fandom ->
                SPostCreate.instance(fandom.id, fandom.languageId, fandom.name, fandom.imageId, emptyArray(), Navigator.TO)
            }
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardPost, Unit> {
        return RecyclerCardAdapterLoading<CardPost, Unit>(CardPost::class) { unit ->
            val card = CardPost(vRecycler, unit as UnitPost)
            if (onSelect != null) card.onClick = {
                Navigator.remove(this)
                onSelect.invoke(unit)
            }
            card.showFandom = true
            card
        }
                .setBottomLoader { onLoad, cards ->
                    val r = RUnitsGetAll()
                            .setOffset(cards.size)
                            .setUnitTypes(API.UNIT_TYPE_POST)
                            .setOrder(RUnitsGetAll.ORDER_NEW)
                            .setDrafts(true)
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                    r.tokenRequired = true
                    r.send(api)
                }
    }

    //
    //  EventBus
    //

    private fun onPostPublicationChange(e: EventPostPublishedChange) {
        if (!e.published) adapter!!.reloadBottom()
    }

}
