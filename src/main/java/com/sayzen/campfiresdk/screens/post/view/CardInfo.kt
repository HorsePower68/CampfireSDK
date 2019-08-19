package com.sayzen.campfiresdk.screens.post.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.notifications.NotificationComment
import com.dzen.campfire.api.models.notifications.NotificationCommentAnswer
import com.dzen.campfire.api.models.units.post.UnitPost
import com.dzen.campfire.api.models.units.tags.UnitTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.sayzen.campfiresdk.models.events.units.EventCommentRemove
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewSpace
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class CardInfo(
        private val unit: UnitPost,
        private val tags: Array<UnitTag>
) : Card(R.layout.screen_post_card_info) {

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventCommentAdd::class) { this.onCommentAdd(it) }
            .subscribe(EventCommentRemove::class) { this.onEventCommentRemove(it) }
            .subscribe(EventNotification::class) {this.onNotification(it) }

    private val xFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xAccount = XAccount(unit, unit.dateCreate) { update() }
    private val xKarma = XKarma(unit) { update() }

    init {
        xFandom.showLanguage = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vFandom: ViewAvatar = view.findViewById(R.id.vFandom)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vKarma: ViewKarma = view.findViewById(R.id.vKarma)
        val vFlow: LayoutFlow = view.findViewById(R.id.vFlow)
        val vMiddleDivider: View = view.findViewById(R.id.vMiddleDivider)

        xFandom.setView(vFandom)
        xAccount.setView(vAvatar)
        xKarma.setView(vKarma)
        vComments.text = "" + unit.subUnitsCount

        val tags = ControllerUnits.parseTags(this.tags)

        if(tags.isEmpty()){
            vMiddleDivider.visibility = View.GONE
            vFlow.visibility = View.GONE
        }else{
            vMiddleDivider.visibility = View.VISIBLE
            vFlow.visibility = View.VISIBLE
        }

        vFlow.removeAllViews()
        for (tagParent in tags) {
            addTag(tagParent.tag, vFlow)
            for (tag in tagParent.tags) addTag(tag, vFlow)
        }
    }

    private fun addTag(t: UnitTag, vFlow: LayoutFlow) {
        val vChip = if (t.parentUnitId == 0L) ViewChip.instance(vFlow.context) else ViewChip.instanceOutline(vFlow.context)
        vChip.text = t.name
        vChip.setOnClickListener { v -> SPostsSearch.instance(t, Navigator.TO) }
        ControllerUnits.createTagMenu(vChip, t)
        if (vFlow.childCount != 0 && t.parentUnitId == 0L) vFlow.addView(ViewSpace(vFlow.context, ToolsView.dpToPx(1).toInt(), 0))
        vFlow.addView(vChip)
        if (t.imageId != 0L) ToolsImagesLoader.load(t.imageId).into { bytes -> vChip.setIcon(ToolsBitmap.decode(bytes)) }
    }


    //
    //  EventBus
    //

    private fun onCommentAdd(e: EventCommentAdd) {
        if (e.parentUnitId == unit.id){
            unit.subUnitsCount++
            update()
        }
    }

    private fun onEventCommentRemove(e: EventCommentRemove) {
        if (e.parentUnitId == unit.id){
            unit.subUnitsCount--
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationComment)
            if ((e.notification as NotificationComment).unitId == unit.id) {
                unit.subUnitsCount++
                update()
            }

        if (e.notification is NotificationCommentAnswer)
            if ((e.notification as NotificationCommentAnswer).unitId == unit.id) {
                unit.subUnitsCount++
                update()
            }

    }


}
