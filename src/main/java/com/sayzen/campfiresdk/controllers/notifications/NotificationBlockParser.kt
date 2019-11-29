package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.account.NotificationAdminBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText

public class NotificationBlockParser(override val n: NotificationAdminBlock) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
        return if (ToolsText.empty(n.comment)) "" else " " + ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
    }

    override fun getTitle(): String {
        return if (n.blockAccountDate > 0) {
            (ToolsResources.sCap(
                    R.string.moderation_notification_blocked,
                    ToolsDate.dateToString(n.blockAccountDate)
            ))
        } else {
            (ToolsResources.sCap(R.string.moderation_notification_warned))
        }
    }

}