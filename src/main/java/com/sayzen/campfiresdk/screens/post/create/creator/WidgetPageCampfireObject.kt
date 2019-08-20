package com.sayzen.campfiresdk.screens.post.create.creator

import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.CampfireLink
import com.dzen.campfire.api.models.units.post.PageCampfireObject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageCampfireObject
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class WidgetPageCampfireObject(
        private val screen: SPostCreate,
        private val card: CardPage?,
        private val oldPage: PageCampfireObject?
) : Widget(R.layout.screen_post_create_widget_campfire_object) {

    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vLink.vField.setSingleLine(true)
        vLink.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vLink.vField.addTextChangedListener(TextWatcherChanged { s -> update() })

        var enterText = R.string.app_create

        if (oldPage != null) {
            enterText = R.string.app_change
            vLink.setText(this.oldPage.link)
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { v -> onEnter() }
        vCancel.setOnClickListener { v -> onCancel() }

        update()
    }

    private fun update() {
        vEnter.isEnabled = (!vLink.getText().isEmpty()
                && vLink.getText().length <= API.PAGE_CAMPFIRE_OBJECT_LINK_MAX
                && (ToolsText.isWebLink(vLink.getText())) || vLink.getText().startsWith("@"))
    }

    private fun onEnter() {
        val page = PageCampfireObject()
        page.link = vLink.getText().trim { it <= ' ' }

        val campfireLink = CampfireLink(page.link)
        if(!campfireLink.isValid()) {
            ToolsToast.show(R.string.error_unsupported_link)
            return
        }

        hide()
        val w = ToolsView.showProgressDialog()
        if (card == null)
            screen.putPage(page, null, w, { page1 -> CardPageCampfireObject(null, page1) }, null, true)
        else
            screen.changePage(page, card, null, w)

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val link = vLink.getText()
        return if (oldPage == null) {
            link.isEmpty()
        } else {
            ToolsText.equals(oldPage.link, link)
        }
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vLink.vField)
    }

    override fun onHide() {
        super.onHide()
        ToolsThreads.main(500) { ToolsView.hideKeyboard() } //  Без задержки будет скрываться под клавиатуру и оставаться посреди экрана
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
