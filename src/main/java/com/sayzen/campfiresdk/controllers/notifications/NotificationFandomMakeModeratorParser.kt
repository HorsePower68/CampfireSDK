package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.fanom.NotificationFandomMakeModerator
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources


public class NotificationFandomMakeModeratorParser(override val n: NotificationFandomMakeModerator) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean) =
            ToolsResources.sCap(R.string.notifications_fandom_make_moderator, n.fandomName)

}