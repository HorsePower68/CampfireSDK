package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.chat.NotificationChatRead
import com.sayzen.campfiresdk.controllers.ControllerNotifications

public class NotificationChatReadParser(override val n: NotificationChatRead) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

    }

    override fun asString(html: Boolean) = ""

}