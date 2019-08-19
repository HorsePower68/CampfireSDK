package com.sayzen.campfiresdk.screens.fandoms.forums.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.notifications.NotificationComment
import com.dzen.campfire.api.models.notifications.NotificationCommentAnswer
import com.dzen.campfire.api.models.units.UnitForum
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.adapters.XKarma
import com.sayzen.campfiresdk.models.events.units.EventCommentAdd
import com.sayzen.campfiresdk.models.events.units.EventCommentRemove
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewTextLinkable
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class CardInfo(
        private val unit: UnitForum
) : Card(R.layout.screen_forum_card_info) {

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventCommentAdd::class) { this.onCommentAdd(it) }
            .subscribe(EventCommentRemove::class) { this.onEventCommentRemove(it) }
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

        xFandom.setView(vFandom)
        xAccount.setView(vAvatar)
        xKarma.setView(vKarma)
        vComments.text = "" + unit.subUnitsCount

        vText.text = unit.text
    }

    //
    //  EventBus
    //

    private fun onCommentAdd(e: EventCommentAdd) {
        if (e.parentUnitId == unit.id) {
            unit.subUnitsCount++
            update()
        }
    }

    private fun onEventCommentRemove(e: EventCommentRemove) {
        if (e.parentUnitId == unit.id) {
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