package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsHTML
import com.sup.dev.java.tools.ToolsText

public class NotificationPublicationBlockParser(override val n: NotificationPublicationBlock) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
        return (""
                + (if (n.blockAccountDate > 0) " " + ToolsResources.s(
                R.string.moderation_notification_account_is_blocked,
                ToolsDate.dateToString(n.blockAccountDate)
        ) else "")
                + if (ToolsText.empty(n.comment)) "" else " " + ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
    }

    override fun getTitle(): String {
        return ToolsResources.sCap(if (n.blockLastUnits) R.string.moderation_notification_publications_is_blocked else if (n.blockUnitType == API.PUBLICATION_TYPE_REVIEW) R.string.moderation_notification_review_is_blocked else R.string.moderation_notification_publication_is_blocked)
    }

}
