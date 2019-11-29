package com.sayzen.campfiresdk.controllers.notifications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sup.dev.android.tools.ToolsResources


public class NotificationCommentParser(override val n: NotificationComment) : ControllerNotifications.Parser(n) {

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return if (n.commentText.isNotEmpty()) n.commentText
        else if (n.commentImageId != 0L || n.commentImagesIds.isNotEmpty()) ToolsResources.s(R.string.app_image)
        else if (n.stickerId != 0L) ToolsResources.s(R.string.app_sticker)
        else ""
    }

    override fun getTitle(): String {
        var title = ""
        if (n.parentPublicationType == API.PUBLICATION_TYPE_POST) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId)
                ToolsResources.sCap(R.string.notification_post_comment, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment),
                        ControllerPublications.getMaskForPost(n.maskText, n.maskPageType))
            else
                ToolsResources.sCap(R.string.notification_post_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment),
                        ControllerPublications.getMaskForPost(n.maskText, n.maskPageType))
        }
        if (n.parentPublicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) ToolsResources.sCap(R.string.notification_stickers_pack_comment, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
            else ToolsResources.sCap(R.string.notification_stickers_pack_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
        }
        if (n.parentPublicationType == API.PUBLICATION_TYPE_MODERATION) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) ToolsResources.sCap(R.string.notification_moderation_comment, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
            else ToolsResources.sCap(R.string.notification_moderation_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
        }
        return title
    }

}