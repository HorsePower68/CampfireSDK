package com.sayzen.campfiresdk.screens.comments

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class SComments constructor(
        val publicationId: Long,
        commentId: Long
) : Screen(R.layout.screen_comments) {

    companion object {

        fun instance(unitId: Long, commentId: Long, action: NavigationAction) {
            Navigator.action(action, SComments(unitId, commentId))
        }

    }

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventPublicationRemove::class) { this.onEventUnitRemove(it) }
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)

    private val adapter: AdapterComments

    init {

        vRecycler.layoutManager = LinearLayoutManager(context)

        adapter = AdapterComments(publicationId, commentId, vRecycler)

        vFab.setOnClickListener { WidgetComment(publicationId) { comment -> adapter.addComment(comment) }.asSheetShow() }
        vRecycler.adapter = adapter
    }


    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.unitId == publicationId) adapter.loadBottom()
    }

    private fun onEventUnitRemove(e: EventPublicationRemove) {
        if (e.unitId == publicationId) Navigator.remove(this)
    }

}