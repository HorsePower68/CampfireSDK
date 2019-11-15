package com.sayzen.campfiresdk.screens.fandoms.moderation.view

import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.dzen.campfire.api.requests.fandoms.RFandomsModerationGet
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sayzen.campfiresdk.adapters.AdapterComments
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sayzen.campfiresdk.models.widgets.WidgetComment
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.libs.eventBus.EventBus

class SModerationView private constructor(
        private val publication: PublicationModeration,
        commentId: Long
) : Screen(R.layout.screen_fandom_moderation_view) {

    companion object {

        fun instance(publicationId: Long, action: NavigationAction) {
            instance(publicationId, 0, action)
        }

        fun instance(publicationId: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RFandomsModerationGet(publicationId)
            ) { r -> SModerationView(r.publication!!, commentId) }
        }
    }

    private val eventBus = EventBus
            .subscribe(EventCommentsCountChanged::class) { e: EventCommentsCountChanged -> this.onEventCommentsCountChanged(e) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vMenu: View = findViewById(R.id.vMenu)
    private val adapter: AdapterComments = AdapterComments(publication.id, commentId, vRecycler)

    init {

        vRecycler.layoutManager = LinearLayoutManager(context)

        adapter.add(CardInfo(publication))

        vMenu.setOnClickListener { ControllerPublications.showModerationPopup(publication) }
        vFab.setOnClickListener { WidgetComment(publication.id) { comment -> adapter.addComment(comment) }.asSheetShow() }
        vRecycler.adapter = adapter

        ToolsView.recyclerHideFabWhenScrollEnd(vRecycler, vFab)
    }


    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.publicationId == publication.id) adapter.loadBottom()
    }


}
