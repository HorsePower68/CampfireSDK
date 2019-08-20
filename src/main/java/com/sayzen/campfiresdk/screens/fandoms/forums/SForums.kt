package com.sayzen.campfiresdk.screens.fandoms.forums

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.units.RUnitsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.sayzen.campfiresdk.models.events.fandom.EventForumCreated
import com.sayzen.campfiresdk.screens.fandoms.forums.create.WidgetForumCreate
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SForums constructor(
        val fandomId: Long,
        val languageId: Long
) : SLoadingRecycler<CardUnit, com.dzen.campfire.api.models.units.Unit>() {

    private val eventBus = EventBus.subscribe(EventForumCreated::class) { reload() }

    init {
        setTitle(R.string.app_forums)
        setTextEmpty(R.string.forum_empty)
        setTextProgress(R.string.forum_loading)
        setBackgroundImage(R.drawable.bg_18)
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_FORUMS)) {
                WidgetForumCreate(fandomId, languageId, 0, null, null, null)
            } else {
                ToolsToast.show(R.string.error_low_lvl_or_karma)
            }
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUnit, com.dzen.campfire.api.models.units.Unit> {
        return RecyclerCardAdapterLoading<CardUnit, com.dzen.campfire.api.models.units.Unit>(CardUnit::class) { unit -> CardUnit.instance(unit, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    val r = RUnitsGetAll()
                            .setOffset(cards.size)
                            .setUnitTypes(API.UNIT_TYPE_FORUM)
                            .setOrder(RUnitsGetAll.ORDER_KARMA)
                            .setFandomId(fandomId)
                            .setLanguageId(languageId)
                            .onComplete { rr -> onLoad.invoke(rr.units) }
                            .onNetworkError { onLoad.invoke(null) }
                    r.tokenRequired = true
                    r.send(api)
                }
    }


}

