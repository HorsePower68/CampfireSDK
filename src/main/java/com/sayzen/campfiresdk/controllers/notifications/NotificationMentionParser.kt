package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources

public class NotificationMentionParser(override val n: NotificationMention) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = n.text

    override fun getTitle() = ToolsResources.sCap(R.string.notification_mention, n.fromAccountName, ToolsResources.sex(n.fromAccountSex, R.string.he_mentioned, R.string.she_mentioned))

}