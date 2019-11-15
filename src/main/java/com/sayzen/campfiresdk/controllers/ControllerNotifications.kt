package com.sayzen.campfiresdk.controllers


import android.content.Intent
import android.util.LongSparseArray
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.*
import com.dzen.campfire.api.models.notifications.account.*
import com.dzen.campfire.api.models.notifications.chat.*
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.fanom.*
import com.dzen.campfire.api.models.notifications.post.*
import com.dzen.campfire.api.models.notifications.project.NotificationProjectABParamsChanged
import com.dzen.campfire.api.models.notifications.project.NotificationQuestFinish
import com.dzen.campfire.api.models.notifications.project.NotificationQuestProgress
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsChangeName
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsChangeOwner
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsKarmaCofChanged
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsMakeOwner
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsRemove
import com.dzen.campfire.api.models.notifications.publications.*
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsRemoveToken
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsView
import com.google.firebase.messaging.RemoteMessage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationReaded
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationsCountChanged
import com.sayzen.devsupandroidgoogle.GoogleNotifications
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsNotifications
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.*

object ControllerNotifications {

    val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"

    private val groupId_app = ToolsNotifications.instanceGroup(1, R.string.settings_notifications_filter_app)
    private val groupId_chat = ToolsNotifications.instanceGroup(3, R.string.settings_notifications_filter_chat)

    val chanelChatMessages = ToolsNotifications.instanceChanel(11).setName(R.string.settings_notifications_filter_chat_messages).setGroupId(groupId_chat).setGroupingType(ToolsNotifications.GroupingType.SINGLE).init()
    val chanelOther = ToolsNotifications.instanceChanel(12).setName(R.string.settings_notifications_filter_app_other).setGroupId(groupId_app).init()
    val chanelChatMessages_salient = ToolsNotifications.instanceChanel(13).setName(R.string.settings_notifications_filter_chat_messages_salient).setGroupId(groupId_chat).setGroupingType(ToolsNotifications.GroupingType.SINGLE).setSound(false).setVibration(false).init()
    val chanelOther_salient = ToolsNotifications.instanceChanel(14).setName(R.string.settings_notifications_filter_app_other_salient).setGroupId(groupId_app).setSound(false).setVibration(false).init()

    val TYPE_NOTIFICATIONS = 1
    val TYPE_CHAT = 2
    val lastNotificationsCodes = LongSparseArray<Boolean>()

    var token = ""
    var executorNotification: ExecutorNotification? = null
    var logoColored = 0
    var logoWhite = 0

