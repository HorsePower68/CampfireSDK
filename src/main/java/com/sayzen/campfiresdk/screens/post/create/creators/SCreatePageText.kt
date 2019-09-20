package com.sayzen.campfiresdk.screens.post.create.creators

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.units.post.Page
import com.dzen.campfire.api.models.units.post.PageText
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.widgets.Widget
import com.sup.dev.android.views.widgets.WidgetGreed
import com.sup.dev.java.tools.ToolsText

class SCreatePageText(
        private val requestPutPage: (page: Page, screen: Screen?, widget: Widget?, mapper: (Page) -> CardPage, onFinish: ((Card) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, widget: Widget?, (Page) -> Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageText?
) : Screen(R.layout.screen_post_create_widget_text) {

    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vTextTitle: ViewIcon = findViewById(R.id.vTextTitle)
    private val vIconAttach: ViewIcon = findViewById(R.id.vIconAttach)

    private var size = PageText.SIZE_0
    private var icon = 0

    private val maxL: Int
        get() = if (size == PageText.SIZE_0) API.PAGE_TEXT_MAX_L else API.PAGE_TEXT_TITLE_MAX_L

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false

        vField.setSingleLine(false)
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { update() })

        vTextTitle.setOnClickListener {
            if (vTextTitle.isIconSelected)
                onTextSizeClicked(PageText.SIZE_0)
            else
                onTextSizeClicked(PageText.SIZE_1)
        }


        if (oldPage != null) {
            vField.setText(this.oldPage.text)
            vField.setSelection(vField.text.length)
            onTextSizeClicked(this.oldPage.size)
            icon = this.oldPage.icon
        } else {
            onTextSizeClicked(size)
        }

        vFab.setOnClickListener { onEnter() }

        vIconAttach.setOnClickListener {
            val w = WidgetGreed()
            for (i in CampfireConstants.TEXT_ICONS.indices) w.addAttr(CampfireConstants.TEXT_ICONS[i]) { _, _ ->
                this.icon = i
                updateIcon()
            }
            w.asSheetShow()
        }

        updateIcon()
        update()
    }

    private fun updateIcon() {
        if (icon == 0) vIconAttach.setImageDrawable(ToolsResources.getDrawableAttr(R.attr.ic_mood_24dp))
        else vIconAttach.setImageDrawable(ToolsResources.getDrawableAttr(CampfireConstants.TEXT_ICONS[icon]))
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    private fun onTextSizeClicked(size: Int) {
        this.size = size
        vTextTitle.isIconSelected = size == PageText.SIZE_1
        update()
    }

    private fun update() {
        val s = vField.text.toString()

        vField.textSize = if (s.length < 200) 22f else 16f

        ToolsView.setFabEnabledR(vFab, !s.isEmpty() && s.length <= maxL, R.color.green_700)
    }

    private fun onEnter() {
        val page = PageText()
        page.text = vField.getText().toString().trim { it <= ' ' }
        page.size = size
        page.icon = icon
        if (oldPage == null)
            requestPutPage.invoke(page, this, null, { page1 -> CardPageText(null, page1 as PageText) }) {}
        else
            requestChangePage.invoke(page, card!!, this, null) {}
    }

    override fun onBackPressed(): Boolean {
        if (notChanged()) return false

        WidgetAdd.showConfirmCancelDialog(this)
        return true
    }

    private fun notChanged(): Boolean {
        val s = vField.text.toString()
        return if (oldPage == null) {
            s.isEmpty()
        } else {
            ToolsText.equals(oldPage.text, s)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vFab.isEnabled = enabled
        vField.isEnabled = enabled
        vTextTitle.isEnabled = enabled
    }

}
