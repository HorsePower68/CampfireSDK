package com.sayzen.campfiresdk.controllers.notifications.fandom

import android.content.Intent
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomAccepted
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationFandomAcceptedParser(override val n: NotificationFandomAccepted) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return if (n.accepted) ToolsResources.s(R.string.fandom_notification_accepted, n.fandomName)
        else ToolsResources.sCap(R.string.fandom_notification_rejected, n.fandomName, n.comment)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (n.accepted) SFandom.instance(n.fandomId, ControllerApi.getLanguageId(), Navigator.TO) else SNotifications.instance(Navigator.TO)
    }

}
