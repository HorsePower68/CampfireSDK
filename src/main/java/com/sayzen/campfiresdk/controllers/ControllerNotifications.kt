package com.sayzen.campfiresdk.controllers


import android.content.Intent
import android.util.LongSparseArray
import com.dzen.campfire.api.models.notifications.*
import com.dzen.campfire.api.models.notifications.account.*
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceLost
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesNewPost
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRejected
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
import com.sayzen.campfiresdk.controllers.notifications.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationReaded
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationsCountChanged
import com.sayzen.devsupandroidgoogle.GoogleNotifications
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsNotifications
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
    var logoColored = 0
    var logoWhite = 0

    internal fun init(
            logoColored: Int,
            logoWhite: Int
    ) {
        this.logoColored = logoColored
        this.logoWhite = logoWhite
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

    fun canShowBySettings(notification: Notification): Boolean {
        if (!ControllerSettings.notifications) return false
        if (notification.dateCreate <= ControllerSettings.notifyDateChecked) return false
        if (ControllerSettings.salientTime > System.currentTimeMillis()) return false
        return true
    }

    fun canSoundBySettings(notification: Notification):Boolean{
        if (ControllerSettings.notificationsSalientOnTimeEnabled) {
            val current = ToolsDate.getCurrentMinutesOfDay()
            val start = ControllerSettings.notificationsSalientOnTimeStartH * 60 + ControllerSettings.notificationsSalientOnTimeStartM
            val end = ControllerSettings.notificationsSalientOnTimeEndH * 60 + ControllerSettings.notificationsSalientOnTimeEndM
            if (start < end) {
                if (current in start..end) return false
            } else {
                if (current >= start || current <= end) return false
            }
        }
        return true
    }

    fun notificationsFilterEnabled(type: Long) = ControllerSettings.notificationsFilterEnabled(type)

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
        for (i in newNotifications) if (notificationsFilterEnabled(i.getType())) count++
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
            is NotificationPublicationBlock -> NotificationPublicationBlockParser(n)
            is NotificationPublicationBlockAfterReport -> NotificationPublicationBlockAfterReportParser(n)
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
            is NotificationActivitiesRelayRaceTurn -> NotificationActivitiesRelayRaceTurnParser(n)
            is NotificationActivitiesRelayRaceLost -> NotificationActivitiesRelayRaceLostParser(n)
            is NotificationActivitiesNewPost -> NotificationActivitiesRelayRaceNewPostParser(n)
            is NotificationActivitiesRelayRejected -> NotificationActivitiesRelayRejectedParser(n)
            else -> NotificationUnknownParserParser(n)

        }
    }

    abstract class Parser(open val n: Notification) {

        abstract fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean)

        abstract fun asString(html: Boolean): String

        abstract fun canShow() : Boolean

        abstract fun doAction()

        open fun getTitle() = ""


    }

}