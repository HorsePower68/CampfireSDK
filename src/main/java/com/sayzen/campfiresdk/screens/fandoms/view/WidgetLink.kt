package com.sayzen.campfiresdk.screens.fandoms.view


import android.graphics.Color
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.notifications.ControllerApp
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText

internal class WidgetLink(
        val callback: (String, String, String, Long) -> Unit
) : Widget(R.layout.screen_fandom_widget_link) {

    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vLinkTitle: SettingsField = findViewById(R.id.vLinkTitle)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vIcon_0: ViewIcon = findViewById(R.id.vIcon_0)
    private val vIcon_1: ViewIcon = findViewById(R.id.vIcon_1)
    private val vIcon_2: ViewIcon = findViewById(R.id.vIcon_2)
    private val vIcon_3: ViewIcon = findViewById(R.id.vIcon_3)
    private val vIcon_4: ViewIcon = findViewById(R.id.vIcon_4)
    private val vIcon_5: ViewIcon = findViewById(R.id.vIcon_5)
    private val vIcon_6: ViewIcon = findViewById(R.id.vIcon_6)
    private val vIcon_7: ViewIcon = findViewById(R.id.vIcon_7)
    private val vIcon_8: ViewIcon = findViewById(R.id.vIcon_8)

    private var selectedIcon = 0L

    init {

        vComment.addOnTextChanged { updateFinishEnabled() }
        vLink.addOnTextChanged { updateFinishEnabled() }
        vLinkTitle.addOnTextChanged { updateFinishEnabled() }

        vEnter.setOnClickListener {
            callback.invoke(vLink.getText(), vLinkTitle.getText(), vComment.getText(), selectedIcon)
            hide()
        }
        vCancel.setOnClickListener { hide() }

        vIcon_0.setOnClickListener { setSelectedIcon(0L) }
        vIcon_1.setOnClickListener { setSelectedIcon(1L) }
        vIcon_2.setOnClickListener { setSelectedIcon(2L) }
        vIcon_3.setOnClickListener { setSelectedIcon(3L) }
        vIcon_4.setOnClickListener { setSelectedIcon(4L) }
        vIcon_5.setOnClickListener { setSelectedIcon(5L) }
        vIcon_6.setOnClickListener { setSelectedIcon(6L) }
        vIcon_7.setOnClickListener { setSelectedIcon(7L) }
        vIcon_8.setOnClickListener { setSelectedIcon(8L) }

        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_YOUTUBE_WHITE else API_RESOURCES.ICON_YOUTUBE_BLACK).into(vIcon_1)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_DISCORD_WHITE else API_RESOURCES.ICON_DISCORD_BLACK).into(vIcon_2)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_WIKI_WHITE else API_RESOURCES.ICON_WIKI_BLACK).into(vIcon_3)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_TWITTER_WHITE else API_RESOURCES.ICON_TWITTER_BLACK).into(vIcon_4)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_STEAM_WHITE else API_RESOURCES.ICON_STEAM_BLACK).into(vIcon_5)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_GOOGLE_PLAY_WHITE else API_RESOURCES.ICON_GOOGLE_PLAY_BLACK).into(vIcon_6)
        ImageLoader.load(if (ControllerApp.isDarkThem()) API_RESOURCES.ICON_APPSTORE_WHITE else API_RESOURCES.ICON_APPSTORE_BLACK).into(vIcon_7)
        ImageLoader.load(API_RESOURCES.ICON_CAMPFIRE).into(vIcon_8)

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        var textCheck = ToolsText.isWebLink(vLink.getText())

        if (vLink.getText().length > 2 && !textCheck) {
            vLink.setError(R.string.error_not_url)
        } else {
            vLink.clearError()
        }

        textCheck = textCheck && vLink.getText().length <= API.FANDOM_LINKS_URL_MAX_L

        vEnter.isEnabled = textCheck
                && vComment.getText().length >= API.MODERATION_COMMENT_MIN_L && vComment.getText().length <= API.MODERATION_COMMENT_MAX_L
                && vLinkTitle.getText().isNotEmpty()
                && vLinkTitle.getText().length < API.FANDOM_LINKS_TITLE_MAX_L

        when {
            vLink.getText().contains("youtube", true) -> setSelectedIcon(1L)
            vLink.getText().contains("discord", true) -> setSelectedIcon(2L)
            vLink.getText().contains("wiki", true) -> setSelectedIcon(3L)
            vLink.getText().contains("twitter", true) -> setSelectedIcon(4L)
            vLink.getText().contains("steam", true) -> setSelectedIcon(5L)
            vLink.getText().contains("play.google", true) -> setSelectedIcon(6L)
            vLink.getText().contains("itunes.apple", true) -> setSelectedIcon(7L)
            vLink.getText().contains("sayzen.ru", true) -> setSelectedIcon(8L)
            else -> setSelectedIcon(0L)
        }
    }

    private fun setSelectedIcon(index: Long) {
        selectedIcon = index
        vIcon_0.isIconSelected = index == 0L
        vIcon_1.isIconSelected = index == 1L
        vIcon_2.isIconSelected = index == 2L
        vIcon_3.isIconSelected = index == 3L
        vIcon_4.isIconSelected = index == 4L
        vIcon_5.isIconSelected = index == 5L
        vIcon_6.isIconSelected = index == 6L
        vIcon_7.isIconSelected = index == 7L
        vIcon_8.isIconSelected = index == 8L
    }


}
