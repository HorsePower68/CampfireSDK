package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.account.NotificationPunishmentRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationPunishmentRemoveParser(override val n: NotificationPunishmentRemove) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean) = ToolsResources.sCap(
            R.string.notification_punishment_remove, n.fromAccountName, ToolsResources.sex(
            n.fromAccountSex,
            R.string.he_remove,
            R.string.she_remove
    )
    )

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (Navigator.getCurrent() !is SNotifications) SNotifications.instance(Navigator.TO)
    }

}