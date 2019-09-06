package com.sayzen.campfiresdk.models.cards

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.NotificationChatMessage
import com.dzen.campfire.api.models.units.chat.UnitChat
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerChats
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardChat(
        var unit: UnitChat,
        messagesCount: Int,
        var subscribed: Boolean
) : Card(R.layout.card_chat) {

    private val xFandom: XFandom
    private val xAccount: XAccount
    var onSelected: ((UnitChat) -> Unit)? = null

    private val eventBus = EventBus
            .subscribe(EventChatMessagesCountChanged::class) { this.onEventChatMessagesCountChanged(it) }
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.onEventOnChatTypingChanged(it) }
            .subscribe(EventChatNewBottomMessage::class) { this.onEventChatNewBottomMessage(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventChatRemoved::class) { this.onEventChatRemoved(it) }
            .subscribe(EventChatReadDateChanged::class) { this.onEventChatReadDateChanged(it) }

    init {
        ControllerChats.putRead(unit.tag, unit.anotherAccountReadDate)

        if (unit.tag.chatType == API.CHAT_TYPE_PRIVATE)
            unit.tag.setMyAccountId(ControllerApi.account.id)
        xFandom = XFandom(if (unit.tag.chatType == API.CHAT_TYPE_FANDOM) unit.tag.targetId else 0, unit.tag.targetSubId, unit.unitChatMessage.fandomName, unit.unitChatMessage.fandomImageId) { update() }
        xAccount = XAccount(if (unit.tag.chatType == API.CHAT_TYPE_PRIVATE) unit.tag.getAnotherId() else 0, unit.anotherAccountImageId, unit.anotherAccountLvl, unit.anotherAccountKarma30, unit.anotherAccountLastOnlineTime) { update() }

        ControllerChats.setMessagesCount(unit.tag, messagesCount, subscribed)
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMessagesCounter: ViewChipMini = view.findViewById(R.id.vMessagesCounter)
        val vMessageDate: TextView = view.findViewById(R.id.vMessageDate)
        val vNotRead: View = view.findViewById(R.id.vNotRead)
        val vSwipe: ViewSwipe = view.findViewById(R.id.vSwipe)

        vAvatar.vSubtitle.ellipsize = TextUtils.TruncateAt.END
        vAvatar.vSubtitle.setSingleLine()

        val hasUnread = !ControllerApi.isCurrentAccount(unit.unitChatMessage.creatorId)
                || ControllerChats.isRead(unit.tag, unit.unitChatMessage.dateCreate)
                || unit.tag.chatType != API.CHAT_TYPE_PRIVATE

        vNotRead.visibility = if (hasUnread) View.GONE else View.VISIBLE

        vSwipe.onClick =  { _, _ -> if (onSelected != null) onSelected!!.invoke(unit) else SChat.instance(unit.tag, true, Navigator.TO) }
        vSwipe.onLongClick = { _, _ -> ControllerChats.instanceChatPopup(unit.tag).asSheetShow() }
        vSwipe.onSwipe = { if (hasUnread) ControllerChats.readRequest(unit.tag) }

        if (unit.tag.chatType == API.CHAT_TYPE_FANDOM) {
            xFandom.setView(vAvatar.vAvatar)
            vAvatar.vAvatar.setChipBackground(if (subscribed) ToolsResources.getAccentColor(view.context) else ToolsResources.getColor(R.color.grey_600))
        } else {
            xAccount.setView(vAvatar.vAvatar)
        }

        if (unit.tag.chatType == API.CHAT_TYPE_FANDOM) vAvatar.setTitle(unit.unitChatMessage.fandomName)
        else vAvatar.setTitle(unit.anotherAccountName)

        if (unit.unitChatMessage.id != 0L) {
            val text = ControllerChats.getTypingText(unit.tag)
            if (text != null)
                vAvatar.setSubtitle(text)
            else {
                var t = unit.unitChatMessage.creatorName + ": "
                t += when {
                    unit.unitChatMessage.resourceId > 0 -> ToolsResources.s(R.string.app_image)
                    unit.unitChatMessage.voiceResourceId > 0 -> ToolsResources.s(R.string.app_voice_message)
                    unit.unitChatMessage.stickerId > 0 -> ToolsResources.s(R.string.app_sticker)
                    unit.unitChatMessage.imageIdArray.isNotEmpty() -> ToolsResources.s(R.string.app_image)
                    unit.unitChatMessage.blockModerationEventId != 0L -> {
                        if (unit.unitChatMessage.blockDate > 0L)
                            ToolsResources.sCap(R.string.message_do_ban_user, ToolsResources.sex(unit.unitChatMessage.blockedAdminSex, R.string.he_blocked, R.string.she_blocked))
                        else
                            ToolsResources.sCap(R.string.message_do_ban_user, ToolsResources.sex(unit.unitChatMessage.blockedAdminSex, R.string.he_warn, R.string.she_warn))
                    }
                    else -> unit.unitChatMessage.text
                }
                vAvatar.setSubtitle(t)
            }
            vMessageDate.text = ToolsDate.dateToString(unit.unitChatMessage.dateCreate)
        } else {
            vMessageDate.text = ""
            vAvatar.setSubtitle(ToolsResources.s(R.string.app_empty))
        }

        val messagesCount: Int = ControllerChats.getMessagesCount(unit.tag)
        vMessagesCounter.setText("$messagesCount")
        vMessagesCounter.setBackgroundColor(if (subscribed) ToolsResources.getAccentColor(view.context) else ToolsResources.getColor(R.color.grey_600))
        vMessagesCounter.visibility = if (messagesCount < 1) View.GONE else View.VISIBLE

        ControllerApi.makeLinkable(vAvatar.vSubtitle)

    }

    //
    //  EventBus
    //

    private fun onEventChatRemoved(e: EventChatRemoved) {
        if (e.tag == unit.tag) adapter?.remove(this)
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        if (e.tag == unit.tag) update()
    }

    private fun onEventChatMessagesCountChanged(e: EventChatMessagesCountChanged) {
        if (e.tag == unit.tag) update()
    }

    private fun onEventOnChatTypingChanged(e: EventChatTypingChanged) {
        if (e.tag == unit.tag) update()

    }

    private fun onEventChatNewBottomMessage(e: EventChatNewBottomMessage) {
        if (e.tag == unit.tag) {
            unit.unitChatMessage = e.unitChatMessage
            update()
        }
    }

    private fun onEventChatSubscriptionChanged(e: EventChatSubscriptionChanged) {
        if (e.tag == unit.tag) {
            subscribed = e.subscribed
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (n.tag == unit.tag) {
                unit.unitChatMessage = n.unitChatMessage
                update()
            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (n.tag == unit.tag) {
                unit.unitChatMessage = n.unitChatMessage
                update()
            }
        }
    }
}