package com.sayzen.campfiresdk.screens.post.view

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.NotificationUnitImportant
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.adapters.XUnit
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.units.*
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber
import com.sup.dev.java.tools.ToolsThreads

class SPost constructor(
        unit: UnitPost,
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

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventPostChanged::class) { this.onPostChanged(it) }
            .subscribe(EventPostStatusChange::class) { this.onEventPostStatusChange(it) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMenu: View = findViewById(R.id.vMenu)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vShare: View = findViewById(R.id.vShare)

    private val adapter: AdapterComments = AdapterComments(unit.id, commentId, vRecycler)
    private val xUnit = XUnit(unit,
            onChangedAccount = { cardInfo.updateAccount() },
            onChangedFandom = { cardInfo.updateFandom() },
            onChangedKarma = { cardInfo.updateKarma() },
            onChangedComments = {
                cardInfo.updateComments()
                adapter.loadBottom()
            },
            onChangedReports = { cardInfo.updateReports() },
            onChangedImportance = {},
            onRemove = { Navigator.remove(this) }
    )
    private val cardInfo: CardInfo = CardInfo(xUnit, tags)

    init {

        vRecycler.layoutManager = LinearLayoutManager(context)
        ToolsView.setRecyclerAnimation(vRecycler)

        for (page in unit.pages) adapter.add(CardPage.instance(unit, page))
        ControllerPost.updateSpoilers(adapter)
        adapter.add(cardInfo)
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

    private fun onPostChanged(e: EventPostChanged) {
        if (e.unitId == xUnit.unit.id) {
            adapter.remove(CardPage::class)
            for (i in 0 until e.pages.size) adapter.add(i, CardPage.instance(xUnit.unit as UnitPost, e.pages[i]))
        }
    }

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        if (e.unitId == xUnit.unit.id && e.status != API.STATUS_PUBLIC) Navigator.remove(this)
    }

}
