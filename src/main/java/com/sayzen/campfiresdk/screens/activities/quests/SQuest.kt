package com.sayzen.campfiresdk.screens.activities.quests

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsImagesLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.widgets.WidgetProgressTransparent
import com.sup.dev.android.views.widgets.WidgetSplash
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsThreads

abstract class SQuest : Screen(R.layout.screen_quest) {

    val vTitleImage: ImageView = findViewById(R.id.vTitleImage)
    val vLabel: TextView = findViewById(R.id.vLabel)
    val vText: TextView = findViewById(R.id.vText)
    val vButton_1: Button = findViewById(R.id.vButton_1)
    val vButton_2: Button = findViewById(R.id.vButton_2)
    val vButton_3: Button = findViewById(R.id.vButton_3)
    val vButton_4: Button = findViewById(R.id.vButton_4)
    val vSplash: View = findViewById(R.id.vSplash)

    var currentItem: QuestItem? = null

    init {
        isNavigationAllowed = false
        navigationBarColor = ToolsResources.getColorAttr(R.attr.content_background_screen)
        statusBarColor = ToolsResources.getColorAttr(R.attr.content_background_screen)
        vSplash.visibility = View.INVISIBLE
    }

    fun setQuestItem(item: QuestItem) {
        if (currentItem == null) setQuestItemNoAnimation(item)
        else setQuestItemWithAnimation(item)
    }

    fun setQuestItemWithAnimation(item: QuestItem) {
        this.currentItem = item
        vButton_1.setOnClickListener { }
        vButton_2.setOnClickListener { }
        vButton_3.setOnClickListener { }
        vButton_4.setOnClickListener { }


        vSplash.visibility = View.INVISIBLE
        ToolsView.fromAlpha(vSplash){
            setQuestItemNoAnimation(item)
            ToolsView.toAlpha(vSplash)
        }
    }

    fun setQuestItemNoAnimation(item: QuestItem) {
        this.currentItem = item

        ToolsImagesLoader.load(item.imageId).into(vTitleImage)

        vLabel.text = parseText(item.label)

        vText.text = parseText(item.text)

        vButton_1.text = parseText(item.button_1)
        vButton_2.text = parseText(item.button_2)
        vButton_3.text = parseText(item.button_3)
        vButton_4.text = parseText(item.button_4)

        vButton_1.setOnClickListener { item.action_1.invoke() }
        vButton_2.setOnClickListener { item.action_2.invoke() }
        vButton_3.setOnClickListener { item.action_3.invoke() }
        vButton_4.setOnClickListener { item.action_4.invoke() }

        vLabel.visibility = if (vLabel.text.isEmpty()) View.GONE else View.VISIBLE

        vButton_1.visibility = if (vButton_1.text.isEmpty()) View.GONE else View.VISIBLE
        vButton_2.visibility = if (vButton_2.text.isEmpty()) View.GONE else View.VISIBLE
        vButton_3.visibility = if (vButton_3.text.isEmpty()) View.GONE else View.VISIBLE
        vButton_4.visibility = if (vButton_4.text.isEmpty()) View.GONE else View.VISIBLE

    }

    fun parseText(text: String): String {
        var result = text
        result = result.replace("%user_name%", ControllerApi.account.name)

        return result
    }


}