    internal fun init(
            logoColored: Int,
            logoWhite: Int,
            notificationExecutor: ExecutorNotification
    ) {
        this.logoColored = logoColored
        this.logoWhite = logoWhite
        this.executorNotification = notificationExecutor
        ToolsNotifications.defChanelId = chanelOther.getId()
        GoogleNotifications.init({ token: String? ->
            onToken(token)
        }, { message: RemoteMessage -> onMessage(message) })

        ToolsNotifications.notificationsListener = { intent, type, _ ->
            if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                val n = Notification.instance(Json(intent.getStringExtra(EXTRA_NOTIFICATION)))
                intent.removeExtra(EXTRA_NOTIFICATION)
                removeNotificationFromNew(n.id)
                if (type == ToolsNotifications.IntentType.CLICK) parser(n).doAction()
            }
        }
    }


    //
    //  Message
    //

    private fun onMessage(message: RemoteMessage) {

        info("ControllerNotifications onMessage $message")

        if (!message.data.containsKey("my_data")) return

        val notification = Notification.instance(Json(message.data["my_data"]!!))

        if (lastNotificationsCodes.get(notification.randomCode, false)) return
        lastNotificationsCodes.put(notification.randomCode, true)

        ToolsThreads.main {
            EventBus.post(
                    EventNotification(
                            notification
                    )
            )
        }

        val b1 = canShowBySettings(notification)
        val parser = parser(notification)
        val b2 = parser.canShow()

        addNewNotifications(notification)
        info("ControllerNotifications can show [${notification}] $b1 $b2")

        if (b1 && b2) {
            val text = parser.asString(false)
            info("ControllerNotifications text $text")
            if (text.isNotEmpty()) {

                val canSound = canSoundBySettings(notification)
                val icon = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) logoWhite else logoColored
                val intent = Intent(SupAndroid.appContext, SupAndroid.activityClass)
                val title = SupAndroid.TEXT_APP_NAME ?: ""
                val tag = tag(notification.id)

                intent.putExtra(EXTRA_NOTIFICATION, notification.json(true, Json()).toString())

                parser.post(icon, intent, text, title, tag, canSound)
            }
        }
    }

    fun canShowBySettings(notification: Notification) = executorNotification!!.canShowBySettings(notification)

    fun canSoundBySettings(notification: Notification) = executorNotification!!.canSoundBySettings(notification)

    fun canShowByFilter(notification: Notification) = parser(notification).canShow()

    fun tag(notificationId: Long) = "id_$notificationId"

    fun hideAll(tag: String = "") {
        hide(TYPE_CHAT, tag)
        hide(-1, tag)
    }

    fun hide(type: Int, tag: String = "") {
        when (type) {
            TYPE_CHAT -> {
                chanelChatMessages.cancelAllOrByTagIfNotEmpty(tag)
                chanelChatMessages_salient.cancelAllOrByTagIfNotEmpty(tag)
            }
            else -> {
                chanelOther.cancelAllOrByTagIfNotEmpty(tag)
                chanelOther_salient.cancelAllOrByTagIfNotEmpty(tag)
            }
        }

    }

    //
    //  New Notifications
    //

    private var newNotifications: Array<Notification> = emptyArray()

    private var removeBuffer: ArrayList<NewNotificationKiller> = ArrayList()

    fun addNewNotifications(n: Notification) {
        if (n.isShadow()) return
        for (i in newNotifications) if (i.id == n.id) return
        newNotifications = Array(newNotifications.size + 1) {
            if (it == newNotifications.size) n
            else newNotifications[it]
        }
        actualizeNewNotifications()
    }

    fun setNewNotifications(array: Array<Notification>) {
        newNotifications = array
        actualizeNewNotifications()
    }

    private fun actualizeNewNotifications() {
        val removeList = ArrayList<NewNotificationKiller>()
        for (i in removeBuffer) {
            removeNotificationFromNew(i, false, false)
            if (i.dateCreate + 1000L * 60 * 5 < System.currentTimeMillis()) removeList.add(i)
        }
        for (i in removeList) removeBuffer.remove(i)

        var count = 0
        for (i in newNotifications) if (executorNotification!!.notificationsFilterEnabled(i.getType())) count++
        ToolsStorage.put("ControllerNotification_count", count)
        EventBus.post(EventNotificationsCountChanged())
    }

    fun getNewNotificationsCount() = ToolsStorage.getInt("ControllerNotification_count", 0)

    fun getNewNotifications(types: Array<Long> = emptyArray()): Array<Notification> {
        val list = ArrayList<Notification>()
        for (i in newNotifications) if (types.contains(i.getType())) list.add(i)
        return list.toTypedArray()
    }

    fun removeNotificationFromNewAll() {
        newNotifications = emptyArray()
        actualizeNewNotifications()
        ApiRequestsSupporter.execute(RAccountsNotificationsView(emptyArray(), emptyArray())) {}
    }

    fun removeNotificationFromNew(types: Array<Long>) {
        val subArray = getNewNotifications(types)
        for (i in subArray) newNotifications = ToolsCollections.removeItem(i, newNotifications)
        for (i in subArray) hideAll(tag(i.id))
        val array = Array(subArray.size) { subArray[it].id }
        if (array.isNotEmpty()) RAccountsNotificationsView(array, emptyArray()).send(api)
        actualizeNewNotifications()
    }

    fun removeNotificationFromNew(notificationId: Long) {
        val array = Array(newNotifications.size) { newNotifications[it] }
        for (i in array) if (i.id == notificationId) removeNotificationFromNew(i)
    }

    fun removeNotificationFromNew(n: Notification) {
        removeNotificationFromNew(n, true)
    }

    private fun removeNotificationFromNew(n: Notification, sendCountEvent: Boolean) {
        val oldSize = newNotifications.size
        newNotifications = ToolsCollections.removeItem(n, newNotifications)
        if (oldSize != newNotifications.size) RAccountsNotificationsView(arrayOf(n.id), emptyArray()).send(api)
        hideAll(tag(n.id))
        EventBus.post(EventNotificationReaded(n.id))
        if (sendCountEvent) {
            actualizeNewNotifications()
        }
    }

    fun removeNotificationFromNew(nClass: Any, arg1: Long = 0, arg2: Long = 0) {
        removeNotificationFromNew(NewNotificationKiller(nClass, arg1, arg2), true, true)
    }

    private fun removeNotificationFromNew(k: NewNotificationKiller, addToBuffer: Boolean, sendCountEvent: Boolean) {
        if (addToBuffer) removeBuffer.add(k)
        val array = Array(newNotifications.size) { newNotifications[it] }
        for (n in array) if (willKill(k, n)) removeNotificationFromNew(n, sendCountEvent)
    }

    fun isNew(notificationId: Long): Boolean {
        for (n in newNotifications) if (n.id == notificationId) return true
        return false
    }

    private fun willKill(k: NewNotificationKiller, n: Notification): Boolean {
        if (n::class == k.nClass) {
            when (n) {
                is NotificationFollowsPublication -> if (n.publicationId == k.arg1) return true
                is NotificationPublicationImportant -> if (n.publicationId == k.arg1) return true
                is NotificationComment -> if (n.commentId == k.arg1) return true
                is NotificationCommentAnswer -> if (n.commentId == k.arg1) return true
                is NotificationMention -> if (n.publicationId == k.arg1) return true
            }
        }
        return false
    }

    private class NewNotificationKiller(
            val nClass: Any,
            val arg1: Long,
            val arg2: Long,
            val dateCreate: Long = System.currentTimeMillis()
    )

    //
    //  Token
    //

    fun clearToken(onClear: (() -> Unit)?, onError: (() -> Unit)?) {

        if (token.isEmpty()) {
            onClear!!.invoke()
            return
        }

        RAccountsNotificationsRemoveToken(token)
                .onComplete { onClear?.invoke() }
                .onError { onError?.invoke() }
                .send(api)
    }

    private fun onToken(token: String?) {
        ControllerNotifications.token = token ?: ""
    }

    //
    //  Notifications
    //

    fun parser(n: Notification): Parser {
        return when (n) {
            is NotificationComment -> NotificationCommentParser(n)
            is NotificationAccountsFollowsAdd -> NotificationAccountsFollowsAddParser(n)
            is NotificationCommentAnswer -> NotificationCommentAnswerParser(n)
            is NotificationChatMessage -> NotificationChatMessageParser(n)
            is NotificationChatAnswer -> NotificationChatAnswerParser(n)
            is NotificationFollowsPublication -> NotificationFollowsPublicationParser(n)
            is NotificationFandomRemoveModerator -> NotificationFandomRemoveModeratorParser(n)
            is NotificationFandomMakeModerator -> NotificationFandomMakeModeratorParser(n)
            is NotificationAchievement -> NotificationAchievementParser(n)
            is NotificationFandomAccepted -> NotificationFandomAcceptedParser(n)
            is NotificationPublicationBlock -> NotificationUnitBlockParser(n)
            is NotificationPublicationBlockAfterReport -> NotificationUnitBlockAfterReportParser(n)
            is NotificationKarmaAdd -> NotificationKarmaAddParser(n)
            is NotificationPublicationReaction -> NotificationUnitReactionParser(n)
            is NotificationPublicationImportant -> NotificationUnitImportantParser(n)
            is NotificationModerationToDraft -> NotificationModerationToDraftParser(n)
            is NotificationModerationMultilingualNot -> NotificationModerationMultilingualNotParser(n)
            is NotificationModerationPostClosed -> NotificationModerationPostClosedParser(n)
            is NotificationModerationPostClosedNo -> NotificationModerationPostClosedNoParser(n)
            is NotificationModerationPostTags -> NotificationModerationPostTagsParser(n)
            is NotificationAdminBlock -> NotificationBlockParser(n)
            is NotificationForgive -> NotificationForgiveParser(n)
            is NotificationPunishmentRemove -> NotificationPunishmentRemoveParser(n)
            is NotificationQuestProgress -> NotificationQuestProgressParser(n)
            is NotificationChatMessageChange -> NotificationChatMessageChangeParser(n)
            is NotificationChatMessageRemove -> NotificationChatMessageRemoveParser(n)
            is NotificationChatRead -> NotificationChatReadParser(n)
            is NotificationChatTyping -> NotificationChatTypingParser(n)
            is NotificationProjectABParamsChanged -> NotificationProjectABParamsChangedParser(n)
            is NotificationAdminNameRemove -> NotificationAdminNameRemoveParser(n)
            is NotificationAdminDescriptionRemove -> NotificationAdminDescriptionRemoveParser(n)
            is NotificationAdminLinkRemove -> NotificationAdminLinkRemoveParser(n)
            is NotificationAdminStatusRemove -> NotificationAdminStatusRemoveParser(n)
            is NotificationModerationRejected -> NotificationModerationRejectedParser(n)
            is NotificationQuestFinish -> NotificationQuestFinishParser(n)
            is NotificationRubricsMakeOwner -> NotificationRubricsMakeOwnerParser(n)
            is NotificationRubricsChangeName -> NotificationRubricsChangeNameParser(n)
            is NotificationRubricsChangeOwner -> NotificationRubricsChangeOwnerParser(n)
            is NotificationRubricsKarmaCofChanged -> NotificationRubricsKarmaCofChangedParser(n)
            is NotificationRubricsRemove -> NotificationRubricsRemoveParser(n)
            is NotificationPublicationRestore -> NotificationUnitRestoreParser(n)
            is NotificationAdminPostFandomChange -> NotificationAdminPostFandomChangeParser(n)
            is NotificationMention -> NotificationMentionParser(n)
            else -> NotificationUnknownParserParser(n)

        }
    }

    abstract class Parser(open val n: Notification) {

        abstract fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean)

        abstract fun asString(html: Boolean): String

        open fun getTitle() = ""

        fun canShow() = executorNotification!!.canShowByFilter(n)

        fun doAction() {
            executorNotification!!.doAction(n)
        }

    }

    private class NotificationCommentParser(override val n: NotificationComment) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
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
                title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) ToolsResources.sCap(R.string.notification_post_comment, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
                else ToolsResources.sCap(R.string.notification_post_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
            }
            if (n.parentPublicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
                title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) ToolsResources.sCap(R.string.notification_stickers_pack_comment, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
                else ToolsResources.sCap(R.string.notification_stickers_pack_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, R.string.he_comment, R.string.she_comment))
            }
            if (n.parentPublicationType == API.PUBLICATION_TYPE_MODERATION) {
                title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) ToolsResources.sCap(
                        R.string.notification_moderation_comment, n.accountName, ToolsResources.sex(
                        n.accountSex,
                        R.string.he_comment,
                        R.string.she_comment
                )
                )
                else ToolsResources.sCap(
                        R.string.notification_moderation_comment_watch, n.accountName, ToolsResources.sex(
                        n.accountSex,
                        R.string.he_comment,
                        R.string.she_comment
                )
                )
            }
            return title
        }

    }

    private class NotificationAccountsFollowsAddParser(override val n: NotificationAccountsFollowsAdd) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean) = ToolsResources.sCap(
                R.string.notification_profile_follows_add, n.accountName, ToolsResources.sex(
                n.accountSex,
                R.string.he_subscribed,
                R.string.she_subscribed
        )
        )

    }

    private class NotificationCommentAnswerParser(override val n: NotificationCommentAnswer) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean) = if (n.commentText.isNotEmpty()) n.commentText
        else if (n.commentImageId != 0L || n.commentImagesIds.isNotEmpty()) ToolsResources.s(R.string.app_image)
        else if (n.stickerId != 0L) ToolsResources.s(R.string.app_sticker)
        else ""

        override fun getTitle() = ToolsResources.sCap(
                R.string.notification_comments_answer, n.accountName, ToolsResources.sex(
                n.accountSex,
                R.string.he_replied,
                R.string.she_replied
        )
        )

    }

    private class NotificationChatMessageParser(override val n: NotificationChatMessage) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            val titleV: String
            var textV = text
            val publication = n.publicationChatMessage
            val tagV = n.tag.asTag()

            val chatMessagesCount = ControllerChats.getMessagesCount(n.tag)
            if (n.tag.chatType == API.CHAT_TYPE_PRIVATE) {
                titleV = publication.creatorName
                if (chatMessagesCount > 1) textV = ToolsResources.s(
                        R.string.notification_chat_private_many, chatMessagesCount, ToolsResources.getPlural(
                        R.plurals.new_fem, chatMessagesCount
                ), ToolsResources.getPlural(R.plurals.private_, chatMessagesCount), ToolsResources.getPlural(
                        R.plurals.messages, chatMessagesCount
                )
                )
            } else {
                titleV = publication.fandomName
                if (chatMessagesCount > 1) textV = ToolsResources.s(
                        R.string.notification_chat_many, chatMessagesCount, ToolsResources.getPlural(
                        R.plurals.new_fem, chatMessagesCount
                ), ToolsResources.getPlural(R.plurals.messages, chatMessagesCount)
                )
            }

            (if (sound) chanelChatMessages else chanelChatMessages_salient).post(icon, titleV, textV, intent, tagV)
        }

        override fun asString(html: Boolean): String {
            if (n.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
                return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_image)
                else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_sticker)
                else n.publicationChatMessage.creatorName + ": " + n.publicationChatMessage.text
            } else {
                return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) ToolsResources.s(R.string.app_image)
                else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) ToolsResources.s(R.string.app_sticker)
                else n.publicationChatMessage.text
            }
        }

    }

    private class NotificationChatAnswerParser(override val n: NotificationChatAnswer) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            val publication = n.publicationChatMessage
            val tagV = n.tag.asTag()

            (if (sound) chanelChatMessages else chanelChatMessages_salient).post(icon, publication.fandomName, text, intent, tagV)
        }

        override fun asString(html: Boolean): String {
            return if (n.publicationChatMessage.resourceId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_image)
            else if (n.publicationChatMessage.stickerId != 0L && n.publicationChatMessage.text.isEmpty()) n.publicationChatMessage.creatorName + ": " + ToolsResources.s(R.string.app_sticker)
            else n.publicationChatMessage.creatorName + ": " + n.publicationChatMessage.text
        }

    }

    private class NotificationFollowsPublicationParser(override val n: NotificationFollowsPublication) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean) = ToolsResources.sCap(
                R.string.notifications_follows_new_content, n.accountName, ToolsResources.sex(
                n.accountSex,
                R.string.he_make,
                R.string.she_make
        )
        )

    }

    private class NotificationFandomRemoveModeratorParser(override val n: NotificationFandomRemoveModerator) :
            Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean) =
                ToolsResources.sCap(R.string.notifications_fandom_remove_moderator, n.fandomName)

    }

    private class NotificationFandomMakeModeratorParser(override val n: NotificationFandomMakeModerator) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean) =
                ToolsResources.sCap(R.string.notifications_fandom_make_moderator, n.fandomName)

    }

    private class NotificationAchievementParser(override val n: NotificationAchievement) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.achievements_notification)
        }

        override fun asString(html: Boolean): String {
            var text = CampfireConstants.getAchievement(n.achiIndex).getText(false)
            if (html) text = ToolsHTML.i(text)
            return text
        }

    }

    private class NotificationFandomAcceptedParser(override val n: NotificationFandomAccepted) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            return if (n.accepted) ToolsResources.s(R.string.fandom_notification_accepted, n.fandomName)
            else ToolsResources.sCap(R.string.fandom_notification_rejected, n.fandomName, n.comment)
        }

    }

    private class NotificationUnitBlockParser(override val n: NotificationPublicationBlock) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
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

    private class NotificationUnitBlockAfterReportParser(override val n: NotificationPublicationBlockAfterReport) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
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
            return ToolsResources.sCap(R.string.moderation_notification_publication_is_blocked_by_report)
        }

    }

    private class NotificationKarmaAddParser(override val n: NotificationKarmaAdd) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val name = if(n.accountId == 0L) ToolsResources.s(R.string.app_anonymous) else n.accountName
            val karmsS = if (!html) "" + (n.karmaCount / 100) else ToolsHTML.font_color(
                    "" + (n.karmaCount / 100),
                    if (n.karmaCount < 0) ToolsHTML.color_red else ToolsHTML.color_green
            )
            if (n.publicationType == API.PUBLICATION_TYPE_POST) return ToolsResources.sCap(
                    R.string.notification_post_karma, name, ToolsResources.sex(
                    n.accountSex,
                    R.string.he_rate,
                    R.string.she_rate
            ), karmsS)
            if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) return ToolsResources.sCap(
                    R.string.notification_comments_karma, name, ToolsResources.sex(
                    n.accountSex,
                    R.string.he_rate,
                    R.string.she_rate
            ), karmsS)
            if (n.publicationType == API.PUBLICATION_TYPE_MODERATION) return ToolsResources.sCap(
                    R.string.notification_moderation_karma, name, ToolsResources.sex(
                    n.accountSex,
                    R.string.he_rate,
                    R.string.she_rate
            ), karmsS)
            if (n.publicationType == API.PUBLICATION_TYPE_REVIEW) return ToolsResources.sCap(
                    R.string.notification_karma_review, name, ToolsResources.sex(
                    n.accountSex,
                    R.string.he_rate,
                    R.string.she_rate
            ), karmsS)
            if (n.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) return ToolsResources.sCap(
                    R.string.notification_karma_stickers_pack, name, ToolsResources.sex(
                    n.accountSex,
                    R.string.he_rate,
                    R.string.she_rate
            ), karmsS)
            return ""
        }

    }

    private class NotificationUnitReactionParser(override val n: NotificationPublicationReaction) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean) = ""

        override fun getTitle() = ToolsResources.sCap(
                R.string.notification_reaction, n.accountName, ToolsResources.sex(
                n.accountSex,
                R.string.he_react,
                R.string.she_react
        )
        )

    }

    private class NotificationUnitImportantParser(override val n: NotificationPublicationImportant) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean) =
                ToolsResources.sCap(R.string.notifications_important_publication, n.fandomName)

    }

    private class NotificationModerationToDraftParser(override val n: NotificationModerationToDraft) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_to_drafts, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_return,
                    R.string.she_return
            )
            )
        }

    }

    private class NotificationModerationMultilingualNotParser(override val n: NotificationModerationMultilingualNot) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_multilingual_not, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_make,
                    R.string.she_make
            )
            )
        }

    }

    private class NotificationModerationPostClosedParser(override val n: NotificationModerationPostClosed) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_post_close, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_close,
                    R.string.she_close
            )
            )
        }

    }

    private class NotificationModerationPostClosedNoParser(override val n: NotificationModerationPostClosedNo) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_post_close_no, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_open,
                    R.string.she_open
            )
            )
        }

    }

    private class NotificationModerationPostTagsParser(override val n: NotificationModerationPostTags) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_tags, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_changed,
                    R.string.she_changed
            )
            )
        }

    }

    private class NotificationBlockParser(override val n: NotificationAdminBlock) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else " " + ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return if (n.blockAccountDate > 0) {
                (ToolsResources.sCap(
                        R.string.moderation_notification_blocked,
                        ToolsDate.dateToString(n.blockAccountDate)
                ))
            } else {
                (ToolsResources.sCap(R.string.moderation_notification_warned))
            }
        }

    }

    private class NotificationForgiveParser(override val n: NotificationForgive) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment

        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notifications_moderation_forgive, n.moderatorName, ToolsResources.sex(
                    n.moderatorSex,
                    R.string.he_forgive,
                    R.string.she_forgive
            ), n.fandomName
            )
        }

    }

    private class NotificationPunishmentRemoveParser(override val n: NotificationPunishmentRemove) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean) = ToolsResources.sCap(
                R.string.notification_punishment_remove, n.fromAccountName, ToolsResources.sex(
                n.fromAccountSex,
                R.string.he_remove,
                R.string.she_remove
        )
        )

    }

    private class NotificationQuestProgressParser(override val n: NotificationQuestProgress) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationChatMessageChangeParser(override val n: NotificationChatMessageChange) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationChatMessageRemoveParser(override val n: NotificationChatMessageRemove) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationChatReadParser(override val n: NotificationChatRead) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationChatTypingParser(override val n: NotificationChatTyping) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationProjectABParamsChangedParser(override val n: NotificationProjectABParamsChanged) :
            Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

    }

    private class NotificationAdminNameRemoveParser(override val n: NotificationAdminNameRemove) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_name_remove, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_remove,
                    R.string.she_remove
            )
            )
        }

    }

    private class NotificationAdminDescriptionRemoveParser(override val n: NotificationAdminDescriptionRemove) :
            Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_description_remove, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_remove,
                    R.string.she_remove
            )
            )
        }

    }

    private class NotificationAdminLinkRemoveParser(override val n: NotificationAdminLinkRemove) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_link_remove, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_remove,
                    R.string.she_remove
            )
            )
        }

    }

    private class NotificationAdminStatusRemoveParser(override val n: NotificationAdminStatusRemove) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_status_remove, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_remove,
                    R.string.she_remove
            )
            )
        }

    }

    private class NotificationModerationRejectedParser(override val n: NotificationModerationRejected) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_moderation_rejected, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_reject,
                    R.string.she_reject
            )
            )
        }

    }

    private class NotificationQuestFinishParser(override val n: NotificationQuestFinish) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            return ToolsResources.s(CampfireConstants.getQuest(n.questIndex).text)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_quest_finish)
        }

    }

    private class NotificationRubricsMakeOwnerParser(override val n: NotificationRubricsMakeOwner) : Parser(n) {
        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_rubric_make_owner, n.adminName, ToolsResources.sex(n.adminSex, R.string.he_make, R.string.she_make), n.rubricName)
        }
    }

    private class NotificationRubricsChangeNameParser(override val n: NotificationRubricsChangeName) : Parser(n) {
        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_rubric_change_name, n.adminName, ToolsResources.sex(n.adminSex, R.string.he_changed, R.string.she_changed), n.rubricOldName, n.rubricNewName)
        }
    }

    private class NotificationRubricsChangeOwnerParser(override val n: NotificationRubricsChangeOwner) : Parser(n) {
        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_rubric_change_owner, n.rubricName, ToolsResources.sex(n.adminSex, R.string.he_changed, R.string.she_changed), n.rubricName, n.newOwnerName)
        }
    }

    private class NotificationRubricsKarmaCofChangedParser(override val n: NotificationRubricsKarmaCofChanged) : Parser(n) {
        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val text_cof = if (!html) ToolsText.numToStringRound(n.cofChange / 100.0, 2) else "{" + (if (n.cofChange < 0) "red " else "green ") + ToolsText.numToStringRound(n.cofChange / 100.0, 2) + "}"
            val text_value = if (!html) ToolsText.numToStringRound(n.newCof / 100.0, 2) else "{" + (if (n.newCof < 0) "red " else "green ") + ToolsText.numToStringRound(n.newCof / 100.0, 2) + "}"
            return ToolsResources.s(R.string.notification_rubric_cof_text, text_cof, text_value)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_rubric_cof_title, n.rubricName)
        }
    }

    private class NotificationRubricsRemoveParser(override val n: NotificationRubricsRemove) : Parser(n) {
        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_rubric_remove, n.adminName, ToolsResources.sex(n.adminSex, R.string.he_remove, R.string.she_remove), n.rubricName)
        }
    }

    private class NotificationUnitRestoreParser(override val n: NotificationPublicationRestore) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(R.string.notification_admin_moderation_restore)
        }

    }

    private class NotificationAdminPostFandomChangeParser(override val n: NotificationAdminPostFandomChange) :
            Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, title, text, intent, tag)
        }

        override fun asString(html: Boolean): String {
            val comment = if (!html) n.comment else ToolsHTML.i(n.comment)
            return (if (ToolsText.empty(n.comment)) "" else ToolsResources.s(R.string.moderation_notification_moderator_comment) + " " + comment)
        }

        override fun getTitle(): String {
            return ToolsResources.sCap(
                    R.string.notification_admin_post_fandom_change, n.adminName, ToolsResources.sex(
                    n.adminSex,
                    R.string.he_move,
                    R.string.she_move
            ), n.oldFandomName, n.newFandomName
            )
        }

    }

    private class NotificationMentionParser(override val n: NotificationMention) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
            (if (sound) chanelOther else chanelOther_salient).post(icon, getTitle(), text, intent, tag)
        }

        override fun asString(html: Boolean) = n.text

        override fun getTitle() = ToolsResources.sCap(R.string.notification_mention, n.fromAccountName, ToolsResources.sex(n.fromAccountSex, R.string.he_mentioned, R.string.she_mentioned))

    }

    private class NotificationUnknownParserParser(override val n: Notification) : Parser(n) {

        override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {

        }

        override fun asString(html: Boolean) = ""

        override fun getTitle() = ""

    }

    interface ExecutorNotification {

        fun canSoundBySettings(notification: Notification): Boolean

        fun canShowBySettings(notification: Notification): Boolean

        fun canShowByFilter(notification: Notification): Boolean

        fun notificationsFilterEnabled(type: Long): Boolean

        fun doAction(notification: Notification)

    }

}