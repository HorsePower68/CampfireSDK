package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationKarmaAdd
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsHTML


public class NotificationKarmaAddParser(override val n: NotificationKarmaAdd) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val name = if (n.accountId == 0L) ToolsResources.s(R.string.app_anonymous) else n.accountName
        val karmsS = if (!html) "" + (n.karmaCount / 100) else ToolsHTML.font_color(
                "" + (n.karmaCount / 100),
                if (n.karmaCount < 0) ToolsHTML.color_red else ToolsHTML.color_green
        )
        if (n.publicationType == API.PUBLICATION_TYPE_POST) {
            return ToolsResources.sCap(R.string.notification_post_karma, name, ToolsResources.sex(n.accountSex, R.string.he_rate, R.string.she_rate), ControllerPublications.getMaskForPost(n.maskText, n.maskPageType), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            return ToolsResources.sCap(R.string.notification_comments_karma, name, ToolsResources.sex(n.accountSex, R.string.he_rate, R.string.she_rate), ControllerPublications.getMaskForComment(n.maskText, n.maskPageType), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_MODERATION) {
            return ToolsResources.sCap(R.string.notification_moderation_karma, name, ToolsResources.sex(n.accountSex, R.string.he_rate, R.string.she_rate), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_REVIEW) {
            return ToolsResources.sCap(R.string.notification_karma_review, name, ToolsResources.sex(n.accountSex, R.string.he_rate, R.string.she_rate), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            return ToolsResources.sCap(R.string.notification_karma_stickers_pack, name, ToolsResources.sex(n.accountSex, R.string.he_rate, R.string.she_rate), karmsS)
        }
        return ""

    }

    override fun canShow() =  ControllerSettings.notificationsKarma

    override fun doAction() {
        doActionNotificationKarmaAdd(n)
    }

    private fun doActionNotificationKarmaAdd(n: NotificationKarmaAdd) {
        var publicationId = n.publicationId
        var publicationType = n.publicationType
        var toCommentId: Long = 0

        if (publicationType == API.PUBLICATION_TYPE_COMMENT) {
            publicationId = n.parentPublicationId
            publicationType = n.parentPublicationType
            toCommentId = n.publicationId
        }

        if (publicationType != 0L) ControllerPublications.toPublication(publicationType, publicationId, toCommentId)
        else SNotifications.instance(Navigator.TO)
    }
}