package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerChats
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationChatMessageParser(override val n: NotificationChatMessage) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        val titleV: String
        var textV = text
        val publication = n.publicationChatMessage
        val tagV = n.tag.asTag()

        val chatMessagesCount = ControllerChats.getMessagesCount(n.tag)
        if (n.tag.chatType == API.CHAT_TYPE_PRIVATE) {
            titleV = publication.creatorName
            if (chatMessagesCount > 1) textV = ToolsResources.s(
                    R.string.notification_chat_private_many, chatMessagesCount, ToolsResources.getPlural(
                    R.plurals.new_fem, chatMessagesCount
            ), ToolsResources.getPlural(R.plurals.private_, chatMessagesCount), ToolsResources.getPlural(
                    R.plurals.messages, chatMessagesCount
            )
            )
        } else {
            titleV = publication.fandomName
            if (chatMessagesCount > 1) textV = ToolsResources.s(
                    R.string.notification_chat_many, chatMessagesCount, ToolsResources.getPlural(
                    R.plurals.new_fem, chatMessagesCount
            ), ToolsResources.getPlural(R.plurals.messages, chatMessagesCount)
            )
        }

        (if (sound) ControllerNotifications.chanelChatMessages else ControllerNotifications.chanelChatMessages_salient).post(icon, titleV, textV, intent, tagV)
    }

    override fun asString(html: Boolean): String {
        if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_sticker)
            else n.publicationChatMessage.creatorName + ": " + n.publicationChatMessage.text
        } else {
            return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) ToolsResources.s(R.string.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) ToolsResources.s(R.string.app_sticker)
            else n.publicationChatMessage.text
        }
    }

    override fun canShow() = canShowNotificationChatMessage(n)

    override fun doAction() {
        doActionNotificationChatMessage(n)
    }

    private fun canShowNotificationChatMessage(n: NotificationChatMessage): Boolean {
        if (!n.subscribed) return false
        if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT && !ControllerSettings.notificationsChatMessages) return false
        if (n.tag.chatType == API.CHAT_TYPE_PRIVATE && !ControllerSettings.notificationsPM) return false
        if (SupAndroid.activityIsVisible && Navigator.getCurrent() is SChat) {
            val screen = Navigator.getCurrent() as SChat
            return screen.tag != n.tag
        } else {
            return true
        }
    }

    private fun doActionNotificationChatMessage(n: NotificationChatMessage) {
        SChat.instance(n.publicationChatMessage.chatType, n.publicationChatMessage.fandomId, n.publicationChatMessage.languageId, n.publicationChatMessage.id, true, Navigator.TO)
    }



}