package com.sayzen.campfiresdk.screens.post.view

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import com.dzen.campfire.api.models.notifications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.NotificationUnitImportant
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.units.*
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber
import com.sup.dev.java.tools.ToolsThreads

class SPost constructor(
        private val unit: UnitPost,
        val tags: Array<UnitTag>,
        commentId: Long
) : Screen(R.layout.screen_post) {

    companion object {

        fun instance(unitId: Long, action: NavigationAction) {
            instance(unitId, 0, action)
        }

        fun instance(unitId: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RPostGet(unitId)) { r ->
                SPost(r.unit, r.tags, commentId)
            }
        }
    }

    private val eventBus: EventBusSubscriber= EventBus
            .subscribe(EventUnitRemove::class) { this.onEventUnitRemove(it) }
            .subscribe(EventCommentAdd::class) {this.onCommentAdd(it) }
            .subscribe(EventPostChanged::class) {this.onPostChanged(it) }
            .subscribe(EventPostPublishedChange::class) { this.onPostPublicationChange(it) }
            .subscribe(EventPostNotifyFollowers::class) { this.onEventPostNotifyFollowers(it) }

    private val vRecycler: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.vRecycler)
    private val vMenu: View = findViewById(R.id.vMenu)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vShare: View = findViewById(R.id.vShare)

    private val adapter: AdapterComments

    init {

        vRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        adapter = AdapterComments(unit.id, commentId, vRecycler)

        for (page in unit.pages) adapter.add(CardPage.instance(unit, page))
        ControllerPost.updateSpoilers(adapter)
        adapter.add(CardInfo(unit, tags))
        adapter.setCommentButton(vFab)

        vShare.setOnClickListener { v -> ControllerApi.sharePost(unit.id) }
        vMenu.setOnClickListener { v -> ControllerPost.showPostPopup(vMenu, unit) }
        vRecycler.adapter = adapter

        ControllerNotifications.removeNotificationFromNew(NotificationFollowsPublication::class, unit.id)
        ControllerNotifications.removeNotificationFromNew(NotificationUnitImportant::class, unit.id)

        if (unit.fandomClosed)
            ToolsThreads.main(true) { ControllerClosedFandoms.showAlertIfNeed(this, unit.fandomId, true) }

    }


    //
    //  EventBus
    //

    private fun onCommentAdd(e: EventCommentAdd) {
        if (e.parentUnitId == unit.id) adapter.loadBottom()
    }

    private fun onEventUnitRemove(e: EventUnitRemove) {
        if (e.unitId == unit.id) Navigator.remove(this)
    }

    private fun onPostChanged(e: EventPostChanged) {
        if (e.unitId == unit.id) {
            adapter.remove(CardPage::class)
            for (i in 0 until e.pages.size) adapter.add(i, CardPage.instance(unit, e.pages[i]))
        }
    }

    private fun onEventPostNotifyFollowers(e: EventPostNotifyFollowers) {
        if (e.unitId == unit.id) {
            unit.tag_3 = 1
        }
    }

    private fun onPostPublicationChange(e: EventPostPublishedChange) {
        if (!e.published) Navigator.remove(this)
    }

}
