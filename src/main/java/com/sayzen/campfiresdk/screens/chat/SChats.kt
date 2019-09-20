package com.sayzen.campfiresdk.screens.chat

import com.dzen.campfire.api.models.notifications.NotificationChatMessage
import com.dzen.campfire.api.models.units.chat.UnitChat
import com.dzen.campfire.api.requests.chat.RChatsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardChat
import com.sayzen.campfiresdk.models.events.chat.EventChatSubscriptionChanged
import com.sayzen.campfiresdk.models.events.chat.EventUpdateChats
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.api_simple.client.Request
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SChats constructor(
        var onSelected: ((UnitChat) -> Unit)? = null
) : SLoadingRecycler<CardChat, UnitChat>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SChats())
        }

    }

    private val eventBus = EventBus
            .subscribe(EventChatSubscriptionChanged::class) { reload() }
            .subscribe(EventNotification::class) { onEventNotification(it) }
            .subscribe(EventUpdateChats::class) { reloadOrFlag() }
    private var needReload = false

    init {
        setBackgroundImage(R.drawable.bg_5)
        setTextEmpty(R.string.chats_empty)
        setTextProgress(R.string.chats_loading)
        setTitle(R.string.app_chats)
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardChat, UnitChat> {
        return RecyclerCardAdapterLoading<CardChat, UnitChat>(CardChat::class)
        { unit ->
            val card = CardChat(unit, unit.unreadCount.toInt(), unit.subscribed)
            if (onSelected != null) card.onSelected = {
                Navigator.remove(this)
                onSelected!!.invoke(it)
            }
            card
        }
                .setBottomLoader { onLoad, cards ->
                    if(ControllerApi.account.id == 0L) ToolsThreads.main(1000) { sendRequest(onLoad, cards) }
                    else sendRequest(onLoad, cards)
                }

    }

    private fun sendRequest(onLoad:(Array<UnitChat>?) -> Unit, cards:ArrayList<CardChat>){
        RChatsGetAll(cards.size)
                .onComplete { r -> onLoad.invoke(r.units) }
                .onNetworkError { onLoad.invoke(null) }
                .send(api)
    }

    override fun onResume() {
        super.onResume()
        if (needReload) {
            needReload = false
            reload()
        }
    }

    private fun reloadOrFlag(){
        if (Navigator.getCurrent() == this) reload()
        else needReload = true
    }

    //
    //  EventBus
    //

    private fun onEventNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            if (adapter == null) return
            val cards = adapter!!.get(CardChat::class)
            if (cards.isNotEmpty() && cards[0].unit.tag == e.notification.tag)
                return
            reloadOrFlag()
        }
    }


}

