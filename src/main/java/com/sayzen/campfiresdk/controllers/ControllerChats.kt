package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.ChatTag
import com.dzen.campfire.api.models.notifications.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.NotificationChatMessage
import com.dzen.campfire.api.models.notifications.NotificationChatRead
import com.dzen.campfire.api.models.notifications.NotificationChatTyping
import com.dzen.campfire.api.requests.chat.RChatRead
import com.dzen.campfire.api.requests.chat.RChatRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsToast
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

    fun instanceChatPopup(tag: ChatTag, onRemove: () -> Unit = {}): WidgetMenu {
        return WidgetMenu()
                .add(R.string.chat_read) { w, card -> readRequest(tag) }
                .add(R.string.app_copy_link) { w, card ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToChat(tag.targetId))
                    ToolsToast.show(R.string.app_copied)
                }.condition(tag.chatType == API.CHAT_TYPE_FANDOM)
                .add(R.string.app_copy_link_with_language) { w, card ->
                    ToolsAndroid.setToClipboard(ControllerApi.linkToChat(tag.targetId, tag.targetSubId))
                    ToolsToast.show(R.string.app_copied)
                }.condition(tag.chatType == API.CHAT_TYPE_FANDOM)
                .add(R.string.chat_remove) { w, card ->
                    ApiRequestsSupporter.executeProgressDialog(RChatRemove(tag)) { r ->
                        EventBus.post(EventChatRemoved(tag))
                        onRemove.invoke()
                        ToolsToast.show(R.string.app_done)
                    }
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

        var count = 0
        for (i in list) if (i.a3) count++

        ToolsStorage.put("ControllerChats_count", count)

        EventBus.post(EventChatMessagesCountChanged(tag))
    }

    fun getMessagesCount(tag: ChatTag): Int {
        val list = loadMessagesCounts()
        for (item in list) if (item.a1 == tag) return item.a2
        return 0
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
        if (date > readDates[tag] ?: 0) {
            readDates[tag] = date
            EventBus.post(EventChatReadDateChanged(tag))
        }

    }

    fun getRead(tag: ChatTag) = readDates[tag] ?: Long.MAX_VALUE

    fun isRead(tag: ChatTag, date: Long): Boolean {
        return date < getRead(tag)
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            removeTyping(e.notification.tag, e.notification.unitChatMessage.creatorName)
        }
        if (e.notification is NotificationChatAnswer) {
            removeTyping(e.notification.tag, e.notification.unitChatMessage.creatorName)
        }
        if (e.notification is NotificationChatTyping) {
            val n = e.notification
            ControllerAccounts.updateOnline(n.accountId, System.currentTimeMillis())
            addTyping(n.chatTag, n.accountName)
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            incrementMessagesCount(n.tag, n.subscribed)
        }
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            incrementMessagesCount(n.tag, n.subscribed)
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