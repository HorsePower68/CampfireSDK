package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.fanom.NotificationModerationRejected
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText

public class NotificationModerationRejectedParser(override val n: NotificationModerationRejected) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
        return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
    }

    override fun getTitle(): String {
        return ToolsResources.sCap(
                R.string.notification_admin_moderation_rejected, n.adminName, ToolsResources.sex(
                n.adminSex,
                R.string.he_reject,
                R.string.she_reject
        )
        )
    }

}