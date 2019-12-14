package com.sayzen.campfiresdk.controllers.notifications.project

import android.content.Intent
import com.dzen.campfire.api.models.notifications.project.NotificationDonate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.activities.support.SDonate
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

public class NotificationDonateParser(override val n: NotificationDonate) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return ToolsResources.s(R.string.notification_donate, ToolsText.numToStringRoundAndTrim(n.sum / 100.0, 2))
    }

    override fun getTitle(): String {
        return ToolsResources.sCap(R.string.app_donate)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (Navigator.getCurrent() !is SDonate) SDonate.instance(Navigator.TO)
    }

}
