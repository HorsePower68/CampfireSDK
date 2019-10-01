package com.sayzen.campfiresdk.screens.chat.create

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardDivider
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.cards.CardMenuButton
import com.sup.dev.android.views.cards.CardTitle
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter

class SChatCreate : Screen(R.layout.screen_chat_create){

    private val vRecycler:RecyclerView = findViewById(R.id.vRecycler)
    private val vFab:FloatingActionButton = findViewById(R.id.vFab)
    private val adapter = RecyclerCardAdapter()

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false


        setTitle(R.string.chat_create_title)

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        adapter.add(CardCreateTitle())
        adapter.add(CardUser())
        adapter.add(CardUser())
        adapter.add(CardUser())
        adapter.add(CardMenu().setText(R.string.app_add).setIcon(ToolsResources.getDrawableAttr(R.attr.ic_add_18dp)))
    }

}