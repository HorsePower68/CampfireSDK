package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PageSpoiler
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageSpoiler
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsThreads

class WidgetPageSpoiler(
        private val requestPutPage:(page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page)->Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageSpoiler?
) : Widget(R.layout.screen_post_create_widget_spoiler) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vCounter: SettingsSelection = findViewById(R.id.vCounter)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vName.vField.setSingleLine(true)
        vName.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vName.vField.addTextChangedListener(TextWatcherChanged { update() })

        var enterText = R.string.app_create

        for (i in 0 until API.PAGE_LINK_SPOILER_MAX) vCounter.add((i + 1).toString() + "")
        vCounter.setCurrentIndex(0)


        if (oldPage != null) {
            enterText = R.string.app_change
            vName.setText(this.oldPage.name)
            vName.vField.setSelection(vName.getText().length)
            vCounter.setCurrentIndex(if (oldPage.count <= API.PAGE_LINK_SPOILER_MAX) oldPage.count - 1 else 0)
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { onEnter() }
        vCancel.setOnClickListener { onCancel() }
    }

    private fun update() {
        vEnter.isEnabled = !vName.getText().isEmpty() && vName.getText().length <= API.PAGE_LINK_SPOILER_NAME_MAX_L
    }

    private fun onEnter() {
        val page = PageSpoiler()
        page.name = vName.getText().trim { it <= ' ' }
        page.count = vCounter.getCurrentIndex() + 1

        if (card == null)
            requestPutPage.invoke(page, null, this, { page1 -> CardPageSpoiler(null, page1 as PageSpoiler) }){}
        else
            requestChangePage.invoke(page, card, null, this){}

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val name = vName.getText()
        return if (oldPage == null) {
            name.isEmpty()
        } else {
            oldPage.count == vCounter.getCurrentIndex() + 1 && name == oldPage.name
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
        vCounter.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }
}
