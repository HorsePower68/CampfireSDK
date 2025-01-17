package com.sayzen.campfiresdk.controllers.notifications.activities

import android.content.Intent
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRejected
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationActivitiesRelayRejectedParser(override val n: NotificationActivitiesRelayRejected) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        if (n.newAccountId > 0)
            return ToolsResources.sCap(R.string.notification_activities_relay_race_rejected_with_new, "@${n.rejectedAccountName}", ToolsResources.sex(n.rejectedAccountSex, R.string.he_reject, R.string.she_reject), n.activityName, ControllerLinks.linkToAccount(n.newAccountName))
        return ToolsResources.sCap(R.string.notification_activities_relay_race_rejected, "@${n.rejectedAccountName}", ToolsResources.sex(n.rejectedAccountSex, R.string.he_reject, R.string.she_reject), n.activityName)
    }

    override fun getTitle() = ToolsResources.s(R.string.app_relay_race)

    override fun canShow() = ControllerSettings.notificationsOther

    override fun doAction() {
        SRelayRaceInfo.instance(n.activityId, Navigator.TO)
    }


}