package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources


public class NotificationChatAnswerParser(override val n: NotificationChatAnswer) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        val publication = n.publicationChatMessage
        val tagV = n.tag.asTag()

        (if (sound) ControllerNotifications.chanelChatMessages else ControllerNotifications.chanelChatMessages_salient).post(icon, publication.fandomName, text, intent, tagV)
    }

    override fun asString(html: Boolean): String {
        return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_image)
        else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_sticker)
        else n.publicationChatMessage.creatorName + ": " + n.publicationChatMessage.text
    }

}