package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.notifications.chat.NotificationChatRead
import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.dzen.campfire.api.models.units.chat.UnitChatMessage
import com.dzen.campfire.api.requests.chat.*
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChangeImageBackground
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetChooseImage
import com.sup.dev.android.views.widgets.WidgetField
import com.sup.dev.android.views.widgets.WidgetMenu
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.classes.items.ItemNullable2
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.tools.ToolsThreads

object ControllerChats {

    val eventBus = EventBus
            .subscribe(EventNotification::class) { e: EventNotification -> onNotification(e) }
            .subscribe(EventChatRead::class) { e: EventChatRead -> onEventChatRead(e) }

    val readDates = HashMap<ChatTag, Long>()

    internal fun init() {

    }

    //
    //  Methods
    //

    fun getSystemText(unit: UnitChatMessage): String {

        when {
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_BLOCK -> return if (unit.blockDate > 0) {
                "${ToolsResources.s(R.string.chat_block_message, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_blocked, R.string.she_blocked), ControllerApi.linkToUser(unit.systemTargetName))} " + "\n ${ToolsResources.s(R.string.app_comment)}: ${unit.systemComment}"
            } else {
                "${ToolsResources.s(R.string.chat_system_block, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_warn, R.string.she_warn), ControllerApi.linkToUser(unit.systemTargetName))} " + "\n ${ToolsResources.s(R.string.app_comment)}: ${unit.systemComment}"
            }
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_ADD_USER -> return "${ToolsResources.s(R.string.chat_system_add, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_add, R.string.she_add), ControllerApi.linkToUser(unit.systemTargetName))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_CREATE -> return "${ToolsResources.s(R.string.chat_system_create, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_created, R.string.she_created))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_REMOVE_USER -> return "${ToolsResources.s(R.string.chat_system_remove, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_remove, R.string.she_remove), ControllerApi.linkToUser(unit.systemTargetName))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_CHANGE_IMAGE -> return "${ToolsResources.s(R.string.chat_system_change_image, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_changed, R.string.she_changed), unit.systemTargetName)}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_CHANGE_NAME -> return "${ToolsResources.s(R.string.chat_system_change_name, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_changed, R.string.she_changed), unit.systemTargetName)}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_LEAVE -> return "${ToolsResources.s(R.string.chat_system_leave, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_leave, R.string.she_leave))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_ENTER -> return "${ToolsResources.s(R.string.chat_system_enter, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_reenter, R.string.she_reenter))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_PARAMS -> return "${ToolsResources.s(R.string.chat_system_params, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_changed, R.string.he_changed))}"
            unit.systemType == UnitChatMessage.SYSTEM_TYPE_LEVEL -> return "${ToolsResources.s(R.string.chat_system_level, ControllerApi.linkToUser(unit.systemOwnerName), ToolsResources.sex(unit.systemOwnerSex, R.string.he_changed, R.string.he_changed),  ControllerApi.linkToUser(unit.systemTargetName), ToolsResources.s(if(unit.systemTag == API.CHAT_MEMBER_LVL_USER) R.string.app_user else if(unit.systemTag == API.CHAT_MEMBER_LVL_MODERATOR) R.string.app_moderator else R.string.app_admin))}"
            else -> return ""
        }

    }

    fun instanceChatPopup(tag: ChatTag, memberStatus: Long?, onRemove: () -> Unit = {}): WidgetMenu {
        return WidgetMenu()
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToChat(tag.targetId));ToolsToast.show(R.string.app_copied) }.condition(tag.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .add(R.string.app_copy_link_with_language) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToChat(tag.targetId, tag.targetSubId));ToolsToast.show(R.string.app_copied) }.condition(tag.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .add(R.string.app_copy_link) { _, _ -> ToolsAndroid.setToClipboard(ControllerApi.linkToConf(tag.targetId));ToolsToast.show(R.string.app_copied) }.condition(tag.chatType == API.CHAT_TYPE_CONFERENCE)
                .add(R.string.app_edit) { _, _ -> SChatCreate.instance(tag.targetId, Navigator.TO) }.condition(tag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .add(R.string.chat_remove) { _, _ -> chatRemove(tag, onRemove) }.condition(tag.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .add(R.string.chat_clear_history) { _, _ -> clearHistory(tag, onRemove) }.condition(tag.chatType != API.CHAT_TYPE_FANDOM_ROOT)
                .add(R.string.chat_leave) { _, _ -> leave(tag) }.condition(tag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .add(R.string.chat_enter) { _, _ -> enter(tag) }.condition(tag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_LEAVE)
                .add(R.string.fandoms_menu_background_change) { _, _ -> changeBackgroundImage(tag.targetId, tag.targetSubId) }.condition(tag.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(tag.targetId, tag.targetSubId, API.LVL_MODERATOR_BACKGROUND_IMAGE)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(R.string.fandoms_menu_background_remove) { _, _ -> removeBackgroundImage(tag.targetId, tag.targetSubId) }.condition(tag.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(tag.targetId, tag.targetSubId, API.LVL_MODERATOR_BACKGROUND_IMAGE)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
    }

    private fun changeBackgroundImage(fandomId:Long, languageId:Long) {
        WidgetChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.FANDOM_IMG_BACKGROUND_W, API.FANDOM_IMG_BACKGROUND_H) { _, b, _, _, _, _ ->
                        WidgetField().setHint(R.string.moderation_widget_comment).setOnCancel(R.string.app_cancel)
                                .setMin(1)
                                .setOnEnter(R.string.app_change) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.FANDOM_IMG_BACKGROUND_W, API.FANDOM_IMG_BACKGROUND_H), API.FANDOM_IMG_BACKGROUND_WEIGHT)
                                        changeBackgroundImageNow(fandomId, languageId, dialog, image, comment)
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    private fun removeBackgroundImage(fandomId:Long, languageId:Long) {
        WidgetField().setHint(R.string.moderation_widget_comment).setOnCancel(R.string.app_cancel)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(R.string.app_change) { _, comment ->
                    changeBackgroundImageNow(fandomId, languageId, ToolsView.showProgressDialog(), null, comment)
                }
                .asSheetShow()
    }

    private fun changeBackgroundImageNow(fandomId:Long, languageId:Long, dialog: Widget, bytes: ByteArray?, comment: String) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsModerationChangeImageBackground(fandomId, languageId, bytes, comment)) { r ->
            EventBus.post(EventFandomBackgroundImageChanged(fandomId, languageId, r.imageId))
            ToolsToast.show(R.string.app_done)
        }
    }

    fun enter(tag: ChatTag) {
        ApiRequestsSupporter.executeProgressDialog(RChatEnter(tag)) { _ ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventChatMemberStatusChanged(tag, ControllerApi.account.id, API.CHAT_MEMBER_STATUS_ACTIVE))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(R.string.error_chat_access) }
    }

    fun leave(tag: ChatTag) {
        ApiRequestsSupporter.executeProgressDialog(RChatLeave(tag)) { _ ->
            ToolsToast.show(R.string.app_done)
            EventBus.post(EventChatMemberStatusChanged(tag, ControllerApi.account.id, API.CHAT_MEMBER_STATUS_LEAVE))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(R.string.error_chat_access) }
    }

    fun clearHistory(tag: ChatTag, onRemove: () -> Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.chat_clear_history, R.string.app_clear_2, RChatClearHistory(tag)) { _ ->
            EventBus.post(EventChatRemoved(tag))
            onRemove.invoke()
            ToolsToast.show(R.string.app_done)
        }
    }

    fun chatRemove(tag: ChatTag, onRemove: () -> Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(R.string.chat_remove, R.string.app_remove, RChatRemove(tag)) { _ ->
            EventBus.post(EventChatRemoved(tag))
            onRemove.invoke()
            ToolsToast.show(R.string.app_done)
        }
    }

    fun readRequest(tag: ChatTag) {
        setMessagesCount(tag, 0, false)
        putRead(tag, System.currentTimeMillis())
        ApiRequestsSupporter.execute(RChatRead(tag)) { r ->
            putRead(tag, r.date)
        }
    }

    //
    //  Messages Count
    //

    fun clearMessagesCount() {
        ToolsStorage.put("ControllerChats", JsonArray())
    }

    fun getMessagesCount() = ToolsStorage.getInt("ControllerChats_count", 0)

    fun incrementMessagesCount(tag: ChatTag, showInNavigation: Boolean) {
        setMessagesCount(tag, getMessagesCount(tag) + 1, showInNavigation)
    }

    fun setMessagesCount(tag: ChatTag, count: Int, showInNavigation: Boolean) {
        val list = loadMessagesCounts()
        var seted = false
        for (item in list) {
            if (item.a1 == tag) {
                if (count < 1) list.remove(item)
                else item.a2 = count
                seted = true
                break
            }
        }

        if (!seted && count > 0) list.add(Item3(tag, count, showInNavigation))

        saveMessagesCounts(list)

        var countV = 0
        for (i in list) if (i.a3) countV++

        ToolsStorage.put("ControllerChats_count", countV)

        EventBus.post(EventChatMessagesCountChanged(tag))
    }

    fun getMessagesCount(tag: ChatTag): Int {
        val list = loadMessagesCounts()
        var count = 0
        for (item in list) if (item.a1 == tag) {
            count = item.a2
            break
        }
        return count
    }

    private fun saveMessagesCounts(list: ArrayList<Item3<ChatTag, Int, Boolean>>) {
        val array = JsonArray()
        for (item in list) array.put(Json().put("tag", item.a1.asTag()).put("count", item.a2).put("showInNavigation", item.a3).toString())
        ToolsStorage.put("ControllerChats", array)
    }

    private fun loadMessagesCounts(): ArrayList<Item3<ChatTag, Int, Boolean>> {
        val array = ToolsStorage.getJsonArray("ControllerChats", JsonArray()).getJsons()
        val list = ArrayList<Item3<ChatTag, Int, Boolean>>()
        for (j in array) try {
            list.add(Item3(ChatTag(j!!.getString("tag", "0,0,0")), j.getInt("count"), j.getBoolean("showInNavigation")))
        } catch (e: Exception) {
            err(e)
        }
        return list
    }


    //
    //  Typing
    //

    private val TYPING_TIME = 1000 * 7
    private val typingList = HashMap<String, ArrayList<ItemNullable2<Long, String>>>()
    private var scheduled = false

    private fun updateTyping() {
        for (tagS in typingList.keys) {
            val tag = ChatTag(tagS)
            val list = typingList[tagS]
            var changed = false
            var i = 0
            while (i < list!!.size) {
                if (list[i].a1!! <= System.currentTimeMillis() - TYPING_TIME + 100) {
                    list.removeAt(i--)
                    changed = true
                }
                i++
            }
            if (changed) {
                EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
            }
        }
        if (!scheduled && typingList.isNotEmpty()) {
            scheduled = true
            ToolsThreads.main(1000) {
                scheduled = false
                updateTyping()
            }
        }
    }

    fun removeTyping(tag: ChatTag, name: String) {
        val list = typingList[tag.asTag()] ?: return
        var i = 0
        while (i < list.size) {
            if (list[i].a2 == name) list.removeAt(i--)
            i++
        }
        EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
    }

    private fun addTyping(tag: ChatTag, name: String?) {
        var list = typingList[tag.asTag()]
        if (list == null) {
            list = ArrayList()
            typingList[tag.asTag()] = list
        }
        for (i in list) if (i.a2 == name) return
        list.add(ItemNullable2(System.currentTimeMillis(), name))
        EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
        updateTyping()
    }

    fun getTypingText(tag: ChatTag): String? {
        updateTyping()
        val list = typingList[tag.asTag()]
        if (list == null || list.isEmpty()) return null

        var name = list[0].a2
        for (i in 1 until list.size) name += ", " + list[i].a2!!
        return name + " " + ToolsResources.s(R.string.app_is_typing)
    }

    //
    //  Read
    //

    fun putRead(tag: ChatTag, date: Long) {
        if (readDates[tag] == null || date > readDates[tag] ?: 0) {
            readDates[tag] = date
            EventBus.post(EventChatReadDateChanged(tag))
        }

    }

    fun getRead(tag: ChatTag): Long {
        return readDates[tag] ?: Long.MAX_VALUE
    }

    fun isRead(tag: ChatTag, date: Long): Boolean {
        return date < getRead(tag)
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            removeTyping(e.notification.tag, e.notification.unitChatMessage.creatorName)
            val n = e.notification
            incrementMessagesCount(n.tag, n.subscribed)
            putRead(n.tag, n.dateCreate)
        }
        if (e.notification is NotificationChatAnswer) {
            removeTyping(e.notification.tag, e.notification.unitChatMessage.creatorName)
            val n = e.notification
            incrementMessagesCount(n.tag, n.subscribed)
            putRead(n.tag, n.dateCreate)
        }
        if (e.notification is NotificationChatTyping) {
            addTyping(e.notification.chatTag, e.notification.accountName)
            val n = e.notification
            putRead(n.chatTag, n.dateCreate)
        }
        if (e.notification is NotificationChatRead) {
            val n = e.notification
            putRead(n.tag, n.date)
        }
    }

    private fun onEventChatRead(e: EventChatRead) {
        setMessagesCount(e.tag, 0, false)
    }

}