package com.sayzen.campfiresdk.screens.post.search

import android.view.View
import com.dzen.campfire.api.requests.post.RPostGetAllByTag
import com.sayzen.campfiresdk.models.cards.CardUnit
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.dzen.campfire.api.requests.tags.RTagsGet
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.support.adapters.recycler_view.decorators.DecoratorVerticalSpace
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPostsSearch(val tag: UnitTag) : SLoadingRecycler<CardUnit, Unit>() {

    companion object {

        fun instance(tag: UnitTag, action: NavigationAction) {
            Navigator.action(action, SPostsSearch(tag))
        }

        fun instance(tagId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RTagsGet(tagId)
            ) { r -> SPostsSearch(r.tag) }
        }
    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(tag.name)
        setTextEmpty(R.string.post_search_empty)
        setAction(R.string.post_search_action) {
            if (Navigator.hasPrevious() && Navigator.getPrevious() is STags)
                Navigator.back()
            else
                STags.instance(tag.fandomId, tag.languageId, Navigator.REPLACE)
        }
        vRecycler.addItemDecoration(DecoratorVerticalSpace())
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(ToolsResources.getDrawableAttrId(R.attr.ic_add_24dp))
        vFab.setOnClickListener {
            SPostCreate.instance(tag.fandomId, tag.languageId, tag.fandomName, tag.fandomImageId, arrayOf(tag.id), Navigator.TO)
        }
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardUnit, Unit> {
        return RecyclerCardAdapterLoading<CardUnit, Unit>(CardUnit::class) { unit -> CardUnit.instance(unit, vRecycler) }
                .setBottomLoader { onLoad, cards ->
                    RPostGetAllByTag(tag.id, cards.size.toLong())
                            .onComplete { r -> onLoad.invoke(r.units) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }

}
