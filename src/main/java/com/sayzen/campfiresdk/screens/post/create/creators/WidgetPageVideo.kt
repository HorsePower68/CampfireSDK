package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerYoutube
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageVideo
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText

class WidgetPageVideo(
        private val requestPutPage:(page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page)->Unit) -> Unit,
        val card: CardPage?,
        val oldPage: PageVideo?
) : Widget(R.layout.screen_post_create_widget_video) {

    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vPast: View = findViewById(R.id.vPast)

    init {

        vLink.vField.setSingleLine(true)
        vLink.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vLink.vField.addTextChangedListener(TextWatcherChanged { update() })

        vPast.setOnClickListener{ vLink.setText(ToolsAndroid.getFromClipboard()) }

        var enterText = R.string.app_create

        if (oldPage != null) {
            enterText = R.string.app_change
            vLink.setText("https://youtu.be/${this.oldPage.videoId}")
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { onEnter() }
        vCancel.setOnClickListener { onCancel() }

        update()
    }

    private fun update() {
        val id = getVideoId()
        vEnter.isEnabled = id != null
    }

    private fun getVideoId():String?{
        val text = vLink.getText()
        if(ToolsText.isLinkToYoutube(text) && text.length >= 11){
            return text.substring(text.length - 11)
        }
        return null
    }

    private fun onEnter() {
        val page = PageVideo()
        page.videoId = getVideoId()!!

        val w = ToolsView.showProgressDialog()
        ControllerYoutube.getImage(page.videoId){ bytes->

            if(bytes == null){
                ToolsToast.show(R.string.error_cant_load_video)
                w.hide()
                return@getImage
            }

            hide()
            page.insertBytes = bytes
            if(oldPage == null) {
                requestPutPage.invoke(page, null, w, { page1 -> CardPageVideo(null, page1 as PageVideo) }){}
            }else{
                requestChangePage.invoke(page, card!!, null, w){}
            }

        }

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val id = getVideoId()
        return if (oldPage == null) {
            id == null
        } else {
            id != null && ToolsText.equals(oldPage.videoId, id)
        }
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vLink.vField)
    }

    override fun onHide() {
        super.onHide()
        ToolsView.hideKeyboard()
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

    override fun setEnabled(enabled: Boolean): Widget {
        super.setEnabled(enabled)
        vLink.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }
}
