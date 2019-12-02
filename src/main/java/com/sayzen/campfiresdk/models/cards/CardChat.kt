package com.sayzen.campfiresdk.models.cards

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.adapters.XAccount
import com.sayzen.campfiresdk.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerChats
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatRemove
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate

class CardChat(
        var chat: Chat,
        messagesCount: Int,
        var subscribed: Boolean
) : Card(R.layout.card_chat) {

    private val xFandom: XFandom
    private val xAccount: XAccount
    var setStack = true
    var onSelected: ((Chat) -> Unit)? = null

    private val eventBus = EventBus
            .subscribe(EventChatMessagesCountChanged::class) { this.onEventChatMessagesCountChanged(it) }
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.onEventOnChatTypingChanged(it) }
            .subscribe(EventChatNewBottomMessage::class) { this.onEventChatNewBottomMessage(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventChatRemoved::class) { this.onEventChatRemoved(it) }
            .subscribe(EventChatReadDateChanged::class) { this.onEventChatReadDateChanged(it) }
            .subscribe(EventFandomChatRemove::class) { this.onEventFandomChatRemove(it) }
            .subscribe(EventFandomChatChanged::class) { this.onEventFandomChatChanged(it) }

    init {
        ControllerChats.putRead(chat.tag, chat.anotherAccountReadDate)
        chat.tag.setMyAccountId(ControllerApi.account.id)
        xFandom = XFandom(if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) chat.tag.targetId else 0, chat.tag.targetSubId, chat.unitChatMessage.fandomName, chat.unitChatMessage.fandomImageId) { update() }
        xAccount = XAccount(if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) chat.tag.getAnotherId() else 0, chat.anotherAccountImageId, chat.anotherAccountLvl, chat.anotherAccountKarma30, chat.anotherAccountLastOnlineTime) { update() }

        ControllerChats.setMessagesCount(chat.tag, messagesCount, subscribed)
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
        vAvatar.vAvatar.vChip.setText("")
        vAvatar.vAvatar.setChipIcon(0)
        vAvatar.vAvatar.setOnClickListener { }

        val hasUnread = !ControllerApi.isCurrentAccount(chat.unitChatMessage.creatorId)
                || ControllerChats.isRead(chat.tag, chat.unitChatMessage.dateCreate)
                || chat.tag.chatType != API.CHAT_TYPE_PRIVATE

        vNotRead.visibility = if (hasUnread) View.GONE else View.VISIBLE

        vSwipe.onClick = { _, _ -> if (onSelected != null) onSelected!!.invoke(chat) else SChat.instance(chat.tag, setStack, Navigator.TO) }
        vSwipe.onLongClick = { _, _ -> ControllerChats.instanceChatPopup(chat.tag, chat.params, chat.anotherAccountImageId, null).asSheetShow() }
        vSwipe.onSwipe = { if (hasUnread) ControllerChats.readRequest(chat.tag) }

        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            xFandom.setView(vAvatar.vAvatar)
            vAvatar.vAvatar.vChip.visibility = View.VISIBLE
        } else if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) {
            xAccount.setView(vAvatar.vAvatar)
            vAvatar.vAvatar.vChip.visibility = View.VISIBLE
        } else {
            ToolsImagesLoader.load(chat.anotherAccountImageId).into(vAvatar.vAvatar.vImageView)
            vAvatar.setTitle(chat.anotherAccountName)
            vAvatar.vAvatar.setOnClickListener { SChatCreate.instance(chat.tag.targetId, Navigator.TO) }
            vAvatar.vAvatar.vChip.visibility = View.GONE
        }

        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) vAvatar.setTitle(chat.unitChatMessage.fandomName)
        else vAvatar.setTitle(chat.anotherAccountName)

        if (chat.unitChatMessage.id != 0L) {
            val text = ControllerChats.getTypingText(chat.tag)
            if (text != null)
                vAvatar.setSubtitle(text)
            else {
                var t = if (chat.unitChatMessage.creatorName.isNotEmpty()) chat.unitChatMessage.creatorName + ": " else ""
                if (ControllerApi.isCurrentAccount((chat.unitChatMessage.creatorId))) t = ToolsResources.s(R.string.app_you) + ": "
                if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) {
                    if (!ControllerApi.isCurrentAccount((chat.unitChatMessage.creatorId))) t = ""
                }

                t += when {
                    chat.unitChatMessage.resourceId > 0 -> ToolsResources.s(R.string.app_image)
                    chat.unitChatMessage.voiceResourceId > 0 -> ToolsResources.s(R.string.app_voice_message)
                    chat.unitChatMessage.stickerId > 0 -> ToolsResources.s(R.string.app_sticker)
                    chat.unitChatMessage.imageIdArray.isNotEmpty() -> ToolsResources.s(R.string.app_image)
                    chat.unitChatMessage.type == PublicationChatMessage.TYPE_SYSTEM -> ControllerChats.getSystemText(chat.unitChatMessage)
                    else -> chat.unitChatMessage.text
                }
                vAvatar.setSubtitle(t)
            }
            vMessageDate.text = ToolsDate.dateToString(chat.unitChatMessage.dateCreate)
        } else {
            vMessageDate.text = ""
            vAvatar.setSubtitle(ToolsResources.s(R.string.app_empty))
        }

        val messagesCount: Int = ControllerChats.getMessagesCount(chat.tag)
        if (messagesCount > 9) vMessagesCounter.setText("9+")
        else vMessagesCounter.setText("$messagesCount")

        vMessagesCounter.setBackgroundColor(if (subscribed) ToolsResources.getAccentColor(view.context) else ToolsResources.getColor(R.color.grey_600))
        vMessagesCounter.visibility = if (messagesCount < 1) View.GONE else View.VISIBLE

        ControllerLinks.makeLinkable(vAvatar.vSubtitle)

    }

    //
    //  EventBus
    //

    private fun onEventChatRemoved(e: EventChatRemoved) {
        if (e.tag == chat.tag) adapter?.remove(this)
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        if (e.tag == chat.tag) update()
    }

    private fun onEventFandomChatRemove(e: EventFandomChatRemove) {
        if (e.chatId == chat.tag.targetId && chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB) adapter?.remove(this)
    }

    private fun onEventFandomChatChanged(e: EventFandomChatChanged) {
        if (e.chatId == chat.tag.targetId && chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            chat.anotherAccountName = e.name
            chat.params = ChatParamsFandomSub(e.text).json(true, Json())
            update()
        }
    }

    private fun onEventChatMessagesCountChanged(e: EventChatMessagesCountChanged) {
        if (e.tag == chat.tag) update()
    }

    private fun onEventOnChatTypingChanged(e: EventChatTypingChanged) {
        if (e.tag == chat.tag) update()

    }

    private fun onEventChatNewBottomMessage(e: EventChatNewBottomMessage) {
        if (e.tag == chat.tag) {
            chat.unitChatMessage = e.unitChatMessage
            update()
        }
    }

    private fun onEventChatSubscriptionChanged(e: EventChatSubscriptionChanged) {
        if (e.tag == chat.tag) {
            subscribed = e.subscribed
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (n.tag == chat.tag) {
                chat.unitChatMessage = n.publicationChatMessage
                update()
            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (n.tag == chat.tag) {
                chat.unitChatMessage = n.publicationChatMessage
                update()
            }
        }
    }
}