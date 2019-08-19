package com.sayzen.campfiresdk.screens.fandoms.moderation.view

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGet
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.dzen.campfire.api.models.units.moderations.UnitModeration
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.libs.eventBus.EventBus

class SModerationView private constructor(
        private val unit: UnitModeration,
        commentId: Long
) : Screen(R.layout.screen_fandom_moderation_view) {

    companion object {

        fun instance(unitId: Long, action: NavigationAction) {
            instance(unitId, 0, action)
        }

        fun instance(unitId: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RFandomsModerationGet(unitId)
            ) { r -> SModerationView(r.unit!!, commentId) }
        }
    }

    private val eventBus = EventBus
            .subscribe(EventCommentAdd::class) { e: EventCommentAdd -> this.onCommentAdd(e) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vMenu: View = findViewById(R.id.vMenu)
    private val adapter: AdapterComments = AdapterComments(unit.id, commentId, vRecycler)

    init {

        vRecycler.layoutManager = LinearLayoutManager(context)

        adapter.add(CardInfo(unit))

        vMenu.setOnClickListener { v-> ControllerUnits.showModerationPopup(v, unit) }
        vFab.setOnClickListener { v -> WidgetComment(unit.id) { comment -> adapter.addComment(comment) }.asSheetShow() }
        vRecycler.adapter = adapter

        ToolsView.recyclerHideFabWhenScrollEnd(vRecycler, vFab)
    }


    //
    //  EventBus
    //

    private fun onCommentAdd(e: EventCommentAdd) {
        if (e.parentUnitId == unit.id) adapter.loadBottom()
    }


}
