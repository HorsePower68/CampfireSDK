package com.sayzen.campfiresdk.screens.fandoms.chats

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.chat.Chat
import com.dzen.campfire.api.requests.chat.RChatsFandomGetAll
import com.dzen.campfire.api.requests.chat.RChatsFandomGetRoot
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.models.cards.CardChat
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatCreated
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus

class SFandomChatsList constructor(
        val fandomId: Long,
        val languageId: Long
) : SLoadingRecycler<CardChat, Chat>() {

    private val eventBus = EventBus.subscribe(EventFandomChatCreated::class) { if (it.fandomId == fandomId) reload() }

    init {
        setTitle(R.string.app_chats)
        setTextEmpty(R.string.chats_empty)
        setTextProgress(R.string.chats_loading)
        setBackgroundImage(R.drawable.bg_18)
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_CHATS)) {
                WidgetFandomChatCreate(fandomId, languageId, 0, null, null, null)
            } else {
                ToolsToast.show(R.string.error_low_lvl_or_karma)
            }
        }

        loadRoot()
    }

    private fun loadRoot(){
        RChatsFandomGetRoot(fandomId, languageId)
                .onComplete{r->
                    adapter?.add(0, CardChat(r.chat, r.chat.unreadCount.toInt(), r.chat.subscribed))
                }
                .send(api)
    }

    override fun reload() {
        adapter?.clear()
        loadRoot()
        super.reload()
    }

    override fun instanceAdapter(): RecyclerCardAdapterLoading<CardChat, Chat> {
        return RecyclerCardAdapterLoading<CardChat, Chat>(CardChat::class) { unit -> CardChat(unit, unit.unreadCount.toInt(), unit.subscribed) }
                .setBottomLoader { onLoad, cards ->
                    RChatsFandomGetAll(cards.size.toLong(), fandomId, languageId)
                            .onComplete { rr -> onLoad.invoke(rr.chats) }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
    }


}
