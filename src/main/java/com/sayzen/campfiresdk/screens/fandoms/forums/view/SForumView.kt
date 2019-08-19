package com.sayzen.campfiresdk.screens.fandoms.forums.view

import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.dzen.campfire.api.models.UnitComment
import com.dzen.campfire.api.models.units.UnitForum
import com.dzen.campfire.api.requests.fandoms.RFandomsForumGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.cards.comments.CardComment
import com.sayzen.campfiresdk.models.events.fandom.EventForumChanged
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads

class SForumView(
        private val unit: UnitForum,
        private val commentId: Long
) : SLoadingRecycler<CardComment, UnitComment>() {

    companion object {

        fun instance(unitId: Long, action: NavigationAction) {
            instance(unitId, 0, action)
        }

        fun instance(unitId: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsForumGet(unitId)) { r ->
                SForumView(r.unit!!, commentId)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventCommentAdd::class) { if (it.parentUnitId == unit.id && adapter != null) (adapter as AdapterComments).loadTop() }
            .subscribe(EventUnitRemove::class) { if (it.unitId == unit.id) Navigator.remove(this) }
            .subscribe(EventForumChanged::class) { onEventForumChanged(it) }


    init {

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        vRecycler.layoutManager = layoutManager

        addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_help_24dp)) {
            showDialog()
        }
        addToolbarIcon(ToolsResources.getDrawableAttrId(R.attr.ic_more_vert_24dp)) {
            ControllerUnits.showForumPopup(it, unit)
        }

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_mode_comment_white_24dp)
        vFab.setOnClickListener { WidgetComment(unit.id) { comment -> (adapter as AdapterComments).addComment(comment) }.asSheetShow() }
        setTextEmpty(R.string.forum_empty_messages)
        setTextProgress(R.string.forum_loading_messages)
        setBackgroundImage(R.drawable.bg_19)


        ToolsView.recyclerHideFabWhenScrollEnd(vRecycler, vFab)

        if (!ControllerSettings.viewedForums.contains(unit.id)) showDialog()
        update()
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardComment, UnitComment> {
        val adapter = AdapterComments(unit.id, commentId, vRecycler, true)
        adapter.enableTopLoader()
        adapter.setEmptyMessage(null)
        return adapter
    }

    private fun showDialog() {
        ToolsThreads.main(100) {
            WidgetAlert()
                    .setTitleImage { ToolsImagesLoader.load(unit.imageId).into(it) }
                    .setText(unit.text)
                    .setOnEnter(R.string.app_continue) {
                        ControllerSettings.viewedForums = ToolsCollections.add(unit.id, ControllerSettings.viewedForums)
                        if (ControllerSettings.viewedForums.size > 50) ControllerSettings.viewedForums = ToolsCollections.remove(0, ControllerSettings.viewedForums)
                    }
                    .asSheetShow()
        }

    }

    private fun update() {
        setTitle(unit.name)
    }

    //
    //  EventBus
    //


    private fun onEventForumChanged(e: EventForumChanged) {
        if (e.unitId == unit.id) {
            unit.name = e.name
            unit.text = e.text
            ToolsImagesLoader.clear(unit.imageId)
            update()
        }
    }

}