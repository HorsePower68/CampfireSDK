package com.sayzen.campfiresdk.screens.chat.create

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatMember
import com.dzen.campfire.api.requests.chat.RChatChange
import com.dzen.campfire.api.requests.chat.RChatCreate
import com.dzen.campfire.api.requests.chat.RChatGetForChange
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.events.chat.EventChatChanged
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.widgets.WidgetAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SChatCreate(
        val changeId: Long,
        val changeName: String,
        val changeImageId: Long,
        val accounts: Array<ChatMember>,
        val myLvl: Long
) : Screen(R.layout.screen_chat_create) {

    companion object {

        fun instance(chatId: Long, action: NavigationAction) {

            ApiRequestsSupporter.executeProgressDialog(RChatGetForChange(chatId)) { r ->
                Navigator.action(action, SChatCreate(chatId, r.chatName, r.chatImageId, r.accounts, r.myLvl))
            }
                    .onApiError(API.ERROR_ACCESS) { ToolsToast.show(R.string.error_chat_access) }

        }

    }


    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val adapter = RecyclerCardAdapter()

    private val cardTitle = CardCreateTitle(changeName, changeImageId) { updateFinish() }

    constructor() : this(0, "", 0, emptyArray(), API.CHAT_MEMBER_LVL_ADMIN)

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false

        setTitle(R.string.chat_create_title)

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        adapter.add(cardTitle)
        for (a in accounts) adapter.add(CardChatMember(a, myLvl))
        adapter.add(CardMenu().setText(R.string.app_add).setIcon(ToolsResources.getDrawableAttr(R.attr.ic_add_18dp)).setOnClick { view, i, i2 ->
            Navigator.to(SAccountSearch(false) {
                val m = ChatMember()
                m.accountId = it.id
                m.accountName = it.name
                m.accountImageId = it.imageId
                adapter.add(adapter.size() - 2, CardChatMember(m, myLvl))
            })
        })
        adapter.add(CardSpace(128))

        vFab.setOnClickListener { create() }

        updateFinish()
    }

    private fun updateFinish() {
        val b = (changeId != 0L || cardTitle.image != null) && cardTitle.text.isNotEmpty() && cardTitle.text.length <= API.CHAT_NAME_MAX
        ToolsView.setFabEnabledR(vFab, b, R.color.green_700)
    }

    private fun create() {





        if (changeId == 0L) {

            val accountsList = ArrayList<Long>()
            for (c in adapter.get(CardChatMember::class)) if (c.chatMember.accountId != ControllerApi.account.id) {
                accountsList.add(c.chatMember.accountId)
            }

            ApiRequestsSupporter.executeProgressDialog(RChatCreate(cardTitle.text, accountsList.toTypedArray(), cardTitle.image)) { r ->
                ToolsThreads.main(300) { Navigator.remove(this) }
                SChat.instance(r.tag, false, Navigator.TO)
            }
        } else {

            val accountsList = ArrayList<Long>()
            val removeAccountList = ArrayList<Long>()

            for (c in adapter.get(CardChatMember::class)) if (c.chatMember.accountId != ControllerApi.account.id && c.chatMember.memberLvl == 0L) {
                accountsList.add(c.chatMember.accountId)
            }

            for (a in accounts) {
                var found = a.accountId == ControllerApi.account.id
                for (c in adapter.get(CardChatMember::class)) if (a.accountId == c.chatMember.accountId) found = true
                if (!found) {
                    removeAccountList.add(a.accountId)
                }
            }

            ApiRequestsSupporter.executeProgressDialog(RChatChange(changeId, cardTitle.text, cardTitle.image, accountsList.toTypedArray(), removeAccountList.toTypedArray())) { r ->
                ImageLoaderId(changeImageId).clear()
                EventBus.post(EventChatChanged(changeId, cardTitle.text, changeImageId, accountsList.size))
                Navigator.remove(this)
            }
        }


    }

    private fun notChanged() = changeId != 0L || (cardTitle.image == null && cardTitle.text.isEmpty() && adapter.get(CardChatMember::class).size < 2)

    override fun onBackPressed(): Boolean {
        if (notChanged()) return false
        WidgetAlert()
                .setText(R.string.post_create_cancel_alert)
                .setOnEnter(R.string.app_yes) { Navigator.remove(this) }
                .setOnCancel(R.string.app_no)
                .asSheetShow()
        return true
    }

}