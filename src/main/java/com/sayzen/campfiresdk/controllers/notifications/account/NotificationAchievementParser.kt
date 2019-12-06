package com.sayzen.campfiresdk.controllers.notifications.account

import android.content.Intent
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsHTML


public class NotificationAchievementParser(override val n: NotificationAchievement) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun getTitle(): String {
        return ToolsResources.sCap(R.string.achievements_notification)
    }

    override fun asString(html: Boolean): String {
        var text = CampfireConstants.getAchievement(n.achiIndex).getText(false)
        if (html) text = ToolsHTML.i(text)
        return text
    }

    override fun canShow() =  ControllerSettings.notificationsAchievements

    override fun doAction() {
        SAchievements.instance(ControllerApi.account.id, ControllerApi.account.name, n.achiIndex, false, Navigator.TO)
    }

}
