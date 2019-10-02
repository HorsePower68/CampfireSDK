package com.sayzen.campfiresdk.screens.chat.create

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.chat.RChatCreate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sup.dev.android.libs.api_simple.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsThreads

class SChatCreate : Screen(R.layout.screen_chat_create){

    private val vRecycler:RecyclerView = findViewById(R.id.vRecycler)
    private val vFab:FloatingActionButton = findViewById(R.id.vFab)
    private val adapter = RecyclerCardAdapter()

    private val cardTitle = CardCreateTitle {updateFinish()}


    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false

        setTitle(R.string.chat_create_title)

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        adapter.add(cardTitle)
        adapter.add(CardUser(ControllerApi.account.id, ControllerApi.account.name, ControllerApi.account.imageId, false))
        adapter.add(CardMenu().setText(R.string.app_add).setIcon(ToolsResources.getDrawableAttr(R.attr.ic_add_18dp)).setOnClick { view, i, i2 ->
            Navigator.to(SAccountSearch{
                adapter.add(adapter.size() - 2, CardUser(it.id, it.name, it.imageId, true))
            })
        })
        adapter.add(CardSpace(128))

        vFab.setOnClickListener { create() }

        updateFinish()
    }

    private fun updateFinish(){
        val b = cardTitle.image != null && cardTitle.text.isNotEmpty() && cardTitle.text.length <= API.CHAT_NAME_MAX
        ToolsView.setFabEnabledR(vFab, b, R.color.green_700)
    }

    private fun create(){
        val accountsList = ArrayList<Long>()
        for(c in adapter.get(CardUser::class)) if(c.accountId != ControllerApi.account.id) accountsList.add(c.accountId)


        ApiRequestsSupporter.executeProgressDialog(RChatCreate(cardTitle.text, accountsList.toTypedArray(), cardTitle.image)){ r->
            ToolsThreads.main(300) { Navigator.remove(this) }
            SChat.instance(r.tag, false, Navigator.TO)
        }
    }

}