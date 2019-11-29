package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.fanom.NotificationForgive
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText

public class NotificationForgiveParser(override val n: NotificationForgive) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
        return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment

    }

    override fun getTitle(): String {
        return ToolsResources.sCap(
                R.string.notifications_moderation_forgive, n.moderatorName, ToolsResources.sex(
                n.moderatorSex,
                R.string.he_forgive,
                R.string.she_forgive
        ), n.fandomName
        )
    }

}
