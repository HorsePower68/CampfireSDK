package com.sayzen.campfiresdk.screens.fandoms.moderation.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.units.moderations.units.ModerationBlock
import com.dzen.campfire.api.models.units.moderations.UnitModeration
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerUnits
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.units.EventCommentsCountChanged
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class CardInfo(
        private val unit: UnitModeration
) : Card(R.layout.screen_moderation_card_info) {

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }
            .subscribe(EventNotification::class) { this.onNotification(it) }

    private val xFandom = XFandom(unit, unit.dateCreate) { update() }
    private val xAccount = XAccount(unit, unit.dateCreate) { update() }
    private val xKarma = XKarma(unit) { update() }

    init {
        xFandom.showLanguage = false
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewTextLinkable = view.findViewById(R.id.vText)
        val vFandom: ViewAvatar = view.findViewById(R.id.vFandom)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vKarma: ViewKarma = view.findViewById(R.id.vKarma)
        val vStatus:ViewTextLinkable = view.findViewById(R.id.vStatus)
        val vStatusComment: ViewTextLinkable = view.findViewById(R.id.vStatusComment)

        if(unit.moderation is ModerationBlock){
            vStatus.visibility = View.VISIBLE
            if(unit.tag_2 == 0L) {
                vStatus.setText(R.string.moderation_checked_empty)
                vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
            }
            if (unit.tag_2 == 1L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
                vStatus.text = ToolsResources.s(R.string.moderation_checked_yes, ControllerApi.linkToUser((unit.moderation!! as ModerationBlock).checkAdminName))
            }
            if (unit.tag_2 == 2L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.red_700))
                vStatus.text = ToolsResources.s(R.string.moderation_checked_no, ControllerApi.linkToUser((unit.moderation!! as ModerationBlock).checkAdminName))
                vStatusComment.visibility = View.VISIBLE
                vStatusComment.text = (unit.moderation!! as ModerationBlock).checkAdminComment
            }
            ControllerApi.makeLinkable(vStatus)
        }else{
            vStatus.visibility = View.GONE
        }

        xFandom.setView(vFandom)
        xAccount.setView(vAvatar)
        xKarma.setView(vKarma)
        vComments.text = "" + unit.subUnitsCount

        ControllerUnits.setModerationText(vText, unit)
    }

    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.unitId == unit.id) {
            unit.subUnitsCount = e.commentsCount
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationComment)
            if (e.notification.unitId == unit.id) {
                unit.subUnitsCount++
                update()
            }

        if (e.notification is NotificationCommentAnswer)
            if (e.notification.unitId == unit.id) {
                unit.subUnitsCount++
                update()
            }

    }


}
