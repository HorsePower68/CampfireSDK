package com.sayzen.campfiresdk.screens.post.create.creator

import android.view.inputmethod.EditorInfo
import android.widget.Button

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.PageQuote
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageQuote
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class WidgetPageQuote(
        private val screen: SPostCreate,
        private val card: CardPage?,
        private val oldPage: PageQuote?
) : Widget(R.layout.screen_post_create_widget_quote) {

    private val vAuthor: SettingsField = findViewById(R.id.vAuthor)
    private val vText: SettingsField = findViewById(R.id.vText)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vAuthor.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vAuthor.vField.addTextChangedListener(TextWatcherChanged { s -> update() })

        vText.vField.setSingleLine(false)
        vText.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vText.vField.addTextChangedListener(TextWatcherChanged { s -> update() })

        vEnter.isEnabled = false

        var enterText = R.string.app_create

        if (oldPage != null) {
            enterText = R.string.app_change
            vAuthor.setText(oldPage.author)
            vText.setText(oldPage.text)
            vAuthor.vField.setSelection(vAuthor.getText().length)
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { v -> onEnter() }
        vCancel.setOnClickListener { v -> onCancel() }
    }

    private fun update() {
        vEnter.isEnabled = !vText.getText().isEmpty()
                && vAuthor.getText().length <= API.PAGE_QUOTE_AUTHOR_MAX_L
                && vText.getText().length <= API.PAGE_QUOTE_TEXT_MAX_L
    }

    private fun onEnter() {
        val page = PageQuote()
        page.author = vAuthor.getText().trim { it <= ' ' }
        page.text = vText.getText().trim { it <= ' ' }

        if (card == null)
            screen.putPage(page, null, this, { page1 -> CardPageQuote(null, page1) }, null, true)
        else
            screen.changePage(page, card, null, this)

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            WidgetAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val author = vAuthor.getText()
        val text = vText.getText()
        return if (oldPage == null)
            author.isEmpty() && text.isEmpty()
        else
            ToolsText.equals((oldPage as PageQuote).author, author) && ToolsText.equals(oldPage.text, text)
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vText)
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
        vAuthor.isEnabled = enabled
        vText.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }
}
