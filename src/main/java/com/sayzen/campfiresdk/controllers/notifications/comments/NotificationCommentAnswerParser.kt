package com.sayzen.campfiresdk.controllers.notifications.comments

import android.content.Intent
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sup.dev.android.tools.ToolsResources

public class NotificationCommentAnswerParser(override val n: NotificationCommentAnswer) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = if (n.commentText.isNotEmpty()) n.commentText
    else if (n.commentImageId != 0L || n.commentImagesIds.isNotEmpty()) ToolsResources.s(R.string.app_image)
    else if (n.stickerId != 0L) ToolsResources.s(R.string.app_sticker)
    else ""

    override fun getTitle() = ToolsResources.sCap(
            R.string.notification_comments_answer, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_replied, R.string.she_replied),
            ControllerPublications.getMaskForComment(n.maskText, n.maskPageType)
    )

    override fun canShow() = ControllerSettings.notificationsCommentsAnswers

    override fun doAction() {
        ControllerPublications.toPublication(n.parentPublicationType, n.publicationId, n.commentId)
    }


}
