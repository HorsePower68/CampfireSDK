package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageLink
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageLink
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText

class WidgetPageLink(
        private val requestPutPage:(page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page)->Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageLink?
) : Widget(R.layout.screen_post_create_widget_link) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vLink.vField.setSingleLine(true)
        vLink.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vLink.vField.addTextChangedListener(TextWatcherChanged { update() })

        vName.vField.setSingleLine(true)
        vName.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vName.vField.addTextChangedListener(TextWatcherChanged { update() })

        var enterText = R.string.app_create

        if (oldPage != null) {
            enterText = R.string.app_change
            vLink.setText(this.oldPage.link)
            vName.setText(this.oldPage.name)
            vName.vField.setSelection(vName.getText().length)
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { onEnter() }
        vCancel.setOnClickListener { onCancel() }

        update()
    }

    private fun update() {
        vEnter.isEnabled = (vLink.getText().isNotEmpty()
                && vName.getText().isNotEmpty()
                && vName.getText().length <= API.PAGE_LINK_NAME_MAX_L
                && vLink.getText().length <= API.PAGE_LINK_WEB_MAX_L
                && ToolsText.isWebLink(vLink.getText()))
    }

    private fun onEnter() {
        val page = PageLink()
        page.link = vLink.getText().trim { it <= ' ' }
        page.name = vName.getText().trim { it <= ' ' }

        hide()
        val w = ToolsView.showProgressDialog()
        if (card == null)
            requestPutPage.invoke(page, null, w, { page1 -> CardPageLink(null, page1 as PageLink) }){}
        else
            requestChangePage.invoke(page, card, null, w){}

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val link = vLink.getText()
        val name = vName.getText()
        return if (oldPage == null) {
            link.isEmpty() && name.isEmpty()
        } else {
            ToolsText.equals(oldPage.link, link) && ToolsText.equals(oldPage.name, name)
        }
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vName.vField)
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
        vName.isEnabled = enabled
        vLink.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }
}
