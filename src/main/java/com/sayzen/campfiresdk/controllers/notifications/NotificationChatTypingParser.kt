package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.sayzen.campfiresdk.controllers.ControllerNotifications

public class NotificationChatTypingParser(override val n: NotificationChatTyping) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

    }

    override fun asString(html: Boolean) = ""

}