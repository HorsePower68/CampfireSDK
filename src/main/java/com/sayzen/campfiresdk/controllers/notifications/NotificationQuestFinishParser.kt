package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.project.NotificationQuestFinish
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationQuestFinishParser(override val n: NotificationQuestFinish) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return ToolsResources.s(CampfireConstants.getQuest(n.questIndex).text)
    }

    override fun getTitle(): String {
        return ToolsResources.sCap(R.string.notification_quest_finish)
    }

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        if (Navigator.getCurrent() !is SNotifications) SNotifications.instance(Navigator.TO)
    }

}
