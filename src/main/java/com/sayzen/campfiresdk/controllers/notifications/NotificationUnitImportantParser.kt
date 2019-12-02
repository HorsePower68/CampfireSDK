package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationImportant
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources

public class NotificationUnitImportantParser(override val n: NotificationPublicationImportant) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean) =
            ToolsResources.sCap(R.string.notifications_important_publication, n.fandomName)

    override fun canShow() = ControllerSettings.notificationsImportant

    override fun doAction() {
        SPost.instance(n.publicationId, 0, Navigator.TO)
    }

}
