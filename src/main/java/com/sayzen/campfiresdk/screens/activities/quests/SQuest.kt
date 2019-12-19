package com.sayzen.campfiresdk.screens.activities.quests

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewTextLinkable

abstract class SQuest : Screen(R.layout.screen_quest) {

    val vTitleImage: ImageView = findViewById(R.id.vTitleImage)
    val vLabel: TextView = findViewById(R.id.vLabel)
    val vText: ViewTextLinkable = findViewById(R.id.vText)
    val vButton_1: Button = findViewById(R.id.vButton_1)
    val vButton_2: Button = findViewById(R.id.vButton_2)
    val vButton_3: Button = findViewById(R.id.vButton_3)
    val vButton_4: Button = findViewById(R.id.vButton_4)
    val vButtonContainer: ViewGroup = findViewById(R.id.vButtonContainer)

    var currentItem: QuestItem? = null

    init {
        isNavigationAllowed = false
        navigationBarColor = ToolsResources.getColorAttr(R.attr.content_background_screen)
        statusBarColor = ToolsResources.getColorAttr(R.attr.content_background_screen)
    }

    fun to(item: QuestItem) {
        if (currentItem == null) setQuestItemNoAnimation(item)
        else setQuestItemWithAnimation(item)
    }

    fun setQuestItemWithAnimation(item: QuestItem) {
        vButton_1.setOnClickListener { }
        vButton_2.setOnClickListener { }
        vButton_3.setOnClickListener { }
        vButton_4.setOnClickListener { }

        ToolsView.toAlpha(vButton_1)
        ToolsView.toAlpha(vButton_2)
        ToolsView.toAlpha(vButton_3)
        ToolsView.toAlpha(vButton_4)
        ToolsView.toAlpha(vText) {
            setQuestItemNoAnimation(item)
            ToolsView.fromAlpha(vButton_1)
            ToolsView.fromAlpha(vButton_2)
            ToolsView.fromAlpha(vButton_3)
            ToolsView.fromAlpha(vButton_4)
            ToolsView.fromAlpha(vText)
        }
    }

    fun setQuestItemNoAnimation(item: QuestItem) {
        currentItem?.onFinish?.invoke()
        this.currentItem = item
        currentItem?.onStart?.invoke()

        ImageLoader.load(item.imageId).into(vTitleImage)

        vLabel.text = parseText(item.label)

        vText.text = parseText(item.text)
        ControllerLinks.makeLinkable(vText)

        vButton_1.text = parseText(item.button_1)
        vButton_2.text = parseText(item.button_2)
        vButton_3.text = parseText(item.button_3)
        vButton_4.text = parseText(item.button_4)

        vButton_1.setOnClickListener { item.action_1.invoke() }
        vButton_2.setOnClickListener { item.action_2.invoke() }
        vButton_3.setOnClickListener { item.action_3.invoke() }
        vButton_4.setOnClickListener { item.action_4.invoke() }

        vLabel.visibility = if (vLabel.text.isEmpty()) View.GONE else View.VISIBLE

        vButtonContainer.removeAllViews()
        if (vButton_1.text.isNotEmpty()) vButtonContainer.addView(vButton_1)
        if (vButton_2.text.isNotEmpty()) vButtonContainer.addView(vButton_2)
        if (vButton_3.text.isNotEmpty()) vButtonContainer.addView(vButton_3)
        if (vButton_4.text.isNotEmpty()) vButtonContainer.addView(vButton_4)

    }

    fun parseText(text: String): String {
        var result = text
        result = result.replace("%user_name%", ControllerApi.account.name)

        return result
    }


}