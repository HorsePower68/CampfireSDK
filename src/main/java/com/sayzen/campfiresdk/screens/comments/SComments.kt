package com.sayzen.campfiresdk.screens.comments

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.units.Unit
import com.dzen.campfire.api.requests.post.RPostGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.sayzen.campfiresdk.models.events.units.EventUnitRemove
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class SComments constructor(
    val unit: Unit,
    commentId: Long
) : Screen(R.layout.screen_comments) {

    companion object{

        fun instance(unitId:Long,  commentId: Long, action:NavigationAction){
            ApiRequestsSupporter.executeInterstitial(action, RPostGet(unitId)){ r->
                SComments(r.unit, commentId)
            }
        }

    }

    private val eventBus: EventBusSubscriber= EventBus
        .subscribe(EventUnitRemove::class) { this.onEventUnitRemove(it) }
        .subscribe(EventCommentAdd::class) {this.onCommentAdd(it) }

    private val vRecycler: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)

    private val adapter: AdapterComments

    init {

        vRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        adapter = AdapterComments(unit.id, commentId, vRecycler)

        vFab.setOnClickListener { v -> WidgetComment(unit.id) { comment -> adapter.addComment(comment) }.asSheetShow() }
        vRecycler.adapter = adapter
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

}