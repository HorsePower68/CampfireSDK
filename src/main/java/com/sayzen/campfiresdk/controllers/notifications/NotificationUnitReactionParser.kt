package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources

public class NotificationUnitReactionParser(override val n: NotificationPublicationReaction) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = ""

    override fun getTitle() = ToolsResources.sCap(
            R.string.notification_reaction, n.accountName, ToolsResources.sex(
            n.accountSex,
            R.string.he_react,
            R.string.she_react
    )
    )

}